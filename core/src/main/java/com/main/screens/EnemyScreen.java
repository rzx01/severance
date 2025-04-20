package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
// <<<<<<< nia
// =======
import com.badlogic.gdx.graphics.Color;
// >>>>>>> main
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
// <<<<<<< nia
// import com.badlogic.gdx.graphics.g2d.Sprite;
// =======
// >>>>>>> main
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
import com.badlogic.gdx.utils.TimeUtils;
import com.main.utils.CollisionManager;

// <<<<<<< nia
// import java.util.*;
// =======
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
// >>>>>>> main

public class EnemyScreen implements Screen {

    private abstract static class Enemy {
        Rectangle bounds;
        int health, maxHealth, damage;
        float speed;
        boolean isDead = false;
// <<<<<<< nia

//         Animation<TextureRegion> runAnimation;
//         float stateTime = 0f;

//         public Enemy(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> runAnimation) {
//             this.bounds = new Rectangle(x, y, 16, 16);
// =======
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
// >>>>>>> main
            this.maxHealth = hp;
            this.health = hp;
            this.damage = dmg;
            this.speed = speed;
            this.runAnimation = runAnimation;
// <<<<<<< nia
//         }

//         public void update(float delta, Vector2 playerPos, CollisionManager collisionManager) {
//             if (isDead) return;

//             stateTime += delta;

// =======
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
// >>>>>>> main
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
// <<<<<<< nia
//         }

//         public void takeDamage(int dmg) {
//             health -= dmg;
//             if (health <= 0) {
//                 isDead = true;
//             }
//         }

//         public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
//             if (isDead) return;

//             TextureRegion currentFrame = runAnimation.getKeyFrame(stateTime, true);
//             batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height);

//             // Health bar
//             float barWidth = bounds.width;
//             float healthPercent = (float) health / maxHealth;
//             shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//             shapeRenderer.setColor(1, 0, 0, 1);
//             shapeRenderer.rect(bounds.x, bounds.y + bounds.height + 2, barWidth, 3);
//             shapeRenderer.setColor(0, 1, 0, 1);
//             shapeRenderer.rect(bounds.x, bounds.y + bounds.height + 2, barWidth * healthPercent, 3);
//             shapeRenderer.end();

// =======

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
// >>>>>>> main
        }
    }

    private static class Orc extends Enemy {
// <<<<<<< nia
//         public Orc(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> anim) {
//             super(x, y, hp, dmg, speed, anim);
// =======
        public Orc(float x, float y, int hp, int dmg, float speed,
                   Animation<TextureRegion> runAnim,
                   Animation<TextureRegion> deathAnim) {
            super(x, y, hp, dmg, speed, runAnim, deathAnim);
// >>>>>>> main
        }
    }

    private static class Skeleton extends Enemy {
// <<<<<<< nia
//         public Skeleton(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> anim) {
//             super(x, y, hp, dmg, speed, anim);
// =======
        public Skeleton(float x, float y, int hp, int dmg, float speed,
                        Animation<TextureRegion> runAnim,
                        Animation<TextureRegion> deathAnim) {
            super(x, y, hp, dmg, speed, runAnim, deathAnim);
// >>>>>>> main
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
// <<<<<<< nia

// =======
// >>>>>>> main
            if (waitingForClear && enemies.stream().noneMatch(e -> !e.isDead)) {
                waitingForClear = false;
                lastSpawnTime = TimeUtils.millis();
            }
        }
    }

    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CollisionManager collisionManager;
// <<<<<<< nia

// =======
// >>>>>>> main
    private Rectangle player;
    private Vector2 playerPos = new Vector2();
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
// <<<<<<< nia

//     private List<Enemy> enemies = new ArrayList<>();
//     private WaveManager waveManager;

//     // Animation assets
//     private Animation<TextureRegion> baseOrcRun, rogueOrcRun, warriorOrcRun;
//     private Animation<TextureRegion> baseSkeletonRun, rogueSkeletonRun, warriorSkeletonRun;
// =======
    private List<Enemy> enemies = new ArrayList<>();
    private WaveManager waveManager;

    // Player variables
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

    // Attack variables
    private long lastAttackTime = 0;
    private final long ATTACK_COOLDOWN = 500; // 500ms cooldown between attacks
    private final float ATTACK_RANGE = 20f; // Increased range for better gameplay
    private final int ATTACK_DAMAGE = 10; // Increased damage to kill enemies faster with attacks
