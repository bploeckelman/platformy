package lando.systems.platformy.entities.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.controllers.mappings.Xbox;
import com.badlogic.gdx.math.Vector3;
import lando.systems.platformy.Config;

import static com.badlogic.gdx.controllers.PovDirection.*;

public class PlayerInput implements ControllerListener, InputProcessor {

    public Player2 player;

    public PlayerInput(Player2 player) {
        this.player = player;
    }

    public void update(float dt) {
        if (Controllers.getControllers().isEmpty()) return;

        Controller controller = Controllers.getControllers().get(0);
        if (controller != null) {
            player.inputs.put(Action.left, false);
            player.inputs.put(Action.right, false);

            float leftStickHorz = controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS);
            float leftStickVert = controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS);
            if (Config.debug) {
                Gdx.app.log("CONTROLLER",
                            "horz = " + String.format("%2.2f", leftStickHorz)
                                  + "  vert = " + String.format("%2.2f", leftStickVert));
            }

            float deadZone = 0.15f;
            if (controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) > deadZone) {
                player.inputs.put(Action.right, true);
            }
            else if (controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) < -deadZone) {
                player.inputs.put(Action.left, true);
            }
        }
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
//        if (Config.debug) {
            Gdx.app.log("CONTROLLER", "button down : " + buttonCode);
//        }

        boolean inputWasProcessed = false;
        if (buttonCode == Xbox.A) {
            player.inputs.put(Action.jump, true);
            inputWasProcessed = true;
        }
        if (buttonCode == Xbox.R_TRIGGER) {
            player.inputs.put(Action.run, true);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
//        if (Config.debug) {
            Gdx.app.log("CONTROLLER", "button up: " + buttonCode);
//        }

        boolean inputWasProcessed = false;
        if (buttonCode == Xbox.A) {
            player.inputs.put(Action.jump, false);
            inputWasProcessed = true;
        }
        if (buttonCode == Xbox.R_TRIGGER) {
            player.inputs.put(Action.run, false);
            inputWasProcessed = true;
        }
        return inputWasProcessed;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        boolean inputWasProcessed = false;
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
        if (keycode == Input.Keys.SHIFT_LEFT) {
            player.inputs.put(Action.run, true);
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
        if (keycode == Input.Keys.SHIFT_LEFT) {
            player.inputs.put(Action.run, false);
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
