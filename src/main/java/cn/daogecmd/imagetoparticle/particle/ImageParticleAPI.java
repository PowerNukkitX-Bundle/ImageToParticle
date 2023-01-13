package cn.daogecmd.imagetoparticle.particle;

import cn.daogecmd.imagetoparticle.Loader;
import cn.daogecmd.imagetoparticle.task.AsyncSendParticle;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFishingRod;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.tag.CompoundTag;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public final class ImageParticleAPI {
    public static final String TEST_PARTICLE_TAG = "test_image_particle";
    private final Server server;
    private Item testItem;
    private final Map<String, ImageParticle> particles = new HashMap<>();

    public ImageParticleAPI() {
        setupTestItem();
        server = Server.getInstance();
    }

    private void setupTestItem() {
        testItem = Item.get(ItemID.FISHING_ROD);
        testItem.setNamedTag(this.testItem.getOrCreateNamedTag().putString(TEST_PARTICLE_TAG, TEST_PARTICLE_TAG));
        testItem.setCustomName("§l§bImage Particle Test Item");
    }

    public boolean isTestItem(Item item) {
        return item instanceof ItemFishingRod && item.getNamedTag().contains(TEST_PARTICLE_TAG);
    }

    public Item createTestItem(
            String name,
            float unit,
            float size,
            float life,
            float motion_x,
            float motion_y,
            float motion_z,
            float speed,
            float accele,
            float roll
    ) {
        var tag = new CompoundTag(TEST_PARTICLE_TAG)
                .putString("name", name)
                .putFloat("unit", unit)
                .putFloat("size", size)
                .putFloat("life", life)
                .putFloat("motion_x", motion_x)
                .putFloat("motion_y", motion_y)
                .putFloat("motion_z", motion_z)
                .putFloat("speed", speed)
                .putFloat("accele", accele)
                .putFloat("roll", roll);
        var item = testItem.clone();
        item.setNamedTag(item.getNamedTag().put(TEST_PARTICLE_TAG, tag));
        item.setLore("§cimage§r: " + name);
        return item;
    }

    public boolean existsParticle(String name) {
        return particles.containsKey(name);
    }

    public ImageParticle getParticle(String name) {
        return particles.get(name);
    }

    public Map<String, ImageParticle> getParticles() {
        return particles;
    }

    public Set<String> getParticleList() {
        return particles.keySet();
    }

    public void registerImage(
            String name,
            String imageFile
    ) {
        if (particles.containsKey(name))
            throw new RuntimeException("already registered Particle Name");
        if (!fileExist(imageFile))
            throw new RuntimeException(imageFile + " is not exists");
        BufferedImage img;
        try {
            img = ImageIO.read(Path.of(imageFile).toFile());
        } catch (IOException e) {
            throw new RuntimeException(imageFile + " load failure");
        }
        var sx = img.getWidth();
        var sy = img.getHeight();
        var cx = sx / 2 - 0.5;
        var cy = sy / 2 - 0.5;
        if (sx % 2 == 0) {
            cx--;
        }
        if (sy % 2 == 0) {
            cy--;
        }
        List<double[][]> data = new ArrayList<>();
        for (var y = 0; y < sy; y++) {
            for (var x = 0; x < sx; x++) {
                var colorAt = img.getRGB(x, y);
                var a = ((~(colorAt >> 24)) << 1) & 0xff;
//                if(a < 50){
//                    continue;
//                }
                var color = new Color(colorAt);
                data.add(new double[][]{
                        {color.getRed(), color.getGreen(), color.getBlue()},
                        {x - cx, y - cy}});
            }
        }
        particles.put(name, new ImageParticle(name, data));
    }

    public void sendParticle(
            String name,
            EulerAngle center,
            CustomParticle customParticle,
            int count,
            float unit,
            boolean asyncEncode
    ) {
        var particle = getParticle(name);
        if (particle == null) return;
        //异步发送
        if (asyncEncode) {
            this.server.getScheduler().scheduleAsyncTask(Loader.getInstance(),
                    new AsyncSendParticle(
                            particle,
                            center,
                            customParticle,
                            count,
                            unit
                    ));
            return;
        }
        //同步发送
        var result = particle.encode(
                center,
                customParticle,
                count,
                unit
        );
        //todo: 不要向看不见的玩家发送数据包
        var players = center.getLevel().getPlayers().values();
        for (var pk : result)
            Server.broadcastPacket(players, pk);
    }

    private boolean fileExist(String file) {
        return Files.exists(Path.of(file));
    }
}
