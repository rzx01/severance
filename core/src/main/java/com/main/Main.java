package com.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.main.screens.MenuScreen;
import com.main.screens.LoginRegisterScreen;

public class Main extends Game {
    public SpriteBatch batch;

    private String localUsername;
    private String localEmail;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new LoginRegisterScreen(this));
    }

    // Setters when login/register is successful
    public void setLocalUser(String username, String email) {
        this.localUsername = username;
        this.localEmail = email;
    }

    // Getters for username and email
    public String getLocalUsername() {
        return localUsername;
    }

    public String getLocalEmail() {
        return localEmail;
    }
}
