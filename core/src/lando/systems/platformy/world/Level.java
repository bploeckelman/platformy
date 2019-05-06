package lando.systems.platformy.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import lando.systems.platformy.Assets;
import lando.systems.platformy.screens.GameScreen;

public class Level {

    public static final float TILE_SIZE = 32f;

    private Assets assets;
    private GameScreen screen;

    public String name;
    public TiledMap map;
    public TiledMapRenderer mapRenderer;
    public TiledMapTileLayer collisionLayer;
    public TiledMapTileLayer backgroundLayer;
    public int[] collisionLayerIndex = new int[1];
    public int[] backgroundLayerIndex = new int[1];
    public MapLayer objectsLayer;
    public SpawnPlayer spawnPlayer;
    public Array<EnemySpawner> enemySpawners;
    public Array<Exit> exits;

    private Rectangle tempRect = new Rectangle();
    private Array<Rectangle> tileRects = new Array<>();
    public Pool<Rectangle> rectPool = Pools.get(Rectangle.class);

    public Level(LevelDescriptor levelDescriptor, GameScreen screen) {
        Gdx.app.log("Map", "Loading map: '" + levelDescriptor.toString() + "'");

        this.assets = screen.game.assets;
        this.screen = screen;

        // Load map
        this.map = (new TmxMapLoader()).load(levelDescriptor.mapFileName, new TmxMapLoader.Parameters() {{
            generateMipMaps = true;
            textureMinFilter = Texture.TextureFilter.MipMap;
            textureMagFilter = Texture.TextureFilter.MipMap;
        }});
        this.mapRenderer = new OrthoCachedTiledMapRenderer(map);
        ((OrthoCachedTiledMapRenderer) mapRenderer).setBlending(true);

        // Load map properties
        this.name = map.getProperties().get("name", "[UNNAMED]", String.class);

        // Validate map layers
        MapLayers layers = map.getLayers();
        collisionLayer = (TiledMapTileLayer) layers.get("collision");
        backgroundLayer = (TiledMapTileLayer) layers.get("background");
        objectsLayer = layers.get("objects");
        if (collisionLayer == null || objectsLayer == null) {
            throw new GdxRuntimeException("Missing required map layer. (required: 'collision', 'objects')");
        } else {
            for (int i = 0; i < layers.size(); ++i) {
                MapLayer layer = layers.get(i);
                if      (layer.getName().equalsIgnoreCase("collision"))  collisionLayerIndex [0] = i;
                else if (layer.getName().equalsIgnoreCase("background")) backgroundLayerIndex[0] = i;
            }
        }

        // Load map objects
        enemySpawners = new Array<EnemySpawner>();
        exits = new Array<Exit>();
        MapObjects objects = objectsLayer.getObjects();
        for (MapObject object : objects) {
            MapProperties props = object.getProperties();
            String type = (String) props.get("type");
            if (type == null) {
                Gdx.app.log("Map", "Map object missing 'type' property");
                continue;
            }

            if ("spawnPlayer".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);
                spawnPlayer = new SpawnPlayer(x, y, assets);
            }
            else if ("spawnEnemy".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);

//                String directionProp = props.get("direction", "left", String.class).toLowerCase();
//                GameEntity.Direction direction = GameEntity.Direction.left;
//                if ("left".equals(directionProp)) direction = GameEntity.Direction.left;
//                else if ("right".equals(directionProp)) direction = GameEntity.Direction.right;
//                else Gdx.app.log("Map", "Unknown direction for spawnEnemy: '" + directionProp + "'");
//
//                String name = object.getName().toLowerCase();
//                EnemySpawner.EnemyType enemyType = null;
//                if      ("chicken" .equals(name)) enemyType = EnemySpawner.EnemyType.chicken;
//                else if ("bunny"   .equals(name)) enemyType = EnemySpawner.EnemyType.bunny;
//                else Gdx.app.log("Map", "Unknown enemy type for spawnEnemy entity: '" + name + "'");
//
//                if (enemyType != null) {
//                    EnemySpawner spawner = new EnemySpawner(x, y, enemyType, direction);
//                    enemySpawners.add(spawner);
//                    spawner.spawnEnemy(screen);
//                }
            }
            else if ("exit".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);
                exits.add(new Exit(x, y, assets));
            }
        }

        // Validate that we have required entities
        if (spawnPlayer == null) {
            throw new GdxRuntimeException("Missing required map object: 'spawnPlayer'");
        }
    }

    public void update(float dt) {
    }

//    private Rectangle entityBounds = new Rectangle();
//    public void handleObjectInteractions(GameEntity entity) {
//        for (Spring spring : springs) {
//            if (spring.springing) continue;
//
//            entityBounds.set(entity.position.x + entity.collisionBoundsOffsets.x, entity.position.y + entity.collisionBoundsOffsets.y, entity.collisionBoundsOffsets.width, entity.collisionBoundsOffsets.height);
//            if (spring.bounds.overlaps(entityBounds)) {
//                spring.trigger();
//                float multiplier = 1.5f;
//                if (entity.groundPoundDelay > 0f) {
//                    entity.groundPoundDelay = 0f;
//                    multiplier = 1.75f;
//                }
//                screen.audio.playSound(Audio.Sounds.Spring, entity.position, screen.player.position);
//                entity.bounce(multiplier, spring.orientation);
//            }
//        }
//        for (Tack tack : tacks) {
//            entityBounds.set(entity.position.x + entity.collisionBoundsOffsets.x, entity.position.y + entity.collisionBoundsOffsets.y, entity.collisionBoundsOffsets.width, entity.collisionBoundsOffsets.height);
//            if (Intersector.intersectRectangles(tack.bounds, entityBounds, tempRect)) {
//                if (entity instanceof Player) {
//                    entity.getHurt(tempRect);
//                } else {
//                    entity.stun();
//                }
//            }
//        }
//    }

    public void render(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public void renderBackgroundLayer(OrthographicCamera camera) {
        if (backgroundLayer == null) return;
        mapRenderer.setView(camera);
        mapRenderer.render(backgroundLayerIndex);
    }

    public void renderCollisionLayer(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render(collisionLayerIndex);
    }

    public void renderForegroundLayer(OrthographicCamera camera) {
    }

    public void renderObjects(SpriteBatch batch, OrthographicCamera camera) {
        // TODO: only render if within current view...
    }

    public void getTiles(float startX, float startY, float endX, float endY, Array<Rectangle> tiles) {
        if (startX > endX) {
            float t = startX;
            startX = endX;
            endX = t;
        }
        if (startY > endY) {
            float t = startY;
            startY = endY;
            endY = t;
        }

        rectPool.freeAll(tiles);
        tiles.clear();

        int iStartX = (int) (startX / collisionLayer.getTileWidth());
        int iStartY = (int) (startY / collisionLayer.getTileHeight());
        int iEndX   = (int) (endX   / collisionLayer.getTileWidth());
        int iEndY   = (int) (endY   / collisionLayer.getTileHeight());
        for (int y = iStartY; y <= iEndY; y++) {
            for (int x = iStartX; x <= iEndX; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x * collisionLayer.getTileWidth(),
                             y * collisionLayer.getTileHeight(),
                             collisionLayer.getTileWidth(),
                             collisionLayer.getTileHeight());
                    tiles.add(rect);
                }
            }
        }
    }

}
