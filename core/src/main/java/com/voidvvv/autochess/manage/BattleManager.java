package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.voidvvv.autochess.KzAutoChess;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.battle.BattleState;
import com.voidvvv.autochess.battle.UnitBehaviorTreeFactory;
import com.voidvvv.autochess.event.BattleEndEvent;
import com.voidvvv.autochess.event.BattleStartEvent;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.GameEventSystem;
import com.voidvvv.autochess.listener.damage.DamageRenderListener;
import com.voidvvv.autochess.logic.CharacterStatsLoader;
import com.voidvvv.autochess.logic.SynergyManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Battlefield;
import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.CardPool;
import com.voidvvv.autochess.model.CharacterStats;
import com.voidvvv.autochess.model.DamageShowModel;
import com.voidvvv.autochess.model.GamePhase;
import com.voidvvv.autochess.model.LevelEnemyConfig;
import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.render.BattleCharacterRender;
import com.voidvvv.autochess.render.BattleFieldRender;
import com.voidvvv.autochess.render.DamageLineRender;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.ProjectileRenderer;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.updater.BattleCharacterUpdater;
import com.voidvvv.autochess.updater.BattleUpdater;
import com.voidvvv.autochess.updater.DamageRenderUpdater;
import com.voidvvv.autochess.utils.CameraController;
import com.voidvvv.autochess.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BattleManager - 管理所有战斗相关操作
 *
 * 职责:
 * - 战斗生命周期管理 (start, update, end)
 * - 角色加载/卸载及黑板管理 (bbList 由本类自管理)
 * - 行为树管理
 * - 战斗渲染 (战场、角色、投掷物、特效)
 * - 通过事件系统与其他 Manager 通信
 */
public class BattleManager implements GameRenderer, GameEventListener {

    // ========== Inner Classes ==========

    public class BattlePhaseManager {
        private float battleTime = 0;
        private int currentRound = 1;
        private boolean isBattleActive = false;

        public void startBattle(int level, CardPool cardPool, CharacterStatsLoader statsLoader) {
            battleTime = 0;
            isBattleActive = true;

            battleState.transitionTo(GamePhase.BATTLE);

            List<Integer> enemyIds = LevelEnemyConfig.getEnemyCardIdsForLevel(level);
            LevelEnemyConfig.spawnEnemiesInBattlefield(battlefield, enemyIds, cardPool, statsLoader);

            for (BattleCharacter c : battlefield.getCharacters()) {
                if (!c.isDead()) {
                    c.enterBattle();
                    characterLifecycle.loadCharacter(c);
                }
            }

            eventSystem.postEvent(new BattleStartEvent());
//            behaviorTreeManager.clear();
        }

        public void updateBattle(float delta) {
            if (!isBattleActive) return;

            battleTime += delta;

            behaviorTreeManager.update(battleTime);

            ProjectileManager projectileManager = battlefield.getProjectileManager();
            if (projectileManager != null) {
                projectileManager.update(delta, battlefield);
            }

            for (BattleCharacter c : battlefield.getCharacters()) {
                if (!c.isDead()) {
                    battleCharacterUpdater.update(c, delta);
                }
            }

            List<BattleCharacter> aliveCharacters = new ArrayList<>();
            for (BattleCharacter c : battlefield.getCharacters()) {
                if (!c.isDead()) {
                    aliveCharacters.add(c);
                }
            }
            if (!aliveCharacters.isEmpty()) {
                synergyManager.applySynergyEffects(aliveCharacters);
            }

            battleUpdater.update(delta);
            damageRenderUpdater.update(delta);

            for (BattleUnitBlackboard bb : bbList) {
                bb.update(delta);
            }

            if (battlefield.getPlayerCharacters().isEmpty() || battlefield.getEnemyCharacters().isEmpty()) {
                endBattle();
            }
        }

        public void endBattle() {
            isBattleActive = false;
            boolean playerWon = battlefield.getEnemyCharacters().isEmpty();

            // 获取剩余敌人数量（用于血量系统扣血计算）
            int remainingEnemies = battlefield.getEnemyCharacters().size();

            List<BattleCharacter> enemiesToRemove = new ArrayList<>();
            for (BattleCharacter c : battlefield.getCharacters()) {
                if (c.isEnemy()) {
                    enemiesToRemove.add(c);
                }
            }
            for (BattleCharacter c : enemiesToRemove) {
                characterLifecycle.unloadCharacter(c);
                battlefield.removeCharacter(c);
            }

            for (BattleCharacter c : battlefield.getCharacters()) {
                c.exitBattle();
                c.reset();
            }

            behaviorTreeManager.clear();
            bbList.clear();
            characterLifecycle.clear();

            battleState.transitionTo(GamePhase.PLACEMENT);
            eventSystem.postEvent(new BattleEndEvent(playerWon, remainingEnemies));

            currentRound++;
        }

