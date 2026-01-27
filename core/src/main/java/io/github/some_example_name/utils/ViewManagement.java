package io.github.some_example_name.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Viewport管理类
 * 管理UI viewport和游戏世界viewport
 */
public class ViewManagement {
    // 游戏世界大小（战场和角色使用）
    public Vector2 worldSize = new Vector2(480f, 600f);
    
    // UI viewport（用于UI元素：商店、卡组、按钮等）
    private Viewport uiViewport;
    private Camera uiCamera;
    
    // 游戏世界viewport（用于战场和角色）
    private Viewport gameViewport;
    private Camera worldCamera;

    public ViewManagement() {
    }

    public void create() {
        // 创建UI viewport（使用屏幕坐标）
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        
        // 创建游戏世界viewport（用于战场和角色）
        OrthographicCamera localCamera = new OrthographicCamera(worldSize.x, worldSize.y);
        localCamera.setToOrtho(false); // Y轴向上
        worldCamera = localCamera;
        this.gameViewport = new StretchViewport(worldSize.x, worldSize.y, localCamera);
    }
    
    /**
     * 更新viewport（在resize时调用）
     */
    public void update(int width, int height) {
        uiViewport.update(width, height, true);
        gameViewport.update(width, height, true);
    }
    
    /**
     * 将屏幕坐标转换为UI坐标
     */
    public Vector2 screenToUI(int screenX, int screenY) {
        Vector3 unprojected = uiViewport.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2(unprojected.x, unprojected.y);
    }
    
    /**
     * 将屏幕坐标转换为游戏世界坐标
     */
    public Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 unprojected = gameViewport.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2(unprojected.x, unprojected.y);
    }

    public Vector2 getWorldSize() {
        return worldSize;
    }

    /**
     * 获取游戏世界viewport（用于战场和角色）
     */
    public Viewport getGameViewport() {
        return gameViewport;
    }
    
    /**
     * 获取UI viewport（用于UI元素）
     */
    public Viewport getUIViewport() {
        return uiViewport;
    }
    
    /**
     * 获取游戏世界camera
     */
    public Camera getWorldCamera() {
        return worldCamera;
    }
    
    /**
     * 获取游戏世界camera（OrthographicCamera类型）
     */
    public OrthographicCamera getWorldCameraOrtho() {
        return (OrthographicCamera) worldCamera;
    }
    
    /**
     * 获取UI camera
     */
    public Camera getUICamera() {
        return uiCamera;
    }
    
    /**
     * 兼容旧代码
     */
    @Deprecated
    public Viewport getGameView() {
        return gameViewport;
    }
}

