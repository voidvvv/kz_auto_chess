package com.voidvvv.autochess.updater;

/**
 * 通用更新器接口
 * 定义所有更新组件的生命周期契约
 */
public interface IUpdater<T> {

    /**
     * 更新目标对象
     * @param target 目标对象
     * @param delta 时间增量（秒）
     */
    void update(T target, float delta);

    /**
     * 初始化更新器
     * @param target 目标对象
     */
    void initialize(T target);

    /**
     * 释放更新器资源
     */
    void dispose();

    /**
     * 检查更新器是否处于激活状态
     * @return 如果激活则返回true
     */
    boolean isActive();

    /**
     * 激活更新器
     */
    default void turnOn() {
        // 默认实现，子类可以覆盖
    }

    /**
     * 停用更新器
     */
    default void turnOff() {
        // 默认实现，子类可以覆盖
    }
}
