package lando.systems.platformy.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.platformy.Assets;
import lando.systems.platformy.entities.test.*;

import java.util.Arrays;

import static lando.systems.platformy.entities.test.CharacterFacing.left;
import static lando.systems.platformy.entities.test.CharacterFacing.right;
import static lando.systems.platformy.entities.test.CharacterState.stand;
import static lando.systems.platformy.entities.test.CharacterState.whipping;

// https://github.com/dbeef/spelunky-ds
public class Avatar {

    public ObjectMap<Action, Boolean> currentInput;
    private ObjectMap<Action, Boolean> previousInput;
    private ObjectMap<CharacterState, Animation<TextureRegion>> animations;

    private CharacterState currentState;
    private CharacterState previousState;

    private CharacterFacing currentFacing;
    private CharacterFacing previousFacing;

    private TextureRegion debugTexture;

    static class Flags {
        static boolean crawling              = false;
        static boolean climbing              = false;
        static boolean whipping              = false;
        static boolean canClimbLadder        = false;
        static boolean canClimbRope          = false;
        static boolean onTopOfClimbingSpace  = false;
        static boolean startedClimbingRope   = false;
        static boolean startedClimbingLadder = false;
        static boolean usingCape             = false;
        static boolean usingJetpack          = false;
        static boolean holdingItem           = false;
        static boolean hangingOnTileLeft     = false;
        static boolean hangingOnTileRight    = false;
        static boolean exitingLevel          = false;
    }

    static class Colliding {
        static boolean below = false;
        static boolean above = false;
        static boolean left   = false;
        static boolean right  = false;

        static void reset() {
            below = false;
            above = false;
            left  = false;
            right = false;
        }
    }

    static class Carrying {
        static boolean springShoes = false;
        static boolean cape        = false;
        static boolean jetpack     = false;
    }

    static class Constants {
        static float damping                   = 0.5f;
        static float gravity                   = -1000f;
        static float jumpSpeed                 = 2.55f;
        static float jumpModifierSpringShoes   = 1.65f;
        static int   minHangingTime            = 100; // millis?
        static float horizSpeedDeltaValue      = 500;  // ???
        static float soundReplayTimeClimbing   = 0.5f; // sec?
    }

    private float animStateTime;
    private float timeSinceLastJump;
    private float timeHanging;
    private float timeJumping;
    private float timeClimbing;

    private int numRopes;
    private int numBombs;
    private int jetpackFuel;

    private int currentXInTiles;
    private int currentYInTiles;

    public Vector2 velocity;
    public AABB bounds;
    private Map map;

    public Avatar(Assets assets, float x, float y, Map map) {
        this.currentInput = new ObjectMap<>();
        this.previousInput = new ObjectMap<>();
        Arrays.stream(Action.values()).forEach(action -> {
            currentInput.put(action, false);
            previousInput.put(action, false);
        });
        this.animations = assets.characterAnimations;
        this.currentState = stand;
        this.previousState = stand;
        this.currentFacing = right;
        this.animStateTime = 0f;
        this.timeSinceLastJump = 0f;
        this.timeHanging = 0f;
        this.timeJumping = 0f;
        this.timeClimbing = 0f;
        this.numRopes = 0;
        this.numBombs = 0;
        this.jetpackFuel = 0;
        this.velocity = new Vector2();
        this.bounds = new AABB(x, y,
                               animations.get(stand).getKeyFrame(0f).getRegionWidth() / 2f,
                               animations.get(stand).getKeyFrame(0f).getRegionHeight() / 2f);
        this.currentXInTiles = Math.floorDiv((int) bounds.center.x, (int) Map.tile_size);
        this.currentYInTiles = Math.floorDiv((int) bounds.center.y, (int) Map.tile_size);
        this.map = map;

        this.debugTexture = assets.whitePixel;
    }

    public void update(float dt) {
        previousState = currentState;
        previousFacing = currentFacing;
        currentInput.entries().forEach(current -> previousInput.put(current.key, current.value));
        animStateTime += dt;

        handleInput(dt);

        velocity.y = Constants.gravity;

        if (Math.abs(velocity.x) < 1f) velocity.x = 0f;
        if (Math.abs(velocity.y) < 1f) velocity.y = 0f;

        Colliding.reset();
        Array<Tile> neighborTiles = map.getNeighboringTiles(currentXInTiles, currentYInTiles);
        if (velocity.x > 0f) {
            if (Map.tileIsType(neighborTiles.get(Map.TileGroup.right), Tile.Type.block)) {
                Colliding.right = true;
                velocity.x = 0f;
            }
        } else if (velocity.x < 0f) {
            if (Map.tileIsType(neighborTiles.get(Map.TileGroup.left), Tile.Type.block)) {
                Colliding.left = true;
                velocity.x = 0f;
            }
        }

        if (velocity.y > 0f) {
            if (Map.tileIsType(neighborTiles.get(Map.TileGroup.up_center), Tile.Type.block)) {
                Colliding.above = true;
                velocity.y = 0f;
            }
        } else if (velocity.y < 0f) {
            if (Map.tileIsType(neighborTiles.get(Map.TileGroup.up_center), Tile.Type.block)) {
                Colliding.below = true;
                velocity.y = 0f;
            }
        }

        bounds.center.add(velocity.x * dt, velocity.y * dt);

        velocity.x *= Constants.damping;
    }

