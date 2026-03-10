package com.voidvvv.autochess.sm.state;

/**
 * 状态类型枚举
 * 用于区分普通状态和异常状态，影响状态转换和优先级规则
 */
public enum StateType {
    /** 正常状态（站立、行走、攻击等） */
    NORMAL,

    /** 异常状态（眩晕、冻结、沉默等） */
    EXCEPTION
}
