package lando.systems.platformy.entities.test;

public class Tile {

    public enum Type { empty, block, oneway }

    public final int index;

    public Type type;

    public Tile(int index) {
        this.index = index;
        this.type = Type.empty;
    }

    public Tile(int index, Type type) {
        this.index = index;
        this.type = type;
    }

}
