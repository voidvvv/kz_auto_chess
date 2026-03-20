@.claude/rules/chinese.md

# KZ AutoChess - LibGDX 自走棋游戏

## 项目概述

基于 LibGDX 框架开发的自走棋游戏，玩家通过购买、升级卡牌，在战场上与敌人自动战斗。

**核心玩法**: 放置阶段部署棋子 → 战斗阶段自动对战 → 循环

## 技术栈

| 组件 | 版本/说明 |
|------|----------|
| Java | 25 |
| LibGDX | 1.14.0 |
| Ashley (ECS) | 1.7.4 |
| gdx-ai | 1.8.2 |
| Box2D | 物理碰撞 |
| FreeType | 字体渲染 |
| 构建工具 | 本地 Gradle（非 ./gradlew） |
| 测试框架 | JUnit 5 |

## 关键目录

```
kz_auto_chess/
├── core/src/main/java/com/voidvvv/autochess/
│   ├── KzAutoChess.java       # 游戏主类，管理全局状态
│   ├── battle/                # 战斗AI、行为树、黑板
│   ├── event/                 # 事件系统（按领域分包）
│   ├── game/                  # GameMode 抽象层
│   ├── input/                 # 输入处理
│   ├── logic/                 # 游戏规则（经济、羁绊、升级）
│   ├── manage/                # 生命周期管理器
│   ├── model/                 # 数据模型（Card, BattleCharacter等）
│   ├── render/                # 渲染层
│   ├── screens/               # 游戏屏幕（Start/LevelSelect/Game）
│   ├── sm/                    # 状态机
│   ├── ui/                    # UI组件
│   ├── updater/               # 更新逻辑
│   └── utils/                 # 工具类
├── assets/                    # 游戏资源（字体、配置JSON）
├── lwjgl3/                    # 桌面平台启动器
└── .claude/docs/              # 架构文档
```

## 构建与运行

```bash
# 构建
gradle build

# 运行游戏
gradle lwjgl3:run

# 运行测试
gradle test
```

## 核心架构原则

1. **Model/Updator/Manager/Render 分离** - 严格的关注点分离
2. **Blackboard 模式** - AI代理的上下文聚合（`BattleUnitBlackboard`）
3. **事件驱动** - 通过 `GameEventSystem` 解耦通信
4. **GameMode 抽象** - 屏幕与游戏逻辑分离

## 添加新模块规则

在代码中添加任何新模块都必须遵守：

1. **Model** - 纯数据结构，无业务逻辑
2. **Updator** - 负责数据更新逻辑
3. **Manager** - 管理生命周期和协调
4. **Render** - 仅负责渲染
5. **Blackboard** - 聚合多种 model 甚至状态机，各场景可有自己的 Blackboard

## 资源说明

- 可使用图形代替真实图片素材
- 字体: 系统中文字体（Mac/Linux 使用 FontUtils 自动适配）
- 复杂渲染可创建定制 Shader

## 附加文档

| 文档                                                                                                            | 说明 |
|---------------------------------------------------------------------------------------------------------------|------|
| [@.claude/docs/architectural_patterns.md](.claude/docs/architectural_patterns.md)                             | 架构模式、设计决策、约定 |
| [@.claude/docs/core_flow.md](.claude/docs/core_flow.md)                                                       | 核心流程图解（PlantUML） |
| [@.claude/skills/kz-autochess-patterns.md](.claude/skills/kz-autochess-patterns.md)                           | 代码模式与工作流详细指南 |
| [@.claude/skills/kz-autochess-code-guidelines/SKILL.md](.claude/skills/kz-autochess-code-guidelines/SKILL.md) | 编码指南与常见问题 |

## 常见注意事项

- **坐标转换**: 使用 `ViewManagement.screenToUI()` / `screenToWorld()`
- **事件分发**: 通过 `GameEventSystem` 分发，不直接调用监听器
- **渲染状态**: SpriteBatch/ShapeRenderer 之间调用 `holder.flush()`
- **输入处理**: 使用 `InputContext.fromInput(camera)` 创建上下文
