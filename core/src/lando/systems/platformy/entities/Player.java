package lando.systems.platformy.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.mappings.Xbox;
import lando.systems.platformy.screens.GameScreen;

public class Player extends GameEntity {

    enum JumpState { none, jumping }

    private JumpState jumpState;

    private final float jumpVelocity = 900f;
    private final float horizontalSpeed = 50f;
    private final float horizontalSpeedMinThreshold = 10f;
    private final float horizontalJoystickThreshold = 0.2f;

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation);
        this.collisionBoundsOffsets.set(0f, 0f, keyframe.getRegionWidth(), keyframe.getRegionHeight());
        this.position.set(x, y);
        this.jumpState = JumpState.none;
    }

    @Override
    public void update(float dt) {
        // Horizontal ----------------------------------------

        // Check for and apply horizontal movement
        float horizAxisMovement = screen.controllers.getController().getAxis(Xbox.L_STICK_HORIZONTAL_AXIS);
        boolean moveLeftPressed = Gdx.input.isKeyPressed(Input.Keys.A)
                               || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                               || horizAxisMovement < -horizontalJoystickThreshold;
        boolean moveRightPressed = Gdx.input.isKeyPressed(Input.Keys.D)
                                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                                || horizAxisMovement > horizontalJoystickThreshold;
        if (moveLeftPressed) {
            velocity.add(-horizontalSpeed, 0);
            direction = Direction.left;
        } else if (moveRightPressed) {
            velocity.add(horizontalSpeed, 0);
            direction = Direction.right;
        }

        // Apply horizontal drag
        if (!grounded && !moveLeftPressed && !moveRightPressed) {
            velocity.x = 0f;
        } else {
            velocity.x *= .85f;
        }

        // Clamp minimum horizontal velocity to zero
        if (Math.abs(velocity.x) < horizontalSpeedMinThreshold) {
            velocity.x = 0f;
        }

        // Vertical ------------------------------------------

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                           || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)
                           || screen.controllers.getController().getButton(Xbox.A);
        if (jumpPressed) {
            jump();
        }

        super.update(dt);
    }

    private void jump() {
        jump(1f);
    }

    private void jump(float velocityMultiplier) {
        if (grounded) {
            velocity.y = jumpVelocity * velocityMultiplier;
            jumpState = JumpState.jumping;
            grounded = false;
        }
    }

    @Override
    public void changeDirection() {
        // noop so it doesn't flip rapidly when pushing against a wall.
    }

}