        public boolean isBattleActive() { return isBattleActive; }
        public float getBattleTime() { return battleTime; }
        public int getCurrentRound() { return currentRound; }
        public void setCurrentRound(int round) { this.currentRound = round; }
    }

    public class CharacterLifecycleManager {
        private final Map<BattleCharacter, BattleUnitBlackboard> characterToBlackboard = new HashMap<>();
        private final Map<BattleUnitBlackboard, BehaviorTree<BattleUnitBlackboard>> blackboardToTree = new HashMap<>();

        public void loadCharacter(BattleCharacter character) {
            if (characterToBlackboard.containsKey(character)) return;

            character.setNextAttackTime(0);
            character.setTarget(null);

            BattleUnitBlackboard blackboard = new BattleUnitBlackboard(character, battlefield);
            characterToBlackboard.put(character, blackboard);
            bbList.add(blackboard);

            BehaviorTree<BattleUnitBlackboard> tree = UnitBehaviorTreeFactory.create(blackboard);
            blackboardToTree.put(blackboard, tree);
            behaviorTreeManager.addTree(tree);
        }

        public void unloadCharacter(BattleCharacter character) {
            BattleUnitBlackboard blackboard = characterToBlackboard.remove(character);
            if (blackboard != null) {
                bbList.remove(blackboard);
                BehaviorTree<BattleUnitBlackboard> tree = blackboardToTree.remove(blackboard);
                if (tree != null) {
                    behaviorTreeManager.removeTree(tree);
                }
            }
        }

        public void clear() {
            characterToBlackboard.clear();
            blackboardToTree.clear();
        }

        public BattleUnitBlackboard getBlackboard(BattleCharacter character) {
            return characterToBlackboard.get(character);
        }
    }

    public class BehaviorTreeManager {
        private final List<BehaviorTree<BattleUnitBlackboard>> trees = new ArrayList<>();

        public void addTree(BehaviorTree<BattleUnitBlackboard> tree) {
            if (!trees.contains(tree)) {
                trees.add(tree);
            }
        }

        public void removeTree(BehaviorTree<BattleUnitBlackboard> tree) {
            trees.remove(tree);
        }

        public void update(float currentTime) {
            for (BehaviorTree<BattleUnitBlackboard> tree : trees) {
                BattleUnitBlackboard bb = tree.getObject();
                if (bb.getSelf().isDead()) continue;
                bb.setCurrentTime(currentTime);
                tree.step();
            }
        }

        public void clear() {
            trees.clear();
        }
    }

    // ========== Fields ==========

    private final KzAutoChess game;
    private final BattleState battleState;
    private final GameEventSystem eventSystem;
    private final CameraController cameraController;

    private final Battlefield battlefield;
    private final CardPool cardPool;
    private final SynergyManager synergyManager;
    private final CharacterStatsLoader characterStatsLoader;

    /** bbList 由 BattleManager 自管理，不放入 BattleContext（可变数据不属于不可变上下文） */
    private final List<BattleUnitBlackboard> bbList = new ArrayList<>();

    private final BattlePhaseManager battlePhaseManager;
    private final CharacterLifecycleManager characterLifecycle;
    private final BehaviorTreeManager behaviorTreeManager;

    private BattleFieldRender battleFieldRender;
    private ProjectileRenderer projectileRenderer;
    private DamageLineRender damageLineRender;

    private final BattleUpdater battleUpdater;
    private final BattleCharacterUpdater battleCharacterUpdater;
    private final DamageRenderUpdater damageRenderUpdater;

    private final MovementEffectManager movementEffectManager;
    private final ParticleSpawner particleSpawner;
    private final ModelHolder<DamageShowModel> damageShowModelHolder;

    private int currentLevel = 1;

    public BattleManager(KzAutoChess game,
                         BattleState battleState,
                         GameEventSystem eventSystem,
                         CameraController cameraController,
                         Battlefield battlefield,
                         CardPool cardPool,
                         SynergyManager synergyManager,
                         CharacterStatsLoader characterStatsLoader) {
        this.game = game;
        this.battleState = battleState;
        this.eventSystem = eventSystem;
        this.cameraController = cameraController;
        this.battlefield = battlefield;
        this.cardPool = cardPool;
        this.synergyManager = synergyManager;
        this.characterStatsLoader = characterStatsLoader;

        // 设置 Battlefield 的 eventSystem 引用，用于技能效果事件发布
        this.battlefield.setEventSystem(eventSystem);

        this.battlePhaseManager = new BattlePhaseManager();
        this.characterLifecycle = new CharacterLifecycleManager();
        this.behaviorTreeManager = new BehaviorTreeManager();

        this.battleUpdater = new BattleUpdater(
            battlefield.getDamageEventHolder(),
            battlefield.getDamageEventListenerHolder()
        );
        this.battleCharacterUpdater = new BattleCharacterUpdater();

        this.damageShowModelHolder = new ModelHolder<>();
        DamageRenderListener damageRenderListener = new DamageRenderListener(damageShowModelHolder);
        battlefield.getDamageEventListenerHolder().addModel(damageRenderListener);
        this.damageRenderUpdater = new DamageRenderUpdater(damageShowModelHolder);

        this.movementEffectManager = new MovementEffectManager();
        this.particleSpawner = new ParticleSpawner(null);
    }

