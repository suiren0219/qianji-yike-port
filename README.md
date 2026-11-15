# 钱迹 → yike v1.3.0：包名 suiren 与图标自定义

本版本在纯离线、Root/Shizuku 辅助授权的基础上：

- **Application ID 改为 `suiren.qianji`**（Debug 变体为 `suiren.qianji.debug`）。Android 构建工具要求包名至少包含一个点，单段 `suiren` 无法生成 Release 清单，因此采用以 suiren 开头的两段式。
- **新增应用图标自定义**：内置四套图标，可在应用设置中切换并随时切回默认。
- 版本：`1.3.0` / `1030000`。

## 重要：这是独立应用

包名变化意味着新版本与旧版 `com.xiaojinzi.tally.opensource.debug` 是**两个独立应用**：

- 新包有独立的数据目录，**不会自动读取旧版账本**。
- 旧版仍会保留在设备上，可继续打开旧账本；不要卸载旧版，否则旧数据会丢失。
- 新包的 Shizuku 授权、Root/无障碍授权、通知权限、小部件都需要重新设置。
- 如需把旧账本带到新包，目前没有内置迁移工具；可继续使用旧包，或通过账单导入功能手动迁移。

## 图标自定义

设置路径：**工具 → 设置 → 应用 → 应用图标**。

提供四套图标：

| 名称 | 样式 |
|---|---|
| 默认 | 保留原应用图标 |
| 海蓝 | 蓝色底、白色描边账本 |
| 墨绿 | 绿色底、白色描边账本 |
| 暖橙 | 橙色底、白色描边账本 |

实现方式是在清单中声明四个 `activity-alias`，运行时只启用一个。切换使用系统包管理器接口，不修改账本、权限或应用数据。桌面图标由系统桌面刷新，通常几秒内更新；个别桌面可能需要等待或重新打开应用列表。

切换图标时，如果当前任务是从正在被替换的入口启动的，系统可能结束该任务并回到桌面。这是 activity-alias 切换的系统行为，重新点击桌面图标即可继续，账本数据不受影响。设置页会提示桌面可能需要刷新。

图标切换不需要联网、Root 或 Shizuku。

## 其他功能（与 v1.2.0 相同）

- 无账号、无登录、无云同步、无在线 AI/更新；不申请 INTERNET 权限，账本与照片保存在本机。
- 本地账本、资产账户、分类、标签、预算、统计、日历、相册与小部件。
- 自动记账支持系统设置、Root、ADB（Shizuku）三种授权方式，仅用于开启本应用自身无障碍服务。
- 导入支持钱迹数据库、CSV、XLS；XLSX 明确拒绝。

## 构建

```bash
cd common
./gradlew :app:opensource:assembleDevDebug      # suiren.qianji.debug
./gradlew :app:opensource:assembleDevRelease    # suiren.qianji，R8
```

需要 JDK 21、Android SDK 36，`common/` 与 `opensource/` 保持同级。产物分别位于 `opensource/app/build/outputs/apk/dev/debug/` 和 `dev/release/`。

## 校验与许可

`MANIFEST.json` 记录整包文件摘要，外层 `SHA256SUMS`、`verification.json` 记录 APK 与 ZIP 摘要及扫描结果。仍使用上游公开测试证书，不是个人发布签名。

保留 GPL v2 许可证与第三方许可；Shizuku API 遵守其 MIT 许可，官方管理器不捆绑。静态脱敏扫描不是完整安全审计。

本轮未提交、推送或创建 GitHub Release，也未覆盖此前版本。
