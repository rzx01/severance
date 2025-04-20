package com.main.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;


public class NPC {
    private final String name;
    private final Texture texture;
    private final TextureRegion region;
    private final Rectangle bounds;
    private final String[] dialogLines;

    public NPC(String name,
               String texturePath,
               float x, float y,
               float width, float height,
               String[] dialogLines) {
        this.name = name;
        this.texture = new Texture(texturePath);
        this.region= new TextureRegion(texture, 0, 0, (int)width, (int)height);
        this.bounds= new Rectangle(x, y, width, height);
        this.dialogLines= dialogLines;
    }

    /** Draws the NPC at its world position. */
    public void draw(SpriteBatch batch) {
        batch.draw(region, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds()   { return bounds; }
    public String[]  getDialogLines() { return dialogLines; }
    public String    getName()     { return name; }


    public void dispose() {
        texture.dispose();
    }
}
