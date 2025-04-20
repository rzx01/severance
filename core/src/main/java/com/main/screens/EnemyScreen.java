package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
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

import java.util.*;

public class EnemyScreen implements Screen {

    private abstract static class Enemy {
        Rectangle bounds;
        int health, maxHealth, damage;
        float speed;
        boolean isDead = false;

        Animation<TextureRegion> runAnimation;
        float stateTime = 0f;

        public Enemy(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> runAnimation) {
            this.bounds = new Rectangle(x, y, 16, 16);
            this.maxHealth = hp;
            this.health = hp;
            this.damage = dmg;
            this.speed = speed;
            this.runAnimation = runAnimation;
        }

        public void update(float delta, Vector2 playerPos, CollisionManager collisionManager) {
            if (isDead) return;

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
        }

        public void takeDamage(int dmg) {
            health -= dmg;
            if (health <= 0) {
                isDead = true;
            }
        }

        public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
            if (isDead) return;

            TextureRegion currentFrame = runAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height);

            // Health bar
            float barWidth = bounds.width;
            float healthPercent = (float) health / maxHealth;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(bounds.x, bounds.y + bounds.height + 2, barWidth, 3);
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(bounds.x, bounds.y + bounds.height + 2, barWidth * healthPercent, 3);
            shapeRenderer.end();

        }
    }

    private static class Orc extends Enemy {
        public Orc(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> anim) {
            super(x, y, hp, dmg, speed, anim);
        }
    }

    private static class Skeleton extends Enemy {
        public Skeleton(float x, float y, int hp, int dmg, float speed, Animation<TextureRegion> anim) {
            super(x, y, hp, dmg, speed, anim);
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

    // Animation assets
    private Animation<TextureRegion> baseOrcRun, rogueOrcRun, warriorOrcRun;
    private Animation<TextureRegion> baseSkeletonRun, rogueSkeletonRun, warriorSkeletonRun;

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
        player = new Rectangle(startRect.x, startRect.y, 32, 32);
        playerPos.set(player.x, player.y);

        loadAnimations();
        waveManager = new WaveManager(enemies);
        initWaves();
    }

    private void loadAnimations() {
    	baseOrcRun = loadRunAnimation("sprites/Orc/baseRun/Run-Sheet.png");
        rogueOrcRun = loadRunAnimation("sprites/Orc/rogueRun/Run-Sheet.png");
        warriorOrcRun = loadRunAnimation("sprites/Orc/warriorRun/Run-Sheet.png");

        baseSkeletonRun = loadRunAnimation("sprites/Skeletons/baseRun/Run-Sheet.png");
        rogueSkeletonRun = loadRunAnimation("sprites/Skeletons/rogueRun/Run-Sheet.png");
        warriorSkeletonRun = loadRunAnimation("sprites/Skeletons/warriorRun/Run-Sheet.png");
    }

    private Animation<TextureRegion> loadRunAnimation(String path) {
        Texture texture = new Texture(Gdx.files.internal(path)); // Fix: use the given path
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);
        TextureRegion[] frames = Arrays.copyOf(tmp[0], tmp[0].length);
        return new Animation<>(0.1f, frames);
    }


    private void initWaves() {
        Rectangle r1 = ((RectangleMapObject) map.getLayers().get("Enemy1").getObjects().get("Enemy1")).getRectangle();
        Rectangle r2 = ((RectangleMapObject) map.getLayers().get("Enemy2").getObjects().get("Enemy2")).getRectangle();
        Rectangle r4 = ((RectangleMapObject) map.getLayers().get("Enemy4").getObjects().get("Enemy4")).getRectangle();

        // Room 1 - Orcs
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x, r1.y, 10, 2, 30f, baseOrcRun)));
        waveManager.queueWave(() -> {
            enemies.add(new Orc(r1.x - 20, r1.y, 10, 2, 30f, baseOrcRun));
            enemies.add(new Orc(r1.x + 20, r1.y, 10, 2, 30f, baseOrcRun));
        });
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x + 40, r1.y, 15, 3, 35f, rogueOrcRun)));
        waveManager.queueWave(() -> enemies.add(new Orc(r1.x - 40, r1.y, 20, 4, 40f, warriorOrcRun)));

        // Room 2 - Skeletons
        waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y, 8, 2, 25f, baseSkeletonRun)));
        waveManager.queueWave(() -> {
            enemies.add(new Skeleton(r2.x - 20, r2.y, 8, 2, 25f, baseSkeletonRun));
            enemies.add(new Skeleton(r2.x + 20, r2.y, 12, 3, 30f, rogueSkeletonRun));
        });
        waveManager.queueWave(() -> enemies.add(new Skeleton(r2.x, r2.y + 20, 18, 4, 35f, warriorSkeletonRun)));

//         Room 4 - Boss (2 Warrior Orcs + 2 Warrior Skeletons)
        waveManager.queueWave(() -> {
            enemies.add(new Orc(r4.x - 30, r4.y, 20, 4, 40f, warriorOrcRun));
            enemies.add(new Orc(r4.x + 30, r4.y, 20, 4, 40f, warriorOrcRun));
            enemies.add(new Skeleton(r4.x, r4.y - 30, 18, 4, 35f, warriorSkeletonRun));
            enemies.add(new Skeleton(r4.x, r4.y + 30, 18, 4, 35f, warriorSkeletonRun));
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
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        for (Enemy e : enemies) e.render(batch, shapeRenderer);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }

    private void update(float delta) {
        float speed = 200 * delta;
        Rectangle next = new Rectangle(player);

        if (Gdx.input.isKeyPressed(Keys.W)) {
            next.y += speed;
            if (!collisionManager.collides(next)) player.y += speed;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            next.y -= speed;
            if (!collisionManager.collides(next)) player.y -= speed;
        }
        if (Gdx.input.isKeyPressed(Keys.A)) {
            next.x -= speed;
            if (!collisionManager.collides(next)) player.x -= speed;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            next.x += speed;
            if (!collisionManager.collides(next)) player.x += speed;
        }

        playerPos.set(player.x, player.y);

        waveManager.update();
        for (Enemy e : enemies) {
            e.update(delta, playerPos, collisionManager);
            if (!e.isDead && e.bounds.overlaps(player)) {
                e.takeDamage(10); // Simulate player attack
            }
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        map.dispose();
        mapRenderer.dispose();
    }
}
