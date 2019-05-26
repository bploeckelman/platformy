package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class MovingObject {

    // Location
    public Vector2 position;
    public Vector2 prevPosition;

    // Velocity
    public Vector2 speed;
    public Vector2 prevSpeed;

    // Scale
    public Vector2 scale;

    // Bounds and offset from sprite
    public AABB bounds;
    public Vector2 boundsOffset;

    // Contact state for vertical axis
    public boolean onGround;
    public boolean wasOnGround;
    public boolean onCeiling;
    public boolean wasOnCeiling;

    // Contact state for horizontal axis
    public boolean pushesLeftWall;
    public boolean pushedLeftWall;
    public boolean pushesRightWall;
    public boolean pushedRightWall;

    private Pool<AABB> aabbs = Pools.get(AABB.class, 50);
    private Pool<Rectangle> rectangles = Pools.get(Rectangle.class, 50);

    public MovingObject(float x, float y) {
        this.position     = new Vector2(x, y);
        this.prevPosition = new Vector2(x, y);
        this.speed        = new Vector2(Vector2.Zero);
        this.prevSpeed    = new Vector2(Vector2.Zero);
        this.scale        = new Vector2(1f, 1f);
        this.bounds       = new AABB(position.x + 16f, position.y + 16f, 16f, 16f);
        this.boundsOffset = new Vector2(0f, 0f);
        this.onGround        = false;
        this.wasOnGround     = false;
        this.onCeiling       = false;
        this.wasOnCeiling    = false;
        this.pushesLeftWall  = false;
        this.pushedLeftWall  = false;
        this.pushesRightWall = false;
        this.pushedRightWall = false;
    }

    private Array<Rectangle> collisionRects = new Array<>();

    public void update(float dt, Map map) {
        prevPosition.set(position);
        prevSpeed.set(speed);

        wasOnGround = onGround;
        wasOnCeiling = onCeiling;
        pushedLeftWall = pushesLeftWall;
        pushedRightWall = pushesRightWall;

        position.add(speed.x * dt, speed.y * dt);

        // TODO: for now, zero is the 'ground'
        // NOTE: shouldn't this be using the bounds instead of position?
//        if (position.y <= 0f) {
//            position.y = 0f;
//            speed.y = 0f;
//            onGround = true;
//        } else {
//            onGround = false;
//        }

        handleCollisions(this, map);
//        if (handleCollisions(this, map)) {
//            // we collided, do something
//            if (speed.y <= 0f) {
//                onGround = true;
//            } else {
//                onGround = false;
//            }
//        } else {
//            onGround = false;
//        }

//        groundY = 0f; // mutated by hasGround(), this is dumb and should be made less dumb
//        if (speed.y <= 0f && hasGround(map, prevPosition, position, speed)) {
//            position.y = prevPosition.y;
////            position.y = groundY;// + bounds.halfSize.y - boundsOffset.y;
//            speed.y = 0f;
//            onGround = true;
//        } else {
//            onGround = false;
//        }

        bounds.center.set(position.x + bounds.halfSize.x + boundsOffset.x,
                          position.y + bounds.halfSize.y + boundsOffset.y);
    }

    private boolean handleCollisions(MovingObject movingObject, Map map) {
        boolean didCollide = false;
        collisionRects.clear();

        AABB bounds = aabbs.obtain().set(movingObject.bounds);
        AABB tileBounds = aabbs.obtain();
        int startX, startY, endX, endY;

        // Check horizontal collisions
        if (movingObject.speed.x > 0f) startX = endX = (int) (bounds.center.x + bounds.halfSize.x + movingObject.speed.x);
        else                           startX = endX = (int) (bounds.center.x - bounds.halfSize.x + movingObject.speed.x);
        startY = (int) (bounds.center.y - bounds.halfSize.y);
        endY   = (int) (bounds.center.y + bounds.halfSize.y);
        map.getTiles(startX, startY, endX, endY, collisionRects, rectangles);
        bounds.center.x += movingObject.speed.x;
        for (Rectangle rect : collisionRects) {
            tileBounds.set(rect);
            if (bounds.overlaps(tileBounds)) {
                didCollide = true;
                movingObject.speed.x = 0f;
                break;
            }
        }
        bounds.center.x = movingObject.bounds.center.x;

        // Check vertical collisions
        if (movingObject.speed.y > 0) startY = endY = (int) (bounds.center.y + bounds.halfSize.y + movingObject.speed.y);
        else                          startY = endY = (int) (bounds.center.y - bounds.halfSize.y + movingObject.speed.y);
        startX = (int) (bounds.center.x - bounds.halfSize.x);
        endX   = (int) (bounds.center.x + bounds.halfSize.x);
        map.getTiles(startX, startY, endX, endY, collisionRects, rectangles);
        bounds.center.y += movingObject.speed.y;
        for (Rectangle rect : collisionRects) {
            tileBounds.set(rect);
            if (bounds.overlaps(tileBounds)) {
                didCollide = true;
//                movingObject.speed.y = 0f;
//                break;
                // reset koala y-position (as opposed to the copy) here, so it is just below/above the collided tile (removes bouncing)
                if (movingObject.speed.y > 0f) {
                    movingObject.position.y = (tileBounds.center.y - tileBounds.halfSize.y) - (2f * movingObject.bounds.halfSize.y);
                } else {
                    movingObject.position.y = tileBounds.center.y + tileBounds.halfSize.y;
                    movingObject.onGround = true;
                }
                movingObject.speed.y = 0f;
                break;
            }
        }
        bounds.center.y = movingObject.bounds.center.y;

        aabbs.free(tileBounds);
        aabbs.free(bounds);

        return didCollide;
    }

    private Vector2 temp1 = new Vector2();
    private Vector2 temp2 = new Vector2();
    private Vector2 temp3 = new Vector2();
    private float groundY;
    public boolean hasGround(Map map, Vector2 prevPosition, Vector2 position, Vector2 speed) {
        Vector2 center      = temp1.set(bounds.center);//set(position).add(boundsOffset);
        Vector2 bottomLeft  = temp2.set(center).sub(bounds.halfSize).sub(Vector2.Y).add(Vector2.X);
        Vector2 bottomRight = temp3.set(bottomLeft.x + bounds.halfSize.x * 2f - 2f, bottomLeft.y);

        float checkedTileY = bottomLeft.y;
        for (float checkedTileX = bottomLeft.x ;; checkedTileX += Map.tile_size) {
            checkedTileX = Math.min(checkedTileX, bottomRight.x);
            Tile tile = map.getTileAtWorldPosition(checkedTileX, checkedTileY);

            groundY = map.position.y + tile.bounds.y;
            if (tile.isObstacle()) {
                return true;
            }

            if (checkedTileX >= bottomRight.x) {
                break;
            }
        }
        return false;
    }

}
