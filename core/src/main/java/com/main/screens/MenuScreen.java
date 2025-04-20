package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.Main;
import com.main.screens.WinScreen;


public class MenuScreen extends ScreenAdapter {

    private static final int    FRAME_COLS       = 6;
    private static final int    FRAME_ROWS       = 1;
    private static final float  FRAME_DURATION   = 0.1f;
    private static final float  BOSS_SCALE       = 3f;
    private static final float  BUTTON_WIDTH     = 200f;
    private static final float  BUTTON_HEIGHT    = 100f;

    private Main game;
    private Stage stage;
    private OrthographicCamera cam;

    private Texture severanceTitle;
    private Texture playBtnTex;
    private Texture exitBtnTex;

    private Texture bossSheet;
    private Animation<TextureRegion> bossAnimation;
    private Image bossLeftImage;
    private Image bossRightImage;
    private float stateTime;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Camera & stage
        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(cam.viewportWidth/2f, cam.viewportHeight/2f, 0);
        cam.update();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load assets
        severanceTitle = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\severance.png"));
        playBtnTex     = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\play_btn.png"));
        exitBtnTex     = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\exit_btn.png"));
        bossSheet      = new Texture(Gdx.files.internal("Pixel Crawler - Free Pack 2.0.4\\Office_Boss_Ranged_Attack.png"));

        // Split boss sheet into frames
        int frameW = bossSheet.getWidth() / FRAME_COLS;
        int frameH = bossSheet.getHeight() / FRAME_ROWS;
        TextureRegion[][] tmp = TextureRegion.split(bossSheet, frameW, frameH);
        Array<TextureRegion> frames = new Array<>(FRAME_COLS * FRAME_ROWS);
        for (int r=0; r<FRAME_ROWS; r++) {
            for (int c=0; c<FRAME_COLS; c++) {
                frames.add(tmp[r][c]);
            }
        }
        bossAnimation = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
        stateTime = 0f;

        // Create actors
        Image titleImage      = new Image(severanceTitle);
        bossLeftImage         = new Image(bossAnimation.getKeyFrame(0));
        bossRightImage        = new Image(bossAnimation.getKeyFrame(0));
        ImageButton playBtn   = new ImageButton(new TextureRegionDrawable(playBtnTex));
        ImageButton exitBtn   = new ImageButton(new TextureRegionDrawable(exitBtnTex));

        // Scale boss images
        float scaledW = frameW * BOSS_SCALE;
        float scaledH = frameH * BOSS_SCALE;
        bossLeftImage.setSize(scaledW, scaledH);
        bossRightImage.setSize(scaledW, scaledH);


        // Button listeners
        playBtn.addListener(e -> {
            if (playBtn.isPressed()) {
                game.setScreen(new GameScreen(game));

                return true;
            }
            return false;
        });
        exitBtn.addListener(e -> {
            if (exitBtn.isPressed()) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Row 1: Title (reduced bottom padding to 20)
        table.add(titleImage)
             .colspan(3)
             .padBottom(20)
             .row();

        // Row 2: Boss left, Play, Boss right (boss row bottom padding tightened to 10)
        table.add(bossLeftImage)
             .size(scaledW, scaledH)
             .padRight(20)
             .padBottom(50);
        table.add(playBtn)
             .size(BUTTON_WIDTH, BUTTON_HEIGHT)
             .padBottom(10);
        table.add(bossRightImage)
             .size(scaledW, scaledH)
             .padLeft(20)
             .padBottom(50)
             .row();

        // Row 3: Exit (now closer to Play)
        table.add(exitBtn)
             .colspan(3)
             .size(BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        TextureRegion frame = bossAnimation.getKeyFrame(stateTime);
        bossLeftImage.setDrawable(new TextureRegionDrawable(frame));
        bossRightImage.setDrawable(new TextureRegionDrawable(frame));

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        severanceTitle.dispose();
        playBtnTex.dispose();
        exitBtnTex.dispose();
        bossSheet.dispose();
    }
}
