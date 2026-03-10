# State Machine Enhancement Brainstorm

## What We're Building

增强游戏中的状态机（StateMachine），支持更复杂的状态转换规则、异常状态、以及灵活的状态扩展能力。

## Why This Approach

当前游戏的状态机实现比较简单：
- 使用固定的状态实例（单例模式）
- 状态转换没有验证规则
- 不支持异常状态和例外规则
- 新增状态需要修改多处代码

需要一套更灵活的状态机设计来满足这些复杂需求。

---

## Key Decisions

### Decision 1: 状态注册机制

**用户选择**：状态作为单例，在代码中写死

**设计**：
- 状态类使用单例模式（public static final）
- 不需要运行时注册系统
- 简单但缺乏动态扩展性

**权衡**：
- ✅ 简单、性能好
- ❌ 新增状态需要修改多处代码

---


### Decision 2: 状态转换验证

**用户选择**：Guard模式（状态自己检查并允许/拒绝）

**设计**：
```java
public abstract class GuardedState<T extends BaseState<T>> implements BaseState<T> {
    private final StateTransitionRule<T> transitionRule;

    public GuardedState(StateTransitionRule<T> rule) {
        this.transitionRule = rule;
    }

    @Override
    public final boolean canTransitionTo(BaseState<T> targetState) {
        if (transitionRule != null) {
            return transitionRule.canTransition(getCurrent(), targetState, getOwner());
        }
        return true; // 没有规则时允许转换
    }

    // 子类实现状态逻辑，通过canTransitionTo检查
}
```

**权衡**：
- ✅ 转换规则可配置
- ✅ 每个状态控制自己的转换逻辑

---

### Decision 3: 异常状态设计

**用户选择**：异常状态类，与正常状态并列

**设计**：
```java
// 状态类型枚举
public enum StateType {
    NORMAL,      // 正常状态（站立、行走、攻击等）
    EXCEPTION    // 异常状态（眩晕、冻结、沉默等）
}

// 异常状态基类
public abstract class ExceptionState<T> implements BaseState<T> {
    // 异常状态在转换时阻塞所有其他状态
    @Override
    public StateType getStateType() {
        return StateType.EXCEPTION;
    }

    // 异常状态优先级最高
    @Override
    public int getTransitionPriority() {
        return 100; // 最高优先级
    }
}

// 具体异常状态
public class StunState extends ExceptionState<BattleUnitBlackboard> {
    @Override
    public void enter(BattleUnitBlackboard entity) {
        // 添加异常状态到blackboard
        entity.addExceptionState(this);
    }

    @Override
    public void exit(BattleUnitBlackboard entity) {
        // 计算下一个异常状态
        String nextException = entity.getNextExceptionState(this);
        if (nextException != null) {
            entity.getStateMachine().switchState(nextException);
        } else {
            // 返回正常状态（站立）
            entity.getStateMachine().switchState(States.NORMAL_STATE);
        }
    }
}
```

**权衡**：
- ✅ 异常状态与正常状态分离
- ✅ 支持异常状态链

---

### Decision 4: 异常状态链管理

**用户选择**：在blackboard中使用队列管理异常状态链

**设计**：
```java
public class BattleUnitBlackboard {
    // 异常状态队列
    private final Deque<BaseState<BattleUnitBlackboard>> exceptionQueue = new ArrayDeque<>();

    // 添加异常状态
    public void addExceptionState(BaseState<BattleUnitBlackboard> state) {
        exceptionQueue.addLast(state);
    }

    // 获取下一个异常状态
    public BaseState<BattleUnitBlackboard> getNextExceptionState(BaseState<BattleUnitBlackboard> current) {
        if (exceptionQueue.isEmpty()) return null;
        if (exceptionQueue.peekLast() == current) {
            exceptionQueue.pollLast(); // 移除当前
        }
        return exceptionQueue.peekLast(); // 返回下一个
    }
}
```

**权衡**：
- ✅ 支持异常状态链
- ✅ 集中管理，便于扩展

---

### Decision 5: 状态转换优先级

**用户选择**：阻塞优先级（异常状态总是阻塞其他状态）

