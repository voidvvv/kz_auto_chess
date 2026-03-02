# kz_auto_chess

## 项目简介
这是一个基于LibGDX框架开发的自走棋游戏项目，提供跨平台的游戏支持。

## 技术栈
- Java
- LibGDX框架
- Gradle构建工具

## 项目结构

### 核心目录结构
```
kz_auto_chess/
├── core/             # 核心游戏代码
├── lwjgl3/           # 桌面平台后端
├── assets/           # 游戏资源
├── gradle/           # Gradle配置
├── tasks/            # 任务文档
├── README.md         # 项目说明文档
└── build.gradle      # 项目构建配置
```

### 核心代码结构 (core/src/main/java/com/voidvvv/autochess/)

#### 1. 战斗系统
- **battle/**: 行为树相关战斗组件
  - AttackTargetTask.java: 攻击目标任务
  - BattleTelegraph.java: 战斗 telegraph 系统
  - BattleUnitBlackboard.java: 战斗单位黑板
  - FindEnemyTask.java: 寻找敌人任务
  - MoveToEnemyTask.java: 移动到敌人位置任务
  - UnitBehaviorTreeFactory.java: 单位行为树工厂

#### 2. 事件系统
- **listener/**: 各种事件监听器
  - damage/: 伤害相关监听器
    - DamageEventListener.java: 伤害事件监听器
    - DamageRenderListener.java: 伤害渲染监听器
    - DamageSettlementListener.java: 伤害结算监听器

#### 3. 数据模型
- **model/**: 基础数据结构
  - battle/: 战斗相关模型
    - Damage.java: 伤害模型
    - DamageEventHolder.java: 伤害事件持有者
    - DamageEventListenerHolder.java: 伤害事件监听器持有者
  - control/: 控制相关模型
    - AutoChessWorldControl.java: 自走棋世界控制
  - event/: 事件模型
    - DamageEvent.java: 伤害事件
  - BattleCharacter.java: 战斗角色
  - Battlefield.java: 战场
  - Card.java: 卡牌
  - CardPool.java: 卡池
  - CardShop.java: 卡牌商店
  - CharacterStats.java: 角色属性
  - DamageShowModel.java: 伤害显示模型
  - GamePhase.java: 游戏阶段
  - LevelEnemyConfig.java: 关卡敌人配置
  - ModelHolder.java: 模型持有者
  - MoveComponent.java: 移动组件
  - MoveGuard.java: 移动守卫
  - PlayerDeck.java: 玩家卡组

#### 4. 消息系统
- **msg/**: 消息处理相关
  - consumer/: 消费者
    - Consumer.java: 消费者接口
    - DefaultConsumer.java: 默认消费者
  - DefaultKZConsumer.java: 默认KZ消费者
  - KZConsumer.java: KZ消费者接口
  - MessageConstants.java: 消息常量

#### 5. 渲染系统
- **render/**: 渲染相关
  - BattleFieldRender.java: 战场渲染
  - DamageLineRender.java: 伤害线渲染

#### 6. 游戏屏幕
- **screens/**: 游戏屏幕
  - GameScreen.java: 游戏主屏幕
  - LevelSelectScreen.java: 关卡选择屏幕
  - StartScreen.java: 开始屏幕

#### 7. 状态机系统
- **sm/**: 状态机相关
  - machine/: 状态机
    - BaseStateMachine.java: 基础状态机
    - StateMachine.java: 状态机接口
  - state/: 状态
    - BaseState.java: 基础状态

#### 8. UI系统
- **ui/**: UI相关
  - CardRenderer.java: 卡牌渲染器

#### 9. 更新系统
- **updater/**: 更新器
  - BaseMyFunction.java: 基础函数
  - BattleCharacterUpdater.java: 战斗角色更新器
  - BattleUpdater.java: 战斗更新器
  - DamageRenderUpdater.java: 伤害渲染更新器
  - MyFunction.java: 函数接口

#### 10. 工具类
- **utils/**: 工具类
  - AutoChessController.java: 自走棋控制器
  - CameraController.java: 相机控制器
  - CharacterCamp.java: 角色阵营
  - CharacterRenderer.java: 角色渲染器
  - FontUtils.java: 字体工具
  - ViewManagement.java: 视图管理

#### 11. 主类
- KzAutoChess.java: 游戏主类
- Main.java: 主入口

### 资源目录 (assets/)
- **ui/**: UI资源
  - font-list.fnt: 列表字体
  - font-subtitle.fnt: 副标题字体
  - font-window.fnt: 窗口字体
  - font.fnt: 主字体
  - uiskin.atlas: UI皮肤图集
  - uiskin.json: UI皮肤配置
  - uiskin.png: UI皮肤图片
- **character_stats.json**: 角色属性配置
- **libgdx.png**: LibGDX默认图片

## 游戏功能

### 核心玩法
- [ ] 回合制战斗系统
- [ ] 卡牌收集与升级
- [ ] 角色养成
- [ ] 关卡挑战

### 技术特性
- [ ] 行为树AI系统
- [ ] 事件驱动架构
- [ ] 状态机管理
- [ ] 跨平台支持

## 构建与运行

### 环境要求
- JDK 8+
- Gradle 7+

### 构建命令
```bash
./gradlew build
```

### 运行游戏
```bash
./gradlew lwjgl3:run
```

## 开发计划

### 待完成功能
- [ ] 完善战斗系统
- [ ] 实现卡牌系统
- [ ] 开发UI界面
- [ ] 添加音效和特效
- [ ] 优化性能

## 注意事项

## 贡献指南

## 许可证

