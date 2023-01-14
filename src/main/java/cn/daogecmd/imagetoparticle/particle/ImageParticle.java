package cn.daogecmd.imagetoparticle.particle;

import cn.nukkit.level.DimensionEnum;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.StrictMath.*;

public final class ImageParticle {
    private final String name;
    private final List<double[][]> particles;

    public ImageParticle(String name, List<double[][]> particles) {
        this.name = name;
        this.particles = particles;
    }

    private static SpawnParticleEffectPacket pk(Vector3f pos, CustomParticle customParticle) {
        var pk = new SpawnParticleEffectPacket();
        pk.dimensionId = DimensionEnum.OVERWORLD.getDimensionData().getDimensionId();
        pk.uniqueEntityId = -1;
        pk.position = pos;
        pk.identifier = "skymin:custom_dust";
        pk.molangVariablesJson = Optional.of(customParticle.encode());
        return pk;
    }

    public String getName() {
        return name;
    }

    public List<double[][]> getParticles() {
        return particles;
    }

    public Set<SpawnParticleEffectPacket> encode(EulerAngle euler, CustomParticle customParticle, int count, float unit) {
        if (count < 0)
            throw new IllegalArgumentException("A value greater than or equal to 0 should be obtained");
        if (unit <= 0.0)
            throw new IllegalArgumentException("Must be a positive value.");
        var p_count = 0;
        var center = euler.asVector3f();
        //yaw
        var yaw = toRadians(euler.getYaw());
        var ysin = sin(yaw);
        var ycos = cos(yaw);
        //pitch
        var pitch = toRadians(euler.getPitch());
        var psin = sin(pitch);
        var pcos = cos(pitch);
        //roll
        var roll = toRadians(euler.getRoll());
        var rsin = sin(roll);
        var rcos = cos(roll);
        Set<SpawnParticleEffectPacket> pks = new HashSet<>();
        for (var data : particles) {
            if (count == 0 || p_count++ % count == 0) {
                var x = data[1][0] * unit;
                var y = data[1][1] * unit;
                var dx = - y * rsin - x * rcos;
                var dy = y * rcos - x * rsin;
                var dz = dy * psin;
                pks.add(pk(center.add(
                        (float) (dz * ysin + dx * ycos),
                        (float) (dy * -pcos),
                        (float) (dz * -ycos + dx * ysin)
                ), customParticle.setColor((int) data[0][0], (int) data[0][1], (int) data[0][2])));
            }
        }
        return pks;
    }
}
