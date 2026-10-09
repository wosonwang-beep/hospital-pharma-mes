import config from './playwright.config'
export default {...config,testMatch:'ebr-book-live.spec.ts',projects:[config.projects![0]],use:{...config.use,channel:'chromium',trace:'off',screenshot:'off'},webServer:{...config.webServer as object,reuseExistingServer:true},outputDir:'C:/Users/Administrator/AppData/Local/Temp/mes-ebr-live-browser-private'}
