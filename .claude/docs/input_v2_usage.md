# 输入处理系统 V2 使用文档

## 概述

输入处理系统 V2 是一个基于事件驱动的输入处理框架，将 LibGDX 原生的 `InputProcessor` 接口封装成更易用的事件监听模式。

## 架构组件

### 1. InputEvent（输入事件）

封装输入事件的信息，包含：
- **InputType** - 输入类型（KEYBOARD, MOUSE, TOUCH, SCROLL）
- **InputState** - 输入状态（PRESSED, RELEASED, TYPED, MOVED, DRAGGED, SCROLLED）
- **InputPosition** - 输入位置（屏幕坐标和世界坐标）
- **KeyCode** - 键盘按键码
- **MouseButton** - 鼠标按钮（LEFT, RIGHT, MIDDLE, BACK, FORWARD）
- **Pointer** - 触摸指针ID
- **ScrollAmount** - 滚轮滚动量

### 2. InputListener（输入监听器接口）

监听输入事件的接口，包含以下方法：
- `handle(InputEvent event)` - 处理输入事件
- `getPriority()` - 获取监听器优先级（数值越大越先处理）
- `accepts(InputEvent event)` - 判断是否接受该事件
- `onAttached()` / `onDetached()` - 生命周期回调

### 3. InputAdapter（输入监听器适配器）

提供默认实现的适配器类，可以只重写感兴趣的方法：
- `handleKeyPressed(InputEvent)` - 处理键盘按下
- `handleKeyReleased(InputEvent)` - 处理键盘释放
- `handleMousePressed(InputEvent)` - 处理鼠标按下
- `handleMouseReleased(InputEvent)` - 处理鼠标释放
- `handleMouseMoved(InputEvent)` - 处理鼠标移动
- `handleMouseDragged(InputEvent)` - 处理鼠标拖拽
- `handleTouchPressed(InputEvent)` - 处理触摸按下
- `handleTouchReleased(InputEvent)` - 处理触摸释放
- `handleTouchDragged(InputEvent)` - 处理触摸拖拽
- `handleScrolled(InputEvent)` - 处理滚轮滚动

### 4. InputHandlerV2（输入处理器）

核心输入处理器，实现了 `InputProcessor` 接口：
- 管理监听器列表
- 将 LibGDX 输入事件转换为 `InputEvent`
- 按优先级分发事件给监听器
- 支持世界坐标转换（需要设置 Camera）

## 使用示例

### 基本使用

```java
// 1. 创建 InputHandlerV2
InputHandlerV2 inputHandler = new InputHandlerV2();

// 2. 注册到 LibGDX
Gdx.input.setInputProcessor(inputHandler);

// 3. 添加监听器
inputHandler.registerListener(new MyInputListener());

// 4. 在游戏循环中更新
@Override
public void render(float delta) {
    inputHandler.update(delta);
    // ...
}
```

### 自定义监听器（实现接口）

```java
public class MyInputListener implements InputListener {

    @Override
    public boolean handle(InputEvent event) {
        // 处理所有类型的事件
        if (event.isKeyboardEvent()) {
            System.out.println("Key: " + event.getKeyCode());
        } else if (event.isMouseEvent()) {
            System.out.println("Mouse at: " +
                event.getInputPosition().getScreenX() + ", " +
                event.getInputPosition().getScreenY());
        }
        return false; // 返回 false 继续传递事件
    }

    @Override
    public int getPriority() {
        return 0; // 默认优先级
    }
}

inputHandler.registerListener(new MyInputListener());
```

### 使用适配器（推荐）

```java
public class GameControlListener extends InputAdapter {

    @Override
    protected boolean handleKeyPressed(InputEvent event) {
        if (event.getKeyCode() == Input.Keys.SPACE) {
            // 跳跃
            player.jump();
            return true; // 消费事件，停止传递
        }
        return false;
    }

    @Override
    protected boolean handleMousePressed(InputEvent event) {
        if (event.isLeftMouseButton()) {
            float worldX = event.getInputPosition().getWorldX();
            float worldY = event.getInputPosition().getWorldY();
            player.moveTo(worldX, worldY);
            return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 10; // 较高优先级
    }
}

inputHandler.registerListener(new GameControlListener());
```

### 使用示例监听器

#### 1. 调试监听器

```java
// 打印所有输入事件，用于调试
DebugInputListener debugListener = new DebugInputListener();
inputHandler.registerListener(debugListener);

// 禁用调试输出
debugListener.setEnabled(false);
```

