---
title: feat: State Machine Enhancement with Validation and Exception Priority
type: feat
status: active
date: 2026-03-10
origin: docs/brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md
---

# State Machine Enhancement with Validation and Exception Priority

## Overview

增强游戏的状态机系统，支持状态转换验证（拒绝非法转换）和异常状态优先级（眩晕、冻结、沉默等覆盖普通状态）。增强在现有 `BaseStateMachine` 和 `BaseState` 架构之上，同时保持与行为树集成的向后兼容性。

## 问题陈述

### 当前状态机问题

当前状态机实现（`sm/` 包）存在以下限制：

1. **无转换验证** - 状态切换没有任何守卫检查
2. **无异常状态支持** - 无法实现覆盖正常行为的debuff如眩晕/冻结
3. **分散的验证逻辑** - 条件检查与状态切换分离（`BattleUnitBlackboard:88-91` 中的反模式）
4. **无优先级系统** - 所有状态被同等对待
5. **无反馈机制** - 失败的转换被静默忽略
6. **无渴望状态队列** - 无法实现"攻击完成后自动切换到站立"的流程

### 当前反模式示例

```java
// BattleUnitBlackboard.java:88-91 - 条件检查在 switchState() 排队之后
if (this.stateMachine.getCurrent().isState(AttackState.INSTANCE)) return;
if (this.self.attackCooldown > 0) return;
this.stateMachine.switchState(States.ATTACK_STATE);
```

检查发生在错误的时间和错误的位置。

## 提出的解决方案

### 核心需求

1. **状态转换验证**：守卫在执行前拒绝无效转换
2. **异常状态优先级**：异常状态（优先级100）覆盖正常状态（优先级0）
3. **异常状态队列**：多个异常效果可以链式触发（眩晕→冻结→沉默→正常）
4. **转换反馈**：拒绝的转换被记录用于调试
5. **渴望状态队列**：状态完成后自动切换到队列中的下一个状态

### 架构决策：增强现有系统

基于 `BaseStateMachine` 构建而非替换：
- 将验证和优先级添加到现有的 `BaseState<T>` 接口
- 将优先级检查添加到 `BaseStateMachine.switchState()`
- 引入 `StateType` 枚举用于分类
- 创建异常状态基类以便复用
- 添加渴望状态队列到状态机

## 技术方案

### 第一阶段：核心接口增强

#### 1.1 添加 StateType 枚举

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/StateType.java`

```java
public enum StateType {
    NORMAL,      // 正常状态（站立、行走、攻击）
    EXCEPTION     // 异常状态（眩晕、冻结、沉默等）
}
```

#### 1.2 增强 BaseState 接口

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/BaseState.java`

```java
public interface BaseState<T> extends State<T> {
    // 现有方法...
    void update(T entity, float delta);
    boolean isState(BaseState<T> other);
    String name();

    // 新增：状态分类
    StateType getStateType();

    // 新增：优先级（数值越高优先级越高）
    default int getPriority() {
        return 0;  // 普通状态的默认值
    }

    // 新增：阻塞行为
    default boolean blocksTransitions() {
        return false;
    }
}
```

#### 1.3 创建 ExceptionState 基类

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/ExceptionState.java`

```java
public abstract class ExceptionState<T> implements BaseState<T> {

    @Override
    public StateType getStateType() {
        return StateType.EXCEPTION;
    }

    @Override
    public int getPriority() {
        return 100;  // 异常状态具有最高优先级
    }

    @Override
    public boolean blocksTransitions() {
        return true;  // 异常状态阻塞所有普通转换
    }
}
```

### 第二阶段：渴望状态队列设计

#### 2.1 增强后的状态机架构

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/machine/BaseStateMachine.java`

