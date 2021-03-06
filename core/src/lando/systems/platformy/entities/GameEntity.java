package lando.systems.platformy.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.platformy.Assets;
import lando.systems.platformy.Config;
import lando.systems.platformy.screens.GameScreen;

public class GameEntity {

    public enum Direction {right, left}
    public enum State { standing, walking, jumping }

    private Assets assets;

    GameScreen screen;
    TextureRegion keyframe;
    Animation<TextureRegion> animation;

    public State state = State.standing;
    public Direction direction = Direction.right;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 acceleration = new Vector2();
    public Rectangle imageBounds = new Rectangle();
    public Rectangle collisionBounds = new Rectangle();
    public Circle collisionCircle = new Circle();

    public boolean grounded;

    private float stateTime;
    private float gravity = -100;
    private float maxHorizontalVelocity = 2000f;
    private float maxVerticalVelocity = 1200f;
    private Array<Rectangle> tiles = new Array<>();

    GameEntity(GameScreen screen, Animation<TextureRegion> animation) {
        this(screen, animation.getKeyFrame(0f));
        this.animation = animation;
    }

    private GameEntity(GameScreen screen, TextureRegion keyframe) {
        this.assets = screen.game.assets;
        this.screen = screen;
        this.animation = null;
        this.keyframe = keyframe;
        this.grounded = true;
        this.stateTime = 0f;
    }

    public void changeDirection() {
        setDirection((direction == Direction.left) ? Direction.right : Direction.left);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void update(float dt) {
        updatePosition(dt);

        stateTime += dt;
        if (animation != null) {
            keyframe = animation.getKeyFrame(stateTime);
        }
    }

    public void updatePosition(float dt) {
        stateTime += dt;

        // apply gravity if we are falling
        velocity.add(0f, gravity);

        // clamp velocity to maximum, horizontal only
        velocity.x = Math.min(maxHorizontalVelocity, Math.max(-maxHorizontalVelocity, velocity.x));

        // stop if entity gets slow enough
        if (Math.abs(velocity.x) < 10f) {
            velocity.x = 0f;
            state = State.standing;
        } else {
            state = State.walking;
        }

        if (!grounded) {
            state = State.jumping;
        }

        // multiply by dt so we know how far we go in this frame
        velocity.scl(dt);

        // perform collision detection & response, on each axis, separately
        // if entity is moving right, check tiles to the right
        // of it's right bounding box edge, otherwise check the ones to the left
        Rectangle entityRect = screen.level.rectPool.obtain();
        entityRect.set(collisionBounds);

        int startX, startY, endX, endY;
        if (velocity.x > 0) startX = endX = (int) (entityRect.x + velocity.x + entityRect.width);
        else                startX = endX = (int) (entityRect.x + velocity.x);
        startY = (int) (entityRect.y);
        endY   = (int) (entityRect.y + entityRect.height);
        entityRect.x += velocity.x;
        screen.level.getTiles(startX, startY, endX, endY, tiles);
        for (Rectangle tile : tiles) {
            if (entityRect.overlaps(tile)) {
                velocity.x = 0f;
                break;
            }
        }

        // TODO: check for object tile interactions (horizontal)

        // if the entity is moving upwards, check the tiles to the top
        // of it's top bounding box edge, otherwise check the ones to the bottom
        grounded = false;
        boolean yVelocityNeedsToBeCleared = false;
        if (velocity.y > 0) startY = endY = (int) (entityRect.y + velocity.y + entityRect.height);
        else                startY = endY = (int) (entityRect.y + velocity.y);
        startX = (int) (entityRect.x);
        endX   = (int) (entityRect.x + entityRect.width);
        entityRect.y += velocity.y;
        screen.level.getTiles(startX, startY, endX, endY, tiles);
        for (Rectangle tile : tiles) {
            if (entityRect.overlaps(tile)) {
                // actually reset the entity y-position here
                // so its just below / above the collided tile (removes bouncing)
                if (velocity.y > 0f) {
                    collisionBounds.y = tile.y - collisionBounds.height;
                } else {
                    collisionBounds.y = tile.y + tile.height;
                    grounded = true;
                }
            }
            yVelocityNeedsToBeCleared = true;
            break;
        }

        // TODO: check for object tile interactions (vertical)

        if (yVelocityNeedsToBeCleared) {
            velocity.y = 0f;
        }

        screen.level.rectPool.free(entityRect);

        collisionBounds.x += velocity.x;
        collisionBounds.y += velocity.y;
        velocity.scl(1f / dt);

        position.set(collisionBounds.x, collisionBounds.y);
        collisionCircle.setPosition(collisionBounds.x + collisionBounds.width / 2f, collisionBounds.y + collisionBounds.height / 2f);
        collisionCircle.setRadius(collisionBounds.width / 2f);
    }

    public void render(SpriteBatch batch) {
        if (keyframe == null) return;

        if (Config.debug) {
            batch.setColor(Color.RED);
            assets.ninePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
            batch.setColor(Color.WHITE);

            batch.setColor(Color.YELLOW);
            for (Rectangle tile : tiles) {
                assets.ninePatch.draw(batch, tile.x, tile.y, tile.width, tile.height);
            }
            batch.setColor(Color.WHITE);
        }

        float scaleX = (direction == Direction.right) ? 1 : -1;
        float scaleY = 1;
        if (!grounded){
            scaleX *= .85f;
            scaleY = 1.15f;
        }

        batch.setColor(Color.WHITE);
        batch.draw(keyframe, collisionBounds.x, collisionBounds.y,
                   collisionBounds.width / 2, collisionBounds.height / 2,
                   collisionBounds.width, collisionBounds.height, scaleX, scaleY, 0);
//        batch.draw(assets.whiteCircleOutline, collisionBounds.x, collisionBounds.y,
//                   collisionBounds.width / 2, collisionBounds.height / 2,
//                   collisionBounds.width, collisionBounds.height, scaleX, scaleY, 0);

        if (Config.debug) {
            batch.setColor(Color.MAGENTA);
            batch.draw(assets.whiteCircleOutline,
                       collisionCircle.x - collisionCircle.radius,
                       collisionCircle.y - collisionCircle.radius,
                       collisionCircle.radius * 2f, collisionCircle.radius * 2f);
            batch.setColor(Color.WHITE);
        }
    }

}
