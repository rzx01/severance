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
import com.main.Main;
import com.main.utils.CollisionManager;
import com.badlogic.gdx.Game; // Add if not already
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class GameScreen implements Screen {
    private Main game;
    public GameScreen(Main game) {
        this.game = game;
    }
	private boolean transitioningToEnemy = false;
	private float fadeAlpha = 0f;
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

    private int cutsceneStage = 0;  // 0: approaching, 1: boss talks, 2: boss leaves, 3: player reacts, 4: sleep
    private float timer = 0f;
    private boolean bossLeaving = false;
    private boolean playerSleeping = false;
    
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

        Gdx.gl.glClearColor(0, 0, 0, 1);  // Set clear color to black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Clear the screen

        camera.position.set(player.x + player.width / 2, player.y + player.height / 2, 0);
        camera.update();

        // Render the map first
        mapRenderer.setView(camera);
        mapRenderer.render();  // This renders the map in the background

        // Start drawing the game elements (player, NPC, etc.)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the player (main character)
        shapeRenderer.setColor(0, 1, 0, 1);  // Player color (green)
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        // Draw the NPC (if any)
        shapeRenderer.setColor(1, 0, 0, 1);  // NPC color (red)
        shapeRenderer.rect(npc.x, npc.y, npc.width, npc.height);

        shapeRenderer.end();  // End drawing of game objects

        // Gradually change fadeAlpha over time to make the fade slower
        if (fadeAlpha > 0) {
            fadeAlpha -= delta * 0.1f;  // Slow down fade speed by multiplying delta by a smaller number
            if (fadeAlpha < 0) fadeAlpha = 0;  // Prevent fadeAlpha from going below 0
        }

        // Apply the fade effect over the entire screen (background + everything else)
        if (fadeAlpha > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, fadeAlpha);  // Fade color (black)
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Full-screen fade
            shapeRenderer.end();
        }

        // Now render the stage (UI elements like dialogue, etc.)
        stage.act(delta);
        stage.draw();  // This renders the UI like the dialogue box

        if (transitioningToEnemy) {
            timer += delta;
            if (timer > 1.5f) {
                game.setScreen(new EnemyScreen(game)); // Transition to the next screen
            }
            return;
        }
    }

    private void update(float delta) {
        float speed = 200 * delta;

        if (cutsceneActive) {
            runCutscene(delta);
            return;
        }

        // Regular movement after cutscene ends
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
    
    private void runCutscene(float delta) {
        float speed = 200 * delta;

        switch (cutsceneStage) {
            case 0: 
      
                Vector2 npcPos = new Vector2(npc.x, npc.y);
                Vector2 playerPos = new Vector2(player.x, player.y);
                Vector2 dir = playerPos.cpy().sub(npcPos);

                if (dir.len() > 2f) {
                    dir.nor();
                    npc.x += dir.x * speed;
                    npc.y += dir.y * speed;
                } else {
                    showDialogue("Green, I just saw your firewall config... you left port 22 open to the world!");
                    cutsceneStage = 1;
                }
                break;

            case 1: // Boss starts scolding
                if (!dialogueShown) {
                    showDialogue("You think this is a joke?");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("You left the firewall config OPEN on prod.");
                    cutsceneStage++;
                }
                break;

            case 2:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("I had to clean up your mess before the breach report went out.");
                    cutsceneStage++;
                }
                break;

            case 3:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("One more screw-up, and you're out of here. Understand?");
                    cutsceneStage++;
                }
                break;

            case 4: // Boss leaves
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    dialogueWindow.setVisible(false);
                    dialogueShown = false;
                    cutsceneStage++;
                }
                break;

            case 5: // NPC walks away
                if (npc.x < player.x - 150) {
                    cutsceneStage++;
                } else {
                    npc.x -= speed;
                }
                break;

            case 6: // Player mutters
                if (!dialogueShown) {
                    showDialogue("...he's always like this.");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Maybe I should’ve taken that game dev internship.");
                    cutsceneStage++;
                }
                break;

            case 7:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Ugh... whatever.");
                    cutsceneStage++;
                }
                break;

            case 8: // Sleep animation
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    dialogueWindow.setVisible(false);
                    dialogueShown = false;
                    cutsceneStage++;
                }
                break;

            case 9: // Start sleep dialogue
                if (!dialogueShown) {
                    showDialogue("Cybersecurity this, cybersecurity that...");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("I'm just... tired.");
                    cutsceneStage++;
                }
                break;

            case 10:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Why is it always me patching the firewall configs...");
                    cutsceneStage++;
                }
                break;

            case 11:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Maybe if I could just press E... to patch it...");
                    cutsceneStage++;
                }
                break;

            case 12:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Move with... W...A...S...D...");
                    cutsceneStage++;
                }
                break;

            case 13:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Space... attack... stop the packet flood...");
                    cutsceneStage++;
                }
                break;

            case 14:
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Zzz... breach neutralized... maybe I *am* a hero...");
                    cutsceneStage++;
                }
                break;

            case 15: // Sleep animation and transition
                if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    dialogueWindow.setVisible(false);
                    dialogueShown = false;
                    cutsceneStage++;
                }
                break;

            case 16: // "Alright... time to lock in."
                if (!dialogueShown) {
                    showDialogue("Alright... time to lock in.");
                    fadeAlpha = 0f;
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    timer = 0;
                    cutsceneStage++;
                    dialogueShown = false;
                }
                break;

            case 17: // Inner monologue + fade to black
                timer += delta;
                fadeAlpha = Math.min(1f, timer / 5f); // Smooth fade over 5 seconds

                if (timer < 1f && !dialogueShown) {
                    showDialogue("Wait...");
                } else if (timer > 1f && timer < 3f && dialogueShown && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Is this... another intrusion attempt?");
                } else if (timer > 3f && timer < 5f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Rogues, orcs, skeletons — doesn’t matter...");
                } else if (timer > 5f && timer < 7f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Malware has many faces.");
                } else if (timer > 7f && timer < 9f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Time to debug them... permanently.");
                } else if (timer > 9f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    timer = 0;
                    dialogueShown = false;
                }
                break;

            case 18: // "Alright... time to lock in."
                timer += delta;
                if (!dialogueShown) {
                    showDialogue("Alright... time to lock in.");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    timer = 0;
                    dialogueShown = false;
                }
                break;

            case 19: // Cyber-defense brainstorming
                timer += delta;

                if (timer < 2f && !dialogueShown) {
                    showDialogue("Firewall protocols... intrusion countermeasures...");
                } else if (timer > 2f && timer < 4f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Trace packet origins... segment the network...");
                } else if (timer > 4f && timer < 6f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    showDialogue("Deploy honeypots... bait them out.");
                } else if (timer > 6f && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    timer = 0;
                    dialogueShown = false;
                }
                break;

            case 20: // Call back to mentors
                if (!dialogueShown) {
                    showDialogue("But I can’t do this alone...");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    dialogueShown = false;
                }
                break;

            case 21:
                if (!dialogueShown) {
                    showDialogue("Maybe... if I interact with the old masters...");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    dialogueShown = false;
                }
                break;

            case 22:
                if (!dialogueShown) {
                    showDialogue("I hear them in the other room,....Press E to reach them.");
                } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                    cutsceneStage++;
                    dialogueShown = false;
                    timer = 0;
                }
                break;

            case 23: // Finally, switch to EnemyScreen
                timer += delta;
                if (timer > 1f) {
                    transitioningToEnemy = true;
                    cutsceneActive = false;
                }
                break;

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