```java
public class BaseStateMachine<T> implements Telegraph, StateMachine<T> {
    private T own;
    private final int stackSize = 20;
    private final Deque<BaseState<T>> stateStack = new ArrayDeque<>();
    private BaseState<T> current;

    // 新增：渴望状态队列（普通状态）
    private final Deque<BaseState<T>> desiredStateQueue = new ArrayDeque<>();

    // 新增：当前异常状态（单独维护，不放入队列）
    private BaseState<T> currentExceptionState;

    // 新增：异常状态队列（BattleUnitBlackboard中维护）
    // 这里不重复存储，只引用

    private BaseState<T> next;

    public void update(float delta) {
        switchToNextState();
        updateCurrentState(delta);
    }

    /**
     * 添加渴望状态到队列末尾
     * 例如：行为树想移动 → addDesiredState(MoveState)
     */
    public void addDesiredState(BaseState<T> state) {
        // 异常状态激活时，忽略普通状态请求
        if (currentExceptionState != null && state.getStateType() == StateType.NORMAL) {
            return;  // 被异常状态阻塞
        }

        // 避免重复添加相同状态
        if (!desiredStateQueue.isEmpty() && desiredStateQueue.peekLast() == state) {
            return;
        }

        desiredStateQueue.addLast(state);

        // 如果队列只有一个状态（即当前没有渴望状态），立即尝试切换
        if (desiredStateQueue.size() == 1) {
            processDesiredState();
        }
    }

    /**
     * 请求切换到异常状态
     * 异常状态总是优先于普通状态
     */
    public void switchToException(BaseState<T> exceptionState) {
        if (exceptionState == null || exceptionState.getStateType() != StateType.EXCEPTION) {
            return;
        }

        // 清空渴望状态队列（异常状态优先）
        desiredStateQueue.clear();

        // 保存当前普通状态到历史栈（如果是普通状态）
        if (current != null && current.getStateType() == StateType.NORMAL) {
            stateStack.push(current);
        }

        // 切换到异常状态
        this.next = exceptionState;
        this.currentExceptionState = exceptionState;
    }

    /**
     * 异常状态结束，恢复普通状态
     * 由 BattleUnitBlackboard 在异常过期时调用
     */
    public void clearCurrentException() {
        if (currentExceptionState == null) {
            return;
        }

        currentExceptionState = null;

        // 尝试切换回历史栈中的状态
        if (!stateStack.isEmpty()) {
            this.next = stateStack.pop();
        } else {
            // 默认切换到站立状态
            this.next = States.NORMAL_STATE;
        }
    }

    /**
     * 处理队列中的下一个渴望状态
     */
    private void processDesiredState() {
        if (currentExceptionState != null) {
            // 异常状态激活时，不处理普通状态
            return;
        }

        if (desiredStateQueue.isEmpty()) {
            return;
        }

        BaseState<T> desired = desiredStateQueue.peekFirst();
        BaseState<T> actualNext = desired;

        // 普通状态之间的转换需要守卫验证
        if (current != null && desired != current) {
            // 这里可以添加状态守卫验证
            // if (!canTransition(current, desired)) {
            //     desiredStateQueue.pollFirst();  // 移除无效请求
            //     logRejectedTransition(current, desired);
            //     processDesiredState();  // 处理下一个
            //     return;
            // }
        }

        this.next = actualNext;
    }

    /**
     * 请求下一个渴望状态
     * 由状态在完成后调用（例如 AttackState 在攻击完成后调用）
     */
    public void requestNextDesiredState() {
        // 移除当前已完成的渴望状态
        if (!desiredStateQueue.isEmpty()) {
            desiredStateQueue.pollFirst();
        }

        // 处理下一个渴望状态
        processDesiredState();
    }

    public void switchState(BaseState<T> next) {
        if (next == null) return;

        // 异常状态有特殊通道
        if (next.getStateType() == StateType.EXCEPTION) {
            switchToException(next);
            return;
        }

        // 普通状态使用渴望状态队列
        addDesiredState(next);
    }

    private void switchToNextState() {
        if (next != null) {
            if (current != null) {
                // 只保存普通状态到历史栈
                if (current.getStateType() == StateType.NORMAL) {
                    stateStack.push(current);
                }
                BaseState<T> last = current;
                last.exit(own);
            }
            while (stateStack.size() >= stackSize) {
                stateStack.poll();
            }

            current = next;
            current.enter(own);
            next = null;
        }
    }

    private void updateCurrentState(float delta) {
        if (current != null) {
            current.update(own, delta);
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        // 可以处理状态控制消息
        return false;
    }

    @Override
    public BaseState<T> getCurrent() {
        return this.current;
    }

    @Override
    public void setOwn(T own) {
        this.own = own;
    }

    // 新增：获取当前异常状态
    public BaseState<T> getCurrentException() {
        return currentExceptionState;
    }

    // 新增：检查是否有异常状态激活
    public boolean hasActiveException() {
        return currentExceptionState != null;
    }

    // 新增：获取渴望状态队列大小（调试用）
    public int getDesiredQueueSize() {
        return desiredStateQueue.size();
    }
}
```

