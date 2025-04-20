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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.utils.CollisionManager;

import java.util.ArrayList;
import java.util.List;

public class DungeonScreen implements Screen {

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Rectangle player;

    private CollisionManager collisionManager;

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

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/dungeon.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        MapObject playerStart = map.getLayers().get("PlayerStart").getObjects().get("PlayerStart");
        RectangleMapObject rectObj = (RectangleMapObject) playerStart;
        Rectangle rect = rectObj.getRectangle();
        player = new Rectangle(rect.x, rect.y, 24, 24);

        collisionManager = new CollisionManager(map, "Collision");

        npcs = new ArrayList<>();
        npcs.add(new NPC("Rogue", "Pixel Crawler - Free Pack 2.0.4\\Pixel Crawler - Free Pack\\Entities\\Npc's\\Rogue\\Idle\\Idle-Sheet.png", 200, 350, 32, 32, rogueDialog));
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
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.1f, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.2f, 1f, 1);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (NPC npc : npcs) {
            npc.draw(batch);
        }
        batch.end();

        if (dialogActive) {
            dialogStage.act(delta);
            dialogStage.draw();
        }
    }

    private void update(float delta) {
        float speed = 200 * delta;

        if (!dialogActive) {
            Rectangle next = new Rectangle(player);

            if (Gdx.input.isKeyPressed(Keys.W)) next.y += speed;
            if (Gdx.input.isKeyPressed(Keys.S)) next.y -= speed;
            if (Gdx.input.isKeyPressed(Keys.A)) next.x -= speed;
            if (Gdx.input.isKeyPressed(Keys.D)) next.x += speed;
            if (!collisionManager.collides(next)) player.setPosition(next.x, next.y);

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
                        dialogActive = false;
                        dialogWindow.setVisible(false);
                    }
                }
            }
        }
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

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        dialogStage.getViewport().update(width, height, true);
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
        dialogStage.dispose();
        skin.dispose();
        for (NPC npc : npcs) {
            npc.dispose();
        }
    }
}
