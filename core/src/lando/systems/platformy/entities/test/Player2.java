package lando.systems.platformy.entities.test;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.platformy.Assets;

import static lando.systems.platformy.entities.test.CharacterState.*;

public class Player2 extends MovingObject {

    public ObjectMap<Action, Boolean> inputs;
    public ObjectMap<Action, Boolean> prevInputs;
    public ObjectMap<CharacterState, Animation<TextureRegion>> animations;

    public CharacterState prevState;
    public CharacterState currentState;
    public CharacterFacing prevFacing;
    public CharacterFacing currentFacing;

    public float animStateTime;
    public TextureRegion keyframe;
    public Animation<TextureRegion> currentAnimation;

    public float jumpSpeed;
    public float walkSpeed;
    public float runModifier;
    public float gravity;
    public float maxFallSpeed;
    public float minJumpSpeed;

    public Player2(Assets assets, float x, float y) {
        super(x, y);
        this.inputs = new ObjectMap<>();
        this.prevInputs = new ObjectMap<>();
        for (Action action : Action.values()) {
            this.inputs.put(action, false);
            this.prevInputs.put(action, false);
        }
        this.animations = assets.characterAnimations;
        this.prevState = CharacterState.stand;
        this.currentState = CharacterState.stand;
        this.prevFacing = CharacterFacing.right;
        this.currentFacing = CharacterFacing.right;
        this.currentAnimation = animations.get(currentState);
        this.animStateTime = 0f;
        // NOTE: think in tiles per second
        float tileSize = 32f;
        this.jumpSpeed    =  40f * tileSize;
        this.walkSpeed    =  30f * tileSize;
        this.runModifier  =  2f;
        this.gravity      = -400f * tileSize;
        this.maxFallSpeed = -500f * tileSize;
        this.minJumpSpeed =  10f * tileSize;
    }

    @Override
    public void update(float dt) {
        // Handle state specific updates
        prevState  = currentState;
        prevFacing = currentFacing;
        switch (currentState) {
            case stand: {
                // Start with nothing
                speed.set(Vector2.Zero);

                // Check for groundedness
                if (!onGround) {
                    currentState = jump_down;
                    break;
                }

                // Check for movement input
                if (inputState(Action.right) != inputState(Action.left)) {
                    currentState = CharacterState.walk;

                    if (inputState(Action.right)) {
                        currentFacing = CharacterFacing.right;
                    } else if (inputState(Action.left)) {
                        currentFacing = CharacterFacing.left;
                    }
                    break;
                } else if (inputState(Action.jump)) {
                    speed.y = jumpSpeed;
                    currentState = jump_up;
                    break;
                }
            } break;
            case walk:
            case run: {
                if (inputState(Action.run)) {
                    currentState = CharacterState.run;
                }

                if (inputState(Action.right) == inputState(Action.left)) {
                    currentState = CharacterState.stand;
                    speed.set(Vector2.Zero);
                    break;
                }
                else if (inputState(Action.right)) {
                    if (pushesRightWall) {
                        speed.x = 0f;
                    } else {
                        if (inputState(Action.run)) {
                            speed.x = walkSpeed * runModifier;
                        } else {
                           speed.x = walkSpeed;
                        }
                    }
                    scale.x = Math.abs(scale.x);
                }
                else if (inputState(Action.left)) {
                    if (pushesLeftWall) {
                        speed.x = 0f;
                    } else {
                        if (inputState(Action.run)) {
                            speed.x = -walkSpeed * runModifier;
                        } else {
                            speed.x = -walkSpeed;
                        }
                    }
                    scale.x = -Math.abs(scale.x);
                }

                if (inputState(Action.jump)) {
                    currentState = jump_up;
                    speed.y = jumpSpeed;
                    break;
                }
                else if (!onGround) {
                    currentState = jump_up;
                    break;
                }
            } break;
            case jump_up:
            case jump_down: {
                speed.y += gravity * dt;
                speed.y = Math.max(speed.y, maxFallSpeed);

                if (onGround) {
                    currentState = CharacterState.stand;
                    break;
                }

                if (inputState(Action.right) == inputState(Action.left)) {
                    speed.x = 0f;
                }
                else if (inputState(Action.right)) {
                    if (pushesRightWall) {
                        speed.x = 0f;
                    } else {
                        if (inputState(Action.run)) {
                            speed.x = walkSpeed * runModifier;
                        } else {
                            speed.x = walkSpeed;
                        }
                    }
                    scale.x = Math.abs(scale.x);
                    currentFacing = CharacterFacing.right;
                }
                else if (inputState(Action.left)) {
                    if (pushesLeftWall) {
                        speed.x = 0f;
                    } else {
                        if (inputState(Action.run)) {
                            speed.x = -walkSpeed * runModifier;
                        } else {
                            speed.x = -walkSpeed;
                        }
                    }
                    scale.x = -Math.abs(scale.x);
                    currentFacing = CharacterFacing.left;
                }

                // Jump speed varies based on how long the jump button is pressed
                // This sort of works in reverse, when a jump press is detected in other states, speed.y is set to jumpSpeed right away
                // here, if we're already jumping, but not still pressing the jump key, speed.y is clamped to minJumpSpeed
                if (!inputState(Action.jump) && speed.y > 0f) {
                    speed.y = Math.min(speed.y, minJumpSpeed);
                }
            } break;
            case ledge_grab: {
                // TODO: implement later
            } break;
        }

        super.update(dt);

        // Update animation if appropriate
        animStateTime += dt;
        if (currentState != prevState) {
            animStateTime = 0f;
        }
        if (currentState == jump_up || currentState == jump_down) {
            if      (Math.signum(speed.y) ==  1) currentAnimation = animations.get(jump_up);
            else if (Math.signum(speed.y) == -1) currentAnimation = animations.get(jump_down);
        } else {
            currentAnimation = animations.get(currentState);
        }

        // Update keyframe
        keyframe = currentAnimation.getKeyFrame(animStateTime);

        // TODO: play sounds based on boolean flags

        // Update previous inputs
        for (Action action : Action.values()) {
            prevInputs.put(action, inputs.get(action));
        }
    }

    // ------------------------------------------------------------------------
    // Action state checks
    // ------------------------------------------------------------------------

    public boolean released(Action action) {
        return (!inputs.get(action) && prevInputs.get(action));
    }

    public boolean pressed(Action action) {
        return (inputs.get(action) && !prevInputs.get(action));
    }

    public boolean inputState(Action action) {
        return inputs.get(action);
    }

}
