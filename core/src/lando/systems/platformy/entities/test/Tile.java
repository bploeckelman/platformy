package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Rectangle;

public class Tile {

    public enum Type { empty, block, oneway }

    public final int index;
    public final int x;
    public final int y;
    public final Rectangle bounds;

    public Type type;

    public Tile(int index) {
        this(index, Type.empty);
    }

    public Tile(int index, Type type) {
        this.index = index;
        this.type = type;
        this.x = index / Map.tiles_wide;
        this.y = index % Map.tiles_high;
        this.bounds = new Rectangle(x * Map.tile_size, y * Map.tile_size, Map.tile_size, Map.tile_size);
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
