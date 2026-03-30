# KZ AutoChess 卡牌商店渲染模块设计文档

## 文档信息
- **创建日期**: 2026-03-27
- **作者**: Claude Code
- **项目**: KZ AutoChess
- **模块**: 卡牌商店渲染系统

---

## 1. 概述

### 1.1 设计目标

为 KZ AutoChess 项目设计一个完整、可扩展的卡牌商店渲染模块，支持以下核心功能：
- **商店刷新**: 刷新商店中的5张随机卡牌
- **购买卡牌**: 点击购买商店中的卡牌
- **出售卡牌**: 从玩家卡组出售卡牌
- **卡牌升级**: 三张相同卡牌合并升级
- **拖拽交互**: 拖拽卡牌进行部署

### 1.2 设计原则

遵循项目既定的架构模式：
1. **Model/Updator/Manager/Render 分离** - 严格的关注点分离
2. **Scene2D 优先** - 使用 LibGDX Scene2D 系统
3. **事件驱动** - 通过 GameEventSystem 解耦通信
4. **国际化支持** - 所有文本通过 I18N 获取
5. **弹性布局** - 使用 Table 实现自适应比例布局

---

## 2. UI 布局设计

### 2.1 整体布局结构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              KZ AUTO CHESS                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│  [返回]  等级: 5    金币: ████████░░ 42    血量: ♥♥♥♥♥♥♥♥░░ 8/10            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        战场区域 (Battlefield)                         │   │
│  │                     3x6 放置格子 + 角色精灵                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │      卡组区域 (Deck)         │  │       商店区域 (Shop)                │  │
│  │  ┌─────┐ ┌─────┐ ┌─────┐   │  │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │  │
│  │  │卡1  │ │卡2  │ │卡3  │   │  │  │卡A  │ │卡B  │ │卡C  │ │卡D  │  │  │
│  │  │  x2 │ │  x1 │ │  x3 │   │  │  │  3G │ │  5G │ │  2G │ │  4G │  │  │
│  │  └─────┘ └─────┘ └─────┘   │  │  └─────┘ └─────┘ └─────┘ └─────┘  │  │
│  │  ┌─────┐ ┌─────┐ ┌─────┐   │  │  ┌─────┐                            │  │
│  │  │卡4  │ │卡5  │ │...  │   │  │  │卡E  │                            │  │
│  │  └─────┘ └─────┘ └─────┘   │  │  └─────┘                            │  │
│  │                            │  │                                      │  │
│  │  [出售选中的卡] [升级]      │  │  [刷新商店 (2金币)] [购买]           │  │
│  └─────────────────────────────┘  └─────────────────────────────────────┘  │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  羁绊面板 (Synergy Panel): [战士 2/4] [法师 1/3] [射手 0/2]           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  [开始战斗] [重新开始] [暂停]                                         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 商店区域详细设计

