package com.hospital.mes.masterdata.domain;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import com.hospital.mes.common.exception.StateTransitionException;
public final class MasterRules {
    private MasterRules() { }
    public record Conversion(String originalValue, String factor, String convertedValue, int targetScale) { }
    public static BigDecimal factor(String value) {
        if (value==null || !value.matches("[0-9]+(\\.[0-9]{1,12})?")) throw new IllegalArgumentException("Invalid factor");
        BigDecimal n=new BigDecimal(value);
        if(n.signum()<=0 || n.precision()-n.scale()>12) throw new IllegalArgumentException("Invalid factor"); return n;
    }
    public static int scale(Integer value) {
        if(value==null || value<0 || value>12) throw new IllegalArgumentException("Scale must be 0..12"); return value;
    }
    public static void hierarchy(String type,String parentType,String parentStatus) {
        var types=List.of("ENTERPRISE","FACTORY","WORKSHOP","LINE"); int i=types.indexOf(type);
        if(i<0 || (i==0 ? parentType!=null : !types.get(i-1).equals(parentType) || !"ACTIVE".equals(parentStatus)))
            throw new MasterGateException("ORG_HIERARCHY_INVALID","SELECT_COMPATIBLE_ACTIVE_PARENT");
    }
    public static Conversion convert(BigDecimal original,String fromDimension,String toDimension,BigDecimal factor,Long materialId,int targetScale) {
        scale(targetScale);
        if(!fromDimension.equals(toDimension) && materialId==null) throw new MasterGateException("UNIT_DIMENSION_MISMATCH","DEFINE_MATERIAL_CONVERSION");
        if(factor==null) throw new MasterGateException("UNIT_CONVERSION_REQUIRED","DEFINE_CONVERSION");
        if(original==null || factor.signum()<=0) throw new IllegalArgumentException("Invalid conversion");
        return new Conversion(original.toPlainString(),factor.toPlainString(),original.multiply(factor).setScale(targetScale,RoundingMode.HALF_EVEN).toPlainString(),targetScale);
    }
    public static void qualification(String status,LocalDate from,LocalDate to,Instant at) {
        LocalDate date=at.atZone(ZoneOffset.UTC).toLocalDate();
        if(!"ACTIVE".equals(status) || from!=null && date.isBefore(from) || to!=null && date.isAfter(to))
            throw new MasterGateException("QUALIFICATION_REQUIRED","OBTAIN_VALID_QUALIFICATION");
    }
    public static void period(LocalDate from,LocalDate to) {
        if(from!=null && to!=null && from.isAfter(to)) throw new IllegalArgumentException("Invalid validity period");
    }
    public static String enable(String status) {
        if(!"INACTIVE".equals(status)) throw new StateTransitionException("INVALID_STATE","Only inactive records may be enabled"); return "ACTIVE";
    }
    public static String disable(String status) {
        if(!List.of("ACTIVE","MAINTENANCE").contains(status)) throw new StateTransitionException("INVALID_STATE","Record is already inactive"); return "INACTIVE";
    }
    public static String text(String value,int max) {
        if(value==null || value.isBlank() || value.strip().length()>max) throw new IllegalArgumentException("Required text is missing or too long"); return value.strip();
    }
    public static String optional(String value,int max) {
        return value==null || value.isBlank() ? null : text(value,max);
    }
}