// >>>>>>> main

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/dungeon.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        collisionManager = new CollisionManager(map, "Collision");

        MapObject start = map.getLayers().get("PlayerStart").getObjects().get("PlayerStart");
        Rectangle startRect = ((RectangleMapObject) start).getRectangle();
// <<<<<<< nia
//         player = new Rectangle(startRect.x, startRect.y, 32, 32);
//         playerPos.set(player.x, player.y);

// =======
        player = new Rectangle(startRect.x, startRect.y, 32, 32); // Increased size for character
        playerPos.set(player.x, player.y);

        loadPlayerAnimations();
// >>>>>>> main
        loadAnimations();
        waveManager = new WaveManager(enemies);
        initWaves();
    }

// <<<<<<< nia
//     private void loadAnimations() {
//     	baseOrcRun = loadRunAnimation("sprites/Orc/baseRun/Run-Sheet.png");
//         rogueOrcRun = loadRunAnimation("sprites/Orc/rogueRun/Run-Sheet.png");
//         warriorOrcRun = loadRunAnimation("sprites/Orc/warriorRun/Run-Sheet.png");

//         baseSkeletonRun = loadRunAnimation("sprites/Skeletons/baseRun/Run-Sheet.png");
//         rogueSkeletonRun = loadRunAnimation("sprites/Skeletons/rogueRun/Run-Sheet.png");
//         warriorSkeletonRun = loadRunAnimation("sprites/Skeletons/warriorRun/Run-Sheet.png");
//     }

//     private Animation<TextureRegion> loadRunAnimation(String path) {
//         Texture texture = new Texture(Gdx.files.internal(path)); // Fix: use the given path
//         TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
//         TextureRegion[] frames = Arrays.copyOf(tmp[0], tmp[0].length);
//         return new Animation<>(0.1f, frames);
//     }

