package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class AABB {

    public Vector2 center;
    public Vector2 halfSize;

    public AABB() {
        this.center = new Vector2();
        this.halfSize = new Vector2();
    }

    public AABB(AABB other) {
        this(other.center, other.halfSize);
    }

    public AABB(Vector2 center, Vector2 halfSize) {
        this.center = center.cpy();
        this.halfSize = halfSize.cpy();
    }

    public AABB(float centerX, float centerY, float halfSizeX, float halfSizeY) {
        this.center = new Vector2(centerX, centerY);
        this.halfSize = new Vector2(halfSizeX, halfSizeY);
    }

    public AABB(Rectangle rectangle) {
        this(rectangle.x + rectangle.width / 2f,
             rectangle.y + rectangle.height / 2f,
             rectangle.width / 2f,
             rectangle.height / 2f);
    }

    public AABB set(AABB other) {
        this.center = other.center.cpy();
        this.halfSize = other.halfSize.cpy();
        return this;
    }

    public AABB set(Rectangle rectangle) {
        this.center.set(rectangle.x + rectangle.width / 2f,
                        rectangle.y + rectangle.height / 2f);
        this.halfSize.set(rectangle.width / 2f, rectangle.height / 2f);
        return this;
    }

    public boolean overlaps(AABB that) {
        if (Math.abs(this.center.x - that.center.x) > (this.halfSize.x + that.halfSize.x)) return false;
        if (Math.abs(this.center.y - that.center.y) > (this.halfSize.y + that.halfSize.y)) return false;
        return true;
    }

    public Rectangle toRect() {
        return new Rectangle(center.x - halfSize.x, center.y - halfSize.y,
                             2f * halfSize.x, 2f * halfSize.y);
    }

    public Rectangle toRect(Pool<Rectangle> pool) {
        return pool.obtain().set(center.x - halfSize.x, center.y - halfSize.y,
                                 2f * halfSize.x, 2f * halfSize.y);
    }

}
