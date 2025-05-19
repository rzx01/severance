
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
        import com.main.database.DatabaseConnection;

        import java.sql.*;
        import java.util.ArrayList;

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

            private int hpLeft;
            private float timeTakenSeconds;
            private float score;
            private ArrayList<String> leaderboardEntries = new ArrayList<>();

            public WinScreen(Main game, int hpLeft, float timeTakenSeconds) {
                this.game = game;
                this.hpLeft = hpLeft;
                this.timeTakenSeconds = timeTakenSeconds;

                camera = new OrthographicCamera();
                camera.setToOrtho(false, 800, 480);

                font = new BitmapFont();
                font.getData().setScale(1.5f);

                
                danceSheet = new Texture(Gdx.files.internal("office_Boss_Character/Sprite_Sheets/Office_Boss_Faint.png"));
                TextureRegion[][] tmp = TextureRegion.split(danceSheet, 32, 32);
                TextureRegion[] frames = new TextureRegion[tmp[0].length];
                for (int i = 0; i < tmp[0].length; i++) frames[i] = tmp[0][i];
                danceAnimation = new Animation<>(0.1f, frames);
                danceAnimation.setPlayMode(Animation.PlayMode.LOOP);
                stateTime = 0f;

                
                winMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/video-game-enhancements-259503 (1).mp3"));
                winMusic.setLooping(true);
                winMusic.setVolume(0.5f);
                winMusic.play();

                buttonBounds = new Rectangle(300, 50, 200, 60);

                
                score = (hpLeft * 10f) - (timeTakenSeconds / 2f);
                saveResultToDatabase();

                Gdx.input.setInputProcessor(new InputAdapter() {
                    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        float x = screenX;
                        float y = 480 - screenY;
                        if (buttonBounds.contains(x, y)) {
                            winMusic.stop();
                            game.setScreen(new MenuScreen(game));
                        }
                        return true;
                    }

                    @Override public boolean mouseMoved(int screenX, int screenY) {
                        float x = screenX;
                        float y = 480 - screenY;
                        isButtonHovered = buttonBounds.contains(x, y);
                        return true;
                    }
                });

                fetchTopLeaderboard();
            }

            private void saveResultToDatabase() {
            	
                String username = game.getLocalUsername(); // locally stored
                try (Connection conn = DatabaseConnection.getConnection()) {
                	System.out.println("Saving to DB: " + username + ", " + timeTakenSeconds + ", " + hpLeft + ", " + score);

                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Leaderboard (username, time_taken, hp_left, score) VALUES (?, ?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setFloat(2, timeTakenSeconds);
                    stmt.setInt(3, hpLeft);
                    stmt.setFloat(4, score);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            private void fetchTopLeaderboard() {
                leaderboardEntries.clear();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                        "SELECT username, hp_left, time_taken, score FROM Leaderboard ORDER BY score DESC LIMIT 10");
                    while (rs.next()) {
                        String entry = String.format("%s | HP: %d | Time: %.1fs | Score: %.2f",
                            rs.getString("username"),
                            rs.getInt("hp_left"),
                            rs.getFloat("time_taken"),
                            rs.getFloat("score"));
                        leaderboardEntries.add(entry);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void render(float delta) {
                stateTime += delta;
                Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                camera.update();
                game.batch.setProjectionMatrix(camera.combined);
                game.batch.begin();

                // Task Completed
                font.draw(game.batch, "TASK COMPLETED!", 0, 460, 800, Align.center, false);

                // Draw animation
                TextureRegion frame = danceAnimation.getKeyFrame(stateTime);
                game.batch.draw(frame, 370, 300, 64, 64);

                // Score display
                font.draw(game.batch, String.format("Your Score: %.2f", score), 0, 270, 800, Align.center, false);

                // Leaderboard
                font.draw(game.batch, "Top Scores:", 50, 240);
                for (int i = 0; i < leaderboardEntries.size(); i++) {
                    font.draw(game.batch, (i + 1) + ". " + leaderboardEntries.get(i), 70, 210 - i * 20);
                }

                // Button
                game.batch.setColor(isButtonHovered ? 0.8f : 1f, 1f, 1f, 1f);
                font.draw(game.batch, "Back to Menu", buttonBounds.x + 25, buttonBounds.y + 40);
                game.batch.setColor(1f, 1f, 1f, 1f);

                game.batch.end();
            }

            @Override public void show() {}
            @Override public void resize(int width, int height) {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void hide() {}
            @Override public void dispose() {
                font.dispose();
                danceSheet.dispose();
                winMusic.dispose();
            }
        }
