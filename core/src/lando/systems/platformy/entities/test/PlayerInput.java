package lando.systems.platformy.entities.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Xbox;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.controllers.PovDirection.*;

public class PlayerInput implements ControllerListener, InputProcessor {

    public Player2 player;

    public PlayerInput(Player2 player) {
        this.player = player;
    }

    // ------------------------------------------------------------------------
    // ControllerListener interface
    // ------------------------------------------------------------------------

    @Override
    public void connected(Controller controller) {
        Gdx.app.log("CONTROLLER", "Connected: " + controller.getName());
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("CONTROLLER", "Disconnected: " + controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        boolean inputWasProcessed = false;
        if (buttonCode == Xbox.A) {
            player.inputs.put(Action.jump, true);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        boolean inputWasProcessed = false;
        if (buttonCode == Xbox.A) {
            player.inputs.put(Action.jump, false);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        boolean inputWasProcessed = false;
        Gdx.app.log("CONTROLLER", "axis moved: value = " + String.format("%2.2f", value));
        float deadZone = 0.15f;
        if (axisCode == Xbox.L_STICK_HORIZONTAL_AXIS && Math.abs(value) > deadZone) {
            if (Math.signum(value) == -1) {
                player.inputs.put(Action.left, true);
                inputWasProcessed = true;
            }
            else if (Math.signum(value) == 1) {
                player.inputs.put(Action.right, true);
                inputWasProcessed = true;
            }
            else {
                // TODO: is this going to interfere with keyboard input?
                player.inputs.put(Action.left, false);
                player.inputs.put(Action.right, false);
            }
        } else {
            // TODO: is this going to interfere with keyboard input?
            player.inputs.put(Action.left, false);
            player.inputs.put(Action.right, false);
        }
        return inputWasProcessed;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection direction) {
        boolean inputWasProcessed = false;
        if (direction == east || direction == northEast || direction == southEast) {
            player.inputs.put(Action.right, true);
            inputWasProcessed = true;
        }
        else if (direction == west || direction == northWest || direction == southWest) {
            player.inputs.put(Action.left, true);
            inputWasProcessed = true;
        } else {
            // TODO: is this going to interfere with keyboard input?
            player.inputs.put(Action.left, false);
            player.inputs.put(Action.right, false);
        }
        return inputWasProcessed;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }

    // ------------------------------------------------------------------------
    // InputProcessor
    // ------------------------------------------------------------------------

    @Override
    public boolean keyDown(int keycode) {
        boolean inputWasProcessed = false;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            player.inputs.put(Action.left, true);
            inputWasProcessed = true;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            player.inputs.put(Action.right, true);
            inputWasProcessed = true;
        }
        if (keycode == Input.Keys.SPACE) {
            player.inputs.put(Action.jump, true);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean inputWasProcessed = false;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            player.inputs.put(Action.left, false);
            inputWasProcessed = true;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            player.inputs.put(Action.right, false);
            inputWasProcessed = true;
        }
        if (keycode == Input.Keys.SPACE) {
            player.inputs.put(Action.jump, false);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

}
