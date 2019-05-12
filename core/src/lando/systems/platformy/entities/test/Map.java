package lando.systems.platformy.entities.test;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.platformy.Assets;

public class Map {

    private final float tile_size = 32f;

    private Assets assets;
    private Vector2 position;
    private Array<Tile> tiles;
    private int tilesWide = 80;
    private int tilesHigh = 60;

    private TextureRegion tileTexture;

    public Map(Assets assets) {
        this.assets = assets;
        this.position = new Vector2();
        this.tiles = new Array<>(tilesWide * tilesHigh);
        for (int i = 0; i < tilesWide * tilesHigh; ++i) {
            Tile tile = new Tile(i);
            this.tiles.add(tile);
        }
        this.tileTexture = assets.atlas.findRegion("tile-placeholder");
    }

    public void update(float dt) {

    }

    public void render(SpriteBatch batch) {
        float x = position.x;
        float y = position.y;
        int rowCounter = 0;
        for (Tile tile : tiles) {
            batch.draw(tileTexture, x, y, tile_size, tile_size);

            x += tile_size;
            rowCounter++;
            if (rowCounter >= tilesWide) {
                rowCounter = 0;
                x = position.x;
                y += tile_size;
            }
        }
    }

    public Tile getTileAtWorldPosition(float x, float y) {
        int ix = (int) ((x - position.x) / tile_size);
        int iy = (int) ((y - position.y) / tile_size);
        int index = ix + iy * tilesWide;
        if (index < 0 || index >= tiles.size) {
            return null;
        }
        return tiles.get(index);
    }

    public Tile getTileAtIndex(int x, int y) {
        int index = x + y * tilesWide;
        if (index < 0 || index >= tiles.size) {
            return null;
        }
        return tiles.get(index);
    }

}
