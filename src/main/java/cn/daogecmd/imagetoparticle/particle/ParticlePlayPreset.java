package cn.daogecmd.imagetoparticle.particle;

import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Builder;
import lombok.Getter;

import static cn.daogecmd.imagetoparticle.particle.ImageParticleAPI.TEST_PARTICLE_TAG;

@Getter
public class ParticlePlayPreset {

    private String name;
    private float unit;
    private float size;
    private float life;
    private float motion_x;
    private float motion_y;
    private float motion_z;
    private float speed;
    private float accele;
    private float roll;

    @Builder
    private ParticlePlayPreset(String name, float unit, float size, float life, float motion_x, float motion_y, float motion_z, float speed, float accele, float roll) {
        this.name = name;
        this.unit = unit;
        this.size = size;
        this.life = life;
        this.motion_x = motion_x;
        this.motion_y = motion_y;
        this.motion_z = motion_z;
        this.speed = speed;
        this.accele = accele;
        this.roll = roll;
    }

    public static ParticlePlayPreset parseFrom(CompoundTag tag) {
        return builder()
                .name(tag.getString("name"))
                .unit(tag.getFloat("unit"))
                .size(tag.getFloat("size"))
                .life(tag.getFloat("life"))
                .motion_x(tag.getFloat("motion_x"))
                .motion_y(tag.getFloat("motion_y"))
                .motion_z(tag.getFloat("motion_z"))
                .speed(tag.getFloat("speed"))
                .accele(tag.getFloat("accele"))
                .roll(tag.getFloat("roll"))
                .build();
    }

    public CompoundTag writeTo(CompoundTag tag) {
        return tag.put(TEST_PARTICLE_TAG, new CompoundTag(TEST_PARTICLE_TAG)
                .putString("name", name)
                .putFloat("unit", unit)
                .putFloat("size", size)
                .putFloat("life", life)
                .putFloat("motion_x", motion_x)
                .putFloat("motion_y", motion_y)
                .putFloat("motion_z", motion_z)
                .putFloat("speed", speed)
                .putFloat("accele", accele)
                .putFloat("roll", roll));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("name='").append(name).append('\'');
        sb.append("\nunit=").append(unit);
        sb.append("\nsize=").append(size);
        sb.append("\nlife=").append(life);
        sb.append("\nmotion_x=").append(motion_x);
        sb.append("\nmotion_y=").append(motion_y);
        sb.append("\nmotion_z=").append(motion_z);
        sb.append("\nspeed=").append(speed);
        sb.append("\naccele=").append(accele);
        sb.append("\nroll=").append(roll);
        return sb.toString();
    }
}