### 第三阶段：BattleUnitBlackboard 异常管理

#### 3.1 添加异常队列和计时器

**文件**: `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java`

```java
public class BattleUnitBlackboard implements Telegraph {
    // 现有字段...
    public StateMachine<BattleUnitBlackboard> stateMachine;

    // 新增：异常状态队列（用于多个异常效果链）
    private final Deque<ExceptionState<BattleUnitBlackboard>> exceptionQueue = new ArrayDeque<>();
    private final Map<String, Float> exceptionTimers = new HashMap<>();

    /**
     * 添加异常状态及持续时间
     */
    public void addExceptionState(ExceptionState<BattleUnitBlackboard> state, float durationSeconds) {
        exceptionQueue.addLast(state);
        exceptionTimers.put(state.name(), durationSeconds);

        // 如果这是第一个异常，立即切换到它
        if (exceptionQueue.size() == 1) {
            stateMachine.switchToException(state);
        }
    }

    /**
     * 获取下一个异常状态（当前异常结束后调用）
     */
    private ExceptionState<BattleUnitBlackboard> getNextExceptionState() {
        if (exceptionQueue.isEmpty()) return null;

        // 移除当前异常
        exceptionQueue.pollLast();

        // 返回下一个异常（如果有）
        return exceptionQueue.peekLast();
    }

    /**
     * 清除所有异常状态（战斗重置、驱散等）
     */
    public void clearExceptionStates() {
        exceptionQueue.clear();
        exceptionTimers.clear();

        // 强制返回正常状态
        if (stateMachine.hasActiveException()) {
            stateMachine.clearCurrentException();
        }
    }

    @Override
    public void update(float delta) {
        // 现有状态机更新
        this.stateMachine.update(delta);

        // 新增：更新异常计时器
        updateExceptionTimers(delta);

        // 现有冷却更新...
    }

    private void updateExceptionTimers(float delta) {
        if (exceptionQueue.isEmpty()) return;

        ExceptionState<BattleUnitBlackboard> current = exceptionQueue.peekLast();
        String stateName = current.name();
        Float remaining = exceptionTimers.get(stateName);

        if (remaining != null) {
            remaining -= delta;
            exceptionTimers.put(stateName, remaining);

            // 计时器过期 - 退出状态
            if (remaining <= 0) {
                onExceptionExpired(current);
            }
        }
    }

    private void onExceptionExpired(ExceptionState<BattleUnitBlackboard> expiredState) {
        // 移除计时器
        exceptionTimers.remove(expiredState.name());

        // 获取下一个异常或返回正常
        ExceptionState<BattleUnitBlackboard> next = getNextExceptionState();
        if (next != null) {
            // 切换到下一个异常
            stateMachine.switchToException(next);
        } else {
            // 没有更多异常，返回正常状态
            stateMachine.clearCurrentException();
        }
    }
}
```

### 第四阶段：具体异常状态

#### 4.1 创建 StunState

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/common/StunState.java`

```java
public class StunState extends ExceptionState<BattleUnitBlackboard> {
    public static final StunState INSTANCE = new StunState();
    private StunState() {}

    @Override
    public String name() {
        return "stun";
    }

    @Override
    public void enter(BattleUnitBlackboard entity) {
        entity.getSelf().currentTime = 0f;
        entity.getSelf().canMove = false;
        entity.getSelf().canAttack = false;
        // 眩晕不做任何事情 - 只是等待计时器过期
    }

    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time += delta;
        entity.getSelf().currentTime += delta;
        // 眩晕期间，所有行为被禁用
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        entity.getSelf().lastStateTime = entity.getSelf().currentTime;
        entity.getSelf().canMove = true;
        entity.getSelf().canAttack = true;
    }

    @Override
    public boolean isState(BaseState<BattleUnitBlackboard> other) {
        return other == this;
    }

    @Override
    public boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        // 异常状态只处理异常消息
        return false;
    }
}
```

#### 4.2 更新 States 注册表

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/common/States.java`

```java
public class States {
    public static BaseState<BattleUnitBlackboard> NORMAL_STATE = NormalState.instance;
    public static BaseState<BattleUnitBlackboard> BASE_MOVE_STATE = MoveState.INSTANCE;
    public static BaseState<BattleUnitBlackboard> ATTACK_STATE = AttackState.INSTANCE;

    // 新增：异常状态
    public static BaseState<BattleUnitBlackboard> STUN_STATE = StunState.INSTANCE;
}
```

