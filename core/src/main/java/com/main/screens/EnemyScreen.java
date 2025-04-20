package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.Main;
import com.main.utils.CollisionManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class EnemyScreen implements Screen {

    private abstract static class Enemy {
        Rectangle bounds;
        int health, maxHealth, damage;
        float speed;
        boolean isDead = false;
        boolean finishedDeath = false;

        Animation<TextureRegion> runAnimation;
        Animation<TextureRegion> deathAnimation;
        float stateTime = 0f;

        float displayedHealth;
        private final float HEALTH_DECAY_RATE = 30f;

        public Enemy(float x, float y, int hp, int dmg, float speed,
                     Animation<TextureRegion> runAnimation,
                     Animation<TextureRegion> deathAnimation) {
            this.bounds = new Rectangle(x, y, 32, 32);
            this.maxHealth = hp;
            this.health = hp;
            this.damage = dmg;
            this.speed = speed;
            this.runAnimation = runAnimation;
            this.deathAnimation = deathAnimation;
            this.displayedHealth = hp;
        }

        public void update(float delta, Vector2 playerPos, CollisionManager collisionManager) {
            if (isDead) {
                stateTime += delta;
                if (deathAnimation.isAnimationFinished(stateTime)) {
                    finishedDeath = true;
                }
                return;
            }

            stateTime += delta;
            Vector2 dir = playerPos.cpy().sub(bounds.x, bounds.y);
            if (dir.len() > 2f) {
                dir.nor();
                Rectangle next = new Rectangle(bounds);
                next.x += dir.x * speed * delta;
                next.y += dir.y * speed * delta;
                if (!collisionManager.collides(next)) {
                    bounds.x += dir.x * speed * delta;
                    bounds.y += dir.y * speed * delta;
                }
            }

            if (displayedHealth > health) {
                displayedHealth -= HEALTH_DECAY_RATE * delta;
                if (displayedHealth < health) {
                    displayedHealth = health;
                }
            } else {
                displayedHealth = health;
            }
        }

        public void takeDamage(int dmg) {
            if (isDead) return;
            health -= dmg;
            if (health < 0) health = 0;
            if (health == 0) {
                isDead = true;
                stateTime = 0f;
            }
        }

        public void render(SpriteBatch batch) {
            if (finishedDeath) return;

            TextureRegion currentFrame;
            if (isDead) {
                currentFrame = deathAnimation.getKeyFrame(stateTime, false);
            } else {
                currentFrame = runAnimation.getKeyFrame(stateTime, true);
            }

            batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        public void renderHealthBar(ShapeRenderer shapeRenderer) {
            if (isDead || finishedDeath) return;

            float HEALTH_BAR_WIDTH = 24f;
            float healthPercent = displayedHealth / maxHealth;
            float offsetX = (bounds.width - HEALTH_BAR_WIDTH) / 2f;
            float offsetY = 2f;
            float barHeight = 3f;

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(bounds.x + offsetX, bounds.y + bounds.height + offsetY, HEALTH_BAR_WIDTH, barHeight);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(bounds.x + offsetX, bounds.y + bounds.height + offsetY, HEALTH_BAR_WIDTH * healthPercent, barHeight);
        }
    }

    private static class Orc extends Enemy {
        public Orc(float x, float y, int hp, int dmg, float speed,
                   Animation<TextureRegion> runAnim,
                   Animation<TextureRegion> deathAnim) {
            super(x, y, hp, dmg, speed, runAnim, deathAnim);
        }
    }

    private static class Skeleton extends Enemy {
        public Skeleton(float x, float y, int hp, int dmg, float speed,
                        Animation<TextureRegion> runAnim,
                        Animation<TextureRegion> deathAnim) {
            super(x, y, hp, dmg, speed, runAnim, deathAnim);
        }
    }

    private static class WaveManager {
        private final List<Enemy> enemies;
        private final Queue<Runnable> waveQueue = new LinkedList<>();
        private long lastSpawnTime;
        private boolean waitingForClear = false;

        public WaveManager(List<Enemy> list) {
            this.enemies = list;
        }

        public void queueWave(Runnable waveLogic) {
            waveQueue.add(waveLogic);
        }

        public void update() {
            if (!waitingForClear && TimeUtils.timeSinceMillis(lastSpawnTime) > 1000 && !waveQueue.isEmpty()) {
                waveQueue.poll().run();
                waitingForClear = true;
            }

            if (waitingForClear && enemies.stream().noneMatch(e -> !e.isDead)) {
                waitingForClear = false;
                lastSpawnTime = TimeUtils.millis();
            }
        }
    }


    private Stage stage;
    private Skin skin;

    private boolean dialogActive = false;
    private int currentLineIndex = 0;
    private String[] dialogLines;
    private String npcName = "";

    private Stage dialogStage;
    private Window dialogWindow;
    private Label dialogLabel;

    private float typeTimer = 0f;
    private float typeInterval = 0.05f;
    private String fullText = "";
    private StringBuilder visibleText = new StringBuilder();
    private boolean isTyping = false;

    private List<NPC> npcs;
    private boolean rogueTalkedTo = false;
    private boolean wizardTalkedTo = false;
    private boolean knightTalkedTo = false;

    private final String[] rogueDialog = {
        "I don’t know you, and I don’t care to know about you.",
        "But you smell like... caffeine. That’s rare down here.",
        "Tell you what—",
        "Take care of those Byte Imps. Nasty things been gobbling at data clusters and corrupting them.",
        "Once you're done, find the wizard"
    };

    private final String[] wizardDialog = {
        "Well I’m sick of it. Sick and tired, tired and sick.",
        "And perhaps a little drunk. But nevermind.",
        "You look like the honest sort, so I've a job for you.",
        "You're going to travel to the South— yes, the one behind the jammed port gate —",
        "and deal with those Trojan skeletons.",
        "They’ve been clogging the corridors with junk data and bottlenecks.",
        "I haven’t been able to leave this blasted cell for a fresh flask of.... rootbrew.",
        "There's fifty gold pieces in it for you, friend... right then. Off you go."
    };

    private final String[] knightDialog = {
        "I once guarded these halls with blade and code.",
        "The Gatekeeper... he was my comrade, until the breach twisted him.",
        "He struck me down when I refused to fall with him.",
        "Now this wound won’t let me finish what I started.",
        "But you—your system’s still clean. Finish it. End him."
    };
    
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CollisionManager collisionManager;
    private Rectangle player;
    private Vector2 playerPos = new Vector2();
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    private List<Enemy> enemies = new ArrayList<>();
    private WaveManager waveManager;
    private Main game;
    public EnemyScreen(Main game) {
        this.game = game;
    }
    private long startTime;
    private long elapsedTime;
    
    private Animation<TextureRegion> playerIdle;
    private Animation<TextureRegion> playerRun;
    private Animation<TextureRegion> playerAttack;
    private Animation<TextureRegion> playerHurt;
    private Animation<TextureRegion> playerDie;
    private float playerStateTime = 0f;
    private boolean isAttacking = false;
    private boolean isHurt = false;
    private boolean isDead = false;
    private int playerHealth = 100;
    private int playerMaxHealth = 100;
    private float playerDisplayedHealth = 100;
    private final float PLAYER_HEALTH_DECAY_RATE = 50f;
    private long lastDamageTime = 0;
    private final long DAMAGE_COOLDOWN = 1000; // 1 second between damage ticks

    private long lastAttackTime = 0;
    private final long ATTACK_COOLDOWN = 750; 
    private final float ATTACK_RANGE = 20f; // Increased range for better gameplay
    private final int ATTACK_DAMAGE = 15; // Increased damage to kill enemies faster with attacks
    
    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/dungeon.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        startTime = TimeUtils.nanoTime();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.zoom = .35f;  // Adjust this value for the desired zoom level

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        collisionManager = new CollisionManager(map, "Collision");

        MapObject start = map.getLayers().get("PlayerStart").getObjects().get("PlayerStart");
        Rectangle startRect = ((RectangleMapObject) start).getRectangle();

        player = new Rectangle(startRect.x, startRect.y, 32, 32);
        playerPos.set(player.x, player.y);
        
        
        npcs = new ArrayList<>();
        npcs.add(new NPC("Rogue", "Pixel Crawler - Free Pack 2.0.4\\Pixel Crawler - Free Pack\\Entities\\Npc's\\Rogue\\Idle\\Idle-Sheet.png", 200, 350,32,32, rogueDialog));
        npcs.add(new NPC("Wizard", "Pixel Crawler - Free Pack 2.0.4\\Pixel Crawler - Free Pack\\Entities\\Npc's\\Wizzard\\Idle\\Idle-Sheet.png", 390, 350, 32, 32, wizardDialog));
        npcs.add(new NPC("Knight", "Pixel Crawler - Free Pack 2.0.4\\Pixel Crawler - Free Pack\\Entities\\Npc's\\Knight\\Idle\\Idle-Sheet.png", 390, 140, 32, 32, knightDialog));

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        dialogStage = new Stage(new ScreenViewport());
        dialogLabel = new Label("", skin);
        dialogLabel.setWrap(true);
        dialogLabel.setAlignment(Align.topLeft);

        dialogWindow = new Window("Dialog", skin);
        dialogWindow.add(dialogLabel).width(400).pad(10).row();
        dialogWindow.pack();
        dialogWindow.setVisible(false);
        dialogWindow.setPosition((Gdx.graphics.getWidth() - dialogWindow.getWidth()) / 2f, 50);
        
        dialogStage.addActor(dialogWindow);
        
        loadPlayerAnimations();
        loadAnimations();
        waveManager = new WaveManager(enemies);
        initWaves();
    }

    private void loadPlayerAnimations() {
        // Replace these paths with your actual NightBorne animation paths
        playerIdle = loadAnimation("sprites/MainCharacter/NightBorne_idle.png", 9, 714, 57);
        playerRun = loadAnimation("sprites/MainCharacter/NightBorne_run.png", 6, 468, 59);
        playerAttack = loadAnimation("sprites/MainCharacter/NightBorne_attack.png", 12, 937, 76);
        playerAttack.setFrameDuration(0.02f);
        playerHurt = loadAnimation("sprites/NightBorne/NightBorne_hurt.png", 5, 387, 60);
        playerDie = loadAnimation("sprites/NightBorne/NightBorne_die.png", 23, 1808, 70);
        playerDie.setFrameDuration(0.10f);
    }

    Animation<TextureRegion> baseOrcRun, baseOrcDeath;
    Animation<TextureRegion> rogueOrcRun, rogueOrcDeath;
    Animation<TextureRegion> warriorOrcRun, warriorOrcDeath;
    Animation<TextureRegion> baseSkeletonRun, baseSkeletonDeath;
    Animation<TextureRegion> rogueSkeletonRun, rogueSkeletonDeath;
    Animation<TextureRegion> warriorSkeletonRun, warriorSkeletonDeath;

    private void loadAnimations() {
        baseOrcRun = loadAnimation("sprites/Orc/baseRun/Run-Sheet.png", 6, 384, 64);
        baseOrcDeath = loadAnimation("sprites/Orc/baseorcDeath/Death-Sheet.png", 6, 384, 64);
        rogueOrcRun = loadAnimation("sprites/Orc/rogueRun/Run-Sheet.png", 6, 384, 64);
        rogueOrcDeath = loadAnimation("sprites/Orc/rogueDeath/Death-Sheet.png", 6, 384, 64);
        warriorOrcRun = loadAnimation("sprites/Orc/warriorRun/Run-Sheet.png", 6, 384, 64);
        warriorOrcDeath = loadAnimation("sprites/Orc/warriororcDeath/Death-Sheet.png", 6, 576, 80);
        baseSkeletonRun = loadAnimation("sprites/Skeletons/baseRun/Run-Sheet.png", 6, 384, 64);
        baseSkeletonDeath = loadAnimation("sprites/Skeletons/baseskelDeath/Death-Sheet.png", 8, 768, 64);
        rogueSkeletonRun = loadAnimation("sprites/Skeletons/rogueRun/Run-Sheet.png", 6, 384, 64);
        rogueSkeletonDeath = loadAnimation("sprites/Skeletons/rogueskelDeath/Death-Sheet.png", 6, 384, 64);
        warriorSkeletonRun = loadAnimation("sprites/Skeletons/warriorRun/Run-Sheet.png", 6, 384, 64);
        warriorSkeletonDeath = loadAnimation("sprites/Skeletons/warriorskelDeath/Death-Sheet.png", 6, 384, 48);
    }

    private Animation<TextureRegion> loadAnimation(String path, int frameCount, int totalWidth, int totalHeight) {
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            int frameWidth = totalWidth / frameCount;
            TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, totalHeight);
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = tmp[0][i];
            }
            return new Animation<>(0.1f, frames);
        } catch (Exception e) {
            Texture fallback = new Texture(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            TextureRegion fallbackRegion = new TextureRegion(fallback);
            return new Animation<>(0.1f, fallbackRegion);
        }
    }

    private void initWaves() {
        initRogueWaves();
        initWizardWaves();
        initKnightWaves();
    }
    
    private void initRogueWaves() {
        Rectangle r1 = ((RectangleMapObject) map.getLayers().get("Enemy1").getObjects().get("Enemy1")).getRectangle();
        
        waveManager.queueWave(() -> {
        	System.out.println("Rogue wave condition: " + rogueTalkedTo);
            if (rogueTalkedTo) {
                enemies.add(new Orc(r1.x, r1.y, 10, 1, 30f, baseOrcRun, baseOrcDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (rogueTalkedTo) {
                enemies.add(new Orc(r1.x - 20, r1.y, 10, 10, 30f, baseOrcRun, baseOrcDeath));
                enemies.add(new Orc(r1.x + 20, r1.y, 10, 10, 30f, baseOrcRun, baseOrcDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (rogueTalkedTo) {
                enemies.add(new Orc(r1.x + 30, r1.y, 15, 3, 35f, rogueOrcRun, rogueOrcDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (rogueTalkedTo) {
                enemies.add(new Orc(r1.x + 40, r1.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath));
            }
        });
    }

    private void initWizardWaves() {
        Rectangle r2 = ((RectangleMapObject) map.getLayers().get("Enemy2").getObjects().get("Enemy2")).getRectangle();
        
        waveManager.queueWave(() -> {
            if (wizardTalkedTo) {
                enemies.add(new Skeleton(r2.x, r2.y, 8, 2, 25f, baseSkeletonRun, baseSkeletonDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (wizardTalkedTo) {
                enemies.add(new Skeleton(r2.x - 20, r2.y, 8, 2, 25f, baseSkeletonRun, baseSkeletonDeath));
                enemies.add(new Skeleton(r2.x + 20, r2.y, 12, 4, 30f, rogueSkeletonRun, rogueSkeletonDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (wizardTalkedTo) {
                enemies.add(new Skeleton(r2.x, r2.y + 20, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath));
            }
        });
    }

    private void initKnightWaves() {
        Rectangle r4 = ((RectangleMapObject) map.getLayers().get("Enemy4").getObjects().get("Enemy4")).getRectangle();
        
        waveManager.queueWave(() -> {
            if (knightTalkedTo) {
                enemies.add(new Orc(r4.x - 30, r4.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath));
                enemies.add(new Orc(r4.x + 30, r4.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath));
            }
        });
        waveManager.queueWave(() -> {
            if (knightTalkedTo) {
                enemies.add(new Skeleton(r4.x - 30, r4.y, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath));
                enemies.add(new Skeleton(r4.x + 30, r4.y, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath));
            }
        });
        
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderPlayer(batch);

        for (Enemy e : enemies) {
            e.render(batch);
        }
        for (NPC npc : npcs) {
            npc.draw(batch);
        }

        if (dialogActive) {
            dialogStage.act(delta);
            dialogStage.draw();
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderPlayerHealthBar(shapeRenderer);

        for (Enemy e : enemies) {
            e.renderHealthBar(shapeRenderer);
        }
        shapeRenderer.end();
    }

    private void renderPlayer(SpriteBatch batch) {
        if (isDead) {
            TextureRegion frame = playerDie.getKeyFrame(playerStateTime, false);
            batch.draw(frame, player.x, player.y, player.width, player.height);
            game.setScreen(new GameOverScreen(game)); 
            dispose();
            return;
        }

        TextureRegion currentFrame;
        if (isAttacking) {
            currentFrame = playerAttack.getKeyFrame(playerStateTime, false);
            if (playerAttack.isAnimationFinished(playerStateTime)) {
                isAttacking = false;
            }
        } else if (isHurt) {
            currentFrame = playerHurt.getKeyFrame(playerStateTime, false);
            if (playerHurt.isAnimationFinished(playerStateTime)) {
                isHurt = false;
            }
        } else if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A) ||
            Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.D)) {
            currentFrame = playerRun.getKeyFrame(playerStateTime, true);
        } else {
            currentFrame = playerIdle.getKeyFrame(playerStateTime, true);
        }

        batch.draw(currentFrame, player.x, player.y, player.width, player.height);
    }

    private void renderPlayerHealthBar(ShapeRenderer shapeRenderer) {
        if (isDead) return;

        float HEALTH_BAR_WIDTH = 32f;
        float healthPercent = playerDisplayedHealth / playerMaxHealth;
        float offsetY = 5f;
        float barHeight = 5f;

        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(player.x, player.y + player.height + offsetY, HEALTH_BAR_WIDTH, barHeight);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(player.x, player.y + player.height + offsetY, HEALTH_BAR_WIDTH * healthPercent, barHeight);
    }

    private void startDialog() {
        currentLineIndex = 0;
        startTyping(dialogLines[currentLineIndex]);
        dialogWindow.setVisible(true);
        dialogWindow.getTitleLabel().setText(npcName);
        dialogActive = true;
    }

    private void startTyping(String text) {
        fullText = text;
        visibleText.setLength(0);
        typeTimer = 0f;
        isTyping = true;
        dialogLabel.setText("");
    }

    private void update(float delta) {
        playerStateTime += delta;
       
        playerPos.set(player.x, player.y);
        

        if (!dialogActive) {
            Rectangle next = new Rectangle(player);
            handleInput(delta);
            if (Gdx.input.isKeyJustPressed(Keys.E)) {
                for (NPC npc : npcs) {
                    if (player.overlaps(npc.getBounds())) {
                        dialogLines = npc.getDialogLines();
                        npcName = npc.getName();
                        startDialog();
                        break;
                    }
                }
            }
        } else {
            if (isTyping) {
                typeTimer += delta;
                while (typeTimer >= typeInterval && visibleText.length() < fullText.length()) {
                    visibleText.append(fullText.charAt(visibleText.length()));
                    dialogLabel.setText(visibleText.toString());
                    typeTimer -= typeInterval;
                }

                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    if (visibleText.length() < fullText.length()) {
                        visibleText.setLength(0);
                        visibleText.append(fullText);
                        dialogLabel.setText(fullText);
                        isTyping = false;
                    } else {
                        currentLineIndex++;
                        if (currentLineIndex < dialogLines.length) {
                            startTyping(dialogLines[currentLineIndex]);
                        } else {
                            // Dialog is fully completed - set the appropriate flag
                            if (npcName.equals("Rogue")) {
                                rogueTalkedTo = true;
                            } else if (npcName.equals("Wizard")) {
                                wizardTalkedTo = true;
                            } else if (npcName.equals("Knight")) {
                                knightTalkedTo = true;
                            }
                            dialogActive = false;
                            dialogWindow.setVisible(false);
                        }
                    }
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    currentLineIndex++;
                    if (currentLineIndex < dialogLines.length) {
                        startTyping(dialogLines[currentLineIndex]);
                    } else {
                    	System.out.println(npcName);
                        // Dialog is fully completed - set the appropriate flag
                        if (npcName.equals("Rogue")) {
                            rogueTalkedTo = true;
                            initRogueWaves();
                        } else if (npcName.equals("Wizard")) {
                            wizardTalkedTo = true;
                            initWizardWaves();
                        } else if (npcName.equals("Knight")) {
                            knightTalkedTo = true;
                            initKnightWaves();
                        }
                        System.out.println(rogueTalkedTo);
                        dialogActive = false;
                        dialogWindow.setVisible(false);
                    }
                }
            }
        }


        // Update all enemies and check for collisions
        for (Enemy e : enemies) {
            e.update(delta, playerPos, collisionManager);

            // Check if enemy is touching player and damage player
            if (!e.isDead && !isDead && e.bounds.overlaps(player) &&
                TimeUtils.timeSinceMillis(lastDamageTime) > DAMAGE_COOLDOWN) {
                playerHealth -= e.damage;
                if (playerHealth < 0) playerHealth = 0;
                isHurt = true;
                playerStateTime = 0f;
                lastDamageTime = TimeUtils.millis();

                if (playerHealth <= 0) {
                    isDead = true;
                    playerStateTime = 0f;
                }
            }
        }

        // Update displayed health with smooth decay
        if (playerDisplayedHealth > playerHealth) {
            playerDisplayedHealth -= PLAYER_HEALTH_DECAY_RATE * delta;
            if (playerDisplayedHealth < playerHealth) {
                playerDisplayedHealth = playerHealth;
            }
        } else {
            playerDisplayedHealth = playerHealth;
        }

        waveManager.update();
        if (allEnemiesDead()) {
        	elapsedTime = TimeUtils.nanoTime() - startTime;  // Calculate elapsed time
	        long elapsedTimeInSeconds = elapsedTime / 1000000000L; // Convert to seconds
	        game.setScreen(new WinScreen(game, elapsedTimeInSeconds));
            dispose();
            return;
        }
    }
    
    private boolean allEnemiesDead() {
        // Check if there are any enemies that aren't finished with their death animations
        for (Enemy enemy : enemies) {
            if (!enemy.finishedDeath) {
                return false;
            }
        }
        
        // Also check if the wave manager has no more waves and all enemies are dead
        return waveManager.waveQueue.isEmpty() && 
               enemies.stream().allMatch(e -> e.finishedDeath) &&
               (rogueTalkedTo && wizardTalkedTo && knightTalkedTo); // Only win if player has talked to at least one NPC
    }

    private void handleInput(float delta) {
        if (isDead) return; 

        float moveSpeed = 150 * delta;
        Rectangle next = new Rectangle(player);
        if (Gdx.input.isKeyPressed(Keys.W)) next.y += moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.S)) next.y -= moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.A)) next.x -= moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.D)) next.x += moveSpeed;
        if (!collisionManager.collides(next)) {
            player.setPosition(next.x, next.y);
        }
        
        // Space bar attack with cool down
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && TimeUtils.timeSinceMillis(lastAttackTime) > ATTACK_COOLDOWN) {
            isAttacking = true;
            playerStateTime = 0f;
            attack();
            lastAttackTime = TimeUtils.millis();
        }
    }

    private void attack() {
        // Attack all enemies within range
        for (Enemy enemy : enemies) {
            if (!enemy.isDead && isInAttackRange(enemy)) {
                enemy.takeDamage(ATTACK_DAMAGE);
            }
        }
    }

    private boolean isInAttackRange(Enemy enemy) {
        // Calculate distance between player and enemy centers
        float playerCenterX = player.x + player.width/2;
        float playerCenterY = player.y + player.height/2;
        float enemyCenterX = enemy.bounds.x + enemy.bounds.width/2;
        float enemyCenterY = enemy.bounds.y + enemy.bounds.height/2;

        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);

        return distance <= ATTACK_RANGE;
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        map.dispose();
//        mapRenderer.dispose();
//       batch.dispose();
//        shapeRenderer.dispose();
        playerIdle.getKeyFrames()[0].getTexture().dispose();
        playerRun.getKeyFrames()[0].getTexture().dispose();
        playerAttack.getKeyFrames()[0].getTexture().dispose();
        playerHurt.getKeyFrames()[0].getTexture().dispose();
        playerDie.getKeyFrames()[0].getTexture().dispose();
        baseOrcRun.getKeyFrames()[0].getTexture().dispose();
        rogueOrcRun.getKeyFrames()[0].getTexture().dispose();
        warriorOrcRun.getKeyFrames()[0].getTexture().dispose();
        baseSkeletonRun.getKeyFrames()[0].getTexture().dispose();
        rogueSkeletonRun.getKeyFrames()[0].getTexture().dispose();
        warriorSkeletonRun.getKeyFrames()[0].getTexture().dispose();
    }
}