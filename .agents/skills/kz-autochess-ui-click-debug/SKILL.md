---
name: kz-autochess-ui-click-debug
description: KZ AutoChess UI 点击检测问题排查 skill。当用户报告 UI 元素有悬停效果但点击无响应、点击位置偏移、或点击事件未触发时使用此 skill。涵盖坐标系统、区域检测、事件状态机等常见问题。适用于 LibGDX 项目中的 InputListener、GameRenderer 相关的点击检测调试。
---

# KZ AutoChess UI 点击检测问题排查

此 skill 用于排查 KZ AutoChess 项目中 UI 点击检测相关的问题。

## 何时使用

- 用户报告"按钮有悬停效果但点击没有反应"
- 点击位置与实际点击的目标不一致
- 某些 UI 元素可以点击，某些不能
- 需要调试 InputListener 的点击检测逻辑

## 常见问题类型

### 1. 坐标系统混淆

LibGDX 有多种坐标系统：

| 坐标系 | 原点位置 | Y轴方向 | 获取方式 |
|--------|----------|---------|----------|
| 屏幕坐标 | 左下角 | 向上 | `Gdx.input.getX()`, `Gdx.input.getY()` |
| UI 坐标 | 左上角 | 向下 | 手动转换：`height - screenY` |
| 世界坐标 | 相机原点 | 取决于相机 | `camera.unproject()` |

**典型错误**：布局参数使用 UI 坐标系（Y 从上往下），但点击检测使用屏幕坐标，导致 Y 轴反向。

**排查方法**：
```java
// 添加日志打印原始坐标和转换后坐标
float x = pos.getScreenX();
float y = Gdx.graphics.getHeight() - pos.getScreenY(); // 转换为 UI 坐标
Gdx.app.log("InputDebug", String.format("screen=(%.0f,%.0f) converted=(%.0f,%.0f)",
    pos.getScreenX(), pos.getScreenY(), x, y));
```

### 2. 区域依赖问题

**问题**：子区域的检测逻辑依赖于父区域的状态。

**示例代码（错误）**：
```java
// ❌ 错误：刷新按钮的悬停检测依赖于商店区域
inputState.isHoveringShop = isInShopArea(x, y, layout);
if (inputState.isHoveringShop) {
    // 只在商店区域内才检查刷新按钮
    inputState.isHoveringRefreshButton = isOnRefreshButton(x, y, layout);
} else {
    inputState.isHoveringRefreshButton = false; // 按钮在商店外时永远为 false
}
```

**修复方法**：
```java
// ✅ 正确：独立检查每个区域
inputState.isHoveringShop = isInShopArea(x, y, layout);
inputState.isHoveringRefreshButton = isOnRefreshButton(x, y, layout); // 不依赖商店区域
```

### 3. 点击状态机问题

**问题**：PRESSED 和 RELEASED 事件处理不当。

**常见模式**：
```java
// PRESSED 时设置标志
if (state == InputState.PRESSED) {
    if (isInValidArea(x, y)) {
        wasPressingInValidArea = true; // ⚠️ 如果 valid area 判断错误，标志不会设置
    }
}

// RELEASED 时检查标志
if (state == InputState.RELEASED) {
    if (wasPressingInValidArea) {
        handleClick();
        wasPressingInValidArea = false;
    }
}
```

**排查方法**：在 PRESSED 和 RELEASED 处理中添加日志，确认标志被正确设置。

### 4. 布局参数不一致

**问题**：渲染使用的布局参数与点击检测使用的布局参数不一致。

**检查清单**：
- [ ] CardShopLayout 的参数是否在渲染和检测中一致
- [ ] 是否有多个 layout 实例
- [ ] 是否有地方修改了 layout 的字段值

## 标准排查流程

### 步骤 1：确认事件被捕获

```java
@Override
public boolean handle(InputEvent event) {
    // 添加日志确认事件类型
    Gdx.app.log("InputDebug", "Event: " + event.getInputState() +
        ", Type: " + event.getInputType());
    // ...
}
```

**预期结果**：点击时应该看到 `PRESSED` 和 `RELEASED` 事件。

### 步骤 2：验证坐标转换

