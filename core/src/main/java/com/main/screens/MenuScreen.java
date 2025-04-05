package com.main.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.main.Main;

public class MenuScreen extends BaseScreen {

    private Main game;
    private Skin skin;

    public MenuScreen(Main game) {
        super(game.batch);
        this.game = game;
        setupUI();
    }

    private void setupUI() {
        skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        skin.add("default", style);

        TextButton playButton = new TextButton("Start Game", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

        createCenteredTable().add(playButton).width(200).height(50).pad(10);
    }
}
