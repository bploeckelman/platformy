package lando.systems.platformy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.platformy.Assets;

public class SpawnPlayer {

    public Vector2 pos;
    public float size = 32f;
    public TextureRegion texture;

    public SpawnPlayer(float x, float y, Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
    }

    public void update() {}

    public void render(SpriteBatch batch) {
        batch.setColor(Color.GREEN);
        batch.draw(texture, pos.x - size / 2f, pos.y - size / 2f, size, size);
        batch.setColor(Color.WHITE);
    }

}
