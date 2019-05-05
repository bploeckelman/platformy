package lando.systems.platformy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.math.Vector3;

public class ControllerInput extends ControllerAdapter {

    private class NopController implements Controller {
        @Override public boolean getButton(int buttonCode) { return false; }
        @Override public float getAxis(int axisCode) { return 0; }
        @Override public PovDirection getPov(int povCode) { return null; }
        @Override public boolean getSliderX(int sliderCode) { return false; }
        @Override public boolean getSliderY(int sliderCode) { return false; }
        @Override public Vector3 getAccelerometer(int accelerometerCode) { return null; }
        @Override public void setAccelerometerSensitivity(float sensitivity) { }
        @Override public String getName() { return null; }
        @Override public void addListener(ControllerListener listener) { }
        @Override public void removeListener(ControllerListener listener) { }
    }
    private Controller nopController;

    public ControllerInput() {
        nopController = new NopController();
    }

    public Controller getController() {
        return getControllerP1();
    }

    public Controller getControllerP1() {
        if (Controllers.getControllers().size == 0) {
            return nopController;
        }
        return Controllers.getControllers().get(0);
    }

    private int indexOf(Controller controller) {
        return Controllers.getControllers().indexOf(controller, true);
    }

    @Override
    public void connected(Controller controller) {
        Gdx.app.debug("Controller", "Connected: " + controller.getName() + " (" + indexOf(controller) + ")");
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.debug("Controller", "Disconnected: " + controller.getName() + " (" + indexOf(controller) + ")");
        if (Controllers.getControllers().size == 0) {
            Gdx.app.debug("Controller", "No controllers connected");
        }
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        Gdx.app.debug("Controller", "Button down: " + buttonIndex + ", for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        Gdx.app.debug("Controller", "Button up: " + buttonIndex + ", for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderIndex, boolean value) {
        Gdx.app.debug("Controller", "X Moved: " + sliderIndex + " ( " + value + "), for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderIndex, boolean value) {
        Gdx.app.debug("Controller", "Y Moved: " + sliderIndex + " (" + value + "), for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

    @Override
    public boolean axisMoved (Controller controller, int axisIndex, float value) {
        if (Math.abs(value) > 0.1f) {
            Gdx.app.debug("Controller", "Axis Moved: " + axisIndex + " (" + value + "), for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        }
        return false;
    }

    @Override
    public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
        Gdx.app.debug("Controller", "Pov Moved: " + povIndex + " (" + value + "), for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

    @Override
    public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
        Gdx.app.debug("Controller", "Accel Moved: " + accelerometerIndex + " (" + value + "), for controller " + controller.getName() + " (" + indexOf(controller) + ")");
        return false;
    }

}
