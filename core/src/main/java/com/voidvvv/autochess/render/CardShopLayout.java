package com.voidvvv.autochess.render;

/**
 * CardShopLayout - 商店布局参数类
 *
 * 持有所有布局相关的参数，避免使用静态变量
 * 支持可配置的布局，便于动态调整
 */
public class CardShopLayout {

    // ========== 商店区域 ==========
    /** 商店区域 X 坐标 */
    public float shopX = 50f;
    /** 商店区域 Y 坐标 */
    public float shopY = 50f;
    /** 商店区域宽度 */
    public float shopWidth = 700f;
    /** 商店区域高度 */
    public float shopHeight = 280f;

    // ========== 卡牌布局 ==========
    /** 单张卡牌宽度 */
    public float cardWidth = 120f;
    /** 单张卡牌高度 */
    public float cardHeight = 160f;
    /** 卡牌间距 */
    public float cardSpacing = 10f;
    /** 卡牌起始 X 坐标 */
    public float cardStartX = 70f;
    /** 卡牌起始 Y 坐标 */
    public float cardStartY = 80f;

    // ========== 刷新按钮 ==========
    /** 刷新按钮 X 坐标 */
    public float refreshButtonX = 50f;
    /** 刷新按钮 Y 坐标 */
    public float refreshButtonY = 340f;
    /** 刷新按钮宽度 */
    public float refreshButtonWidth = 140f;
    /** 刷新按钮高度 */
    public float refreshButtonHeight = 35f;

    // ========== 标题偏移 ==========
    /** 标题距离顶部的偏移量 */
    public float titleOffsetFromTop = 15f;

    /**
     * 默认构造函数
     */
    public CardShopLayout() {}

    /**
     * 复制构造函数
     * @param other 另一个布局对象
     */
    public CardShopLayout(CardShopLayout other) {
        this.shopX = other.shopX;
        this.shopY = other.shopY;
        this.shopWidth = other.shopWidth;
        this.shopHeight = other.shopHeight;

        this.cardWidth = other.cardWidth;
        this.cardHeight = other.cardHeight;
        this.cardSpacing = other.cardSpacing;
        this.cardStartX = other.cardStartX;
        this.cardStartY = other.cardStartY;

        this.refreshButtonX = other.refreshButtonX;
        this.refreshButtonY = other.refreshButtonY;
        this.refreshButtonWidth = other.refreshButtonWidth;
        this.refreshButtonHeight = other.refreshButtonHeight;

        this.titleOffsetFromTop = other.titleOffsetFromTop;
    }

    /**
     * 创建默认布局
     * @return 默认布局对象
     */
    public static CardShopLayout createDefault() {
        return new CardShopLayout();
    }
}
