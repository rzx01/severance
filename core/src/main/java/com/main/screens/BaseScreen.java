package com.main.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.main.Main;

public abstract class BaseScreen implements Screen {
    protected Main game;
    protected Stage stage;
    protected FitViewport viewport;
    protected OrthographicCamera camera;

    public BaseScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    // Call this method to adjust zoom level based on map size
    protected void setCameraZoom(float zoomLevel) {
        camera.zoom = zoomLevel;  // Adjust zoom (1 = normal, <1 = zoomed in, >1 = zoomed out)
        camera.update();
    }

    @Override
    public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
