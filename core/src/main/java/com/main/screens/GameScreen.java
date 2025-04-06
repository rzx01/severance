package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Input.Keys;
import com.main.utils.CollisionManager;

public class GameScreen implements Screen {

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle player;
    private CollisionManager collisionManager;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/office.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Get player start position
        MapObject playerStart = map.getLayers().get("PlayerStart").getObjects().get("PlayerStart");
        RectangleMapObject rectObj = (RectangleMapObject) playerStart;
        Rectangle rect = rectObj.getRectangle();

        // Initialize player rectangle
        player = new Rectangle(rect.x, rect.y, 32, 32);

        // Initialize collision manager for "Collision" layer
        collisionManager = new CollisionManager(map, "Collision");
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

        shapeRenderer.setProjectionMatrix(camera.combined);
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
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }
}