#### 2. 点击监听器

```java
ClickInputListener clickListener = new ClickInputListener((screenX, screenY, worldX, worldY) -> {
    System.out.println("Clicked at world: " + worldX + ", " + worldY);
    // 执行点击逻辑
});
inputHandler.registerListener(clickListener);
```

#### 3. 快捷键监听器

```java
KeyboardShortcutListener shortcutListener = new KeyboardShortcutListener()
    .onEscape(event -> {
        // ESC 键 - 暂停游戏
        game.pause();
    })
    .onSpace(event -> {
        // SPACE 键 - 确认/跳跃
        game.confirm();
    })
    .onFunctionKey(5, event -> {
        // F5 键 - 切换渲染模式
        RenderConfig.toggleRendering();
    })
    .onPress(Input.Keys.R, event -> {
        // R 键 - 重新开始
        game.restart();
    });
inputHandler.registerListener(shortcutListener);
```

### 设置摄像机（世界坐标转换）

```java
// 创建带摄像机的 InputHandlerV2
OrthographicCamera camera = new OrthographicCamera();
InputHandlerV2 inputHandler = new InputHandlerV2(camera);

// 或者稍后设置
inputHandler.setCamera(camera);

// 现在 InputEvent 中的 InputPosition 会包含世界坐标
public class WorldClickListener extends InputAdapter {
    @Override
    protected boolean handleMousePressed(InputEvent event) {
        InputEvent.InputPosition pos = event.getInputPosition();
        if (pos.hasWorldCoords()) {
            float worldX = pos.getWorldX();
            float worldY = pos.getWorldY();
            // 使用世界坐标
            selectUnitAt(worldX, worldY);
        }
        return true;
    }
}
```

### 事件过滤

```java
public class OnlyLeftClickListener implements InputListener {

    @Override
    public boolean accepts(InputEvent event) {
        // 只接受左键点击事件
        return event.isMouseEvent() &&
               event.isLeftMouseButton() &&
               event.getInputState() == InputEvent.InputState.PRESSED;
    }

    @Override
    public boolean handle(InputEvent event) {
        // 这里只会收到左键点击事件
        System.out.println("Left click!");
        return true;
    }
}
```

### 分发模式

```java
// 默认模式：批量分发（在 update 中处理）
InputHandlerV2 inputHandler = new InputHandlerV2();
// 事件会在 inputHandler.update(delta) 中统一分发

// 立即分发模式：事件在 InputProcessor 回调中立即分发
inputHandler.setImmediateDispatch(true);
```

## 优先级系统

监听器按优先级从高到低依次处理事件：

```java
// 高优先级：快捷键
inputHandler.registerListener(new KeyboardShortcutListener()); // priority = 100

// 中优先级：游戏控制
inputHandler.registerListener(new GameControlListener()); // priority = 10

// 低优先级：调试输出
inputHandler.registerListener(new DebugInputListener()); // priority = -100

// 如果高优先级监听器返回 true（消费事件），
// 低优先级监听器将不会收到该事件
```

## 生命周期管理

```java
public class MyScreen implements Screen {

    private InputHandlerV2 inputHandler;
    private MyInputListener listener;

    @Override
    public void show() {
        inputHandler = new InputHandlerV2();
        Gdx.input.setInputProcessor(inputHandler);

        listener = new MyInputListener();
        inputHandler.registerListener(listener); // 会调用 listener.onAttached()
    }

    @Override
    public void hide() {
        inputHandler.unregisterListener(listener); // 会调用 listener.onDetached()
        inputHandler.dispose();
    }

    @Override
    public void render(float delta) {
        inputHandler.update(delta); // 分发队列中的事件
        // ...
    }
}
```

## 设计模式

- **观察者模式** - 监听器订阅输入事件
- **策略模式** - 不同监听器实现不同的输入处理策略
- **责任链模式** - 事件按优先级链式传递
- **Builder 模式** - InputEvent 使用 Builder 构建

## 注意事项

1. **线程安全** - 监听器列表使用 `CopyOnWriteArrayList`，可以在多线程环境中安全使用
2. **内存管理** - `InputEvent` 对象在事件分发后会被垃圾回收，注意不要长期持有引用
3. **事件消费** - 返回 `true` 会停止事件传递，返回 `false` 继续传递给下一个监听器
4. **性能** - 对于高频事件（如 `mouseMoved`），建议在 `accepts()` 方法中进行过滤
5. **摄像机** - 如果需要世界坐标，确保在处理输入前设置摄像机