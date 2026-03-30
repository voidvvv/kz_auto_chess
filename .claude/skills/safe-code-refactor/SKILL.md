---
name: safe-code-refactor
description: 防止代码重构中的常见陷阱，特别是使用全局字符串替换时导致的无限递归、意外修改等问题。当用户需要重命名方法、批量修改代码、使用 Edit 工具的 replace_all 功能时，应使用此 skill。请记住：全局替换非常危险，即使是看似简单的修改也可能破坏代码结构。
---

# 安全代码重构指南

本技能帮助你避免代码重构中的常见陷阱，特别是**全局字符串替换**导致的灾难性错误。

## 核心原则

> **全局字符串替换是危险的**。即使是看似简单的替换，也可能破坏你意想不到的地方。

## 常见陷阱

### 陷阱 1: 无限递归（最危险）

**场景**：你创建了一个新的包装方法，然后用 `replace_all=true` 替换所有调用。

**错误示例**：
```java
// 原代码
private static void log(String tag, String message) {
    if (Gdx.app != null) {
        Gdx.app.log(tag, message);  // ← 想保留这个
    } else {
        System.out.println("[" + tag + "] " + message);
    }
}

// 使用 Edit: replace_all=true 将 Gdx.app.log 替换为 log
// 结果：方法内部的 Gdx.app.log 也被替换了！

private static void log(String tag, String message) {
    if (Gdx.app != null) {
        log(tag, message);  // ← 无限递归！StackOverflowError！
    } else {
        System.out.println("[" + tag + "] " + message);
    }
}
```

**为什么会发生**：`replace_all=true` 不区分"方法定义内部"和"方法调用"，全部替换。

### 陷阱 2: 意外修改注释和字符串字面量

**场景**：替换方法名时，注释和字符串中的相同词汇也被修改。

```java
// 原代码
/**
 * 处理用户登录 (login)
 */
public void login(String username) {
    logger.info("用户 login");  // ← 这里的 "login" 也会被替换
}

// 使用 replace_all=true 将 "login" 替换为 "authenticate"
// 结果：注释和字符串也被破坏了！

/**
 * 处理用户 authenticate (authenticate)
 */
public void authenticate(String username) {
    logger.info("用户 authenticate");  // ← 语义不自然
}
```

### 陷阱 3: 破坏第三方库调用

```java
// 原代码
import com.badlogic.gdx.Gdx;

Gdx.app.log("Tag", "message");

// 如果不小心将 "Gdx.app.log" 替换为 "log"
// 可能影响到其他依赖 Gdx.app.log 的代码
```

## 安全重构流程

### ✅ 步骤 1: 先修改目标方法，暂不替换调用

```java
// 步骤 1a: 创建新的包装方法（保留原有调用）
private static void logInfo(String message) {
    if (Gdx.app != null) {
        Gdx.app.log("CardShopManager", message);  // 保留 Gdx.app.log
    } else {
        System.out.println("[CardShopManager] " + message);
    }
}

// 步骤 1b: 编译验证
gradle compileJava
```

### ✅ 步骤 2: 逐个替换调用（使用更精确的匹配模式）

```java
// ✅ 安全：包含更多上下文
Gdx.app.log("CardShopManager", → logInfo(

// ❌ 危险：过于宽泛
Gdx.app.log → log
```

**建议**：使用 `replace_all=false`，逐个确认每个替换。

### ✅ 步骤 3: 每次修改后立即验证

```bash
# 修改一个文件
gradle compileJava

# 或者运行快速测试
gradle test --tests "*TestClass*"
```

### ✅ 步骤 4: 使用 IDE 重构功能（推荐）

IDE 的重构功能更安全：
- **IntelliJ IDEA**: Shift+F6 (Rename), Refactor → Rename
- **VS Code**: F2 (Rename Symbol)
- **Eclipse**: Alt+Shift+R (Rename)

优点：
- 只重命名方法和调用
- 不会修改注释或字符串字面量
- 自动查找所有引用
- 支持 Undo

## 替换策略对比

| 场景 | 工具 | 安全性 | 建议 |
|------|------|--------|------|
| 重命名方法/变量 | IDE Rename | ⭐⭐⭐⭐⭐ | **首选** |
| 修改方法签名 | IDE Change Signature | ⭐⭐⭐⭐⭐ | **首选** |
| 批量修改特定调用 | Edit (replace_all=false) | ⭐⭐⭐ | 逐个确认 |
| 全局字符串替换 | Edit (replace_all=true) | ⭐ | **极度危险** |
| 复杂重构 | 手动 + 验证 | ⭐⭐⭐ | 谨慎使用 |

## 危险模式识别

如果你正要做以下事情，**停下来，重新考虑**：

| 危险模式 | 更安全的替代方案 |
|----------|----------------|
| "把所有的 X 替换为 Y" | 先创建新方法，再逐个修改调用 |
| "replace_all=true" | 使用 replace_all=false，逐个确认 |
| "全局替换这个函数名" | 使用 IDE 的 Rename 功能 |
| 批量修改多个文件 | 一次只改一个文件，每次都编译验证 |

## 验证检查清单

每次代码修改后，在提交前检查：

- [ ] 编译成功（`gradle compileJava` 或等效命令）
- [ ] 相关测试通过
- [ ] 搜索代码，确认没有遗漏的旧调用
- [ ] 检查日志输出，确认没有无限循环迹象
- [ ] 读取修改后的文件，确认替换范围正确

## 修复无限递归的方法

如果你已经遇到无限递归错误：

1. **检查方法定义**：查找自调用
   ```bash
   grep -A 5 "private static void log" file.java
   ```

2. **恢复被错误替换的代码**：
   ```java
   private static void log(String tag, String message) {
       if (Gdx.app != null) {
           Gdx.app.log(tag, message);  // ← 恢复为原始调用
       } else {
           System.out.println("[" + tag + "] " + message);
       }
   }
   ```

3. **使用全限定名避免再次被替换**：
   ```java
   com.badlogic.gdx.Gdx.app.log(tag, message);  // 全限定名不会被简单替换匹配
   ```

## 实际案例教训

### 案例：CardShopManager 日志重构

**错误操作**：
```bash
# 使用 Edit 工具，replace_all=true
Gdx.app.log → log
```

**后果**：
- `log` 方法内部的 `Gdx.app.log` 被替换为 `log`
- 造成无限递归
- StackOverflowError

**正确做法**：
```java
// 方案 1: 使用不同的方法名
private static void logInfo(String message) {
    if (Gdx.app != null) {
        Gdx.app.log("CardShopManager", message);
    } else {
        System.out.println("[CardShopManager] " + message);
    }
}

// 方案 2: 在 log 方法内部使用全限定名
private static void log(String tag, String message) {
    if (Gdx.app != null) {
        com.badlogic.gdx.Gdx.app.log(tag, message);  // 全限定名
    } else {
        System.out.println("[" + tag + "] " + message);
    }
}
```

## 总结

记住这些黄金规则：

1. **永远不要对方法定义内部的内容使用全局替换**
2. **创建新方法时，使用不同的名字避免冲突**
3. **每次修改后立即编译验证**
4. **优先使用 IDE 的重构功能**
5. **如果必须用全局替换，先用 `replace_all=false` 逐个确认**

---

> **最后提醒**：当你看到 `StackOverflowError` 或方法无限循环时，首先检查：是否刚刚做了全局字符串替换？
