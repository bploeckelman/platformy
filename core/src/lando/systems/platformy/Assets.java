package lando.systems.platformy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.platformy.entities.test.CharacterState;

public class Assets implements Disposable {

    // Initialize descriptors for all assets
    private final AssetDescriptor<TextureAtlas> atlasAsset = new AssetDescriptor<>("images/sprites.atlas", TextureAtlas.class);
    private final AssetDescriptor<Texture> pixelTextureAsset = new AssetDescriptor<>("images/pixel.png", Texture.class);
    private final AssetDescriptor<BitmapFont> pixelFont16Asset = new AssetDescriptor<>("fonts/emulogic-16pt.fnt", BitmapFont.class);

//    private final ShaderProgramLoader.ShaderProgramParameter defaultVertParam = new ShaderProgramLoader.ShaderProgramParameter() {{ vertexFile = "shaders/vertex/default.vert"; }};


    public enum Loading { SYNC, ASYNC }


    public boolean initialized;
    public SpriteBatch batch;
    public GlyphLayout layout;
    public AssetManager mgr;
    public Texture pixel;
    public TextureAtlas atlas;
    public TextureRegion debugTexture;
    public TextureRegion whitePixel;
    public TextureRegion whiteCircleOutline;
    public TextureRegion xboxControllerIcon;

    public Animation<TextureRegion> playerAnimation;
    public ObjectMap<CharacterState, Animation<TextureRegion>> characterAnimations;

    public NinePatch ninePatch;
    public BitmapFont font;

    public ShaderProgram loadingShader;
    public Array<ShaderProgram> randomTransitions;
    public ShaderProgram blindsShader;
    public ShaderProgram fadeShader;
    public ShaderProgram radialShader;
    public ShaderProgram doomShader;
    public ShaderProgram pixelizeShader;
    public ShaderProgram doorwayShader;
    public ShaderProgram crosshatchShader;
    public ShaderProgram rippleShader;
    public ShaderProgram heartShader;
    public ShaderProgram stereoShader;
    public ShaderProgram circleCropShader;

    public Assets() {
        this(Loading.SYNC);
    }

    public Assets(Loading loading) {
        // Let us write shitty shader programs
        ShaderProgram.pedantic = false;

        loadingShader = loadShader("shaders/vertex/loading.vert",
                                   "shaders/fragment/loading.frag");

        initialized = false;

        batch = new SpriteBatch();
        layout = new GlyphLayout();

        mgr = new AssetManager();
        mgr.load(atlasAsset);
        mgr.load(pixelTextureAsset);
        mgr.load(pixelFont16Asset);

        if (loading == Loading.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (!mgr.update()) return mgr.getProgress();
        if (initialized) return 1f;
        initialized = true;

        pixel = mgr.get(pixelTextureAsset);

        // Cache TextureRegions from TextureAtlas in fields for quicker access
        atlas = mgr.get(atlasAsset);
        debugTexture = atlas.findRegion("white-circle");
        whitePixel = atlas.findRegion("white-pixel");
        whiteCircleOutline = atlas.findRegion("white-circle-outline");
        xboxControllerIcon = atlas.findRegion("ui/xbox-controller-icon");

        Array<TextureAtlas.AtlasRegion> playerAnimFrames = atlas.findRegions("bunny");
        playerAnimation = new Animation<>(0.1f, playerAnimFrames, Animation.PlayMode.LOOP);

        characterAnimations = new ObjectMap<>();
        characterAnimations.put(CharacterState.stand,      new Animation<>(0.01f, atlas.findRegions("character/char-idle"),        Animation.PlayMode.LOOP));
        characterAnimations.put(CharacterState.walk,       new Animation<>(0.01f, atlas.findRegions("character/char-run-right")  , Animation.PlayMode.LOOP));
        characterAnimations.put(CharacterState.run,        new Animation<>(0.005f, atlas.findRegions("character/char-run-right")  , Animation.PlayMode.LOOP));
        characterAnimations.put(CharacterState.jump_up,    new Animation<>(0.01f, atlas.findRegions("character/char-jump-up"),     Animation.PlayMode.LOOP));
        characterAnimations.put(CharacterState.jump_down,  new Animation<>(0.01f, atlas.findRegions("character/char-jump-down"),   Animation.PlayMode.LOOP));
        characterAnimations.put(CharacterState.ledge_grab, new Animation<>(0.01f, atlas.findRegions("character/char-stand-ready"), Animation.PlayMode.LOOP));

        ninePatch = new NinePatch(atlas.findRegion("ui/ninepatch-screws"), 6, 6, 6, 6);

        font = mgr.get(pixelFont16Asset);

        randomTransitions = new Array<>();
        blindsShader     = loadShader("shaders/vertex/default.vert", "shaders/fragment/blinds.frag");
        fadeShader       = loadShader("shaders/vertex/default.vert", "shaders/fragment/dissolve.frag");
        radialShader     = loadShader("shaders/vertex/default.vert", "shaders/fragment/radial.frag");
        doomShader       = loadShader("shaders/vertex/default.vert", "shaders/fragment/doomdrip.frag");
        pixelizeShader   = loadShader("shaders/vertex/default.vert", "shaders/fragment/pixelize.frag");
        doorwayShader    = loadShader("shaders/vertex/default.vert", "shaders/fragment/doorway.frag");
        crosshatchShader = loadShader("shaders/vertex/default.vert", "shaders/fragment/crosshatch.frag");
        rippleShader     = loadShader("shaders/vertex/default.vert", "shaders/fragment/ripple.frag");
        heartShader      = loadShader("shaders/vertex/default.vert", "shaders/fragment/heart.frag");
        stereoShader     = loadShader("shaders/vertex/default.vert", "shaders/fragment/stereo.frag");
        circleCropShader = loadShader("shaders/vertex/default.vert", "shaders/fragment/circlecrop.frag");

        randomTransitions.add(blindsShader);
        randomTransitions.add(fadeShader);
        randomTransitions.add(radialShader);
        randomTransitions.add(doomShader);
        randomTransitions.add(pixelizeShader);
        randomTransitions.add(doorwayShader);
        randomTransitions.add(crosshatchShader);
        randomTransitions.add(rippleShader);
        randomTransitions.add(heartShader);
        randomTransitions.add(stereoShader);
        randomTransitions.add(circleCropShader);

        return 1f;
    }

    @Override
    public void dispose() {
        mgr.clear();
        batch.dispose();
    }

    private static ShaderProgram loadShader(String vertSourcePath, String fragSourcePath) {
        ShaderProgram.pedantic = false;
        ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal(vertSourcePath),
                Gdx.files.internal(fragSourcePath));

        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("LoadShader", "compilation failed:\n" + shaderProgram.getLog());
            throw new GdxRuntimeException("LoadShader: compilation failed:\n" + shaderProgram.getLog());
        } else if (Config.debug){
            Gdx.app.debug("LoadShader", "ShaderProgram compilation log: " + shaderProgram.getLog());
        }

        return shaderProgram;
    }

}
