# 新版本输入处理plan
你需要为我实现一个新的输入处理逻辑。要求满足我的诉求并且代码规范合理。

我有以下诉求：
1. 输入类已经建好：com.voidvvv.autochess.input.v2.InputHandlerV2，你需要在这个类中实现输入处理逻辑。需要实现inputprocessor接口中的方法。
2. 你需要定义一个新的InputEvent类来封装输入事件的信息，包括但不限于：输入类型（键盘、鼠标等）、输入状态（按下、释放等）、输入位置（如果是鼠标输入）等。
3. 你需要定义一个新的InputListener接口来监听输入事件，接口中需要定义一个方法来处理输入事件。
4. InputHandlerV2中需要持有一个InputListener列表，当有输入事件发生时，InputHandlerV2需要通知所有的InputListener。
5. 你需要实现一个简单的输入事件分发机制，当inputprocessor接口中方法触发时，InputHandlerV2需要根据输入事件的类型和状态来初始化一个InputEvent对象，并将其传递给所有的InputListener进行处理。
6. InputEvent对象需要包含以下属性：
   - 输入类型（InputType）：一个枚举类型，包含键盘、鼠标等输入类型。
   - 输入状态（InputState）：一个枚举类型，包含按下、释放等输入状态。
   - 输入位置（InputPosition）：一个类，包含x和y坐标，用于表示鼠标输入的位置。
   - 输入键码（KeyCode）：一个整数，表示键盘输入的键码。
   - 输入按钮（MouseButton）：一个枚举类型，表示鼠标输入的按钮（左键、右键等）。
7. InputListener接口中的方法需要接受一个InputEvent对象作为参数，以便处理输入事件。
8. 当前所有的设计都需要在com.voidvvv.autochess.input.v2包中实现。
