package cn.daogecmd.imagetoparticle.task;

import cn.daogecmd.imagetoparticle.particle.CustomParticle;
import cn.daogecmd.imagetoparticle.particle.EulerAngle;
import cn.daogecmd.imagetoparticle.particle.ImageParticle;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import cn.nukkit.scheduler.AsyncTask;

import java.util.Set;

public class AsyncSendParticle extends AsyncTask {

    private final ImageParticle particle;
    private final EulerAngle center;
    private final CustomParticle custom;
    private final float yaw;
    private final float pitch;
    private final float roll;

    private final int count;
    private final float unit;

    private final Level world;

    private Set<SpawnParticleEffectPacket> result;

    public AsyncSendParticle(
            ImageParticle particle,
            EulerAngle center,
            CustomParticle custom,
            int count, float unit) {
        yaw = (float) center.getYaw();
        pitch = (float) center.getPitch();
        roll = center.getRoll();
        world = center.getLevel();
        this.particle = particle;
        this.center = center;
        this.custom = custom;
        this.count = count;
        this.unit = unit;
    }

    @Override
    public void onRun() {
        result = particle.encode(
                EulerAngle.from(center, null, yaw, pitch, roll),
                custom,
                count,
                unit
        );
    }

    @Override
    public void onCompletion(Server server) {
        //todo: 不要向看不见的玩家发送数据包
        var players = world.getPlayers().values();
        for (var pk : result)
            Server.broadcastPacket(players, pk);
    }
}
