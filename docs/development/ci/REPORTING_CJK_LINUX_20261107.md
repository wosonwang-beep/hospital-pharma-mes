# Linux CJK DOCX-to-PDF regression — 2026-11-07

**Scope:** bounded reporting/CI font fallback. No changes to DOCX contents, reporting rules, production history, signatures, QC standards or UI.

## Reproduced root cause

GitHub Actions run [#56](https://github.com/wosonwang-beep/hospital-pharma-mes/actions/runs/42366127449), at commit `3f376fd95a3741b9a8e2f75f6254b7efbef61756`, failed four `mes-reporting` assertions in both backend and infrastructure CI: Chinese report title, 75-row last inspection item, native visual document and merged table header. The same failures were present in preceding run #54.

Reproduced **4/7 test failures** in isolated Linux Docker using Ubuntu 24.04 Noble, Java 21, Maven 3.9.16, fontconfig, `fonts-noto-cjk` and `fonts-wqy-microhei`. PDF extracted text contained `#` instead of Chinese glyphs.

The DOCX uses `Microsoft YaHei`; the worker previously tried `Microsoft YaHei` then `Noto Sans CJK SC` in docx4j `PhysicalFonts`. In this Linux environment, physical font discovery returned **null** for `Noto Sans CJK SC` even while fontconfig listed the TTC. Explicit docx4j scanning identified the physical font `WenQuanYi Micro Hei`. Without a valid fallback, FOP used a font lacking Chinese glyphs.

## Minimal correction and real Linux test

- `Docx4jPdfWorker` preserves prior choices, adding `WenQuanYi Micro Hei` as third fallback only.
- GitHub backend and integration jobs install the matching font package and assert that the system actually exposes the font.
- No proprietary font or font binary is added to Git.

The **original test assertions were not modified, skipped, or relaxed**. The same isolated Linux image subsequently passed all 7 reporting tests with 0 failures/errors/skips:

```text
mvn -pl backend/mes-reporting -am -Dtest=PureJavaPdfSmokeTest,WordTableComposerTest -Dsurefire.failIfNoSpecifiedTests=false test
# Exit 0; WORD_PASTE_MERGE_FOP_PDF ... merged=yes bound=yes
```

`scripts/verify-ci.ps1` also passed locally. Existing Windows 8080 MES service and data were not touched.

## Separate, unresolved CI blocker

Run #56 also had a frontend TypeScript failure: the committed QC detail page imports QC basic-library API/types, but its matching QC API/domain/schema/migration refactor remains **uncommitted** locally and is governed by `DCP-QC-SPEC-BASIC-LIBRARY-001-APPROVED.md`. Stub exports or a partial QC API commit would hide a runtime schema mismatch. This targeted PDF correction does not change or release those QC files.

**Status:** Linux reporting regression PASS. Full remote GitHub CI must be checked after push; it cannot be called green while the separate QC work is pending.
