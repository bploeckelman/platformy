package lando.systems.platformy.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.platformy.ControllerInput;
import lando.systems.platformy.Game;
import lando.systems.platformy.entities.GameEntity;
import lando.systems.platformy.entities.Player;
import lando.systems.platformy.screenshake.ScreenShakeCameraController;
import lando.systems.platformy.world.Level;
import lando.systems.platformy.world.LevelDescriptor;

public class GameScreen extends BaseScreen {

    public boolean firstRun;

    private float cameraHorMargins = 100;
    private float cameraVertMargins = 20;
    private float cameraVertJumpMargin = 150;

    public Level level;
    public Player player;
    public ScreenShakeCameraController shaker;
    public Hud hud;
    public Array<GameEntity> gameEntities = new Array<>();
    public ControllerInput controllers;

    public GameScreen(Game game) {
        super(game);
        firstRun = true;

        shaker = new ScreenShakeCameraController(worldCamera);
        hud = new Hud(this);

        this.controllers = game.controllers;

        loadLevel(LevelDescriptor.demo);
    }

    private void loadLevel(LevelDescriptor levelDescriptor) {
        switch (levelDescriptor) {
            case demo: level = new Level(levelDescriptor, this); break;
        }
        player = new Player(this, level.spawnPlayer.pos.x, level.spawnPlayer.pos.y);
    }

    @Override
    public void update(float dt) {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop
         && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (!firstRun && !allowInput) return;
        firstRun = false;

        player.update(dt);
        level.update(dt);

        handleCameraConstraints();
        shaker.update(dt);
        hud.update(dt);

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            Gdx.app.debug("Controllers", "num connected: " + Controllers.getControllers().size);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(shaker.getCombinedMatrix());
        {
//            level.render(shaker.getViewCamera());
            level.renderBackgroundLayer(shaker.getViewCamera());
            level.renderForegroundLayer(shaker.getViewCamera());
            batch.begin();
            {
                player.render(batch);
//            }
//            batch.end();
//            batch.begin();
//            {
                level.renderObjects(batch, shaker.getViewCamera());
            }
            batch.end();

        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            hud.render(batch, hudCamera);
        }
        batch.end();
    }

    public void handleCameraConstraints() {
        float playerX = player.position.x + player.width / 2f;
        if (playerX < cameraTargetPos.x - cameraHorMargins) cameraTargetPos.x = playerX + cameraHorMargins;
        if (playerX > cameraTargetPos.x + cameraHorMargins) cameraTargetPos.x = playerX - cameraHorMargins;

        float playerY = player.position.y + player.height / 2f;
        if (playerY < cameraTargetPos.y - cameraVertMargins) cameraTargetPos.y = playerY + cameraVertMargins;
        if (player.grounded) {
            if (playerY > cameraTargetPos.y + cameraVertMargins) cameraTargetPos.y = playerY - cameraVertMargins;
        } else {
            if (playerY > cameraTargetPos.y + cameraVertJumpMargin) cameraTargetPos.y = playerY - cameraVertJumpMargin;
        }


        float cameraLeftEdge = worldCamera.viewportWidth / 2f;
        cameraTargetPos.x = MathUtils.clamp(cameraTargetPos.x, cameraLeftEdge, level.collisionLayer.getWidth() * level.collisionLayer.getTileWidth() - cameraLeftEdge);

        float cameraVertEdge = worldCamera.viewportHeight / 2f;
        cameraTargetPos.y = MathUtils.clamp(cameraTargetPos.y, cameraVertEdge, level.collisionLayer.getHeight() * level.collisionLayer.getTileHeight() - cameraVertEdge);

//        targetZoom.setValue(1 + Math.abs(player.velocity.y / 2000f));

        updateCamera();
    }

}
