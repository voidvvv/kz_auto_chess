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

## 羁绊系统

| 类型 | 阈值 | 效果 |
|------|--------|------|
| WARRIOR | 2,4,6 | 攻击+5%/级，防御+10%/级 |
| MAGE | 3,6,9 | 魔法+8%/级，回蓝+15%/级 |
| ARCHER | 2,4,6 | 攻速+10%/级，暴击+3%/级 |
| ASSASSIN | 3,6 | 暴伤+15%/级，闪避+5%/级 |
| TANK | 2,4,6 | 生命+10%/级，减伤+3%/级 |
| DRAGON | 2 | 全属性+5%/级 |
| BEAST | 2,4 | 攻击+5%/级，吸血+4%/级 |
| HUMAN | 2,4,6 | 全属性+3%/级，经验+10%/级 |

## 关键配置

- **视口**：双视口（UI固定/Game世界坐标）
- **坐标系**：左下角原点
- **战场**：玩家区（下半）vs 敌人区（上半）
- **投掷物**：ARROW直线/MAGIC_BALL追踪
- **伤害**：物理-防50%，魔法-防25%，真实-无视防御

## 重要须知

### 文件路径（必须使用Windows绝对路径）
```
C:/myFiles/dev/project/idea_projects/kz_auto_chess/src/main/java/com/voidvvv/autochess/Example.java
```
禁止使用 `./src/` 或 `/c/Users/` 形式（会导致文件修改bug）。

### 渲染模式切换
按 **F5** 在几何渲染和Tiled纹理渲染之间切换。

### 性能优化建议（高优先级）

| 问题 | 影响 | 建议 |
|------|--------|------|
| 无对象池 | GC压力 | 为`Projectile`、`DamageEvent`、`Particle`实现LibGDX Pool |
| 资源未统一管理 | 内存泄漏风险 | 使用AssetManager统一管理Skin、TiledMap、Font |
| 渲染批处理低效 | 性能下降 | 减少begin/end对，合并投影矩阵设置 |
| 碰撞O(n²) | 扩展性差 | 使用Box2D或空间分区 |

### 代码质量技术债

- `io/TestMain.java` - 无关测试文件，应删除
- `BattleUnitBlackboard.doSomething()` - 空方法，应删除
- `Ashley`依赖 - 已引入但未使用ECS模式
- 测试覆盖率极低 - 需添加核心逻辑单元测试

## 国际化

`I18N.get("key")` / `I18N.format("key", arg)`

支持：中文（默认）、英文、日文。资源文件位于 `assets/i18n/`。