// =======
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
// >>>>>>> main

    private void initWaves() {
        Rectangle r1 = ((RectangleMapObject) map.getLayers().get("Enemy1").getObjects().get("Enemy1")).getRectangle();
        Rectangle r2 = ((RectangleMapObject) map.getLayers().get("Enemy2").getObjects().get("Enemy2")).getRectangle();
        Rectangle r4 = ((RectangleMapObject) map.getLayers().get("Enemy4").getObjects().get("Enemy4")).getRectangle();

// <<<<<<< nia
//         // Room 1 - Orcs
//         waveManager.queueWave(() -> enemies.add(new Orc(r1.x, r1.y, 10, 2, 30f, baseOrcRun)));
//         waveManager.queueWave(() -> {
//             enemies.add(new Orc(r1.x - 20, r1.y, 10, 2, 30f, baseOrcRun));
//             enemies.add(new Orc(r1.x + 20, r1.y, 10, 2, 30f, baseOrcRun));
//         });
//         waveManager.queueWave(() -> enemies.add(new Orc(r1.x + 40, r1.y, 15, 3, 35f, rogueOrcRun)));
//         waveManager.queueWave(() -> enemies.add(new Orc(r1.x - 40, r1.y, 20, 4, 40f, warriorOrcRun)));

//         // Room 2 - Skeletons
//         waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y, 8, 2, 25f, baseSkeletonRun)));
//         waveManager.queueWave(() -> {
//             enemies.add(new Skeleton(r2.x - 20, r2.y, 8, 2, 25f, baseSkeletonRun));
//             enemies.add(new Skeleton(r2.x + 20, r2.y, 12, 3, 30f, rogueSkeletonRun));
//         });
//         waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y + 20, 18, 4, 35f, warriorSkeletonRun)));

// //         Room 4 - Boss (2 Warrior Orcs + 2 Warrior Skeletons)
//         waveManager.queueWave(() -> {
//             enemies.add(new Orc(r4.x - 30, r4.y, 20, 4, 40f, warriorOrcRun));
//             enemies.add(new Orc(r4.x + 30, r4.y, 20, 4, 40f, warriorOrcRun));
//             enemies.add(new Skeleton(r4.x, r4.y - 30, 18, 4, 35f, warriorSkeletonRun));
//             enemies.add(new Skeleton(r4.x, r4.y + 30, 18, 4, 35f, warriorSkeletonRun));
// =======
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x, r1.y, 10, 1, 30f, baseOrcRun, baseOrcDeath)));
        waveManager.queueWave(() -> {
            enemies.add(new Orc(r1.x - 20, r1.y, 10, 1, 30f, baseOrcRun, baseOrcDeath));
            enemies.add(new Orc(r1.x + 20, r1.y, 10, 1, 30f, baseOrcRun, baseOrcDeath));
        });
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x + 30, r1.y, 15, 3, 35f, rogueOrcRun, rogueOrcDeath)));
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x + 40, r1.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath)));
        waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y, 8, 2, 25f, baseSkeletonRun, baseSkeletonDeath)));
        waveManager.queueWave(() -> {
            enemies.add(new Skeleton(r2.x - 20, r2.y, 8, 2, 25f, baseSkeletonRun, baseSkeletonDeath));
            enemies.add(new Skeleton(r2.x + 20, r2.y, 12, 4, 30f, rogueSkeletonRun, rogueSkeletonDeath));
        });
        waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y + 20, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath)));
        waveManager.queueWave(() -> {
            enemies.add(new Orc(r4.x - 30, r4.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath));
            enemies.add(new Orc(r4.x + 30, r4.y, 20, 5, 40f, warriorOrcRun, warriorOrcDeath));
        });
        waveManager.queueWave(() -> {
            enemies.add(new Skeleton(r4.x - 30, r4.y, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath));
            enemies.add(new Skeleton(r4.x + 30, r4.y, 18, 6, 35f, warriorSkeletonRun, warriorSkeletonDeath));
// >>>>>>> main
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
// <<<<<<< nia
//         shapeRenderer.setProjectionMatrix(camera.combined);

//         batch.begin();
//         for (Enemy e : enemies) e.render(batch, shapeRenderer);
//         batch.end();

//         shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//         shapeRenderer.setColor(0, 1, 0, 1);
//         shapeRenderer.rect(player.x, player.y, player.width, player.height);
//         shapeRenderer.end();
//     }

//     private void update(float delta) {
//         float speed = 200 * delta;
//         Rectangle next = new Rectangle(player);

//         if (Gdx.input.isKeyPressed(Keys.W)) {
//             next.y += speed;
//             if (!collisionManager.collides(next)) player.y += speed;
//         }
//         if (Gdx.input.isKeyPressed(Keys.S)) {
//             next.y -= speed;
//             if (!collisionManager.collides(next)) player.y -= speed;
//         }
//         if (Gdx.input.isKeyPressed(Keys.A)) {
//             next.x -= speed;
//             if (!collisionManager.collides(next)) player.x -= speed;
//         }
//         if (Gdx.input.isKeyPressed(Keys.D)) {
//             next.x += speed;
//             if (!collisionManager.collides(next)) player.x += speed;
//         }

//         playerPos.set(player.x, player.y);

//         waveManager.update();
//         for (Enemy e : enemies) {
//             e.update(delta, playerPos, collisionManager);
//             if (!e.isDead && e.bounds.overlaps(player)) {
//                 e.takeDamage(10); // Simulate player attack
//             }
//         }
//     }

//     @Override public void resize(int width, int height) {}
//     @Override public void pause() {}
//     @Override public void resume() {}
//     @Override public void hide() {}

//     @Override
//     public void dispose() {
//         batch.dispose();
//         shapeRenderer.dispose();
//         map.dispose();
//         mapRenderer.dispose();
// =======
        batch.begin();
        // Draw player
        renderPlayer(batch);

        // Draw enemies
        for (Enemy e : enemies) {
            e.render(batch);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Draw player health bar
        renderPlayerHealthBar(shapeRenderer);

        // Draw enemy health bars
        for (Enemy e : enemies) {
            e.renderHealthBar(shapeRenderer);
        }
        shapeRenderer.end();
    }

    private void renderPlayer(SpriteBatch batch) {
        if (isDead) {
            TextureRegion frame = playerDie.getKeyFrame(playerStateTime, false);
            batch.draw(frame, player.x, player.y, player.width, player.height);
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

    private void update(float delta) {
        playerStateTime += delta;
        handleInput(delta);
        playerPos.set(player.x, player.y);

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
    }

    private void handleInput(float delta) {
        if (isDead) return; // Can't move if dead

        float moveSpeed = 200 * delta;
        Rectangle next = new Rectangle(player);
        if (Gdx.input.isKeyPressed(Keys.W)) next.y += moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.S)) next.y -= moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.A)) next.x -= moveSpeed;
        if (Gdx.input.isKeyPressed(Keys.D)) next.x += moveSpeed;
        if (!collisionManager.collides(next)) {
            player.setPosition(next.x, next.y);
        }

        // Space bar attack with cooldown
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
        mapRenderer.dispose();
        batch.dispose();
        shapeRenderer.dispose();
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
// >>>>>>> main
    }
}
