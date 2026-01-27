package io.github.some_example_name.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * 相机控制器
 * 控制游戏世界camera的移动、缩放和复位
 */
public class CameraController {
    private OrthographicCamera camera;
    
    // 移动速度
    private float moveSpeed = 200f;
    
    // 缩放相关
    private float minZoom = 0.5f;
    private float maxZoom = 3.0f;
    private float zoomSpeed = 0.1f;
    
    // 初始状态（用于复位）
    private Vector3 initialPosition = new Vector3();
    private float initialZoom = 1.0f;
    
    // 滚轮缩放增量（由外部设置）
    private float scrollDelta = 0f;
    
    public CameraController(OrthographicCamera camera, float initialViewportWidth, float initialViewportHeight) {
        this.camera = camera;
        
        // 保存初始状态
        initialPosition.set(camera.position);
        initialZoom = camera.zoom;
    }
    
    /**
     * 更新相机控制器（每帧调用）
     */
    public void update(float deltaTime) {
        // 处理键盘输入移动相机
        handleMovement(deltaTime);
        
        // 处理鼠标滚轮缩放
        handleZoom();
        
        // 处理复位
        handleReset();
        
        // 更新相机
        camera.update();
        
        // 重置滚轮增量
        scrollDelta = 0f;
    }
    
    /**
     * 处理相机移动（WASD或方向键）
     */
    private void handleMovement(float deltaTime) {
        float moveX = 0;
        float moveY = 0;
        
        // WASD移动
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += moveSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= moveSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= moveSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += moveSpeed * deltaTime;
        }
        
        // 应用移动
        if (moveX != 0 || moveY != 0) {
            camera.translate(moveX, moveY, 0);
        }
    }
    
    /**
     * 处理鼠标滚轮缩放
     */
    private void handleZoom() {
        if (scrollDelta != 0) {
            float oldZoom = camera.zoom;
            float newZoom = oldZoom + scrollDelta * zoomSpeed;
            
            // 限制缩放范围
            newZoom = Math.max(minZoom, Math.min(maxZoom, newZoom));
            
            // 如果缩放没有变化，直接返回
            if (newZoom == oldZoom) {
                return;
            }
            
            // 获取鼠标屏幕坐标
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.input.getY();
            
            // 将鼠标屏幕坐标转换为世界坐标（缩放前）
            Vector3 mouseWorldPos = new Vector3(mouseX, mouseY, 0);
            camera.unproject(mouseWorldPos);
            
            // 设置新缩放
            camera.zoom = newZoom;
            camera.update();
            
            // 将鼠标屏幕坐标转换为世界坐标（缩放后）
            Vector3 mouseWorldPosAfter = new Vector3(mouseX, mouseY, 0);
            camera.unproject(mouseWorldPosAfter);
            
            // 调整相机位置，使鼠标指向的世界位置保持不变
            camera.position.x += mouseWorldPos.x - mouseWorldPosAfter.x;
            camera.position.y += mouseWorldPos.y - mouseWorldPosAfter.y;
        }
    }
    
    /**
     * 设置滚轮缩放增量（由外部调用）
     * @param amount 滚轮滚动量（正数向上，负数向下）
     */
    public void setScrollDelta(float amount) {
        this.scrollDelta = amount;
    }
    
    /**
     * 处理复位（R键）
     */
    private void handleReset() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reset();
        }
    }
    
    /**
     * 复位相机到初始状态
     */
    public void reset() {
        camera.position.set(initialPosition);
        camera.zoom = initialZoom;
        camera.update();
    }
    
    /**
     * 设置移动速度
     */
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
    
    /**
     * 设置缩放范围
     */
    public void setZoomRange(float min, float max) {
        this.minZoom = min;
        this.maxZoom = max;
    }
    
    /**
     * 设置缩放速度
     */
    public void setZoomSpeed(float speed) {
        this.zoomSpeed = speed;
    }
    
    /**
     * 获取当前相机
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
}

