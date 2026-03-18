---
name: kz-autochess-code-guidelines
description: KzAutoChess 项目代码生成指南 - 避免常见编译错误和架构问题。在为项目添加新功能（如 Model、Manager、Event、Render 等）时，必须遵守本 skill 的规范，特别是类名与文件名匹配、Manager 生命周期集成、全局状态管理和渲染管线切换。
compatibility:
  - KzAutoChess (Java + LibGDX)
---

# KzAutoChess 代码生成指南

本 skill 提供了在 KzAutoChess 项目中编写 Java 代码时必须遵守的规范，避免常见的编译错误和架构问题。

## 适用场景

当你在为 KzAutoChess 项目执行以下任务时，请参考本 skill：
- 创建新的 Model 类（如 `XxxModel`）
- 创建新的 Manager 类（如 `XxxManager`）
- 创建新的 Event 类（如 `XxxEvent`）
- 创建新的 Blackboard 类（如 `XxxBlackboard`）
- 创建新的 Renderer 类（如 `XxxRenderer`）
- 修改现有 Screen（如 `GameScreen`, `LevelSelectScreen`）
- 添加全局状态管理

## 核心规则

### 1. 类名与文件名必须完全匹配

**规则**: `public` 类名必须与文件名完全一致（包括大小写）

**错误示例**:
```java
// 文件: LifeConfigLoader.java  ❌
public final class LifeConfig {  // 类名不匹配！
    // ...
}
```

**正确做法**:
```java
// 文件: LifeConfig.java  ✅
public final class LifeConfig {
    // ...
}

// 或者
// 文件: LifeConfigLoader.java  ✅
public final class LifeConfigLoader {
    // ...
}
```

**检查清单**:
- [ ] 文件名与类名完全匹配（大小写一致）
- [ ] 如果类是 public 的，文件名必须与类名相同
- [ ] 避免复制粘贴时的命名错误

### 2. Manager 生命周期集成

**规则**: 所有 Manager 必须通过 `onEnter/onExit` 方法管理生命周期，并在 `AutoChessGameMode` 中正确集成。

**必须实现的方法**:
- `onEnter()` - 初始化时调用，注册事件监听器
- `onExit()` - 退出时调用，注销事件监听器
- `pause()` / `resume()` - 可选，用于暂停/恢复逻辑

**集成方式**:

在 `AutoChessGameMode.java` 中:
```java
// 1. 添加字段
private final PlayerLifeManager playerLifeManager;

// 2. 构造函数接收参数
public AutoChessGameMode(..., PlayerLifeManager playerLifeManager, ...) {
    // ...
    this.playerLifeManager = playerLifeManager;
}

// 3. 在 onEnter() 中调用
@Override
public void onEnter() {
    battleManager.onEnter();
    economyManager.onEnter();
    cardManager.onEnter();
    playerLifeManager.onEnter();  // ✅ 新的 Manager 也要调用
    // ...
}

// 4. 在其他生命周期方法中调用
@Override
public void pause() {
    battleManager.pause();
    economyManager.pause();
    cardManager.pause();
    playerLifeManager.pause();  // ✅
}

@Override
public void resume() {
    battleManager.resume();
    economyManager.resume();
    cardManager.resume();
    playerLifeManager.resume();  // ✅
}

@Override
public void onExit() {
    battleManager.onExit();
    economyManager.onExit();
    cardManager.onExit();
    playerLifeManager.onExit();  // ✅
}
```

**检查清单**:
- [ ] 新 Manager 是否实现了 `GameEventListener` 接口
- [ ] `onEnter()` 中是否调用了 `eventSystem.registerListener(this)`
- [ ] `onExit()` 中是否调用了 `eventSystem.unregisterListener(this)`
- [ ] `AutoChessGameMode.onEnter()` 中是否调用了 manager 的 `onEnter()`
- [ ] `AutoChessGameMode.onExit()` 中是否调用了 manager 的 `onExit()`

### 3. 全局状态管理

**规则**: 需要跨 Screen 共享的状态（如血量、进度等）必须放在 `KzAutoChess` 游戏主类中，而不是各个 Screen 中。

**正确做法**:

在 `KzAutoChess.java` 中:
```java
public class KzAutoChess extends Game {
    private PlayerLifeBlackboard playerLifeBlackboard;  // ✅ 全局状态

    public PlayerLifeBlackboard getPlayerLifeBlackboard() {
        return playerLifeBlackboard;
    }

    @Override
    public void create() {
        playerLifeBlackboard = new PlayerLifeBlackboard();  // ✅ 初始化一次
        setScreen(new StartScreen(this));
    }
}
```

**错误做法**:
```java
// ❌ 在每个 Screen 中各自创建，无法跨 Screen 共享
public class GameScreen {
    private PlayerLifeBlackboard playerLifeBlackboard = new PlayerLifeBlackboard();
}

public class LevelSelectScreen {
    private PlayerLifeBlackboard playerLifeBlackboard = new PlayerLifeBlackboard();
}
```

**检查清单**:
- [ ] 全局状态是否在 `KzAutoChess` 中声明为字段
- [ ] 是否提供了 getter 方法（如 `getPlayerLifeBlackboard()`）
- [ ] 是否在 `create()` 方法中初始化
- [ ] 各个 Screen 是否通过 `game.getXxx()` 获取共享状态

### 4. 渲染管线切换

**规则**: `ShapeRenderer` 和 `SpriteBatch` 不能同时处于活跃状态，切换时必须先 `end()` 当前渲染器，再 `begin()` 目标渲染器。

**正确做法**:
```java
// 使用 SpriteBatch 渲染文字
game.getBatch().begin();
font.draw(game.getBatch(), layout, x, y);
// ... 更多文字渲染 ...
game.getBatch().end();  // ✅ 先结束 batch

// 使用 ShapeRenderer 渲染图形（如血条）
shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
shapeRenderer.rect(x, y, width, height);
shapeRenderer.end();

// 如果需要混合使用
game.getBatch().begin();      // ✅ 重新开始 batch
font.draw(...);
game.getBatch().end();
```

**错误示例**:
```java
// ❌ 忘记结束 batch 就开始 ShapeRenderer
game.getBatch().begin();
font.draw(...);
shapeRenderer.begin(...);  // ❌ 错误！batch 还在活跃状态
shapeRenderer.end();
```

**检查清单**:
- [ ] `ShapeRenderer` 和 `SpriteBatch` 的 `begin/end` 是否成对出现
- [ ] 是否在渲染 ShapeRenderer 前调用了 `game.getBatch().end()`
- [ ] 是否在 ShapeRenderer 后重新调用了 `game.getBatch().begin()`（如需要）

## 命名规范

### 文件/类命名模式

| 组件类型 | 命名模式 | 示例 | 说明 |
|----------|----------|------|------|
| 纯数据类 | `XxxModel` | `PlayerLifeModel` | 只存储数据，无逻辑 |
| 配置类 | `XxxConfig` | `LifeConfig` | 配置加载器 |
| 黑板类 | `XxxBlackboard` | `PlayerLifeBlackboard` | 聚合同一场景的多模型 |
| 管理器类 | `XxxManager` | `PlayerLifeManager` | 管理生命周期和业务逻辑 |
| 渲染器类 | `XxxRenderer` | `LifeBarRenderer` | 负责 UI/图形渲染 |
| 事件类 | `XxxEvent` | `PlayerDeathEvent` | 事件数据对象 |
| 屏幕类 | `XxxScreen` | `GameOverScreen` | 实现 Screen 接口 |

### 包结构

```
com.voidvvv.autochess
├── model/          # 数据模型
├── logic/           # 业务逻辑和配置加载
├── manage/          # 管理器
├── battle/          # 战斗相关（包括 Blackboard）
├── render/          # 渲染器
├── event/           # 事件类
├── screens/         # 屏幕类
└── utils/           # 工具类
```

## Model-Manager-Render 分离原则

当创建新功能时，必须遵循这个分离原则：

1. **Model 层** - 纯数据类，使用不可变设计
2. **Blackboard 层** - 聚合同一场景的多模型和状态
3. **Manager 层** - 管理生命周期，处理业务逻辑，响应事件
4. **Render 层** - 负责 UI/图形渲染，依赖 Model 数据

**示例**:
```java
// Model - 不可变数据
public final class PlayerLifeModel {
    private final int currentHealth;
    public PlayerLifeModel takeDamage(int damage) { ... }  // 返回新实例
}

// Blackboard - 聚合状态
public class PlayerLifeBlackboard {
    private final PlayerLifeModel lifeModel;
    public void onBattleEnd(...) { ... }
}

// Manager - 生命周期和事件
public class PlayerLifeManager implements GameEventListener {
    public void onEnter() { eventSystem.registerListener(this); }
    public void onGameEvent(GameEvent event) { ... }
}

// Render - UI 渲染
public class LifeBarRenderer {
    public static void render(ShapeRenderer sr, SpriteBatch batch, PlayerLifeModel model, ...) { ... }
}
```

