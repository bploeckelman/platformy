package lando.systems.platformy.entities.test;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.platformy.Assets;

public class Map implements Disposable {

    public static final float tile_size = 32f;
    public static int tiles_wide = 30;
    public static int tiles_high = 17;


    public Vector2 position;

    private Assets assets;
    private Array<Tile> tiles;
    private Array<Tile> tempTiles;

    private static final int num_extra_pool_tiles = 20;
    private Pool<Tile> tilePool = Pools.get(Tile.class, tiles_wide * tiles_high + num_extra_pool_tiles);

    private ObjectMap<Tile.Type, TextureRegion> tileTextures;

    public Map(Assets assets) {
        this.assets = assets;
        this.position = new Vector2();
        this.tiles = new Array<>(tiles_wide * tiles_high);
        for (int i = 0; i < tiles_wide * tiles_high; ++i) {
            this.tiles.add(tilePool.obtain()
                                   .setIndex(i)
                                   .setType(Tile.Type.empty));
        }
        this.tempTiles = new Array<>();
        for (int i = 0; i < 9; ++i) {
            this.tempTiles.add(null);
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
        this.tiles.get(6 + 1 * tiles_wide).type = Tile.Type.block;
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
//        // Add a platform
//        for (int x = 10; x < 20; ++x) {
//            int row = 2;
//            int index = x + row * tiles_wide;
//            this.tiles.get(index).type = Tile.Type.block;
//        }
//        // And another
//        for (int x = 13; x < 17; ++x) {
//            int row = 4;
//            int index = x + row * tiles_wide;
//            this.tiles.get(index).type = Tile.Type.block;
//        }
        this.tileTextures = new ObjectMap<>();
        this.tileTextures.put(Tile.Type.none        , assets.atlas.findRegion("debug"));
        this.tileTextures.put(Tile.Type.empty       , assets.atlas.findRegion("tile-empty"));
        this.tileTextures.put(Tile.Type.block       , assets.atlas.findRegion("tile-block-spelunky"));
        this.tileTextures.put(Tile.Type.ladder      , assets.atlas.findRegion("debug"));
        this.tileTextures.put(Tile.Type.ladder_deck , assets.atlas.findRegion("debug"));
        this.tileTextures.put(Tile.Type.exit        , assets.atlas.findRegion("debug"));
        this.tileTextures.put(Tile.Type.spawn       , assets.atlas.findRegion("debug"));
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
                Tile tile = getTileAtTilePosition(x, y);
                if (tile == null || tile.isEmpty()) continue;
                collisionRects.add(rectanglePool.obtain()
                        .set(tile.bounds.x, tile.bounds.y, tile.bounds.width, tile.bounds.height));
            }
        }

//        for (int y = startY; y <= endY; ++y) {
//            for (int x = startX; x <= endX; ++x) {
//                Tile tile = getTileAtWorldPosition(x, y);
//                if (tile == null || tile.isEmpty()) continue;
//                collisionRects.add(rectanglePool.obtain().set(x, y, tile_size, tile_size));
//            }
//        }
    }

    public static class TileGroup {
        public static final int center      = 0;
        public static final int up_left     = 1;
        public static final int up_center   = 2;
        public static final int up_right    = 3;
        public static final int right       = 4;
        public static final int down_right  = 5;
        public static final int down_center = 6;
        public static final int down_left   = 7;
        public static final int left        = 8;
    }
    /**
     * Get a set of 9 tiles around (and including) the tile with an index specified by the parameters
     *
     * These tiles can be indexed in the returned array by using the constants from Map.TileGroup
     *
     * @param centerTileX
     * @param centerTileY
     * @return
     */
    public Array<Tile> getNeighboringTiles(int centerTileX, int centerTileY) {
        tempTiles.set(TileGroup.center      , getTileAtTilePosition(centerTileX + 0, centerTileY + 0));
        tempTiles.set(TileGroup.up_left     , getTileAtTilePosition(centerTileX - 1, centerTileY + 1));
        tempTiles.set(TileGroup.up_center   , getTileAtTilePosition(centerTileX + 0, centerTileY + 1));
        tempTiles.set(TileGroup.up_right    , getTileAtTilePosition(centerTileX + 1, centerTileY + 1));
        tempTiles.set(TileGroup.right       , getTileAtTilePosition(centerTileX + 1, centerTileY + 0));
        tempTiles.set(TileGroup.down_right  , getTileAtTilePosition(centerTileX + 1, centerTileY - 1));
        tempTiles.set(TileGroup.down_center , getTileAtTilePosition(centerTileX + 0, centerTileY - 1));
        tempTiles.set(TileGroup.down_left   , getTileAtTilePosition(centerTileX - 1, centerTileY - 1));
        tempTiles.set(TileGroup.left        , getTileAtTilePosition(centerTileX - 1, centerTileY + 0));

        return tempTiles;
    }

    public static boolean tileIsType(Tile tile, Tile.Type type) {
        return (tile != null) && (tile.type == type);
    }

    @Override
    public void dispose() {
        tilePool.freeAll(tiles);
        tilePool.freeAll(tempTiles);
    }

}
