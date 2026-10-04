package com.hospital.mes.ebr.domain;

import java.math.*;
import java.time.Instant;
import java.util.*;

/** Bounded whitelist parser. It never delegates to an executable language or a query engine. */
public final class EbrDsl {
    public static final String ENGINE_VERSION="ebr-dsl-1.0";
    private static final MathContext MATH=new MathContext(28,RoundingMode.HALF_UP);
    @FunctionalInterface public interface Converter { BigDecimal convert(BigDecimal value,String from,String to); }
    public record Context(Map<String,List<Object>> values,Set<String> roles,Map<String,String> statuses,Instant time,Converter converter) {}
    private interface Node { Object evaluate(Context context); }
    private record Literal(Object value) implements Node { public Object evaluate(Context c){return value;} }
    public record Expression(Node root,Set<String> references,List<String> roles,List<List<String>> conversions) {
        public Object evaluate(Context context) {
            try {return root.evaluate(context);} catch(ArithmeticException ex){throw invalid("Invalid arithmetic");}
        }
    }
    private EbrDsl(){}
    public static Expression parse(String source) {return new Parser(source).parse();}
    private static IllegalArgumentException invalid(String text){return new IllegalArgumentException("DSL: "+text);}
    private static BigDecimal decimal(Object value){
        if(value instanceof BigDecimal n){if(n.precision()>1000||Math.abs((long)n.scale())>1000)throw invalid("Numeric magnitude exceeds limit");return n;}
        if(value instanceof Number n)return new BigDecimal(n.toString(),MATH);
        throw invalid("Numeric value required");
    }
    public static boolean truth(Object value){if(value instanceof Boolean b)return b;throw invalid("Boolean value required");}
    private static String string(Object value){if(value instanceof String s)return s;throw invalid("String required");}
    private static Object binary(String operator,Object a,Object b){
        return switch(operator){
            case "+"->decimal(a).add(decimal(b),MATH);case "-"->decimal(a).subtract(decimal(b),MATH);
            case "*"->decimal(a).multiply(decimal(b),MATH);case "/"->decimal(a).divide(decimal(b),MATH);
            case "%"->decimal(a).remainder(decimal(b),MATH);
            case "=="->equal(a,b);case "!="->!equal(a,b);
            case ">"->compare(a,b)>0;case ">="->compare(a,b)>=0;case "<"->compare(a,b)<0;case "<="->compare(a,b)<=0;
            default->throw invalid("Unknown operator");
        };
    }
    private static boolean equal(Object a,Object b){return a instanceof Number&&b instanceof Number?decimal(a).compareTo(decimal(b))==0:Objects.equals(a,b);}
    private static int compare(Object a,Object b){if(a instanceof Number&&b instanceof Number)return decimal(a).compareTo(decimal(b));if(a instanceof String x&&b instanceof String y)return x.compareTo(y);throw invalid("Incompatible comparison");}
    private static Object function(String name,List<Node> args,Context c){
        List<Object> a=args.stream().map(n->n.evaluate(c)).toList();
        return switch(name){
            case "value"->{var values=c.values().get(string(a.getFirst()));if(values==null||values.isEmpty())throw invalid("Missing field value");yield values.getFirst();}
            case "exists"->{var values=c.values().get(string(a.getFirst()));yield values!=null&&!values.isEmpty();}
            case "sum"->{var values=c.values().get(string(a.getFirst()));if(values==null)throw invalid("Missing field value");BigDecimal total=BigDecimal.ZERO;for(Object v:values)total=total.add(decimal(v),MATH);yield total;}
            case "abs"->decimal(a.getFirst()).abs(MATH);
            case "round"->{int scale=decimal(a.get(1)).intValueExact();if(scale<0||scale>12)throw invalid("Precision outside 0..12");yield decimal(a.getFirst()).setScale(scale,RoundingMode.HALF_UP);}
            case "convert"->c.converter().convert(decimal(a.getFirst()),string(a.get(1)),string(a.get(2)));
            case "hasRole"->c.roles().contains(string(a.getFirst()));
            case "status"->{String value=c.statuses().get(string(a.getFirst()));if(value==null)throw invalid("Unknown object status");yield value;}
            case "now"->c.time().toString();default->throw invalid("Unknown function");
        };
    }
    private static final class Parser {
        private final String source;private int cursor,nodes,depth;private String token;private Object literal;
        private final Set<String> references=new LinkedHashSet<>();private final List<String> roles=new ArrayList<>();private final List<List<String>> conversions=new ArrayList<>();
        Parser(String source){if(source==null||source.isBlank()||source.length()>4000)throw invalid("Expression length");this.source=source;next();}
        Expression parse(){Node root=expression(0);if(!token.equals("EOF"))throw invalid("Unexpected token");return new Expression(root,Set.copyOf(references),List.copyOf(roles),List.copyOf(conversions));}
        Node expression(int minimum){
            if(++depth>40)throw invalid("Maximum nesting exceeded");
            Node left=primary();
            while(precedence(token)>=minimum){String op=token;int p=precedence(op);next();Node right=expression(p+1), previous=left;count();left=c->op.equals("&&")?truth(previous.evaluate(c))&&truth(right.evaluate(c)):op.equals("||")?truth(previous.evaluate(c))||truth(right.evaluate(c)):binary(op,previous.evaluate(c),right.evaluate(c));}
            depth--;return left;
        }
        Node primary(){
            count();
            if(token.equals("-")||token.equals("+")||token.equals("!")){String op=token;next();Node n=expression(7);return c->op.equals("!")?!truth(n.evaluate(c)):op.equals("-")?decimal(n.evaluate(c)).negate(MATH):decimal(n.evaluate(c));}
            if(token.equals("(")){next();Node n=expression(0);take(")");return n;}
            if(token.equals("NUMBER")||token.equals("STRING")){Object v=literal;next();return new Literal(v);}
            if(token.equals("IDENT")){
                String name=string(literal);next();if(name.equals("true")||name.equals("false"))return new Literal(Boolean.valueOf(name));
                var arity=Map.of("value",1,"exists",1,"sum",1,"abs",1,"round",2,"convert",3,"status",1,"hasRole",1,"now",0);
                Integer expected=arity.get(name);if(expected==null)throw invalid("Unknown function: "+name);take("(");List<Node> args=new ArrayList<>();if(!token.equals(")")){do{if(args.size()>=3)throw invalid("Function arity");args.add(expression(0));if(!token.equals(","))break;next();}while(true);}take(")");if(args.size()!=expected)throw invalid("Function arity");
                if(Set.of("value","exists","sum","status","hasRole").contains(name)){String reference=literalString(args.getFirst());if(Set.of("value","exists","sum").contains(name))references.add(reference);if(name.equals("hasRole"))roles.add(reference);}
                if(name.equals("convert"))conversions.add(List.of(literalString(args.get(1)),literalString(args.get(2))));
                return c->function(name,args,c);
            }
            throw invalid("Expected value");
        }
        private String literalString(Node n){if(n instanceof Literal l&&l.value() instanceof String s&&!s.isBlank())return s;throw invalid("Literal reference required");}
        private void count(){if(++nodes>200)throw invalid("Maximum nodes exceeded");}
        private void take(String expected){if(!token.equals(expected))throw invalid("Expected "+expected);next();}
        private static int precedence(String token){return switch(token){case "||"->0;case "&&"->1;case "==","!="->2;case "<","<=",">",">="->3;case "+","-"->4;case "*","/","%"->5;default->-1;};}
        private void next(){
            while(cursor<source.length()&&Character.isWhitespace(source.charAt(cursor)))cursor++;
            if(cursor==source.length()){token="EOF";return;}
            char c=source.charAt(cursor++);
            if(c=='\''||c=='"'){StringBuilder s=new StringBuilder();boolean closed=false;while(cursor<source.length()){char v=source.charAt(cursor++);if(v==c){closed=true;break;}if(v=='\\'){if(cursor==source.length())throw invalid("String escape");v=source.charAt(cursor++);if(v!=c&&v!='\\')throw invalid("String escape");}if(Character.isISOControl(v))throw invalid("String control character");s.append(v);if(s.length()>1000)throw invalid("String length");}if(!closed)throw invalid("Unclosed string");token="STRING";literal=s.toString();return;}
            if(c>='0'&&c<='9'){int start=cursor-1;while(cursor<source.length()&&(Character.isDigit(source.charAt(cursor))||source.charAt(cursor)=='.'))cursor++;String n=source.substring(start,cursor);if(n.length()>40||!n.matches("[0-9]+(\\.[0-9]+)?"))throw invalid("Number format");token="NUMBER";literal=new BigDecimal(n,MATH);return;}
            if(Character.isLetter(c)||c=='_'){int start=cursor-1;while(cursor<source.length()&&(Character.isLetterOrDigit(source.charAt(cursor))||source.charAt(cursor)=='_'))cursor++;token="IDENT";literal=source.substring(start,cursor);return;}
            if(cursor<source.length()){String pair=""+c+source.charAt(cursor);if(Set.of("&&","||","==","!=",">=","<=").contains(pair)){cursor++;token=pair;return;}}
            if("()+-*/%,!<>".indexOf(c)>=0){token=Character.toString(c);return;}throw invalid("Forbidden token");
        }
    }
}
