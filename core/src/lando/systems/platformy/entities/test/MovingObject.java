package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Vector2;

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

    public void update(float dt) {
        prevPosition.set(position);
        prevSpeed.set(speed);

        wasOnGround = onGround;
        wasOnCeiling = onCeiling;
        pushedLeftWall = pushesLeftWall;
        pushedRightWall = pushesRightWall;

        position.add(speed.x * dt, speed.y * dt);

        // TODO: for now, zero is the 'ground'
        // NOTE: shouldn't this be using the bounds instead of position?
        if (position.y <= 0f) {
            position.y = 0f;
            speed.y = 0f;
            onGround = true;
        } else {
            onGround = false;
        }

        bounds.center.set(position.x + bounds.halfSize.x + boundsOffset.x,
                          position.y + bounds.halfSize.y + boundsOffset.y);
    }

}
