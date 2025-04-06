package com.main.utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class CollisionManager {

    private List<Rectangle> collisionRects;

    public CollisionManager(TiledMap map, String layerName) {
        collisionRects = new ArrayList<>();
        MapObjects objects = map.getLayers().get(layerName).getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                collisionRects.add(((RectangleMapObject) object).getRectangle());
            }
        }
    }

    public boolean collides(Rectangle rect) {
        for (Rectangle r : collisionRects) {
            if (r.overlaps(rect)) return true;
        }
        return false;
    }
}
