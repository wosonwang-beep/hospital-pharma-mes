import type Editor from '@hufe921/canvas-editor'
declare module '@hufe921/canvas-editor' {
  interface Command {
    executeImportDocx(options: {arrayBuffer: ArrayBuffer; isAppend?: boolean}): Promise<void>
    executeExportDocx(options: {fileName: string; toc?: boolean}): Promise<Blob>
  }
}
export default function docxPlugin(editor: Editor): void
