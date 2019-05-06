package lando.systems.platformy.screens;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.platformy.entities.Player;

public class Hud {

    public Player player;
    public GameScreen screen;

    private Array<Rectangle> bounds;

    public Hud(GameScreen screen) {
        this.screen = screen;
        this.player = screen.player;
        this.bounds = new Array<>();
    }

    public void update(float dt) {
        Array<Controller> controllers = Controllers.getControllers();
        if (bounds.size != controllers.size) {
            bounds.clear();

            float scale = 2f;
            float size = scale * 32f;
            float margin = 10f;
            float x = margin;
            float y = screen.hudCamera.viewportHeight - margin - size;
            for (Controller controller : controllers) {
                bounds.add(new Rectangle(x, y, size, size));
                x += size + margin;
            }
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        // Draw connected controllers
        for (Rectangle bound : bounds) {
            batch.setColor(0.2f, 0.4f, 0.2f, 0.5f);
            batch.draw(screen.game.assets.whitePixel, bound.x, bound.y, bound.width, bound.height);

            batch.setColor(Color.WHITE);
            batch.draw(screen.game.assets.xboxControllerIcon, bound.x, bound.y, bound.width, bound.height);

            batch.setColor(Color.WHITE);
            screen.game.assets.ninePatch.draw(batch, bound.x, bound.y, bound.width, bound.height);
        }

        if (screen.player.state != null) {
            screen.game.assets.font.draw(batch, "Player state: " + screen.player.state.name(), 10f, 25f);
            screen.game.assets.font.draw(batch, String.format("Player veloc: (%2.1f, %2.1f)", screen.player.velocity.x, screen.player.velocity.y), 10f, 50f);
        }
    }

}
