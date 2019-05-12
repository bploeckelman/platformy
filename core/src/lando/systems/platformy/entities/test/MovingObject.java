package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Vector2;
import lando.systems.platformy.entities.test.Map;

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
        groundY = 0f; // mutated by hasGround(), this is dumb and should be made less dumb
        if (speed.y <= 0f && hasGround(map, prevPosition, position, speed)) {
            position.y = prevPosition.y;
//            position.y = groundY;// + bounds.halfSize.y - boundsOffset.y;
            speed.y = 0f;
            onGround = true;
        } else {
            onGround = false;
        }

        bounds.center.set(position.x + bounds.halfSize.x + boundsOffset.x,
                          position.y + bounds.halfSize.y + boundsOffset.y);
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