## 事件系统规范

### Event 类结构

```java
public class XxxEvent implements GameEvent {
    private final long timestamp;
    private final /* 其他字段 */;

    public XxxEvent(/* 参数 */) {
        this.timestamp = System.currentTimeMillis();
        // ...
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        // 可选：不支持修改
    }
}
```

### Manager 注册事件监听器

```java
public class XxxManager implements GameEventListener {
    private final GameEventSystem eventSystem;

    public void onEnter() {
        eventSystem.registerListener(this);  // ✅ 必须注册
    }

    public void onExit() {
        eventSystem.unregisterListener(this);  // ✅ 必须注销
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof XxxEvent) {
            // 处理事件
        }
    }
}
```

### Screen 注册事件监听器

```java
public class XxxScreen implements Screen, GameEventListener {
    @Override
    public void show() {
        eventSystem.registerListener(this);  // ✅
    }

    @Override
    public void hide() {
        eventSystem.unregisterListener(this);  // ✅
    }
}
```

## 国际化规范

所有显示的文本必须通过 `I18N` 工具获取：

```java
// ✅ 正确
String text = I18N.get("key_name");
String formatted = I18N.format("key_with_param", param);

// ❌ 错误
String text = "硬编码的中文或英文";
```

**添加新翻译时**:
1. 在 `assets/i18n/i18n_zh.properties` 中添加中文翻译
2. 在 `assets/i18n/i18n_en.properties` 中添加英文翻译
3. 使用描述性的 key 名称（如 `game_over`, `max_level_reached`）

## 资源文件规范

### 配置文件

将配置文件放在 `core/src/main/resources/` 目录：

```
core/src/main/resources/
├── life_config.json       # 血量配置
├── character_stats.json   # 角色属性
└── ...
```

### 加载配置

```java
FileHandle file = Gdx.files.internal("config.json");
JsonValue root = new JsonReader().parse(file);
int value = root.getInt("key", defaultValue);
```

## 编译前检查清单

在完成代码修改后、提交前，请运行以下检查：

```bash
# 验证 Java 编译
gradle compileJava
```

**手动检查项**:
- [ ] 所有新增的 Java 文件类名与文件名匹配
- [ ] 所有 Manager 都在 `AutoChessGameMode.onEnter()` 中被调用
- [ ] 所有 Manager 都在 `AutoChessGameMode.onExit()` 中被调用
- [ ] 事件监听器在 `onEnter`/`onExit` 或 `show`/`hide` 中正确注册/注销
- [ ] `ShapeRenderer` 和 `SpriteBatch` 的切换正确
- [ ] 全局状态在 `KzAutoChess` 中管理
- [ ] 国际化文本使用 `I18N.get()` 获取
- [ ] 配置文件路径正确

## 参考文档

当不确定如何实现某个功能时，参考现有代码：

- `EconomyManager.java` - Manager 生命周期和事件处理示例
- `CardManager.java` - 复杂业务逻辑处理示例
- `BattleManager.java` - 战斗系统管理示例
- `GameUIManager.java` - UI 渲染和事件处理示例

## 常见编译错误速查

| 错误类型 | 错误信息 | 解决方法 |
|----------|----------|---------|
| 类名不匹配 | `class X is public, should be declared in a file named X.java` | 修改文件名或类名使其匹配 |
| 符号找不到 | `cannot find symbol: Xxx` | 检查 import 语句或类名拼写 |
| 包不匹配 | `package xxx does not exist` | 检查 package 声明 |
| 泛型错误 | `incompatible types` | 检查泛型参数匹配 |
| 空指针 | `variable might not have been initialized` | 检查对象初始化顺序 |

## 总结

遵守本 skill 的规范可以有效避免：
1. 类名与文件名不匹配的编译错误
2. Manager 生命周期未集成导致的事件监听失败
3. 全局状态管理不当导致的跨 Screen 共享问题
4. 渲染管线切换错误导致的渲染问题

在为 KzAutoChess 项目添加新功能时，请始终参考此指南。
