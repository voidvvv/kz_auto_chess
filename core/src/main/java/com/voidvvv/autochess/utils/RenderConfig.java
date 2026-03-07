package com.voidvvv.autochess.utils;

/**
 * 全局渲染配置类
 * 用于控制渲染模式的切换
 */
public class RenderConfig {

    /**
     * 是否使用Tiled渲染模式
     * false = 使用几何渲染
     * true = 使用Tiled纹理渲染
     */
    public static boolean USE_TILED_RENDERING = false;

    /**
     * 切换渲染模式
     */
    public static void toggleRendering() {
        USE_TILED_RENDERING = !USE_TILED_RENDERING;
    }
}
