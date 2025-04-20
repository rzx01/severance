package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.Main;
import com.main.screens.WinScreen;


public class MenuScreen extends ScreenAdapter {

    private Main game;
    private Stage stage;
    private Skin skin;

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); // Make sure you have uiskin.* in assets

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Office Adventure", skin, "default");
        TextButton startButton = new TextButton("Start Game", skin);

        startButton.addListener(e -> {
            if (startButton.isPressed()) {
                game.setScreen(new EnemyScreen());
                return true;
            }
            return false;
        });

        table.add(title).padBottom(40).row();
        table.add(startButton).width(200).height(50);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
