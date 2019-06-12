package lando.systems.platformy.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.platformy.Assets;
import lando.systems.platformy.Game;
import lando.systems.platformy.entities.Avatar;
import lando.systems.platformy.entities.test.Action;
import lando.systems.platformy.entities.test.AvatarInput;
import lando.systems.platformy.entities.test.Map;

public class SepAxisScreen extends BaseScreen {

    private Assets assets;
    private Map map;

    private Avatar player;
    private AvatarInput input;

    private TextureRegion arrowUp;
    private TextureRegion arrowRight;

    static class OnScreenTiles {
        static final int numWide = 30;
        static final int numHigh = 18;
    }

    public SepAxisScreen(Game game) {
        super(game);
        this.assets = game.assets;
        this.map = new Map(assets);

        this.worldCamera.setToOrtho(false, OnScreenTiles.numWide, OnScreenTiles.numHigh);
        this.worldCamera.update();
        this.cameraTargetPos.set(this.worldCamera.position);

        this.arrowUp = assets.atlas.findRegion("arrow-up");
        this.arrowRight = assets.atlas.findRegion("arrow-right");

        this.player = new Avatar(assets, 5, 5, map);

        this.input = new AvatarInput(player);
        Controllers.addListener(input);
        Gdx.input.setInputProcessor(new InputMultiplexer(input, this));
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        input.update(dt);
        player.update(dt);
        map.update(dt);

        cameraTargetPos.set(player.bounds.center, 0f);
        updateCamera();
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            map.render(batch);
            player.render(batch);

            batch.setColor(Color.RED);   batch.draw(arrowRight, 0f, -1f / 2f, 1f, 1f / 2f);
            batch.setColor(Color.GREEN); batch.draw(arrowUp,    -1f / 2f, 0f, 1f / 2f, 1f);

            batch.setColor(Color.WHITE);
        }
        batch.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            float margin = 10f;
            assets.layout.setText(assets.font, "Separating Axis Test");
            float y = hudCamera.viewportHeight - margin;
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Pos: (%2.1f, %2.1f)", player.bounds.center.x, player.bounds.center.y));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Vel: (%2.2f, %2.2f)", player.velocity.x, player.velocity.y));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin + margin;


            assets.layout.setText(assets.font, String.format("Run: %b", player.currentInputs.get(Action.run)));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Jmp: %b", player.currentInputs.get(Action.jump)));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Atk: %b", player.currentInputs.get(Action.attack)));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Rop: %b", player.currentInputs.get(Action.rope)));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;

            assets.layout.setText(assets.font, String.format("Bom: %b", player.currentInputs.get(Action.bomb)));
            assets.font.draw(batch, assets.layout, margin, y);
            y -= assets.layout.height + margin;
        }
        batch.end();
    }

    @Override
    public boolean scrolled(int amount) {
        targetZoom.setValue(targetZoom.floatValue() + 0.1f * amount);
        return true;
    }

}
