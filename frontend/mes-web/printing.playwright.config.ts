import config from './playwright.config'
export default {...config,testMatch:'printing.spec.ts',use:{...config.use,channel:'chromium'},webServer:{...config.webServer as object,reuseExistingServer:true},outputDir:process.env.MES_PRINT_EVIDENCE_DIR?`${process.env.MES_PRINT_EVIDENCE_DIR}/test-results`:'test-results'}
