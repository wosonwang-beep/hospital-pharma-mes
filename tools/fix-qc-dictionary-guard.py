from pathlib import Path
p=Path(r'D:\codex\_project\gmp\hospital-pharma-mes\backend\mes-system\src\main\java\com\hospital\mes\system\api\SystemDictionaryController.java')
s=p.read_text(encoding='utf-8')
anchor='    private static final Pattern CODE=Pattern.compile('
assert anchor in s
s=s.replace(anchor,'''    private static final String CONTROLLED_QC_OBJECT_TYPE="QC_OBJECT_TYPE";
    private static final java.util.Set<String> CONTROLLED_QC_OBJECT_CODES=java.util.Set.of(
        "MATERIAL","FINISHED_PRODUCT","INTERMEDIATE","ENVIRONMENT","PROCESS");
'''+anchor,1)
s=s.replace('''        verify(before.kind().equals(r.kind()),"Dictionary type cannot be changed after creation");''','''        verify(before.kind().equals(r.kind()),"Dictionary type cannot be changed after creation");
        if(CONTROLLED_QC_OBJECT_TYPE.equals(before.code())){
            verify("SYSTEM".equals(r.kind()) && "FLAT".equals(r.structure()) && "ACTIVE".equals(r.status()),
                "QC inspection object type is a fixed active system dictionary");
        }''',1)
s=s.replace('''        DictType t=requireType(typeId);Long parent=parent(t,r.parentId(),null);''','''        DictType t=requireType(typeId);Long parent=parent(t,r.parentId(),null);
        verify(!CONTROLLED_QC_OBJECT_TYPE.equals(t.code()),
            "QC inspection object codes require an approved design change");''',1)
s=s.replace('''        verify(before.code().equals(r.code()),"Referenced item codes are immutable");''','''        verify(before.code().equals(r.code()),"Referenced item codes are immutable");
        if(CONTROLLED_QC_OBJECT_TYPE.equals(t.code())){
            verify(CONTROLLED_QC_OBJECT_CODES.contains(before.code()) && "ACTIVE".equals(r.status())
                && (r.parentId()==null || r.parentId().isBlank()) && !Boolean.TRUE.equals(r.isDefault()),
                "QC inspection object options are fixed active root codes");
        }''',1)
p.write_text(s,encoding='utf-8')
print('Fixed QC dictionary guards added')