**设计**：
```java
public interface BaseState<T> {
    // 状态类型
    StateType getStateType();

    // 转换优先级（数值越高，优先级越高）
    default int getTransitionPriority() { return 0; }

    // 是否阻塞其他转换
    default boolean blocksTransitions() { return false; }
}

// 状态机修改
public void switchState(BaseState<T> next) {
    if (next == null) return;

    BaseState<T> current = getCurrent();
    int currentPriority = current != null ? current.getTransitionPriority() : 0;
    int nextPriority = next.getTransitionPriority();

    // 检查是否被阻塞
    if (current != null && current.blocksTransitions()) {
        // 当前状态阻塞所有转换，无法切换
        return;
    }

    // 优先级检查
    if (nextPriority > currentPriority) {
        // 高优先级覆盖低优先级
        executeSwitch(next);
    } else if (nextPriority == currentPriority) {
        // 同优先级：异常状态优先
        if (next.getStateType() == StateType.EXCEPTION) {
            executeSwitch(next);
        }
    }
}

private void executeSwitch(BaseState<T> next) {
    // 正常切换逻辑
}
```

**权衡**：
- ✅ 简单有效的优先级机制
- ✅ 异常状态优先处理

---

### Decision 6: 新状态扩展

**用户选择**：继承状态基类BaseState并实现对应方法

**设计**：
```java
public abstract class EnhancedBaseState<T extends BaseState<T>> {
    // 钩子类必须实现的核心方法
    protected abstract void onEnter(T entity);
    protected abstract void onExit(T entity);
    protected abstract void onUpdate(T entity, float delta);
    protected abstract boolean onMessage(T entity, Telegram telegram);

    // 空实现基类，提供默认行为
    @Override
    public final void enter(T entity) {
        onEnter(entity);
    }

    @Override
    public final void exit(T entity) {
        onExit(entity);
    }

    @Override
    public final void update(T entity, float delta) {
        onUpdate(entity, delta);
    }

    @Override
    public final boolean onMessage(T entity, Telegram telegram) {
        return onMessage(entity, telegram);
    }
}

// 新状态实现
public class DodgeState extends EnhancedBaseState<BattleUnitBlackboard> {
    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        entity.getSelf().setDodgeMode(true);
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        entity.getSelf().setDodgeMode(false);
    }

    @Override
    protected void onUpdate(BattleUnitBlackboard entity, float delta) {
        // 闪避逻辑
    }

    @Override
    protected boolean onMessage(BattleUnitBlackboard entity, Telegram telegram) {
        return false;
    }
}
```

**权衡**：
- ✅ 新状态只需继承和实现方法
- ✅ 基类提供默认行为

---

---

## Additional Decisions from Continued Discussion

### Decision 7: 状态数据存储位置

**用户选择**：状态数据存在BattleUnitBlackboard中

**设计**：
```java
public class BattleUnitBlackboard {
    // 状态相关数据
    private BaseState<BattleUnitBlackboard> currentState;
    private StateType currentStateType;
    private int stateEnterTime;

    // 异常状态管理
    private final Map<String, Float> exceptionStateTimers = new HashMap<>();
    private final Deque<BaseState<BattleUnitBlackboard>> exceptionQueue = new ArrayDeque<>();

    // 添加异常状态
    public void addExceptionState(BaseState<BattleUnitBlackboard> state, float duration) {
        exceptionQueue.addLast(state);
        exceptionStateTimers.put(state.getClass().getSimpleName(), duration);
    }

    // 获取下一个异常状态
    public BaseState<BattleUnitBlackboard> getNextExceptionState(BaseState<BattleUnitBlackboard> current) {
        if (exceptionQueue.isEmpty()) return null;
        if (exceptionQueue.peekLast() == current) {
            exceptionQueue.pollLast();
        }
        return exceptionQueue.peekLast();
    }
}
```

**权衡**：
- ✅ 状态数据与角色数据集中
- ✅ 方便扩展到其他场景（城镇黑板等）

### Decision 8: 消息系统设计

**设计**：扩展现MessageConstants

```java
public class MessageConstants {
    // 现有
    public static final int ATTACK = 1;
    public static final int DO_ATTACK = 2;
    public static final int END_ATTACK = 3;

    // 新增：状态控制消息
    public static final int FORCE_TERMINATE_STATE = 10;
    public static final int CLEAR_EXCEPTION_STATES = 11;
    public static final int TRANSITION_DENIED = 12;
}
```

**状态中断处理**：
```java
public class BaseState<T> {
    @Override
    public boolean onMessage(T entity, Telegram telegram) {
        int message = telegram.message;

        switch (message) {
            case MessageConstants.FORCE_TERMINATE_STATE:
                handleForceTerminate(entity, telegram);
                return true;

            case MessageConstants.TRANSITION_DENIED:
                handleTransitionDenied(entity, telegram);
                return true;

            default:
                return false;
        }
    }

    // 供子类重写的处理方法
    protected void handleForceTerminate(T entity, Telegram telegram) {
        entity.getStateMachine().switchState(null);
    }

    protected void handleTransitionDenied(T entity, Telegram telegram) {
        // 默认不做任何事
    }
}
```