    public void render(SpriteBatch batch) {
        Animation<TextureRegion> currentAnimation = animations.get(currentState);
        // TODO: decouple render bounds from collision bounds
        batch.draw(currentAnimation.getKeyFrame(animStateTime),
                   bounds.center.x - bounds.halfSize.x, bounds.center.y - bounds.halfSize.y);
        batch.setColor(Color.MAGENTA);
        batch.draw(debugTexture, bounds.center.x - 2f, bounds.center.y - 2f, 4f, 4f);
        batch.setColor(Color.WHITE);
    }

    private void handleInput(float dt) {
        if (currentState != CharacterState.stunned && currentState != CharacterState.dead) {

            // Check for jump input --------------------------------------------------------
            if (currentInput.get(Action.jump) && timeSinceLastJump > 100f) {

                // If we can jump, do so
                if (Colliding.below || Flags.climbing) {
                    // TODO: play jump sound

                    velocity.y = Constants.jumpSpeed;
                    if (Carrying.springShoes) {
                        velocity.y *= Constants.jumpModifierSpringShoes;
                    }

                    Flags.climbing = false;
                    Flags.canClimbRope = false;
//                    Flags.canClimbLadder = false;
                    Flags.startedClimbingRope = false;
                    Flags.startedClimbingLadder = false;
                    timeSinceLastJump = 0f;
                }
                // Otherwise, if we can use the cape, do so
                else if (Carrying.cape && velocity.y < 0f) {
                    Flags.usingCape = true;
                }

                // Can also jump from a hanging position...
                if ((Flags.hangingOnTileLeft || Flags.hangingOnTileRight) && timeHanging > Constants.minHangingTime && timeSinceLastJump > 100) {
                    // TODO: play jump sound

                    velocity.y = Constants.jumpSpeed;
                    // NOTE: don't apply spring shoes when jumping from a hang...
                    Flags.hangingOnTileLeft = false;
                    Flags.hangingOnTileRight = false;
                    timeHanging = 0f;
                    timeSinceLastJump = 0f;
                }
            }

            // Check for jetpack input -----------------------------------------------------
            if (currentInput.get(Action.jump) && timeSinceLastJump > 100f) {
                Flags.usingJetpack = false;

                if (Carrying.jetpack && jetpackFuel > 0 && !Flags.climbing) {
                    // TODO: play jetpack sound

                    Flags.usingJetpack = true;
                    velocity.y += Constants.jumpSpeed;
                    jetpackFuel -= 1;

                    timeJumping = 0f;
                    timeSinceLastJump = 0f;
                }
            }

            // Check for whip input --------------------------------------------------------
            if (currentInput.get(Action.attack)) {
                if (currentState != CharacterState.stunned && currentState != whipping) {
                    if (Flags.holdingItem) {
                        throwHeldItem();
                    } else {
                        // TODO: play whip sound

                        // TODO: use flag or character state for this? or both to separate animation from input?
                        Flags.whipping = true;
                        currentState = whipping;
                        animStateTime = 0f;
                    }
                }
            }

            // Check for rope/bomb input ---------------------------------------------------
            if (currentInput.get(Action.rope) && numRopes > 0) {
                throwRope();
            }
            else if (currentInput.get(Action.bomb) && !Flags.holdingItem && numBombs > 0) {
                takeOutBomb();
            }

            // Check for move input --------------------------------------------------------
            if (currentInput.get(Action.left)) {
                currentFacing = left;
                Flags.hangingOnTileLeft = false;

                // TODO: not 100% what's going on here
                // if (!(Flags.hangingOnTileLeft || Flags.hangingOnTileRight) && !Flags.climbing) {
                if (!Flags.hangingOnTileRight && !Flags.climbing) {
                    velocity.x = -Constants.horizSpeedDeltaValue;
                }
            }
            if (currentInput.get(Action.right)) {
                currentFacing = right;
                Flags.hangingOnTileRight = false;

                // TODO: not 100% what's going on here
                // if (!(Flags.hangingOnTileLeft || Flags.hangingOnTileRight) && !Flags.climbing) {
                if (!Flags.hangingOnTileLeft && !Flags.climbing) {
                    velocity.x = Constants.horizSpeedDeltaValue;
                }
            }

            int xx = Math.floorDiv((int) bounds.center.x, (int) Map.tile_size);
            int yy = Math.floorDiv((int) bounds.center.y, (int) Map.tile_size);

            currentXInTiles = xx;
            currentYInTiles = yy;

            // Check for up/down input -----------------------------------------------------
            if (currentInput.get(Action.up) || currentInput.get(Action.down)) {
                if (Flags.climbing) {
                    timeClimbing += dt;
                    if (timeClimbing > Constants.soundReplayTimeClimbing) {
                        timeClimbing -= Constants.soundReplayTimeClimbing;
                        // TODO: shuffle between different climb sounds based on an accumulating index
                        Gdx.app.log("Player", "climb sound");
                    }
                } else {
                    timeClimbing = 200; // TODO: ???
                }

                // Check neighboring tiles for climb-ability and exit-ability
                Array<Tile> neighborTiles = map.getNeighboringTiles(currentXInTiles, currentYInTiles);

                Flags.canClimbLadder = currentInput.get(Action.up)
                                    && (Map.tileIsType(neighborTiles.get(Map.TileGroup.center), Tile.Type.ladder)
                                     || Map.tileIsType(neighborTiles.get(Map.TileGroup.center), Tile.Type.ladder_deck));
//                                    && (neighborTiles.get(Map.TileGroup.center) != null
//                                    && (neighborTiles.get(Map.TileGroup.center).type == Tile.Type.ladder ||
//                                        neighborTiles.get(Map.TileGroup.center).type == Tile.Type.ladder_deck));
                Flags.canClimbRope &= currentInput.get(Action.up);

                Flags.exitingLevel = Map.tileIsType(neighborTiles.get(Map.TileGroup.center), Tile.Type.exit);
                if (Flags.exitingLevel) {
                    // TODO: move player directly in front of exit tile
                    // TODO: play sounds, update music, etc...
                    velocity.set(0f, 0f);
                    animStateTime = 0f;
                }

                Flags.onTopOfClimbingSpace |= neighborTiles.get(Map.TileGroup.up_center) != null
                                           && neighborTiles.get(Map.TileGroup.up_center).isObstacle();

                // TODO: not sure what this is going.. centering player on ladder?
//                if (Flags.canClimbLadder) {
//                    _x = neighborTiles.get(Map.TileGroup.center).x * 16;
//                }

                if (Flags.canClimbLadder || Flags.canClimbRope) {
                    Flags.climbing = true;
                    timeJumping = 0f;
                    velocity.x = 0f;

                    if (currentInput.get(Action.up)) {
                        velocity.y = Map.tile_size; // probably supposed to be one tile per second? was = -1 (b/c y down)
                    }

                    // TODO: ???
                    if (Flags.canClimbRope) {
                        Flags.startedClimbingRope = true;
                    } else {
                        Flags.startedClimbingLadder = true;
                    }
                }

                if (!Flags.canClimbRope && Flags.climbing && Flags.onTopOfClimbingSpace && !Flags.canClimbLadder) {
                    velocity.x = 0f;
                    velocity.y = 0f;
                    timeJumping = 0f;
                }
            } else if (Flags.climbing) {
                velocity.y = 0f;
            }

            // Check for down input --------------------------------------------------------
            if (currentInput.get(Action.down)) {
                Array<Tile> neighborTiles = map.getNeighboringTiles(currentXInTiles, currentYInTiles);

                Flags.canClimbLadder = Map.tileIsType(neighborTiles.get(Map.TileGroup.center), Tile.Type.ladder)
                                    || Map.tileIsType(neighborTiles.get(Map.TileGroup.center), Tile.Type.ladder_deck);
                if (Flags.climbing) {
                    Flags.canClimbLadder &= (neighborTiles.get(Map.TileGroup.down_center) != null
                                         &&  neighborTiles.get(Map.TileGroup.down_center).isEmpty());
                }

                if (Flags.climbing) {
                    velocity.y = -Map.tile_size; // probably supposed to be one tile per second? was = 1 (b/c y down)
                }

                if ((!Flags.canClimbRope && Flags.climbing && !Flags.onTopOfClimbingSpace) || (!Flags.canClimbLadder && Flags.climbing)) {
                    Flags.climbing = false;
                    Flags.startedClimbingRope = false;
                    Flags.startedClimbingLadder = false;
                    timeJumping = 0f;
                }

                Flags.hangingOnTileLeft = false;
                Flags.hangingOnTileRight = false;
                if (Colliding.below) {
                    Flags.crawling = true;
                    // TODO: ???
//                    maxSpeedHoriz = maxSpeedHorizCrawling;
//                    posUpdateDelta = posUpdateDeltaCrawling;
                }
            } else {
                Flags.crawling = false;
                if (!currentInput.get(Action.run)) {
                    // TODO: ???
//                    maxSpeedHoriz = maxSpeedHorizWalking;
//                    posUpdateDelta = posUpdateDeltaWalkingRunning;
                }
            }

        } else { // stunned or dead...
            Flags.crawling = false;
            if (!currentInput.get(Action.run)) {
                // TODO: ???
//                maxSpeedHoriz = Constants.maxSpeedWalkingHoriz;
//                posUpdateDelta = posUpdateDeltaWalkingRunning;
            }
        }

        updateSpritePosition();
    }

    private void updateSpritePosition() {


    }

    private void throwHeldItem() {
        Gdx.app.log("Player", "throw held item");
    }

    private void throwRope() {
        Gdx.app.log("Player", "throw rope");
    }

    private void takeOutBomb() {
        Gdx.app.log("Player", "take out bomb");
    }

}
