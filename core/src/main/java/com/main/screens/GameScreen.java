package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.utils.CollisionManager;

public class GameScreen implements Screen {

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Rectangle player;
    private Rectangle npc;

    private CollisionManager collisionManager;

    private boolean cutsceneActive = true;
    private boolean dialogueShown = false;

    private Stage stage;
    private Skin skin;
    private Window dialogueWindow;
    private Label dialogueLabel;

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/office.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Player start position
        MapObject playerStart = map.getLayers().get("PlayerStart").getObjects().get("PlayerStart");
        RectangleMapObject rectObj = (RectangleMapObject) playerStart;
        Rectangle rect = rectObj.getRectangle();
        player = new Rectangle(rect.x, rect.y, 32, 32);

        // NPC starts far from player
        npc = new Rectangle(player.x - 150, player.y, 32, 32);

        collisionManager = new CollisionManager(map, "Collision");

        // UI
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        dialogueLabel = new Label("", skin);
        dialogueWindow = new Window("", skin);
        dialogueWindow.add(dialogueLabel).pad(10);
        dialogueWindow.pack();
        dialogueWindow.setPosition(50, 50);
        dialogueWindow.setVisible(false);
        stage.addActor(dialogueWindow);
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

        shapeRenderer.setColor(0, 1, 0, 1); // Player
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        shapeRenderer.setColor(1, 0, 0, 1); // NPC
        shapeRenderer.rect(npc.x, npc.y, npc.width, npc.height);

        shapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    private void update(float delta) {
        float speed = 200 * delta;

        if (cutsceneActive) {
            moveNpcTowardPlayer(speed);
            return;
        }

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

    private void moveNpcTowardPlayer(float speed) {
        Vector2 npcPos = new Vector2(npc.x, npc.y);
        Vector2 playerPos = new Vector2(player.x, player.y);
        Vector2 dir = playerPos.cpy().sub(npcPos);

        if (dir.len() > 2f) {
            dir.nor();
            Rectangle nextNpcPos = new Rectangle(npc);
            nextNpcPos.x += dir.x * speed;
            nextNpcPos.y += dir.y * speed;

            if (!collisionManager.collides(nextNpcPos)) {
                npc.x += dir.x * speed;
                npc.y += dir.y * speed;
            }
        } else if (!dialogueShown) {
            showDialogue("Hey there! Welcome to the office. Let's get started!");
        }

        if (dialogueShown && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            dialogueWindow.setVisible(false);
            cutsceneActive = false;
            Gdx.input.setInputProcessor(null); // Give control back
        }
    }

    private void showDialogue(String message) {
        dialogueLabel.setText(message);
        dialogueWindow.pack();
        dialogueWindow.setVisible(true);
        dialogueShown = true;
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        stage.dispose();
        skin.dispose();
    }
}
