# 钱迹 → yike 功能移植（交付包）

把「钱迹」的记账能力移植到 `xiaojinzi123/yike-app` 脚手架上的工程快照。
不包含钱迹原 APK / dex / smali，只包含移植代码、测试数据与构建产物。

- 版本：`v1.0.2`（versionCode `1000200`）
- 应用包名：`com.xiaojinzi.tally.opensource.debug`（debug 变体）
- 产物：`artifacts/qianji-yike-port-v1.0.2-debug.apk`（约 103 MB，x86_64/arm 通用）

## 已实现功能

| 功能 | 说明 |
|---|---|
| 钱迹原库导入 | `qianjiapp` / `*.db` → yike Room：账单、账户、分类、标签、按月预算 |
| 文件导入 | CSV / XLS / XLSX，表头按列名识别 |
| 账单类型映射 | 支出/收入/转账/信用卡还款/退款（含 `refundsid` 原账单关联）/借款/债务利息/报销 |
| 标签 | `user_tag` + `user_bill.EXTRA.tags` → `label` / `bill_label` |
| 预算 | 总预算 + 分类预算（`yyyy-MM`），预算页可增改 |
| 首页预算进度 | 月支出卡片显示预算/剩余/超支，分类超支提示 |
| 自动记账 | 无障碍服务监听支付四件套 → 确认页 → 落库；工具页含调试入口 |
| 桌面小部件 | 2×1 Glance 小部件：本月支出/预算/剩余，点击打开 App |
| 入口 | 设置页「钱迹工具」+ 账单页顶部工具图标 |

## 目录结构

```
qianji-yike-port-20261114/
├── common/          # yike 主工程（含 module-qianji 移植模块）
├── opensource/      # opensource app 变体 + module-network
├── artifacts/       # 可安装 APK
├── testdata/        # 自造的钱迹测试库（无真实用户数据）
└── README.md
```

`common/settings.gradle.kts` 通过相对路径引用 `../opensource/app`，两个目录需保持同级。

## 构建

前置：JDK 21、Android SDK（compileSdk 36 / build-tools 36）、可访问 Google Maven 或阿里云镜像。

```bash
cd common
# 新建 local.properties（不入库）
# sdk.dir=D:/android-sdk
./gradlew :app:opensource:assembleDevDebug
```

产物：`opensource/app/build/outputs/apk/dev/debug/opensource-dev-debug.apk`

## 安装

```bash
adb install -r artifacts/qianji-yike-port-v1.0.2-debug.apk
```

首次启动若停在初始化页，请确认已同意隐私协议（本包为 debug 构建，
`isAgreedPrivacyAgreementConfig` 在模拟器快照回滚后可能为 false）。

## 脱敏处理

本快照已做以下处理：

1. **内网地址替换**：`opensource/app/build.gradle.kts` 中 dev 变体的原内网服务地址
   已改为公网 `https://yike.icxj.cn`。
2. **不包含**：`.git`、`build/`、`.gradle/`、`.idea/`、`.kotlin/`、
   `local.properties`、构建日志、设备数据库导出。
3. **签名文件**：`opensource/app/signForOpenSource` 来自上游开源工程，
   密码为上游公开值（`opensource`），不是个人私钥。
4. **测试数据**：`testdata/*.db` 为本次压测自造数据（账户名「现金/银行卡」等），
   不含真实用户账目。
5. **网络权限**：APK 保留上游的 INTERNET 权限（yike 自身含同步/更新功能），
   钱迹导入与预算功能全程只写本机 Room。

## 合规与许可证

- `common/LICENSE`、`opensource/LICENSE` 为上游 `xiaojinzi123/yike-app` 的许可证，
  分发与修改需遵守其条款（GPL/AGPL 系）。
- 本包不包含钱迹（`com.mutangtech.qianji`）的任何二进制或反编译产物。
- 仅供个人学习与研究使用，请勿用于商业分发。