```java
InputEvent.InputPosition pos = event.getInputPosition();
float screenX = pos.getScreenX();
float screenY = pos.getScreenY();
float uiX = screenX;
float uiY = Gdx.graphics.getHeight() - screenY;

Gdx.app.log("InputDebug", String.format("screen=(%.0f,%.0f) ui=(%.0f,%.0f)",
    screenX, screenY, uiX, uiY));
```

**验证方法**：
1. 在日志中找到点击时的坐标
2. 与布局参数对比（如 `refreshButtonX=50, refreshButtonY=340, width=140, height=35`）
3. 确认坐标是否在预期范围内

### 步骤 3：检查区域检测逻辑

```java
// 添加日志检查每个区域
boolean inShop = isInShopArea(uiX, uiY, layout);
boolean onButton = isOnRefreshButton(uiX, uiY, layout);
Gdx.app.log("InputDebug", String.format("inShop=%s, onButton=%s", inShop, onButton));
```

### 步骤 4：检查状态依赖关系

查看 `updateHoverState()` 或类似方法，确认：
- 子区域的检测是否独立于父区域
- 是否有 `if (parentArea) { checkChild() }` 的模式

### 步骤 5：检查点击处理流程

```java
private void handleClick() {
    Gdx.app.log("ClickDebug", String.format(
        "hoverShop=%s, hoverButton=%s, cardIndex=%d",
        inputState.isHoveringShop,
        inputState.isHoveringRefreshButton,
        inputState.hoveredCardIndex));
    // ...
}
```

## 需要避免的操作

### ❌ 不要盲目修改坐标

在未确认坐标系统之前，不要简单地反转 Y 坐标或添加偏移量。先通过日志确认实际的坐标值。

### ❌ 不要假设悬停正常=点击正常

悬停效果可能由渲染器独立处理（如 CardRenderer），而点击检测由 InputListener 处理。两者可能使用不同的逻辑。

### ❌ 不要忽略区域关系

当子区域（如刷新按钮）在父区域（如商店背景）之外时，不要让子区域的检测依赖于父区域的状态。

### ❌ 不要在多个地方维护布局

避免在多个类中重复定义布局参数。使用单一的 CardShopLayout 类。

## 布局参数最佳实践

```java
public class CardShopLayout {
    // 商店区域
    public float shopX = 50f;
    public float shopY = 50f;
    public float shopWidth = 700f;
    public float shopHeight = 280f;

    // 子元素坐标可以是相对于商店区域
    // 或绝对坐标（取决于设计）
    public float refreshButtonX = 50f;   // 绝对坐标
    public float refreshButtonY = 340f;  // 可以在 shopY + shopHeight 之外
}
```

## 诊断检查清单

使用此清单逐项排查：

- [ ] 日志中是否出现 PRESSED/RELEASED 事件
- [ ] 坐标转换是否正确（screen vs UI）
- [ ] 点击坐标是否在目标区域内
- [ ] 区域检测方法是否正确（边界是否包含等号）
- [ ] 子区域检测是否独立于父区域
- [ ] wasPressingInXXX 标志是否在 PRESSED 时正确设置
- [ ] handleClick() 是否被调用
- [ ] handleClick() 中的条件判断是否正确

## 快速修复模板

### 场景 1：子区域在父区域外，点击无响应

```java
// 修复前：子区域依赖父区域
if (isInParentArea(x, y)) {
    isHoveringChild = isInChildArea(x, y);
}

// 修复后：独立检查
isInParentArea = isInParentArea(x, y);
isInChildArea = isInChildArea(x, y); // 不依赖父区域
```

### 场景 2：PRESSED 时标志未设置

```java
// 修复前：只在父区域内设置标志
if (state == PRESSED && isInParentArea(x, y)) {
    wasPressing = true;
}

// 修复后：扩展有效区域
if (state == PRESSED) {
    boolean validArea = isInParentArea(x, y) || isInChildArea(x, y);
    if (validArea) {
        wasPressing = true;
    }
}
```

## 相关文件

- `input/v2/listener/CardShopInputListener.java` - 输入监听器示例
- `input/v2/event/InputEvent.java` - 事件类型定义
- `input/v2/InputHandlerV2.java` - 输入处理器
- `render/CardShopLayout.java` - 布局参数类
- `render/CardShopRenderer.java` - 点击检测静态方法