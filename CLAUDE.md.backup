# CLAUDE.md

LibGDX自走棋游戏，支持多平台（桌面端LWJGL3）。核心玩法：购卡、部署、羁绊、自动战斗。

## 快速开始

```bash
./gradlew build          # 构建
./gradlew lwjgl3:run      # 运行
./gradlew :core:test       # 测试
```

## 技术栈

- **LibGDX** - 游戏框架
- **GDX-AI** - 行为树AI
- **GDX-Box2D** - 物理引擎（已引入但未使用）
- **Java 17** + **Gradle 8+**
- **JUnit 5** - 测试

## 核心架构

### 分层模式：Model → Updater → Manager → Render

```
model/          # 纯数据模型
updater/         # 更新逻辑
manage/          # 生命周期管理
render/          # 渲染
battle/          # AI行为树
sm/              # 状态机
listener/        # 事件监听
```

### 关键类映射

| 模块 | 类 | 作用 |
|------|-----|------|
| model | `Battlefield`, `BattleCharacter`, `Card`, `Projectile` | 数据实体 |
| manage | `ProjectileManager`, `SynergyManager`, `CardShop` | 系统协调 |
| updater | `BattleUpdater`, `ProjectileUpdater` | 逻辑更新 |
| render | `BattleFieldRender`, `ProjectileRenderer`, `TiledBattleCharacterRender` | 视觉渲染 |
| battle | `UnitBehaviorTreeFactory`, `BattleUnitBlackboard` | AI决策 |

### 游戏循环（GameScreen）

```
show() → 初始化资源、重置战场
render(delta) →
  handleInput() → 处理点击、拖拽
  stage.act() → Scene2D UI更新
  if (phase == BATTLE):
    updateBattle() → 行为树、状态机、投掷物、伤害事件
  drawWorldContent() → 战场+角色+投掷物
  drawUIContent() → 商店+卡组
  drawDragging() → 拖拽预览
```

### 战斗流程

```
startBattle() →
  生成敌人 → 为每个角色创建BehaviorTree + StateMachine
  循环：寻敌→移动→攻击→伤害结算→死亡移除
endBattle() → 清除敌人/复活玩家/经济结算
```

## 包结构速查

- `model/` - 数据模型
- `updater/` - 更新逻辑
- `manage/` - 管理器
- `render/` - 渲染器
- `battle/` - AI行为树
- `sm/` - 状态机（`machine/`接口、`state/common/`实现）
- `listener/` - 事件监听
- `msg/` - 消息系统
- `logic/` - 业务逻辑（`SynergyManager`、`CardUpgradeLogic`）
- `screens/` - 游戏屏幕
- `ui/` - UI组件
- `utils/` - 工具类（`I18N`、`FontUtils`、`RenderConfig`、`TiledAssetLoader`）


## 重要须知

### 文件路径（必须使用Windows绝对路径）
```
C:/myFiles/dev/project/idea_projects/kz_auto_chess/src/main/java/com/voidvvv/autochess/Example.java
```
禁止使用 `./src/` 或 `/c/Users/` 形式（会导致文件修改bug）。

### 渲染模式切换
按 **F5** 在几何渲染和Tiled纹理渲染之间切换。

## 国际化

`I18N.get("key")` / `I18N.format("key", arg)`

支持：中文（默认）、英文、日文。资源文件位于 `assets/i18n/`。
