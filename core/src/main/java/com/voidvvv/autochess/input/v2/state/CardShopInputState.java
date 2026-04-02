package com.voidvvv.autochess.input.v2.state;

/**
 * CardShopInputState - 商店输入状态
 *
 * 纯粹的数据类，持有输入相关的状态
 * 无业务逻辑，便于测试和复用
 */
public class CardShopInputState {

    /** 鼠标 X 坐标（UI 坐标系） */
    public float mouseX = 0;

    /** 鼠标 Y 坐标（UI 坐标系） */
    public float mouseY = 0;

    /** 左键是否按下 */
    public boolean isLeftButtonPressed = false;

    /** 是否在商店区域内按下 */
    public boolean wasPressingInShop = false;

    /** 当前悬停的卡牌索引 */
    public int hoveredCardIndex = -1;

    /** 是否悬停在刷新按钮上 */
    public boolean isHoveringRefreshButton = false;

    /** 是否悬停在商店区域内 */
    public boolean isHoveringShop = false;

    /**
     * 重置状态
     */
    public void reset() {
        mouseX = 0;
        mouseY = 0;
        isLeftButtonPressed = false;
        wasPressingInShop = false;
        hoveredCardIndex = -1;
        isHoveringRefreshButton = false;
        isHoveringShop = false;
    }
}
