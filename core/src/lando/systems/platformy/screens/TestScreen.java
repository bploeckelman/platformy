package lando.systems.platformy.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.platformy.Assets;
import lando.systems.platformy.Config;
import lando.systems.platformy.Game;
import lando.systems.platformy.entities.test.Map;
import lando.systems.platformy.entities.test.Player2;
import lando.systems.platformy.entities.test.PlayerInput;

public class TestScreen extends BaseScreen {

    private Assets assets;
    private Player2 player;
    private PlayerInput playerInput;
    private Map map;

    public TestScreen(Game game) {
        super(game);
        this.assets = game.assets;
        this.player = new Player2(assets, 100f, 100f);
        this.playerInput = new PlayerInput(player);
        this.map = new Map(assets);

        cameraOverride = true;
        worldCamera.translate(0f, -20f);
        worldCamera.update();

        Gdx.input.setInputProcessor(playerInput);
        Controllers.addListener(playerInput);
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        map.update(dt);
        player.update(dt, map);

        updateCamera();
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            // tile map
            map.render(batch);

            // ground
            batch.draw(assets.whitePixel, -1000f, -10f, 2000f, 10f);

            // player
            float x = player.prevPosition.x;
            float y = player.prevPosition.y;
            float w = player.keyframe.getRegionWidth();
            float h = player.keyframe.getRegionHeight();
            float rotation = 0f;
            batch.setColor(1f, 1f, 1f, 0.1f);
            batch.draw(player.keyframe, x, y, w / 2f, h / 2f, w, h, player.scale.x, player.scale.y, rotation);
            batch.setColor(Color.WHITE);
            x = player.position.x;
            y = player.position.y;
            batch.draw(player.keyframe, x, y, w / 2f, h / 2f, w, h, player.scale.x, player.scale.y, rotation);

            // player bounds (debug)
            if (Config.debug) {
                batch.setColor(Color.YELLOW);
                assets.ninePatch.draw(batch, player.position.x, player.position.y,
                                      player.keyframe.getRegionWidth(),
                                      player.keyframe.getRegionHeight());
                batch.setColor(1f, 0f, 0f, 0.5f);
                assets.ninePatch.draw(batch,
                                      player.bounds.center.x - player.bounds.halfSize.x,
                                      player.bounds.center.y - player.bounds.halfSize.y,
                                      player.bounds.halfSize.x * 2f,
                                      player.bounds.halfSize.y * 2f);
                batch.setColor(Color.WHITE);
            }
        }
        batch.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            assets.layout.setText(assets.font, "Player state: " + player.currentState.name());
            assets.font.draw(batch, assets.layout, 10f, hudCamera.viewportHeight - 10f);
            assets.layout.setText(assets.font, "Player facing: " + player.currentFacing.name());
            assets.font.draw(batch, assets.layout, 10f, hudCamera.viewportHeight - 10f - assets.layout.height - 10f);
        }
        batch.end();
    }

}
