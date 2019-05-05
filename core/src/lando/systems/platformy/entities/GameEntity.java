package lando.systems.platformy.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.platformy.Assets;
import lando.systems.platformy.Config;
import lando.systems.platformy.screens.GameScreen;
import lando.systems.platformy.world.Level;

public class GameEntity {

    public enum Direction {right, left}

    private Assets assets;

    GameScreen screen;
    TextureRegion keyframe;
    Animation<TextureRegion> animation;
    Direction direction = Direction.right;

    // TODO: remove these, keep in collisionBounds rect
    public float width;
    public float height;
    public Rectangle collisionBoundsOffsets;
    private float stateTime;

    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();

    public boolean grounded;

    // TODO: shouldn't this be in level?
    private float gravity = 2500;

    private Array<Rectangle> tiles;
    private Rectangle bounds = new Rectangle();
    private Vector2 tempPos = new Vector2();

    GameEntity(GameScreen screen, Animation<TextureRegion> animation) {
        this(screen, animation.getKeyFrame(0f));
        this.animation = animation;
    }

    private GameEntity(GameScreen screen, TextureRegion keyframe) {
        this.assets = screen.game.assets;
        this.screen = screen;
        this.animation = null;
        this.keyframe = keyframe;
        this.tiles = new Array<>();
        this.collisionBoundsOffsets = new Rectangle();
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
        velocity.y -= gravity * dt;

        tempPos.set(position);
        tempPos.add(velocity.x * dt, velocity.y * dt);

        Rectangle entityRect = screen.level.rectPool.obtain();
        float startX, startY, endX, endY;

        // Check Horizontal
        entityRect.set(tempPos.x  + collisionBoundsOffsets.x,
                       position.y + collisionBoundsOffsets.y,
                       collisionBoundsOffsets.width,
                       collisionBoundsOffsets.height);
        if (velocity.x > 0) {
            startX = endX = entityRect.x + entityRect.width;
//            startX = entityRect.x - Level.TILE_SIZE;
//            endX = entityRect.x + entityRect.width + Level.TILE_SIZE;
        } else {
            startX = endX = entityRect.x;
//            startX = entityRect.x - Level.TILE_SIZE;
//            endX   = entityRect.x;
        }
        startY = entityRect.y;
        endY   = entityRect.y + entityRect.height;
        screen.level.getTiles(startX, startY, endX, endY, tiles);

        for (Rectangle tile : tiles) {
            entityRect.set(tempPos.x  + collisionBoundsOffsets.x,
                           position.y + collisionBoundsOffsets.y,
                           collisionBoundsOffsets.width,
                           collisionBoundsOffsets.height);

            if (entityRect.overlaps(tile)) {
                tempPos.x = position.x;
//                changeDirection();
                break;
            }
        }


        // Check vertical
        entityRect.set(tempPos.x + collisionBoundsOffsets.x,
                       tempPos.y + collisionBoundsOffsets.y,
                       collisionBoundsOffsets.width,
                       collisionBoundsOffsets.height);
        // Above?
        if (velocity.y > 0) {
            startY = endY = entityRect.y + entityRect.height;
        } else {
            startY = position.y;
            endY   = entityRect.y;
        }
        startX = entityRect.x;
        endX   = entityRect.x + entityRect.width;

        boolean wasGrounded = grounded;
        grounded = false;
        screen.level.getTiles(startX, startY, endX, endY, tiles);
        for (Rectangle tile : tiles) {
            entityRect.set(tempPos.x + collisionBoundsOffsets.x, tempPos.y + collisionBoundsOffsets.y, collisionBoundsOffsets.width, collisionBoundsOffsets.height);
            if (entityRect.overlaps(tile)) {
                if (velocity.y > 0) {
                    tempPos.y = Math.min(tempPos.y, tile.y - height);
                } else {
                    tempPos.y = Math.max(tempPos.y, tile.y + tile.height);
                    grounded = true;
                }
                velocity.y = 0;
            }
        }

//        screen.level.handleObjectInteractions(this);

        screen.level.rectPool.free(entityRect);
        position.set(tempPos);

        bounds.set(collisionBoundsOffsets);
        bounds.x += position.x;
        bounds.y += position.y;
    }

    public void render(SpriteBatch batch) {
        if (keyframe == null) return;

        float scaleX = (direction == Direction.right) ? 1 : -1;
        float scaleY = 1;
        if (!grounded){
            scaleX *= .85f;
            scaleY = 1.15f;
        }

        batch.setColor(Color.WHITE);
        batch.draw(keyframe, position.x, position.y,
                   width / 2, height / 2,
                   width, height, scaleX, scaleY, 0);

        if (Config.debug) {
            assets.ninePatch.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.RED);
            assets.ninePatch.draw(batch, position.x + collisionBoundsOffsets.x, position.y + collisionBoundsOffsets.y, collisionBoundsOffsets.width, collisionBoundsOffsets.height);
            batch.setColor(Color.WHITE);

            batch.setColor(Color.YELLOW);
            for (Rectangle tile : tiles) {
                assets.ninePatch.draw(batch, tile.x, tile.y, tile.width, tile.height);
            }
            batch.setColor(Color.WHITE);
        }
    }

}
