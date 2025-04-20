package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Music;
import com.main.Main;


public class GameOverScreen implements Screen {

    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Texture gameOverImage;
    private Texture healthBarImage;

    private float gameOverWidth, gameOverHeight;
    private float gameOverX, gameOverY;

    private float healthBarWidth, healthBarHeight;
    private float healthBarX, healthBarY;

    private Texture skeletonSheet;
    private Animation<TextureRegion> skeletonIdleAnimation;

    private Texture bossRunSheet;
    private Texture bossIdleSheet;

    private Animation<TextureRegion> bossRunAnimation;
    private Animation<TextureRegion> bossIdleAnimation;

    private float stateTime = 0f;

    private float bossX, bossY;
    private boolean bossReachedStop = false;

    private Stage stage;
    private Skin skin;

    private BitmapFont firedFont;
    private GlyphLayout layout;

    // Declare the Music object
    private Music gameOverMusic;

    // Flag to check if the music has been played
    private boolean musicPlayed = false;
    private Main game;
    public GameOverScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();

        // GAME OVER image
        gameOverImage = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\gameover_text.png"));
        gameOverWidth = gameOverImage.getWidth() * 0.5f;
        gameOverHeight = gameOverImage.getHeight() * 0.5f;
        gameOverX = (Gdx.graphics.getWidth() - gameOverWidth) / 2f;
        gameOverY = (Gdx.graphics.getHeight() - gameOverHeight) / 2f;

        // Health bar
        healthBarImage = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\healthbar.png"));
        healthBarWidth = healthBarImage.getWidth() * 0.3f;
        healthBarHeight = healthBarImage.getHeight() * 0.3f;
        healthBarX = (Gdx.graphics.getWidth() - healthBarWidth) / 2f;
        healthBarY = gameOverY + gameOverHeight + 40;

        // Skeleton
        skeletonSheet = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\Pixel Crawler - Free Pack\\Entities\\Mobs\\Skeleton Crew\\Skeleton - Mage\\Idle\\Idle-Sheet.png"));
        TextureRegion[][] tmpFrames = TextureRegion.split(skeletonSheet, 32, 32);
        TextureRegion[] frames = tmpFrames[0];
        skeletonIdleAnimation = new Animation<>(0.2f, frames);

        // Boss Run
        bossRunSheet = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\Office_Boss_Run.png"));
        TextureRegion[][] bossRunTmp = TextureRegion.split(bossRunSheet, 32, 32);
        TextureRegion[] bossRunFrames = bossRunTmp[0];
        bossRunAnimation = new Animation<>(0.1f, bossRunFrames);

        // Boss Idle
        bossIdleSheet = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\Office_Boss_Crouch_Idle.png"));
        TextureRegion[][] bossIdleTmp = TextureRegion.split(bossIdleSheet, 32, 32);
        TextureRegion[] bossIdleFrames = bossIdleTmp[0];
        bossIdleAnimation = new Animation<>(0.2f, bossIdleFrames);

        bossX = -64; // Start from off-screen
        bossY = healthBarY + healthBarHeight + 20;

        // Font for "YOU'RE FIRED"
        firedFont = new BitmapFont();
        firedFont.getData().setScale(1f);
        firedFont.setColor(Color.RED);
        layout = new GlyphLayout();

        // UI
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center().bottom().padBottom(110);
        stage.addActor(table);

        TextButton retryButton = new TextButton("Retry", skin);
        retryButton.addListener(e -> {
            if (retryButton.isPressed()) {
            	if (gameOverMusic != null) {
                    gameOverMusic.stop();
                    gameOverMusic.dispose();
                }
            	game.setScreen(new EnemyScreen(game)); 
                System.out.println("Retry button clicked!");
                return true;
            }
            return false;
        });

        table.add(retryButton).width(200).height(50);
    }

    @Override
    public void show() {
        // Load the music file and play it if it's not already played
        if (!musicPlayed) {
            gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\YOUREFIRED.mp3"));
            gameOverMusic.setLooping(false); // Music plays only once
            gameOverMusic.setVolume(0.5f);  // Set volume level
            gameOverMusic.play();
            musicPlayed = true; // Prevent music from playing again when navigating back to this screen
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        float centerX = (Gdx.graphics.getWidth() - 64) / 2f;
        float stopBeforeCenter = centerX - 80;

        if (!bossReachedStop) {
            bossX += 200 * delta;
            if (bossX >= stopBeforeCenter) {
                bossX = stopBeforeCenter;
                bossReachedStop = true;
                stateTime = 0f;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Health bar
        batch.draw(healthBarImage, healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // Skeletons
        TextureRegion skeletonFrame = skeletonIdleAnimation.getKeyFrame(stateTime, true);
        float skeletonWidth = 32;
        float skeletonHeight = 32;
        float spacing = 20;
        float centerY = gameOverY + (gameOverHeight - skeletonHeight) / 2f;

        float leftX = gameOverX - skeletonWidth - spacing;
        float rightX = gameOverX + gameOverWidth + spacing;

        batch.draw(skeletonFrame, leftX, centerY, skeletonWidth, skeletonHeight);
        batch.draw(skeletonFrame, rightX, centerY, skeletonWidth, skeletonHeight);

        // Boss
        TextureRegion bossFrame = bossReachedStop
                ? bossIdleAnimation.getKeyFrame(stateTime, true)
                : bossRunAnimation.getKeyFrame(stateTime, true);

        batch.draw(bossFrame, bossX, bossY, 64, 64);

        // GAME OVER image
        batch.draw(gameOverImage, gameOverX, gameOverY, gameOverWidth, gameOverHeight);

        // YOU'RE FIRED text
        String firedText = "YOU'RE FIRED";
        layout.setText(firedFont, firedText);
        float textX = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float textY = Gdx.graphics.getHeight() * 0.78f;
        firedFont.draw(batch, layout, textX, textY);

        batch.end();

        // UI stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
//        batch.dispose();
        gameOverImage.dispose();
        healthBarImage.dispose();
        skeletonSheet.dispose();
        bossRunSheet.dispose();
        bossIdleSheet.dispose();
        firedFont.dispose();
        stage.dispose();
        skin.dispose();

        // Dispose the music
        if (gameOverMusic != null) {
            gameOverMusic.stop();
            gameOverMusic.dispose();
        }
    }
}
