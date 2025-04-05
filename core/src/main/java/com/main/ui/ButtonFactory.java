package com.main.ui;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class ButtonFactory {

    public static TextButton createDefaultTextButton(String text) {
        BitmapFont font = new BitmapFont(); // uses default font
        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;

        Skin skin = new Skin();
        skin.add("default", font);

        return new TextButton(text, skin, "default") {{
            setStyle(style);
        }};
    }
}
