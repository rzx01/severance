package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.InputAdapter;
import com.main.Main;

public class WinScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private BitmapFont font;
    private Animation<TextureRegion> danceAnimation;
    private float stateTime;
    private Music winMusic;

    private Texture danceSheet;
    private Rectangle buttonBounds;
    private boolean isButtonHovered;

    public WinScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        font = new BitmapFont();
        font.getData().setScale(2f);

        // Load and split dancing sprite sheet
        danceSheet = new Texture(Gdx.files.internal("assets/office_Boss_Character/Sprite_Sheets/Office_Boss_Faint.png"));
        TextureRegion[][] tmp = TextureRegion.split(danceSheet, 32, 32);

        TextureRegion[] frames = new TextureRegion[tmp[0].length];
        for (int i = 0; i < tmp[0].length; i++) {
            frames[i] = tmp[0][i];
        }

        danceAnimation = new Animation<>(0.1f, frames);
        danceAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;

        // Load background music
        winMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/video-game-enhancements-259503 (1).mp3"));
        winMusic.setLooping(true);
        winMusic.setVolume(0.5f);
        winMusic.play();

        // Button setup
        buttonBounds = new Rectangle(300, 50, 200, 60);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = screenX;
                float y = 480 - screenY;

                if (buttonBounds.contains(x, y)) {
                    winMusic.stop();
                    game.setScreen(new MenuScreen(game));
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                float x = screenX;
                float y = 480 - screenY;
                isButtonHovered = buttonBounds.contains(x, y);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Draw "Task Completed" message
        font.draw(game.batch, "TASK COMPLETED!", 0, 400, 800, Align.center, false);

        // Draw animated player
        TextureRegion frame = danceAnimation.getKeyFrame(stateTime);
        game.batch.draw(frame, 370, 200, 64, 64);

        // Draw back to menu button
        game.batch.setColor(isButtonHovered ? 0.8f : 1f, 1f, 1f, 1f);
        font.draw(game.batch, "Back to Menu", buttonBounds.x + 25, buttonBounds.y + 40);
        game.batch.setColor(2f, 1f, 4f, 1f);

        game.batch.end();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        font.dispose();
        danceSheet.dispose();
        winMusic.dispose();
    }
}