### 第五阶段：状态完成自动切换机制

#### 5.1 更新 AttackState 以支持自动切换

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/common/AttackState.java`

```java
public class AttackState implements BaseState<BattleUnitBlackboard> {
    public static final AttackState INSTANCE = new AttackState.INSTANCE;

    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time += delta;
        entity.getSelf().currentTime += delta;

        entity.getSelf().currentAttackProgress += delta;
        float maxAttackProgress = entity.getSelf().progressCouldDamage;

        if (entity.getSelf().currentAttackProgress >= maxAttackProgress) {
            // 造成伤害
            MessageManager.getInstance().dispatchMessage(entity.stateMachine, entity, MessageConstants.doAttack, "");
            entity.getSelf().attackCooldown = 1f;
            entity.couldDamage = false;
        }
        if (entity.getSelf().currentAttackProgress >= entity.getSelf().maxAttackActProgress) {
            // 攻击动作完成 - 请求下一个渴望状态
            MessageManager.getInstance().dispatchMessage(BattleTelegraph.INSTANCE, entity, MessageConstants.endAttack, "");
            entity.stateMachine.requestNextDesiredState();  // 新增：自动切换到下一个状态
        }
    }

    // ... 其他方法保持不变 ...
}
```

#### 5.2 更新 MoveState 以支持自动切换

**文件**: `core/src/main/java/com/voidvvv/autochess/sm/state/common/MoveState.java`

```java
public class MoveState implements BaseState<BattleUnitBlackboard> {
    public static final MoveState INSTANCE = new MoveState.INSTANCE;

    @Override
    public void update(BattleUnitBlackboard entity, float delta) {
        entity.getSelf().time += delta;
        entity.getSelf().currentTime += delta;

        // 检查是否到达目标
        if (entity.getTarget() == null || entity.getSelf().distanceTo(entity.getTarget()) < 0.5f) {
            // 到达目标 - 请求下一个渴望状态（通常切换到站立）
            entity.stateMachine.requestNextDesiredState();
        }
    }

    // ... 其他方法保持不变 ...
}
```

### 第六阶段：消息系统增强

#### 6.1 扩展 MessageConstants

**文件**: `core/src/main/java/com/voidvvv/autochess/msg/MessageConstants.java`

```java
public class MessageConstants {
    // 现有消息
    public static final int attack = 1;
    public static final int doAttack = 2;
    public static final int endAttack = 3;

    // 新增：状态控制消息
    public static final int CLEAR_EXCEPTION_STATES = 11;
    public static final int FORCE_STATE_CHANGE = 12;  // 强制切换到指定状态
}
```

## 状态转换流程图

### 普通状态流程（无异常）

```
行为树 → addDesiredState(MoveState)
          ↓
状态机 → desiredStateQueue = [MoveState]
          ↓
      switchToNextState()
          ↓
      current = MoveState (enter)
          ↓
      [移动中...]
          ↓
      到达目标 → requestNextDesiredState()
          ↓
      desiredStateQueue = []
          ↓
      [使用历史栈或默认站立]
          ↓
      current = NormalState (enter)
```

### 异常状态流程

```
外部 → addExceptionState(StunState, 3.0秒)
        ↓
BattleUnitBlackboard → exceptionQueue = [StunState]
                          exceptionTimers = {"stun": 3.0}
        ↓
状态机 → currentExceptionState = StunState
        ↓
     [眩晕3秒中...]（渴望状态队列被清空/忽略）
        ↓
    3秒后 → onExceptionExpired(StunState)
        ↓
     exceptionQueue = []
        ↓
状态机 → clearCurrentException()
        ↓
     [恢复到历史栈中的状态或默认站立]
```

### 异常状态覆盖普通状态

```
当前状态: AttackState
渴望队列: [NormalState]（攻击完成后切换）
        ↓
外部 → addExceptionState(StunState)
        ↓
状态机 → desiredStateQueue.clear()（清空普通状态队列）
        ↓
      currentExceptionState = StunState
        ↓
     [眩晕...]（攻击被中断）
        ↓
    眩晕结束 → clearCurrentException()
        ↓
     current = NormalState（历史栈为空，使用默认站立）
