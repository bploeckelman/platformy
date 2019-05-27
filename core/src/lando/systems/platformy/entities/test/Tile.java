package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class Tile implements Pool.Poolable {

    public enum Type { none, empty, block, oneway, ladder, ladder_deck, exit, spawn }

    public final Rectangle bounds;

    public int index;
    public int x;
    public int y;
    public Type type;

    public Tile() {
        this.bounds = new Rectangle();
        this.type = Type.none;
        reset();
    }

    public Tile(int index) {
        this(index, Type.empty);
    }

    public Tile(int index, Type type) {
        this.type = type;
        this.bounds = new Rectangle(x * Map.tile_size, y * Map.tile_size, Map.tile_size, Map.tile_size);
        setIndex(index);
    }

    public Tile setIndex(int index) {
        this.index = index;
        this.x = index / Map.tiles_wide;
        this.y = index % Map.tiles_high;
        return this;
    }

    public Tile setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public void reset() {
        index = -1;
        type = Type.none;
        x = -1;
        y = -1;
        bounds.set(x, y, Map.tile_size, Map.tile_size);
    }

    public boolean isEmpty() {
        return type == Type.empty;
    }

    public boolean isObstacle() {
        return type == Type.block;
    }

    public boolean isOneway() {
        return type == Type.oneway;
    }

}
