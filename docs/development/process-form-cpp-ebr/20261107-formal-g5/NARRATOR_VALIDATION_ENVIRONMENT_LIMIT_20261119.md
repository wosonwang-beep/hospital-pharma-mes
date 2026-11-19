# Narrator 语音播报验证环境记录（2026-11-19）

状态：**NOT RUN / 环境能力不足，不能签发 PASS**。本记录不是屏幕阅读器语音验证通过证明。

## 实机事实
- 操作系统：Microsoft Windows 10 专业版，版本 10.0.19045，Build 19045（22H2）。
- Narrator：C:\Windows\System32\Narrator.exe，文件版本 10.0.19041.4522；中文 Huihui Desktop TTS voice token 已安装。
- Narrator 进程可由本机操作启动；Narrator Home 的 UI Automation 树可读取，Narrator 设置/辅助进程可见。
- 当前用户配置 NarratorModifiers=3，即 Caps Lock / Insert 作为 Narrator modifier；Narrator speech recap 快捷键尝试未产生 speech recap 窗口。
- Chrome 真实浏览器可通过独立 profile + CDP 启动；前端 Vite 5173 HTTP 200，后端 8080/actuator/health 返回 {"status":"UP"}。
- Windows 音频端点枚举共 14 个设备，状态全部为 Unknown；FFmpeg 当前构建没有 WASAPI 输入支持。不能取得本机 Narrator 语音回环录音。

## 官方能力差异
微软当前 Narrator 文档说明 Speech Recap（Narrator+Alt+X）及 live transcription 是 2025 年 3 月推出的新版能力，当前文档面向 Windows 11；本机是 Windows 10 22H2，不能假设具有此 UI。官方参考：
- https://support.microsoft.com/en-us/accessibility/windows/narrator/complete-guide-to-narrator
- https://support.microsoft.com/en-us/accessibility/windows/narrator/chapter-2-narrator-basics

## 本轮已执行尝试
1. 直接启动 Narrator.exe，确认 Narrator 进程存在。
2. 使用 Windows UI Automation 读取 Narrator Home 的控件树与设置入口。
3. 尝试 Narrator+Alt+X（Caps Lock 和 Insert 两种 Narrator modifier），没有出现 speech recap window。
4. 尝试 ms-settings:easeofaccess-narrator 与 Narrator Home 设置入口；Windows Settings 进程存在，但无可读取的设置主窗口内容。
5. 核查 Windows 音频设备与 FFmpeg 设备支持，未取得可用的音频回环采集路径。
6. 检查 Chrome 的 UI Automation 树和本机 MES 服务；UIA/DOM 可证明可访问名称与状态存在，但不能替代实际 spoken announcement。

## 判定与后续门禁
- 真实 Narrator spoken output：**NOT RUN**。
- Chrome/DOM/ARIA/UI Automation 可访问名称：既有证据继续保留，但不升级为语音播报 PASS。
- 不允许把 Narrator 进程启动、UIA 树可读或 SAPI 独立 TTS 演示冒充 Narrator 语音输出。
- 最终 G5 manifest 保持 IN PROGRESS；interaction check 11 保持 NOT RUN。只有在支持 Speech Recap 或可验证实际 Narrator 音频的桌面环境中完成一次真实页面播报验证后，才可关闭该门禁。