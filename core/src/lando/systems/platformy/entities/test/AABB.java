package lando.systems.platformy.entities.test;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AABB {

    public Vector2 center;
    public Vector2 halfSize;

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

    public boolean overlaps(AABB that) {
        if (Math.abs(this.center.x - that.center.x) > (this.halfSize.x + that.halfSize.x)) return false;
        if (Math.abs(this.center.y - that.center.y) > (this.halfSize.y + that.halfSize.y)) return false;
        return true;
    }

}