```
┌─────────────────────────────────────────────────────────────────────┐
│                         卡牌商店 (Shop)                              │
├─────────────────────────────────────────────────────────────────────┤
│  玩家等级: [5]  ┃  刷新费用: 2金币  ┃  回合: 3/10                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │  ★★★   │  │  ★★    │  │  ★     │  │  ★★★  │  │  ★★   │    │
│  │ 图标   │  │ 图标   │  │ 图标   │  │ 图标  │  │ 图标  │    │
│  │ 战士   │  │ 法师   │  │ 射手   │  │ 刺客  │  │ 坦克  │    │
│  │ T5 神话│  │ T3 高级│  │ T2 精英│  │ T4 传 │  │ T3 高 │    │
│  │        │  │        │  │        │  │       │  │       │    │
│  │ 费用: 8│  │ 费用: 3│  │ 费用: 2│  │ 费用: 5│  │ 费用: 3│    │
│  │ [1/3]  │  │ [9/12] │  │ [12/15]│  │ [4/6] │  │ [7/9] │    │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │
│                                                                      │
│  卡牌1: 传说战士      卡牌2: 精英法师      卡牌3: 稀有射手              │
│  (悬停时显示详细描述和技能)                                            │
│                                                                      │
├─────────────────────────────────────────────────────────────────────┤
│              [刷新商店 (2金币)]    [上一页] [下一页]                  │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.3 卡牌视觉设计

#### 卡牌状态

| 状态 | 视觉效果 |
|------|----------|
| **普通** | 等级颜色边框 + 半透明背景 |
| **悬停** | 黄色高亮边框 + 卡牌放大 1.1x |
| **可购买** | 绿色边框 + 金币足够指示 |
| **不可购买** | 灰色边框 + 红色半透明遮罩 |
| **可升级** | 脉动绿色边框 + 升级箭头图标 |

#### 等级颜色系统

```
T1 (新手): 灰色 (#888888)
T2 (精英): 绿色 (#33AA33)
T3 (高级): 蓝色 (#3366CC)
T4 (传奇): 紫色 (#9933CC)
T5 (神话): 金色 (#CCAA00)
```

#### 卡牌元素层级

```
┌─────────────────────┐
│    [星级] [类型图标]│ ← 顶部信息栏
├─────────────────────┤
│                     │
│                     │
│    [卡牌图片/图标]   │ ← 中央视觉区域
│                     │
│                     │
├─────────────────────┤
│   卡牌名称 (等级)    │ ← 名称栏
├─────────────────────┤
│   [费用/数量]        │ ← 底部信息栏
│   [池数量 5/15]      │
└─────────────────────┘
```

---

## 3. 渲染模块架构设计

### 3.1 组件层次结构

```
CardShopUIManager (新建)
    │
    ├── CardShopPanel (核心商店面板)
    │   ├── CardListWidget (卡牌列表容器)
    │   │   └── CardWidget[] (单张卡牌组件)
    │   │       ├── CardVisualComponent (视觉渲染)
    │   │       ├── CardInfoLabel (文本信息)
    │   │       └── CardCountBadge (数量徽章)
    │   ├── ShopHeaderPanel (顶部信息栏)
    │   │   ├── PlayerLevelLabel
    │   │   ├── RefreshCostLabel
    │   │   └── RoundCountLabel
    │   └── ShopFooterPanel (底部操作栏)
    │       ├── RefreshButton
    │       ├── PrevPageButton
    │       └── NextPageButton
    │
    ├── CardDeckPanel (玩家卡组面板)
    │   ├── CardListWidget
    │   │   └── CardWidget[]
    │   └── DeckActionPanel
    │       ├── SellButton
    │       └── UpgradeButton
    │
    └── CardTooltipPanel (悬停提示面板)
        ├── CardDetailVisual
        ├── SkillDescriptionLabel
        └── StatsBreakdownLabel
```

### 3.2 核心类设计

#### CardShopUIManager

```java
/**
 * 卡牌商店UI管理器
 * 职责: 协调商店、卡组UI，处理交互事件
 */
public class CardShopUIManager implements GameEventListener, GameRenderer {
    private final Stage uiStage;
    private final Table rootTable;

    // 子组件
    private final CardShopPanel shopPanel;
    private final CardDeckPanel deckPanel;
    private final CardTooltipPanel tooltipPanel;

    // 数据引用
    private final CardShop cardShop;
    private final PlayerDeck playerDeck;
    private final PlayerEconomy playerEconomy;
    private final SharedCardPool sharedCardPool;

    // 业务逻辑回调
    private final ShopInteractionCallback callback;

    /**
     * 构造函数
     * @param game 游戏实例
     * @param cardShop 商店数据
     * @param playerDeck 玩家卡组
     * @param playerEconomy 经济系统
     * @param sharedCardPool 共享卡池
     * @param callback 业务逻辑回调
     */
    public CardShopUIManager(...);

    /**
     * 渲染方法 (GameRenderer接口)
     */
    @Override
    public void render(RenderHolder holder);

    /**
     * 更新UI数据
     */
    public void updateShopDisplay();
    public void updateDeckDisplay();
    public void updateEconomyDisplay();

    /**
     * 显示/隐藏商店
     */
    public void setVisible(boolean visible);
    public boolean isVisible();

    /**
     * 生命周期方法
     */
    public void onEnter();
    public void onExit();
    public void dispose();
}
```

#### CardShopPanel

```java
/**
 * 商店面板组件
 * 职责: 渲染商店卡牌列表，处理商店交互
 */
public class CardShopPanel extends Table {
    private final CardShop cardShop;
    private final PlayerEconomy playerEconomy;
    private final SharedCardPool sharedCardPool;

    private CardListWidget cardListWidget;
    private TextButton refreshButton;

    // 布局参数
    private float cardWidth = 100f;  // 相对单位
    private float cardHeight = 140f;
    private float cardSpacing = 8f;

    public CardShopPanel(...);

    /**
     * 构建UI布局
     */
    private void buildLayout();

    /**
     * 刷新卡牌列表显示
     */
    public void refreshCardList();

    /**
     * 处理刷新按钮点击
     */
    private void onRefreshClicked();

    /**
     * 处理卡牌点击
     */
    private void onCardClicked(Card card);

    /**
     * 处理卡牌悬停
     */
    private void onCardHovered(Card card);
}
```

#### CardWidget

```java
/**
 * 单张卡牌组件
 * 职责: 渲染单张卡牌，处理卡牌状态变化
 */
public class CardWidget extends Table {
    private final Card card;
    private final CardDisplayMode mode; // SHOP, DECK, PREVIEW

    // 视觉组件
    private final CardVisualComponent visualComponent;
    private final Label nameLabel;
    private final Label costLabel;
    private final Label countLabel;
    private final Label poolCountLabel;

    // 状态
    private boolean hovered = false;
    private boolean selected = false;
    private boolean affordable = true;
    private boolean upgradable = false;

    public CardWidget(Card card, CardDisplayMode mode, Skin skin);

    /**
     * 更新卡牌状态
     */
    public void updateState(boolean hovered, boolean selected,
                           boolean affordable, boolean upgradable);

    /**
     * 更新卡牌数据（用于升级后刷新）
     */
    public void updateCard(Card newCard);

    /**
     * 获取卡牌
     */
    public Card getCard();

    /**
     * 动画效果
     */
    public void playHoverAnimation(boolean enter);
    public void playPurchaseAnimation();
    public void playUpgradeAnimation();
}
```

#### CardVisualComponent (自定义 Actor)

```java
/**
 * 卡牌视觉组件
 * 职责: 使用 SpriteBatch 和 ShapeRenderer 渲染卡牌外观
 */
public class CardVisualComponent extends Actor {
    private final Card card;
    private final ShapeRenderer shapeRenderer;
    private final GlyphLayout glyphLayout;

    // 状态
    private boolean hovered;
    private boolean selected;
    private boolean affordable;
    private boolean upgradable;

    // 动画
    private float scale = 1f;
    private float targetScale = 1f;
    private float pulseTime = 0f;

    public CardVisualComponent(Card card, Skin skin);

    @Override
    public void draw(Batch batch, float parentAlpha);

    /**
     * 渲染卡牌背景
     */
    private void renderBackground(float x, float y, float width, float height);

    /**
     * 渲染卡牌边框
     */
    private void renderBorder(float x, float y, float width, float height);

    /**
     * 渲染等级图标
     */
    private void renderTierIcon(float x, float y);

    /**
     * 渲染类型图标
     */
    private void renderTypeIcon(float x, float y);

    /**
     * 渲染星级
     */
    private void renderStars(float x, float y);

    /**
     * 更新动画
     */
    @Override
    public void act(float delta);
}
```

#### CardTooltipPanel

```java
/**
 * 卡牌悬停提示面板
 * 职责: 显示卡牌详细信息、技能描述
 */
public class CardTooltipPanel extends Table {
    private final Label nameLabel;
    private final Label typeLabel;
    private final Label tierLabel;
    private final Label skillLabel;
    private final Label statsLabel;
    private final CardVisualComponent previewCard;

    private Card currentCard;
    private float showDelay = 0.3f;  // 延迟显示
    private float hoverTimer = 0f;

    public CardTooltipPanel(Skin skin);

    /**
     * 显示卡牌详情
     */
    public void showCard(Card card);

    /**
     * 隐藏面板
     */
    public void hide();

    /**
     * 更新显示计时器
     */
    @Override
    public void act(float delta);
}
```

### 3.3 交互事件流设计

#### 购买卡牌流程

```
用户点击商店卡牌
    ↓
CardWidget.onClick()
    ↓
CardShopPanel.onCardClicked(card)
    ↓
验证: EconomyManager.canAfford(card.getCost())
    ↓ (通过)
ShopInteractionCallback.onBuyCardRequested(card)
    ↓
CardManager.buyCard(card)
    ├─→ EconomyManager.spendGold(cost, "buy_card")
    ├─→ SharedCardPool.decrementCopies(cardId)
    ├─→ PlayerDeck.addCard(card)
    └─→ CardShop.buyCard(card)
    ↓
GameEventSystem.postEvent(CardBuyEvent)
    ↓
CardShopUIManager.updateShopDisplay()
CardShopUIManager.updateDeckDisplay()
    ↓
播放购买动画 (CardWidget.playPurchaseAnimation())
```

#### 刷新商店流程

```
用户点击刷新按钮
    ↓
RefreshButton.onClick()
    ↓
CardShopPanel.onRefreshClicked()
    ↓
验证: EconomyManager.canAfford(refreshCost)
    ↓ (通过)
ShopInteractionCallback.onRefreshRequested()
    ↓
EconomyManager.spendGold(refreshCost, "refresh_shop")
    ↓
CardManager.refreshShop()
    ├─→ CardPool.getRandomCardsByLevel(5, playerLevel)
    └─→ CardShop.refresh()
    ↓
GameEventSystem.postEvent(RefreshEvent)
    ↓
CardShopUIManager.updateShopDisplay()
    ↓
播放刷新动画 (卡片淡入淡出)
```

#### 出售卡牌流程

```
用户长按卡组卡牌 (或点击后点击出售按钮)
    ↓
CardWidget.onLongPress()
    ↓
CardDeckPanel.onSellRequested(card)
    ↓
ShopInteractionCallback.onSellRequested(card)
    ↓
CardManager.sellCard(card)
    ├─→ PlayerDeck.removeCard(card)
    ├─→ SharedCardPool.incrementCopies(cardId)
    └─→ EconomyManager.earnGold(sellValue, "sell_card")
    ↓
GameEventSystem.postEvent(CardSellEvent)
    ↓
CardShopUIManager.updateDeckDisplay()
CardShopUIManager.updateEconomyDisplay()
    ↓
播放出售动画 (卡牌缩小消失)
```

#### 升级卡牌流程

```
用户点击升级按钮
    ↓
CardDeckPanel.onUpgradeClicked()
    ↓
检查: CardManager.canUpgrade(card)
    ↓ (通过)
ShopInteractionCallback.onUpgradeRequested(card)
    ↓
CardManager.upgradeCard(card)
    ├─→ PlayerDeck.removeCardsByBaseId(baseCardId, 3)
    ├─→ 创建高星级卡牌
    └─→ PlayerDeck.addCard(upgradedCard)
    ↓
GameEventSystem.postEvent(CardUpgradeEvent)
    ↓
CardShopUIManager.updateDeckDisplay()
    ↓
播放升级动画 (三张卡牌合并特效)
```

### 3.4 回调接口设计

#### ShopInteractionCallback

```java
/**
 * 商店交互回调接口
 * 由 GameMode 或相关 Manager 实现
 */
public interface ShopInteractionCallback {
    /**
     * 请求购买卡牌
     * @return true if purchase successful
     */
    boolean onBuyCardRequested(Card card);

    /**
     * 请求刷新商店
     */
    void onRefreshRequested();

    /**
     * 请求出售卡牌
     */
    void onSellRequested(Card card);

    /**
     * 请求升级卡牌
     */
    void onUpgradeRequested(Card card);

    /**
     * 卡牌悬停（用于显示详情）
     */
    void onCardHovered(Card card);

    /**
     * 卡牌悬停结束
     */
    void onCardHoverEnded(Card card);
}
```

---

## 4. 布局系统设计

### 4.1 弹性布局参数

使用 LibGDX Table 实现自适应比例布局：

```java
// 主容器 - 使用百分比布局
rootTable.defaults().grow().pad(10f);

// 顶部栏 - 固定高度
headerTable.height(Value.percentHeight(0.08f, rootTable));

// 战场区域 - 比例高度
battlefieldTable.height(Value.percentHeight(0.45f, rootTable));

// 商店/卡组区域 - 比例高度
shopDeckTable.height(Value.percentHeight(0.35f, rootTable));

// 底部栏 - 固定高度
footerTable.height(Value.percentHeight(0.07f, rootTable));
```

### 4.2 卡牌列表布局

```java
// 商店卡牌列表 - 水平滚动
cardListWidget.defaults().size(100f, 140f).space(8f);
cardListWidget.row();

for (Card card : shopCards) {
    CardWidget cardWidget = new CardWidget(card, CardDisplayMode.SHOP, skin);
    cardListWidget.add(cardWidget);
}

// 自适应列数
int maxColumns = 5;
cardListWidget.getScrollPane().setScrollingDisabled(true, false);
```

### 4.3 响应式设计

```java
// 根据屏幕尺寸调整
float screenWidth = Gdx.graphics.getWidth();
float screenHeight = Gdx.graphics.getHeight();

// 手机: 竖向布局
if (screenWidth < 600) {
    shopDeckTable.row();
    shopTable.expandX().fillX();
    deckTable.expandX().fillX();
}
// 平板/桌面: 横向布局
else {
    shopTable.expandX().fillX();
    deckTable.expandX().fillX();
}
```

---

## 5. 动画系统设计

### 5.1 交互动画

| 动画类型 | 效果描述 | 持续时间 |
|---------|---------|---------|
| **悬停进入** | 卡牌放大 1.1x，边框高亮 | 150ms |
| **悬停退出** | 卡牌恢复原大小 | 100ms |
| **点击购买** | 卡牌飞向卡组区域 | 400ms |
| **刷新** | 旧卡牌淡出 → 新卡牌淡入 | 300ms |
| **升级** | 三张卡牌旋转合并 → 新卡牌弹出 | 600ms |
| **出售** | 卡牌缩小消失 + 金币飞向经济栏 | 350ms |

### 5.2 动画实现

```java
/**
 * 使用 LibGDX Actions 实现动画
 */
public class CardWidget extends Table {
    /**
     * 悬停动画
     */
    public void playHoverAnimation(boolean enter) {
        float targetScale = enter ? 1.1f : 1f;
        float duration = enter ? 0.15f : 0.1f;

        this.addAction(Actions.sequence(
            Actions.scaleTo(targetScale, targetScale, duration, Interpolation.fade),
            Actions.run(() -> hovered = enter)
        ));
    }

    /**
     * 购买动画
     */
    public void playPurchaseAnimation(Vector2 targetPosition) {
        this.addAction(Actions.sequence(
            // 1. 缩小
            Actions.scaleTo(0.5f, 0.5f, 0.1f),
            // 2. 移动到目标位置
            Actions.moveToAligned(targetPosition.x, targetPosition.y,
                Align.center, 0.3f, Interpolation.circleOut),
            // 3. 完全消失
            Actions.alpha(0f, 0.05f),
            Actions.run(() -> this.remove())
        ));
    }

    /**
     * 脉动效果（可升级状态）
     */
    @Override
    public void act(float delta) {
        super.act(delta);

        if (upgradable) {
            pulseTime += delta * 3f;
            float pulseAlpha = 0.3f + 0.2f * (float) Math.sin(pulseTime);
            visualComponent.setPulseAlpha(pulseAlpha);
        }
    }
}
```

---

## 6. 国际化设计

### 6.1 新增国际化 Key

需要在 `assets/i18n/i18n_zh.properties` 和 `i18n_en.properties` 中添加：

```properties
# 商店面板
shop.panel.title = 卡牌商店
shop.panel.player_level = 等级: {0}
shop.panel.refresh_cost = 刷新费用: {0}金币
shop.panel.round = 回合: {0}/{1}
shop.panel.empty = 商店暂无卡牌

# 商店操作
shop.action.refresh = 刷新 ({0}金币)
shop.action.cannot_afford = 金币不足
shop.action.purchase_success = 购买成功: {0}
shop.action.purchase_failed = 购买失败: {0}
shop.action.refresh_success = 商店已刷新
shop.action.sell_success = 出售成功: {0} (+{1}金币)
shop.action.upgrade_success = 升级成功: {0} → {1}

# 卡牌信息
card.tooltip.stats = 属性: 攻击 {0} / 防御 {1} / 生命 {2}
card.tooltip.skill = 技能: {0}
card.tooltip.skill_value = 技能值: {0}
card.tooltip.skill_range = 技能范围: {0}
card.tooltip.synergies = 羁绊: {0}

# 池数量
card.pool_count = 剩余: {0}/{1}
card.pool_empty = 已耗尽
card.count_suffix = x{0}
```

### 6.2 使用方式

```java
// 在代码中使用 I18N
String titleText = I18N.get("shop.panel.title");
String levelText = I18N.format("shop.panel.player_level", playerLevel);
String refreshText = I18N.format("shop.action.refresh", refreshCost);
```

---

## 7. 与现有架构集成

### 7.1 GameMode 集成

```java
/**
 * AutoChessGameMode 集成
 */
public class AutoChessGameMode implements GameMode {
    private CardShopUIManager cardShopUIManager;

    @Override
    public void onEnter() {
        // 初始化商店UI管理器
        cardShopUIManager = new CardShopUIManager(
            game,
            cardManager.getCardShop(),
            cardManager.getPlayerDeck(),
            economyManager.getPlayerEconomy(),
            cardManager.getSharedCardPool(),
            new ShopInteractionCallback() {
                @Override
                public boolean onBuyCardRequested(Card card) {
                    return economyManager.buyCard(card.getCost()) &&
                           cardManager.buyCard(card);
                }

                @Override
                public void onRefreshRequested() {
                    if (economyManager.payForRefresh(cardManager.getRefreshCost())) {
                        cardManager.refreshShop();
                    }
                }

                @Override
                public void onSellRequested(Card card) {
                    cardManager.sellCard(card, calculateSellValue(card));
                }

                @Override
                public void onUpgradeRequested(Card card) {
                    if (cardManager.canUpgrade(card) &&
                        economyManager.payForUpgrade(getUpgradeCost())) {
                        cardManager.upgradeCard(card);
                    }
                }

                @Override
                public void onCardHovered(Card card) {
                    // 显示详情面板
                    cardShopUIManager.showTooltip(card);
                }

                @Override
                public void onCardHoverEnded(Card card) {
                    cardShopUIManager.hideTooltip();
                }
            }
        );

        // 注册到渲染协调器
        renderCoordinator.addRenderer(cardShopUIManager);

        // 注册事件监听
        cardShopUIManager.onEnter();
    }

    @Override
    public void render(RenderHolder holder) {
        renderCoordinator.renderAll();
    }
}
```

### 7.2 事件系统集成

```java
/**
 * CardShopUIManager 监听相关事件
 */
@Override
public void onGameEvent(GameEvent event) {
    if (event instanceof CardBuyEvent) {
        CardBuyEvent buyEvent = (CardBuyEvent) event;
        // 播放购买动画
        CardWidget widget = findCardWidget(buyEvent.card);
        if (widget != null) {
            widget.playPurchaseAnimation(getDeckPosition());
        }
    }
    else if (event instanceof RefreshEvent) {
        // 播放刷新动画
        playRefreshAnimation();
        updateShopDisplay();
    }
    else if (event instanceof CardUpgradeEvent) {
        CardUpgradeEvent upgradeEvent = (CardUpgradeEvent) event;
        // 播放升级动画
        playUpgradeAnimation(upgradeEvent.oldCard, upgradeEvent.newCard);
        updateDeckDisplay();
    }
    else if (event instanceof GoldEarnEvent || event instanceof GoldSpendEvent) {
        // 更新金币显示
        updateEconomyDisplay();
    }
}
```

---

## 8. 文件结构

### 8.1 新增文件

```
core/src/main/java/com/voidvvv/autochess/
├── ui/
│   └── shop/
│       ├── CardShopUIManager.java           # 商店UI管理器
│       ├── panel/
│       │   ├── CardShopPanel.java           # 商店面板
│       │   ├── CardDeckPanel.java           # 卡组面板
│       │   └── CardTooltipPanel.java        # 悬停提示面板
│       ├── widget/
│       │   ├── CardListWidget.java          # 卡牌列表容器
│       │   ├── CardWidget.java              # 单张卡牌组件
│       │   └── CardVisualComponent.java     # 卡牌视觉渲染
│       └── CardDisplayMode.java             # 卡牌显示模式枚举
└── event/
    └── shop/
        └── CardShopEvent.java               # 商店相关事件
```

### 8.2 修改文件

```
# 修改以集成新的商店UI
game/AutoChessGameMode.java                   # 添加 CardShopUIManager

# 可选：更新国际化文件
assets/i18n/i18n_zh.properties               # 添加商店相关key
assets/i18n/i18n_en.properties
```

---

## 9. 实现优先级

### Phase 1: 基础结构 (必须)
1. 创建 `CardShopUIManager` 类框架
2. 创建 `CardShopPanel` 和 `CardDeckPanel` 基础布局
3. 创建 `CardWidget` 基础组件
4. 实现基本的事件回调机制

### Phase 2: 核心功能 (必须)
1. 实现卡牌渲染 (CardVisualComponent)
2. 实现购买/刷新功能
3. 集成 EconomyManager 和 CardManager
4. 添加基础动画效果

### Phase 3: 增强功能 (可选)
1. 实现悬停提示面板
2. 添加升级功能
3. 添加出售功能
4. 完善动画系统

### Phase 4: 优化 (可选)
1. 性能优化 (对象池、缓存)
2. 响应式布局完善
3. 可访问性支持
4. 单元测试

---

## 10. 测试计划

### 10.1 单元测试

```java
// CardWidget 测试
@Test
void testCardWidgetInitialState() {
    Card card = createTestCard();
    CardWidget widget = new CardWidget(card, CardDisplayMode.SHOP, skin);
    assertEquals(card, widget.getCard());
    assertFalse(widget.isSelected());
}

// CardShopPanel 测试
@Test
void testRefreshButtonCallback() {
    boolean[] callbackCalled = {false};
    CardShopPanel panel = new CardShopPanel(..., () -> callbackCalled[0] = true);
    panel.findActor("refreshButton").simulateClick();
    assertTrue(callbackCalled[0]);
}
```

### 10.2 集成测试

1. **购买流程测试**: 验证购买后金币扣除、卡牌添加、事件分发
2. **刷新流程测试**: 验证刷新后卡牌更新、金币扣除
3. **升级流程测试**: 验证三张卡牌合并、星级提升

### 10.3 UI测试

1. **布局测试**: 不同屏幕尺寸下的布局正确性
2. **交互测试**: 点击、悬停、拖拽响应
3. **动画测试**: 各种动画的流畅性

---

## 11. 参考资料

### 11.1 现有代码文件

| 文件 | 路径 | 用途 |
|------|------|------|
| CardShopRenderer | `render/scene2d/CardShopRenderer.java` | 参考现有Scene2D实现 |
| CardRenderer | `ui/CardRenderer.java` | 参考卡牌渲染逻辑 |
| GameUIManager | `ui/GameUIManager.java` | 参考UI管理模式 |
| CardManager | `manage/CardManager.java` | 业务逻辑接口 |
| EconomyManager | `manage/EconomyManager.java` | 金钱系统接口 |

### 11.2 LibGDX 文档

- [Scene2D](https://libgdx.com/wiki/extensions/scene2d/scene2d)
- [Table Layout](https://libgdx.com/wiki/extensions/scene2d/table)
- [Actor Actions](https://libgdx.com/wiki/actions/scene2d-actions)

---

## 附录：UI视觉规范

### A.1 颜色规范

```java
// 等级颜色
public static final Color TIER_COLORS[] = {
    new Color(0.5f, 0.5f, 0.5f, 1f),  // T1: 灰色
    new Color(0.2f, 0.7f, 0.2f, 1f),  // T2: 绿色
    new Color(0.2f, 0.4f, 0.8f, 1f),  // T3: 蓝色
    new Color(0.6f, 0.2f, 0.8f, 1f),  // T4: 紫色
    new Color(0.8f, 0.6f, 0.1f, 1f),  // T5: 金色
};

// 状态颜色
public static final Color HOVER_COLOR = new Color(1f, 0.9f, 0.3f, 1f);     // 悬停黄
public static final Color SELECTED_COLOR = new Color(0.3f, 0.9f, 0.3f, 1f); // 选中绿
public static final Color DISABLED_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f); // 禁用灰
public static final Color ERROR_COLOR = new Color(0.9f, 0.2f, 0.2f, 1f);   // 错误红
```

### A.2 尺寸规范

```java
// 基础尺寸（相对单位，基于屏幕高度）
public static final float CARD_WIDTH_RATIO = 0.12f;   // 12% 屏幕宽度
public static final float CARD_HEIGHT_RATIO = 0.18f;  // 18% 屏幕高度
public static final float CARD_SPACING_RATIO = 0.01f; // 1% 屏幕宽度

// 字体大小
public static final float TITLE_FONT_SIZE = 24f;
public static final float BODY_FONT_SIZE = 16f;
public static final float CAPTION_FONT_SIZE = 12f;
```

### A.3 动画时长

```java
public static final float HOVER_DURATION = 0.15f;      // 悬停动画
public static final float CLICK_DURATION = 0.1f;       // 点击反馈
public static final float PURCHASE_DURATION = 0.4f;    // 购买动画
public static final float REFRESH_DURATION = 0.3f;     // 刷新动画
public static final float UPGRADE_DURATION = 0.6f;     // 升级动画
```

---

**文档版本**: 1.0
**最后更新**: 2026-03-27
