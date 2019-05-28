package lando.systems.platformy.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.platformy.Assets;
import lando.systems.platformy.Game;
import lando.systems.platformy.entities.Avatar;
import lando.systems.platformy.entities.test.*;

public class SepAxisScreen extends BaseScreen {

    private Assets assets;
    private Map map;

    private AABB playerBounds;

    static class Environment {
        public static float gravity = -1000f;
    }

    private float animStateTime;
    private Animation<TextureRegion> playerIdleAnim;

    private Avatar player;
    private AvatarInput input;

    private TextureRegion arrowUp;
    private TextureRegion arrowRight;

    public SepAxisScreen(Game game) {
        super(game);
        this.assets = game.assets;
        this.map = new Map(assets);
        this.animStateTime = 0f;
        this.playerIdleAnim = assets.characterAnimations.get(CharacterState.stand);

        float playerX = 6f * Map.tile_size;
        float playerY = 4f * Map.tile_size;
        this.playerBounds = new AABB(playerX, playerY,
                                     playerIdleAnim.getKeyFrame(0f).getRegionWidth() / 2f,
                                     playerIdleAnim.getKeyFrame(0f).getRegionHeight() / 2f);

        this.arrowUp = assets.atlas.findRegion("arrow-up");
        this.arrowRight = assets.atlas.findRegion("arrow-right");

        this.player = new Avatar(assets, playerX, playerY, map);

//        worldCamera.translate(0f, -10f);
//        worldCamera.update();

        this.input = new AvatarInput(player);
        Controllers.addListener(input);
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        animStateTime += dt;

        input.update(dt);
        player.update(dt);

//        playerBounds.center.y += Environment.gravity * dt;
//        if (playerBounds.center.y - playerBounds.halfSize.y < Map.tile_size) {
//            playerBounds.center.y = playerBounds.halfSize.y + Map.tile_size;
//        }

        cameraTargetPos.set(playerBounds.center, 0f);
        updateCamera();
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            map.render(batch);

//            batch.draw(assets.whiteCircleOutline, //playerIdleAnim.getKeyFrame(animStateTime),
//                       playerBounds.center.x - playerBounds.halfSize.x,
//                       playerBounds.center.y - playerBounds.halfSize.y,
//                       2f * playerBounds.halfSize.x, 2f * playerBounds.halfSize.y);

            player.render(batch);

            batch.setColor(Color.RED);
            batch.draw(arrowRight, 8f, 0f, 16f, 8f);
            batch.setColor(Color.GREEN);
            batch.draw(arrowUp, 0f, 8f, 8f, 16f);

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

}
