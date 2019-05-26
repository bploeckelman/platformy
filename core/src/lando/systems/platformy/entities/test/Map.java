package lando.systems.platformy.entities.test;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import lando.systems.platformy.Assets;

public class Map {

    public static final float tile_size = 32f;
    public static int tiles_wide = 30;
    public static int tiles_high = 17;

    public Vector2 position;

    private Assets assets;
    private Array<Tile> tiles;

    private ObjectMap<Tile.Type, TextureRegion> tileTextures;

    public Map(Assets assets) {
        this.assets = assets;
        this.position = new Vector2();
        this.tiles = new Array<>(tiles_wide * tiles_high);
        for (int i = 0; i < tiles_wide * tiles_high; ++i) {
            Tile tile = new Tile(i);
            this.tiles.add(tile);
        }
        // Add a floor and a ceiling
        for (int x = 0; x < tiles_wide; ++x) {
            int row = 0;
            int index;

            index = x + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;

            row = tiles_high - 1;
            index = x + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;
        }
        // Add two walls
        for (int y = 0; y < tiles_high; ++y) {
            int row = y;
            int leftCol = 0;
            int rightCol = tiles_wide - 1;

            int index;
            index = leftCol + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;

            index = rightCol + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;
        }
        // Add a platform
        for (int x = 10; x < 20; ++x) {
            int row = 2;
            int index = x + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;
        }
        // And another
        for (int x = 13; x < 17; ++x) {
            int row = 4;
            int index = x + row * tiles_wide;
            this.tiles.get(index).type = Tile.Type.block;
        }
        this.tileTextures = new ObjectMap<>();
        this.tileTextures.put(Tile.Type.empty, assets.atlas.findRegion("tile-empty"));
        this.tileTextures.put(Tile.Type.block, assets.atlas.findRegion("tile-block"));
    }

    public void update(float dt) {

    }

    public void render(SpriteBatch batch) {
        float x = position.x;
        float y = position.y;
        int rowCounter = 0;
        for (Tile tile : tiles) {
            batch.draw(tileTextures.get(tile.type), x, y, tile_size, tile_size);

            x += tile_size;
            rowCounter++;
            if (rowCounter >= tiles_wide) {
                rowCounter = 0;
                x = position.x;
                y += tile_size;
            }
        }
    }

    public boolean isObstacle(int x, int y) {
        if (isOffMap(x, y)) return true;
        return (getTileAtTilePosition(x, y).type == Tile.Type.block);
    }

    public boolean isGround(int x, int y) {
        if (isOffMap(x, y)) return true;
        return (getTileAtTilePosition(x, y).type == Tile.Type.block);
    }

    public boolean isEmpty(int x, int y) {
        if (isOffMap(x, y)) return false;
        return (getTileAtTilePosition(x, y).type == Tile.Type.empty);
    }

    public boolean isOffMap(int x, int y) {
        return (x < 0 || x >= tiles_wide
             || y < 0 || y >= tiles_high);
    }

    public Tile getTileAtWorldPosition(float x, float y) {
        int ix = (int) ((x - position.x) / tile_size);
        int iy = (int) ((y - position.y) / tile_size);
        int index = ix + iy * tiles_wide;
        if (index < 0 || index >= tiles.size) {
            return null;
        }
        return tiles.get(index);
    }

    public Tile getTileAtTilePosition(int x, int y) {
        if (isOffMap(x, y)) return null;
        int index = x + y * tiles_wide;
//        if (index < 0 || index >= tiles.size) {
//            return null;
//        }
        return tiles.get(index);
    }

    public void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> collisionRects, Pool<Rectangle> rectanglePool) {
        rectanglePool.freeAll(collisionRects);
        collisionRects.clear();

        for (int y = startY; y <= endY; ++y) {
            for (int x = startX; x <= endX; ++x) {
                Tile tile = getTileAtWorldPosition(x, y);
                if (tile == null || tile.isEmpty()) continue;
                collisionRects.add(rectanglePool.obtain().set(x, y, tile_size, tile_size));
            }
        }
    }
}
