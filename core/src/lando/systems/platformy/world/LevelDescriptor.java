package lando.systems.platformy.world;

public enum LevelDescriptor {

    demo("maps/demo.tmx");

    public String mapFileName;

    LevelDescriptor(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    @Override
    public String toString() {
        return "[Level: " + mapFileName + "]";
    }

}
