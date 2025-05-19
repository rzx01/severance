// LoginRegisterScreen.java
package com.main.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.main.Main;
import com.main.database.DatabaseConnection;

import java.sql.*;
import java.util.prefs.Preferences;

public class LoginRegisterScreen implements Screen, InputProcessor {
    private final Main game;
    private OrthographicCamera camera;
    private BitmapFont font;
    private boolean showRegister = false;
    private String username = "", email = "", password = "", message = "";
    private int cursorPosition = 0;

    // 0: username, 1: email, 2: password
    private int selectedField = 0;

    public LoginRegisterScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        font = new BitmapFont();
        Gdx.input.setInputProcessor(this);
    }

    private void saveUserLocally(String username, String email) {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
        prefs.put("username", username);
        prefs.put("email", email);
    }

    private void handleLogin() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM User WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                saveUserLocally(username, rs.getString("email"));
                game.setLocalUser(username, email);
                message = "Login successful!";
                game.setScreen(new MenuScreen(game));
            } else {
                message = "Invalid credentials.";
            }
        } catch (SQLException e) {
            message = "Error: " + e.getMessage();
        }
    }

    private void handleRegister() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO User (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            message = "Registered successfully! Now login.";
            showRegister = false;
        } catch (SQLException e) {
            message = "Error: " + e.getMessage();
        }
    }

    @Override
    public void render(float delta) {
        // Set white background
        Gdx.gl.glClearColor(1, 1, 1, 1); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.setColor(0, 0, 0, 1); // black text
        font.draw(batch, showRegister ? "Register" : "Login", 350, 400);
        font.draw(batch, "Username: " + username + (selectedField == 0 ? " |" : ""), 100, 300);
        font.draw(batch, "Email: " + email + (selectedField == 1 ? " |" : ""), 100, 270);
        font.draw(batch, "Password: " + password.replaceAll(".", "*") + (selectedField == 2 ? " |" : ""), 100, 240);
        font.draw(batch, "[Tab] Toggle Login/Register", 100, 180);
        font.draw(batch, "[Enter] Submit", 100, 150);
        font.draw(batch, "[Up/Down] Select Field", 100, 130);
        font.draw(batch, message, 100, 100);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            showRegister = !showRegister;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (showRegister) handleRegister();
            else handleLogin();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedField = (selectedField + 2) % 3; // cycle backwards
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedField = (selectedField + 1) % 3; // cycle forward
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}

    // InputProcessor methods
    @Override public boolean keyTyped(char character) {
        if (character == '\b') { // backspace
            if (selectedField == 0 && username.length() > 0) username = username.substring(0, username.length() - 1);
            else if (selectedField == 1 && email.length() > 0) email = email.substring(0, email.length() - 1);
            else if (selectedField == 2 && password.length() > 0) password = password.substring(0, password.length() - 1);
        } else if (character >= 32 && character <= 126) { // printable characters
            if (selectedField == 0) username += character;
            else if (selectedField == 1) email += character;
            else if (selectedField == 2) password += character;
        }
        return true;
    }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
//    @Override public boolean keyTyped() { return false; }
    @Override public boolean touchDown(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchUp(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int x, int y, int pointer) { return false; }
    @Override public boolean mouseMoved(int x, int y) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