```

## 验收标准

### 功能需求

- [ ] **状态转换验证**
  - [ ] 普通状态之间的转换需要验证
  - [ ] 拒绝的转换被记录
  - [ ] 状态机维护拒绝计数

- [ ] **渴望状态队列**
  - [ ] 状态机维护渴望状态队列
  - [ ] 状态完成后自动请求下一个状态
  - [ ] 从队列获取默认站立状态
  - [ ] 行为树可以添加渴望状态

- [ ] **异常状态优先级**
  - [ ] 异常状态（优先级100）覆盖普通状态（优先级0）
  - [ ] 异常状态激活时忽略普通状态请求
  - [ ] 异常状态之间可以转换
  - [ ] 异常状态覆盖时清空渴望状态队列

- [ ] **异常状态队列**
  - [ ] 多个异常状态可以被排队
  - [ ] 异常状态基于计时器过期
  - [ ] 异常过期后下一个异常激活或返回正常
  - [ ] 异常队列可以被清除（战斗重置、驱散）

- [ ] **状态类型分类**
  - [ ] `StateType.NORMAL` 用于 NormalState、MoveState、AttackState
  - [ ] `StateType.EXCEPTION` 用于 StunState 和未来的异常状态
  - [ ] 通过 `getStateType()` 查询状态类型

- [ ] **向后兼容**
  - [ ] 现有行为树集成不变
  - [ ] 现有消息流不变
  - [ ] 现有状态（Normal、Move、Attack）无需修改即可工作
  - [ ] 状态历史栈仍然有效（最多20个状态）

### 非功能需求

- [ ] **性能**
  - [ ] 状态转换验证增加 <1μs 开销
  - [ ] 异常队列操作是 O(1)
  - [ ] 异常计时器没有内存泄漏

- [ ] **可测试性**
  - [ ] 所有新代码有单元测试
  - [ ] 状态机行为是确定性的
  - [ ] 边缘情况已覆盖（空队列、空状态等）

- [ ] **可维护性**
  - [ ] 代码遵循现有项目模式
  - [ ] 验证和状态逻辑清晰分离
  - [ ] 日志提供足够的调试信息

## 系统影响

### 交互图

```
GameScreen.render(delta)
  → BattleUpdater.update(delta)
    → for each BattleUnitBlackboard:
      → bb.update(delta)
        → stateMachine.update(delta)
          → switchToNextState()
            → 检查异常状态
            → 处理 desiredStateQueue
          → current.update(delta)
        → updateExceptionTimers(delta)
          → if timer <= 0: onExceptionExpired()
            → getNextExceptionState()
            → if (next exists): switchToException(next)
            → else: clearCurrentException()

行为树 (并行)
  → AttackTargetTask.execute()
    → MessageManager.dispatch(attack)
      → BattleUnitBlackboard.handleMessage()
        → onMessageAttack()
          → stateMachine.addDesiredState(ATTACK_STATE)  // 添加到渴望队列

状态完成 (自动)
  → AttackState.update()
    → 攻击完成时
      → stateMachine.requestNextDesiredState()
        → 从队列获取下一个状态

外部效果 (并行)
  → addExceptionState(StunState, 3.0)
    → stateMachine.switchToException(StunState)
      → desiredStateQueue.clear()  // 清空普通状态队列
