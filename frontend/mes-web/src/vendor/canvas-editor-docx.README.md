# canvas-editor DOCX adapter

Source: https://github.com/Hufe921/canvas-editor-plugin (MIT), npm `@hufe921/canvas-editor-plugin-docx@1.0.0`, SHA-512 `f3MIzNU6RyJ6ZYT7SW8ZKBS260hJnq4eidPOD2e4YmauGJzvqtePf2Hrser0+mJkDkv4hyZ4bes6AsEYyVLbng==`.

The published ESM bundle is reused with its license. One local change removes the unconditional FileSaver download from `executeExportDocx`; it returns the same generated Blob so the MES caller can upload an immutable draft through the existing authenticated DOCX endpoint. Import, formatting and DOCX composition are otherwise unchanged. The local declaration exposes the existing import/export commands. Canvas-editor remains the project's pinned 1.0.4 dependency.
