package com.voidvvv.autochess.utils;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.voidvvv.autochess.model.BaseCollision;

import java.util.HashMap;
import java.util.Map;

public class TiledAssetLoader {

    public Map<String, BaseCollision> collisionMapping = new HashMap<>();

    public Map<String, TextureRegion> textureRegionMap = new HashMap<>();

    public void loadBaseCollision (TiledMapTileSet tiledSet) {
        String prefix = tiledSet.getName();
        for (TiledMapTile tiledMapTile : tiledSet) {
            int id = tiledMapTile.getId();
            int d = tiledMapTile.getProperties().get("firstid", 1, Integer.class);
            String key = prefix + (id - d + 1);
            BaseCollision bc = extractCollision(tiledMapTile);

            if (bc != null) {
                collisionMapping.put(key, bc);
                textureRegionMap.put(key, tiledMapTile.getTextureRegion());
            }
        }
    }

    private BaseCollision extractCollision(TiledMapTile tiledMapTile) {
        MapObjects objects = tiledMapTile.getObjects();
        MapObject mo = objects.get("base");
        MapObject face = objects.get("face");
        MapObject bottom = objects.get("bottom");

        BaseCollision bc = null;
        if (mo instanceof PointMapObject pmo) {
            bc =  new BaseCollision();
            bc.base.set(pmo.getPoint());
        }
        if (bc != null && face instanceof RectangleMapObject rmo) {
            bc.face.set(rmo.getRectangle());
        }
        if (bc != null && bottom instanceof RectangleMapObject rmo) {
            bc.bottom.set(rmo.getRectangle());
        }
        return bc;
    }

    /**
     * 根据key获取碰撞框
     * @param key 资源key (格式: "tilesetId+tileId")
     * @return 碰撞框，如果不存在则返回null
     */
    public BaseCollision getCollision(String key) {
        return collisionMapping.get(key);
    }

    /**
     * 根据key获取纹理区域
     * @param key 资源key (格式: "tilesetId+tileId")
     * @return 纹理区域，如果不存在则返回null
     */
    public TextureRegion getTexture(String key) {
        return textureRegionMap.get(key);
    }

    /**
     * 检查是否存在指定的资源
     * @param key 资源key (格式: "tilesetId+tileId")
     * @return 如果碰撞框和纹理都存在则返回true
     */
    public boolean hasResource(String key) {
        return collisionMapping.containsKey(key) && textureRegionMap.containsKey(key);
    }
}
