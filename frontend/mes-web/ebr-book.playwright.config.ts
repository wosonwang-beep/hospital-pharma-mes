import config from './playwright.config'
export default {...config,testMatch:'ebr-book.spec.ts',use:{...config.use,channel:'chromium'},webServer:{...config.webServer as object,reuseExistingServer:true},outputDir:process.env.MES_EBR_EVIDENCE_DIR+'/browser-results'}
