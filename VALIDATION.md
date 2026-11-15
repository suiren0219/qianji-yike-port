# v1.3.0 包名与图标自定义验收记录

日期：2026-11-15。设备验证仅操作 emulator-5554，未操作 emulator-5556 或实体手机。

## 包名调整

最初按要求把 Application ID 改为单段 `suiren`。Debug 因自动追加 `.debug` 后缀可以构建；**Release 在清单合并阶段被 Android 构建工具拒绝**：

```text
Package name 'suiren' at position AndroidManifest.xml:2:1 should contain at least one '.' (dot) character
```

因此改为以 suiren 开头的两段式 `suiren.qianji`：Release 为 `suiren.qianji`，Debug 为 `suiren.qianji.debug`。若需其他形式（例如 `com.suiren`），只需修改 `opensource/app/build.gradle.kts` 中一行并重新构建。

包名变化使新旧版本成为独立应用：新包不读取旧包 `com.xiaojinzi.tally.opensource.debug` 的账本，Shizuku、无障碍、通知与小部件授权均需重新设置。旧包未卸载，旧数据保留；两者可并存。

## 图标自定义实现

- 在 `opensource/app/AndroidManifest.xml` 中为启动页声明四个 `activity-alias`：`LauncherDefault`、`LauncherBlue`、`LauncherGreen`、`LauncherAmber`，均指向 `LoadingAct`，并各自带 MAIN/LAUNCHER 入口。
- 默认别名安装时启用，其余禁用；应用运行时只启用一个。
- 三套新图标为 adaptive icon：蓝色 `#3478F6`、墨绿 `#2E7D5B`、暖橙 `#E8793A` 背景，配**白色描边账本**前景，保证底色可见；默认沿用原图标。
- 设置页新增“应用图标”入口，选择框显示四套图标预览（通过系统包管理器读取别名图标），切换不修改账本或权限。
- 切换顺序为先启用目标别名、再禁用其他，避免桌面短暂没有入口。
- 开发中先使用白色填充账本，实机预览偏白、蓝底不明显，已改为描边版本并重建。

## 设备验证

### 中间构建（suiren.debug）

包名调整为 `suiren.qianji` 之前的中间构建，用于验证图标切换交互：

- 安装后系统只解析到一个启动入口 `suiren.debug/...LauncherDefault`。
- 设置页“应用图标”显示四套选项及预览。
- 通过界面选择“海蓝”后：`enabledComponents` 为 `LauncherBlue`，其余三个别名禁用；`resolve-activity` 返回 `LauncherBlue`；应用抽屉图标变为蓝色底账本。
- 通过界面切回“默认”后：`enabledComponents` 回到 `LauncherDefault`，系统解析恢复默认，抽屉图标恢复原样。

### 最终构建（suiren.qianji.debug）

- 安装后系统只解析到 `suiren.qianji.debug/...LauncherDefault`；`pm query-activities` 返回 1 个入口。
- 应用以 `suiren.qianji.debug` 启动，关于页显示 v1.3.0 与新包名。
- 通过应用自身身份（`run-as` 执行 `pm enable/disable`，与 `setComponentEnabledSetting` 等价）切换到 `LauncherBlue` 后，`resolve-activity` 返回蓝别名，应用抽屉显示**蓝底白色描边账本**；切回默认后恢复原图标。
- **限制：** 该模拟器上 `input tap`/`touchscreen tap` 对设置页底部若干行命中不稳定（同一坐标在滚动前后命中不同行，或点击被列表吞掉），未能稳定通过 UI 复测重构后的图标对话框。重构前的同一交互已在中间构建上通过 UI 完成。系统组件状态、系统解析结果和桌面渲染均已实测。

### 测试环境处理

- 旧版 `com.xiaojinzi.tally.opensource.debug` 的无障碍服务会占用 UI 自动化通道。验证期间曾临时关闭，完成后已恢复为原值。
- 未修改屏幕尺寸、密度、字体、主题或勿扰设置。
- 验证中一次误触打开了系统通讯录的通知权限弹窗，已拒绝并退出，未授权。

## 构建结果

- Debug：`suiren.qianji.debug`，1.3.0 / 1030000，构建成功；APK 87,216,561 字节，SHA-256 `0b2195099abdb7bbe6805af8bfd84343d1996ac0a5aa02c031c4f52ed102969c`。
- Release：`suiren.qianji`，1.3.0 / 1030000，R8 构建成功（4 分 35 秒）；APK 49,365,812 字节，SHA-256 `3a26d2cf12b9a4c4820215df687b7f42bd0476f75274ad415c4908165f72ce72`。
- 两个 APK 均无 INTERNET/NETWORK/WIFI 权限，无已删账号或支付 SDK 类残留，v2/v3 签名通过。
- Release DEX 中 Shizuku 辅助服务、小部件 Worker、XLS 解析器构造器保留，服务注册资源正确（`release-r8-final.json`）。
- 整包 ZIP CRC 与逐文件摘要均通过；脱敏模式扫描无命中。

交付目录：

- Debug：`dist/qianji-yike-port-v1.3.0-20261115/`
- Release：`dist/qianji-yike-port-v1.3.0-release-20261115/`

## 限制

- 图标方案是预置图标切换，不是任意相册图片替换；Android 不允许运行时替换启动图标资源。
- 桌面缓存刷新时间由各厂商桌面决定。
- 最终 `suiren.qianji` 包未做完整设备回归（启动、记账、Shizuku、小部件）；Release 未安装验证。
- 设置页底部行的 UI 点击在该模拟器上不稳定，建议在实体设备上手工复核一次图标选择对话框。
- 未提交、推送或上传 Release。