    // ========== Lifecycle ==========

    public void onEnter() {
        eventSystem.registerListener(this);
    }

    public void pause() {}
    public void resume() {}

    public void onExit() {
        eventSystem.unregisterListener(this);
        characterLifecycle.clear();
        behaviorTreeManager.clear();
        bbList.clear();
    }

    public void dispose() {
        if (projectileRenderer != null) {
            projectileRenderer.dispose();
        }
    }

    // ========== Update (事件分发由 AutoChessGameMode 统一处理，此处不重复) ==========

    public void update(float delta) {
        cameraController.update(delta);

        if (battleState.getContext().getPhase() == GamePhase.BATTLE) {
            battlePhaseManager.updateBattle(delta);
        }
    }

    // ========== Render ==========

    @Override
    public void render(RenderHolder holder) {
        game.getViewManagement().getGameViewport().apply();
        holder.getSpriteBatch().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);
        holder.getShapeRenderer().setProjectionMatrix(game.getViewManagement().getWorldCamera().combined);

        if (battleFieldRender == null) {
            battleFieldRender = new BattleFieldRender(holder.getShapeRenderer(), game, null);
        }
        if (projectileRenderer == null) {
            projectileRenderer = new ProjectileRenderer(game, holder.getShapeRenderer(), null);
        }
        if (damageLineRender == null) {
            damageLineRender = new DamageLineRender(damageShowModelHolder);
        }

        battleFieldRender.render(battlefield);

        ProjectileManager projectileManager = battlefield.getProjectileManager();
        if (projectileManager != null) {
            projectileRenderer.render(projectileManager, Gdx.graphics.getDeltaTime());
        }

        damageLineRender.render(holder.getShapeRenderer(), holder.getSpriteBatch());

        holder.getSpriteBatch().begin();
        for (BattleUnitBlackboard bb : bbList) {
            BattleCharacterRender.render(holder.getSpriteBatch(), bb.getSelf());
        }
        holder.getSpriteBatch().end();

        holder.getShapeRenderer().setAutoShapeType(true);
        holder.getShapeRenderer().begin();
        holder.getShapeRenderer().set(ShapeRenderer.ShapeType.Filled);
        for (BattleUnitBlackboard bb : bbList) {
            BattleCharacterRender.render(holder.getShapeRenderer(), bb);
        }
        holder.getShapeRenderer().end();

        BitmapFont smallFont = FontUtils.getSmallFont();
        holder.getSpriteBatch().begin();
        for (BattleUnitBlackboard bb : bbList) {
            int currentTime = (int) bb.getSelf().currentTime;
            String stateName = bb.stateMachine.getCurrent() == null ? "null" : bb.stateMachine.getCurrent().name();
            String text = String.format("[%s]:%d", stateName, currentTime);
            smallFont.draw(holder.getSpriteBatch(), text, bb.getSelf().getX(), bb.getSelf().getY());
        }
        holder.getSpriteBatch().end();

        holder.flush();
    }

    // ========== Event Handling ==========

    @Override
    public void onGameEvent(GameEvent event) {
        // 当前无需处理特定事件，BattleEndEvent 由 EconomyManager 响应
    }

    // ========== Public API ==========

    public void startBattle(int level) {
        this.currentLevel = level;
        battlePhaseManager.startBattle(level, cardPool, characterStatsLoader);
    }

    public BattleCharacter placeCharacter(Card card, float x, float y) {
        CharacterStats stats = characterStatsLoader.getStats(card.getId());
        if (stats == null) return null;
        BattleCharacter character = battlefield.placeCharacter(card, stats, x, y);
        if (character != null) {
            characterLifecycle.loadCharacter(character);
        }
        return character;
    }

    public boolean moveCharacter(BattleCharacter character, float x, float y) {
        return battlefield.moveCharacter(character, x, y);
    }

    public void removeCharacter(BattleCharacter character) {
        characterLifecycle.unloadCharacter(character);
        battlefield.removeCharacter(character);
    }

    public BattleCharacter getCharacterAt(float x, float y) {
        return battlefield.getCharacterAt(x, y);
    }

    public boolean contains(float x, float y) {
        return battlefield.contains(x, y);
    }

    public List<BattleCharacter> getPlayerCharacters() {
        return battlefield.getPlayerCharacters();
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public BattlePhaseManager getBattlePhaseManager() {
        return battlePhaseManager;
    }

    public List<BattleUnitBlackboard> getBbList() {
        return bbList;
    }

    public boolean isBattleActive() {
        return battlePhaseManager.isBattleActive();
    }

    public float getBattleTime() {
        return battlePhaseManager.getBattleTime();
    }

    public int getCurrentRound() {
        return battlePhaseManager.getCurrentRound();
    }
}
