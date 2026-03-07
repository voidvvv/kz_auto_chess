# 角色加载器与Tiled渲染实施计划

## 项目概述
实现从Tiled文件加载角色纹理和碰撞框的系统，并提供渲染方式切换开关。

## 实施步骤

### 阶段1：基础数据结构修改
- [x] **修改Card类** - 添加`tiledResourceKey`字段
  - 文件：`core/src/main/java/com/voidvvv/autochess/model/Card.java`
  - 添加字段：`private String tiledResourceKey`
  - 修改构造函数支持该字段
  - 添加getter方法：`getTiledResourceKey()`

- [x] **修改CardPool类** - 为每个卡牌设置Tiled资源key
  - 文件：`core/src/main/java/com/voidvvv/autochess/model/CardPool.java`
  - 在`initCards()`方法中为每个`Card`构造函数添加Tiled资源key参数
  - 示例：`new Card(1, ..., "abc+140")` // abc tileset, tile id 140
  - 需要确定每个卡牌对应的具体Tiled资源key

### 阶段2：TiledAssetLoader扩展
- [x] **扩展TiledAssetLoader类** - 添加资源获取方法
  - 文件：`core/src/main/java/com/voidvvv/autochess/utils/TiledAssetLoader.java`
  - 添加方法：`getCollision(String key)`
  - 添加方法：`getTexture(String key)`
  - 添加方法：`hasResource(String key)`

### 阶段3：BattleCharacter集成
- [x] **修改BattleCharacter类** - 添加纹理字段和加载方法
  - 文件：`core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java`
  - 添加字段：`private TextureRegion tiledTexture`
  - 添加方法：`loadTiledResources(TiledAssetLoader loader)`
  - 添加方法：`getTiledTexture()`和`hasTiledTexture()`
  - 在`loadTiledResources`中设置`baseCollision`字段

### 阶段4：新渲染器实现
- [x] **实现TiledBattleCharacterRender类**
  - 文件：`core/src/main/java/com/voidvvv/autochess/render/TiledBattleCharacterRender.java`
  - 实现`render(SpriteBatch, BattleCharacter)`方法
  - 实现`renderWithAlpha(SpriteBatch, BattleCharacter, float)`方法
  - 确保纹理正确渲染，支持透明度和阵营颜色区分

### 阶段5：渲染开关系统
- [x] **创建RenderConfig类** - 全局渲染配置
  - 文件：`core/src/main/java/com/voidvvv/autochess/utils/RenderConfig.java`
  - 添加静态字段：`public static boolean USE_TILED_RENDERING = false`
  - 添加静态方法：`toggleRendering()`

- [x] **修改BattleFieldRender类** - 集成渲染开关
  - 文件：`core/src/main/java/com/voidvvv/autochess/render/BattleFieldRender.java`
  - 修改`render(Battlefield)`方法，根据`RenderConfig.USE_TILED_RENDERING`选择渲染器
  - 确保`SpriteBatch`在正确的时间开始和结束

### 阶段6：资源加载集成
- [x] **修改GameScreen类** - 加载Tiled资源
  - 文件：`core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java`
  - 在`show()`方法中初始化`TiledAssetLoader`
  - 使用`TmxMapLoader`加载地图文件`tiled/demo/2.tmx`
  - 遍历地图中的所有tileset，调用`tiledLoader.loadBaseCollision()`
  - 为战场上的所有角色调用`character.loadTiledResources(tiledLoader)`

### 阶段7：开关控制功能
- [x] **添加热键切换功能**
  - 文件：`core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java`
  - 在输入处理中添加热键支持（例如F5键）
  - 调用`RenderConfig.toggleRendering()`切换渲染模式
  - 添加日志输出显示当前渲染模式

## 详细实施说明

### Card类修改细节
```java
// 在Card类中添加
private String tiledResourceKey;

// 修改构造函数
public Card(int id, String name, String description, int cost, int tier, CardType type,
            int starLevel, int baseCardId, List<SynergyType> synergies, String tiledResourceKey) {
    // ... 现有字段初始化
    this.tiledResourceKey = tiledResourceKey;
}

public String getTiledResourceKey() {
    return tiledResourceKey;
}
```