**AttackState特殊处理**：
```java
public class AttackState implements BaseState<BattleUnitBlackboard> {
    @Override
    protected void handleForceTerminate(T entity, Telegram telegram) {
        BattleUnitBlackboard bb = entity;
        BaseState<BattleUnitBlackboard> next = bb.getNextExceptionState(this);

        if (next != null) {
            entity.getStateMachine().switchState(next);
        } else {
            entity.getStateMachine().switchState(States.NORMAL_STATE);
        }
    }
}
```

### Decision 9: 状态机架构模式

**用户选择**：现有状态机增强

**设计**：
- 保留`BaseStateMachine`的基本结构
- 添加**优先级**和**验证器**支持
- 添加**状态类型**枚举

```java
// 新增接口到BaseState
public interface BaseState<T> {
    // 现有方法...
    StateType getStateType(); // 新增
    int getPriority(); // 新增
    boolean blocksTransitions(); // 新增
}

// 新增枚举
public enum StateType {
    NORMAL,      // 正常状态（站立、行走、攻击）
    EXCEPTION,   // 异常状态（眩晕、冻结）
    TRANSITION   // 过渡状态
}

// 状态机增强
public class BaseStateMachine<T> implements StateMachine<T>, Telegraph {
    // 现有字段...

    // 新增：优先级检查
    private boolean checkPriority(BaseState<T> from, BaseState<T> to) {
        if (from != null && from.blocksTransitions()) {
            return false; // 当前状态阻塞
        }

        if (to != null) {
            int fromPriority = from.getPriority();
            int toPriority = to.getPriority();
            return toPriority > fromPriority;
        }
        return true;
    }

    @Override
    public void switchState(BaseState<T> next) {
        // 添加优先级检查逻辑
        if (next == null) return;

        BaseState<T> current = getCurrent();
        if (checkPriority(current, next)) {
            executeSwitch(next);
        } else {
            // 记录转换被拒绝
            // 可以发送TRANSITION_DENIED消息
        }
    }
}
```

**权衡**：
- ✅ 最小化改动，不破坏现有代码
- ✅ 逐步扩展，风险可控

---

## Open Questions

1. 状态转换规则是否需要支持"同时激活"的情况？例如某些条件下可以同时处于两个状态？

2. 异常状态的持续时间如何与游戏时钟同步？（每帧减少？每秒减少？）

3. 是否需要状态历史记录功能？（记录状态转换历史用于调试）

4. 状态机的更新频率是否与游戏帧率一致，还是可以独立控制？

5. 是否需要"状态转换事件"系统？（通知其他系统状态变化，如UI更新）

---

## Additional Decisions from Continued Discussion

### Decision 8: 能力拦截修正

**用户反馈的问题**：
> "在当前的状态体系下，enter方法的调用对于状态的实际进入并没有任何影响。它只是在状态改变时调用，初始化一些目标的状态变量。你在这里使用ability对其进行拦截，在实际的游戏逻辑中只会导致目标状态没有初始化，但是后续还会更新，会出问题。"

**正确的设计应该是**：
- ability拦截应该在状态机层面，在调用state.enter()之前进行验证
- 或者在状态enter中拦截后，确保状态仍能正常执行其核心逻辑
- 避免在enter方法中过早返回导致状态变量未初始化

**修正方案**：
```java
// 方案1：状态机层面拦截
public void switchState(BaseState<T> next) {
    if (validator != null && !validator.canTransition(getCurrent(), next, context)) {
        // 不允许转换
        return;
    }

    // 先切换状态
    executeSwitch(next);
}

// 方案2：状态内部处理
@Override
public void enter(T entity) {
    // 先执行核心enter逻辑（初始化变量）
    doEnterCore(entity);
    
    // 然后检查能力拦截
    if (!shouldBlockByAbility(entity)) {
        // 被拦截，执行清理逻辑
        return;
    }
}

protected void doEnterCore(T entity) {
    // 原有的enter逻辑
}

protected boolean shouldBlockByAbility(T entity) {
    // 能力拦截检查
    return false; // 默认不拦截
}
```

**权衡**：
- ⚠️ enter中拦截会导致状态未初始化
- ✅ 修改到状态机层面或后置处理
