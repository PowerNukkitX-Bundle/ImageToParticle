package cn.daogecmd.imagetoparticle.particle;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;

public final class EulerAngle extends Location {
    public float roll;

    public EulerAngle(double x, double y, double z, Level level, double yaw, double pitch, float roll) {
        super(x, y, z, yaw, pitch, yaw, level);
        this.roll = roll;
    }

    public static EulerAngle from(
            Vector3 pos,
            Level world,
            float yaw,
            float pitch,
            float roll
    ) {
        return new EulerAngle(
                pos.x,
                pos.y,
                pos.z,
                pos instanceof Position position ? position.level : world,
                pos instanceof Location location ? location.yaw : yaw,
                pos instanceof Location location ? location.pitch : pitch,
                roll
        );
    }

    public float getRoll() {
        return roll;
    }
}