### CardPool中的Tiled资源key映射
需要确定每个卡牌对应的具体Tiled资源key。查看`assets/tiled/demo/`中的文件：
- `abc.tsx` - tileset文件，包含多个tile
- 需要检查tile id与卡牌的对应关系
- 示例映射：
  - novice_warrior (cardId=1) -> "abc140" (如果tile id=140)
  - novice_mage (cardId=2) -> "abc141" (如果tile id=141)
  - 等等...

### TiledAssetLoader扩展
```java
public class TiledAssetLoader {
    // ... 现有字段

    public BaseCollision getCollision(String key) {
        return collisionMapping.get(key);
    }

    public TextureRegion getTexture(String key) {
        return textureRegionMap.get(key);
    }

    public boolean hasResource(String key) {
        return collisionMapping.containsKey(key) && textureRegionMap.containsKey(key);
    }
}
```

### BattleCharacter集成
```java
public class BattleCharacter {
    private TextureRegion tiledTexture;

    public void loadTiledResources(TiledAssetLoader loader) {
        String key = this.getCard().getTiledResourceKey();
        if (key != null && loader.hasResource(key)) {
            this.baseCollision = loader.getCollision(key); // 使用提取的碰撞框
            this.tiledTexture = loader.getTexture(key);
        }
    }

    public TextureRegion getTiledTexture() {
        return tiledTexture;
    }

    public boolean hasTiledTexture() {
        return tiledTexture != null;
    }
}
```

### 渲染器实现
```java
public class TiledBattleCharacterRender {
    public static void render(SpriteBatch batch, BattleCharacter character) {
        TextureRegion texture = character.getTiledTexture();
        if (texture == null) return;

        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 可选：根据阵营设置颜色
        if (character.isEnemy()) {
            batch.setColor(1f, 0.7f, 0.7f, 1f);
        }

        batch.draw(texture, x - size/2, y - size/2, size, size);

        if (character.isEnemy()) {
            batch.setColor(1f, 1f, 1f, 1f); // 恢复默认颜色
        }
    }

    // renderWithAlpha方法类似，但设置透明度
}
```

### 开关集成到BattleFieldRender
```java
public class BattleFieldRender {
    public void render(Battlefield battlefield) {
        // ... 现有战场背景渲染

        boolean useTiled = RenderConfig.USE_TILED_RENDERING;

        // 如果需要Tiled渲染，开始SpriteBatch
        if (useTiled) {
            game.getBatch().begin();
            game.getBatch().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        }

        for (BattleCharacter character : battlefield.getCharacters()) {
            if (character.isDead()) {
                float alpha = 0.3f;
                if (useTiled && character.hasTiledTexture()) {
                    TiledBattleCharacterRender.renderWithAlpha(game.getBatch(), character, alpha);
                } else {
                    CharacterRenderer.renderWithAlpha(shapeRenderer, character, alpha);
                }
            } else {
                if (useTiled && character.hasTiledTexture()) {
                    TiledBattleCharacterRender.render(game.getBatch(), character);
                } else {
                    CharacterRenderer.render(shapeRenderer, character);
                }
            }
        }

        // 结束SpriteBatch（如果开始了）
        if (useTiled) {
            game.getBatch().end();
        }
    }
}
```

## 测试计划
1. [x] 编译项目，确保没有语法错误
2. [ ] 运行游戏，验证现有几何渲染正常工作
3. [ ] 按F5键启用Tiled渲染，检查角色是否显示纹理
4. [ ] 再次按F5键切换回几何渲染
5. [ ] 测试死亡角色的半透明渲染
6. [ ] 验证碰撞框是否正确应用到角色
7. [ ] 测试不同阵营角色的颜色区分

## 注意事项
1. 确保向后兼容：默认`USE_TILED_RENDERING = false`
2. 如果角色没有对应的Tiled纹理，自动回退到几何渲染
3. 确保资源加载失败时不会崩溃
4. 热键切换时无需重启游戏
5. 保持代码简洁，避免不必要的复杂性

## 预计完成时间
- 阶段1-3：1小时
- 阶段4-5：1小时
- 阶段6-7：1小时
- 测试和调试：1小时
- 总计：约4小时