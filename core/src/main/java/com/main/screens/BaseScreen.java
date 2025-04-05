package com.main.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Gdx;

public abstract class BaseScreen implements Screen {

    protected Stage stage;
    protected SpriteBatch batch;

    public BaseScreen(SpriteBatch batch) {
        this.batch = batch;
        this.stage = new Stage(new FitViewport(800, 480), batch); // Centralized resolution
        Gdx.input.setInputProcessor(stage); // Handles input globally
    }

    protected Table createCenteredTable() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        return table;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }
    @Override
    public void show() {
        // Default no-op (can be overridden if needed)
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
    }
}