```

### 错误与故障传播

| 故障点 | 检测 | 恢复 |
|--------|--------|--------|
| 无效转换 | `processDesiredState()` 检查 | 转换被拒绝，从队列移除 |
| 计时器下溢 | `remaining <= 0` 检查 | 计时器限制为0，状态退出 |
| 队列中的空状态 | `peekFirst()` 检查 | 返回 null，切换到 NORMAL_STATE |
| 异常状态覆盖时的并发请求 | 单个 `next` 字段 | 异常请求获胜（已文档化行为） |

### 状态生命周期风险

| 场景 | 风险 | 缓解 |
|--------|------|------|
| 攻击期间强制终止 | 投射物继续飞行 | `exit()` 重置 `couldDamage = false` |
| 激活异常状态下战斗重置 | 计时器跨战斗持久存在 | 战斗重置时调用 `clearExceptionStates()` |
| 计时器精度漂移 | 长时间异常错误过期 | 一致使用游戏循环中的 `delta` |
| 队列损坏（重复添加） | 状态永不过期 | `addDesiredState()` 检查重复 |

### API 表面一致性

| 接口 | 受影响方法 | 需要更新 |
|-------|-----------|----------|
| `BaseState<T>` | `enter()`, `exit()`, `update()`, `onMessage()` | 添加 `getStateType()`, `getPriority()`, `blocksTransitions()` |
| `StateMachine<T>` | `switchState()`, `update()` | 添加 `addDesiredState()`, `requestNextDesiredState()`, `switchToException()`, `clearCurrentException()` |
| `BattleUnitBlackboard` | `handleMessage()`, `update()` | 添加异常队列方法 |

### 集成测试场景

1. **正常状态转换**（Move → Attack → Normal）- 验证现有行为不变
2. **渴望状态队列**（Move → Attack → Normal 自动完成）- 验证队列机制
3. **单个异常**（Normal → Stun → Normal）- 验证基于计时器的过期
4. **异常链**（Normal → Stun → Freeze → Silence → Normal）- 验证队列管理
5. **攻击被中断**（AttackState 激活 → 添加 Stun）- 验证攻击正确取消
6. **转换拒绝**（Stun 激活 → 尝试 Attack）- 验证拒绝被记录
7. **战斗重置**（激活异常 → 战斗结束）- 验证队列被清除

## 依赖与风险

### 依赖

| 依赖 | 状态 | 影响 |
|-------|--------|------|
| LibGDX `State<T>` 接口 | 现有 | 不需要更改 |
| GDX-AI `Telegram` 系统 | 现有 | 添加新的消息常量 |
| `BattleUnitBlackboard` | 现有 | 添加异常队列 |
| 行为树系统 | 现有 | 不需要更改 |

### 风险

| 风险 | 可能性 | 影响 | 缓解 |
|-------|--------|------|------|
| 计时器精度问题 | 中 | 中 | 一致使用游戏循环中的 `delta`，添加各种帧率的测试 |
| 状态栈与队列冲突 | 低 | 高 | 异常状态不推入状态栈（已文档化） |
| 性能下降 | 低 | 低 | 验证是简单的整数比较 |
| 向后兼容性破坏 | 低 | 高 | 默认值维护现有行为 |

## 实现序列

### 第一阶段：基础（优先级1）
1. 创建 `StateType` 枚举
2. 使用新方法更新 `BaseState` 接口
3. 创建 `ExceptionState` 基类
4. 编写状态分类的单元测试

### 第二阶段：渴望状态队列（优先级1）
1. 添加 `desiredStateQueue` 到 `BaseStateMachine`
2. 实现 `addDesiredState()` 方法
3. 实现 `requestNextDesiredState()` 方法
4. 实现 `processDesiredState()` 方法
5. 编写队列机制的单元测试

### 第三阶段：状态机异常处理（优先级1）
1. 添加 `currentExceptionState` 到 `BaseStateMachine`
2. 实现 `switchToException()` 方法
3. 实现 `clearCurrentException()` 方法
4. 更新 `switchToNextState()` 处理异常状态
5. 编写异常优先级的单元测试

### 第四阶段：异常管理（优先级1）
1. 添加异常队列到 `BattleUnitBlackboard`
2. 添加计时器管理方法
3. 实现 `updateExceptionTimers()`
4. 编写异常链的集成测试

### 第五阶段：具体状态（优先级2）
1. 创建 `StunState`
2. 使用 `getStateType()` 更新现有状态
3. 添加到 `States` 注册表
4. 编写状态行为的单元测试

### 第六阶段：状态完成机制（优先级2）
1. 更新 `AttackState` 在完成时调用 `requestNextDesiredState()`
2. 更新 `MoveState` 在完成时调用 `requestNextDesiredState()`
3. 测试自动状态切换
4. 测试异常状态覆盖自动切换

### 第七阶段：清理（优先级2）
1. 从 `BattleUnitBlackboard` 移除反模式验证
2. 将 `clearExceptionStates()` 添加到战斗重置
3. 更新消息常量
4. 更新文档

### 第八阶段：完善（优先级3）
1. 添加状态历史日志（可选）
2. 性能分析
3. 代码审查和改进
4. 文档更新

## 成功指标

| 指标 | 目标 | 测量 |
|------|--------|------|
| 转换拒绝率 | 正常游戏 <1% | 日志分析 |
| 异常状态准确度 | 100% 正确过期 | 单元测试覆盖率 |
| 性能影响 | <0.1% 开销 | 性能分析 |
| 测试覆盖率 | 新代码 >90% | JaCoCo 报告 |
| 向后兼容性 | 0% 破坏性更改 | 集成测试 |

## 未来考虑

### 扩展点

1. **额外异常状态** - 按照 `StunState` 模式添加 `FreezeState`、`SilenceState`、`CharmState`
2. **状态转换事件** - 添加 UI 更新的监听器系统（显示眩晕图标）
3. **状态历史调试** - 可选地记录所有转换以进行故障排除
4. **守卫规则** - 除优先级外的每状态验证规则（例如，“HP<10%时无法切换到Attack”）
5. **状态机可视化** - 实时显示状态转换的调试视图

### 已知限制

1. **并发转换请求** - 最后一个请求获胜（已文档化，对当前用例可接受）
2. **无转换回滚** - 无效转换被拒绝，不回滚
3. **每单位单个状态机** - 无法拥有并行状态机（当前游戏不需要）

## 源与参考

### 起源

- **头脑风暴文档：** [docs/brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md](../brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md)
  - 决策1：状态注册为单例
  - 决策2：验证的守卫模式
  - 决策3：具有优先级100的异常状态设计
  - 决策4：黑板中的异常队列
  - 决策5：基于优先级的阻塞系统
  - 决策6：新状态的模板方法模式
  - 决策7：BattleUnitBlackboard 中的状态数据
  - 决策8：消息系统扩展
  - 决策9：增强现有 BaseStateMachine

### 内部参考

| 组件 | 文件路径 | 用途 |
|--------|----------|------|
| 状态机接口 | `core/src/main/java/com/voidvvv/autochess/sm/machine/StateMachine.java` | 状态机契约 |
| 状态机实现 | `core/src/main/java/com/voidvvv/autochess/sm/machine/BaseStateMachine.java` | 延迟切换逻辑 |
| 状态接口 | `core/src/main/java/com/voidvvv/autochess/sm/state/BaseState.java` | 状态契约 |
| 正常状态 | `core/src/main/java/com/voidvvv/autochess/sm/state/common/NormalState.java` | 待机状态模式 |
| 移动状态 | `core/src/main/java/com/voidvvv/autochess/sm/state/common/MoveState.java` | 移动状态模式 |
| 攻击状态 | `core/src/main/java/com/voidvvv/autochess/sm/state/common/AttackState.java` | 攻击状态模式 |
| 黑板 | `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java` | 状态机拥有者 |
| 消息 | `core/src/main/java/com/voidvvv/autochess/msg/MessageConstants.java` | 消息常量 |
| 攻击任务 | `core/src/main/java/com/voidvvv/autochess/battle/AttackTargetTask.java` | 从行为树的状态切换 |
| 移动任务 | `core/src/main/java/com/voidvvv/autochess/battle/MoveToEnemyTask.java` | 从行为树的状态切换 |

### 模式参考

| 模式 | 示例文件 | 应用 |
|------|----------|------|
| 单例状态 | `NormalState.java:8` | 所有状态使用单例模式 |
| 守卫模式 | 头脑风暴决策2 | 转换验证 |
| 管理器模式 | `RenderDataManager.java` | 异常队列管理 |
| 逻辑分离 | `CardUpgradeLogic.java` | logic/ 包中的验证 |
| 模板方法 | 头脑风暴决策6 | 状态扩展钩子 |

### 外部参考

- **LibGDX AI 框架：** https://libgdx.com/ai/
- **状态设计模式：** Gang of Four - 状态模式
- **有限状态机：** https://en.wikipedia.org/wiki/Finite-state_machine

### 相关工作

- Model-Update-Render 重构：[docs/plans/2026-03-08-refactor-model-update-render-separation-plan.md](2026-03-08-refactor-model-update-render-separation-plan.md)
- 代码审查发现：`review.md`（状态机问题部分）

### 测试策略

**要创建的单元测试：**
- `BaseStateMachineTest` - 渴望状态队列、异常处理
- `BattleUnitBlackboardTest` - 异常队列、计时器管理
- `StunStateTest` - 状态生命周期、标志位
- `ExceptionChainTest` - 序列中的多个异常

**集成测试：**
- 状态机 + 行为树协调
- 激活攻击时的异常状态
- 激活异常的战斗重置

**手动测试场景：**
1. 攻击期间施加眩晕 → 验证攻击取消
2. 施加多个debuff → 验证链工作
3. 眩晕时尝试移动 → 验证拒绝已记录
4. 等待眩晕过期 → 验证返回正常
5. 攻击完成 → 验证自动切换到站立/移动
