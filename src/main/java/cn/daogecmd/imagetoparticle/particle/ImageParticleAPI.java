package cn.daogecmd.imagetoparticle.particle;

import cn.daogecmd.imagetoparticle.Loader;
import cn.daogecmd.imagetoparticle.task.AsyncSendParticle;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFishingRod;
import cn.nukkit.item.ItemID;

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
        testItem.setCustomName("§bImage Particle Item");
    }

    public boolean isTestItem(Item item) {
        return item instanceof ItemFishingRod && item.getNamedTag().contains(TEST_PARTICLE_TAG);
    }

    public Item createTestItem(ParticlePlayPreset preset) {
        var item = testItem.clone();
        item.setNamedTag(preset.writeTo(item.getNamedTag()));
        item.setLore(preset.toString());
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

    public void registerImage(String name, String imageFile) {
        registerImage(name, imageFile, 1.0);
    }

    public void registerImage(
            String name,
            String imageFile,
            double zoom
    ) {
        if (particles.containsKey(name))
//            throw new RuntimeException("already registered Particle Name");
            return;//Able to reload?
        if (!fileExist(imageFile))
            throw new RuntimeException(imageFile + " is not exists");
        BufferedImage img;
        try {
            img = ImageIO.read(Path.of(imageFile).toFile());
        } catch (IOException e) {
            throw new RuntimeException(imageFile + " load failure");
        }
        if (zoom != 1.0d)
            img = zoomImage(img, (int) (img.getWidth() * zoom), (int) (img.getHeight() * zoom));
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
                if (colorAt >> 24 == 0)
                    continue;//Transparent
                var a = ((~(colorAt >> 24)) << 1) & 0xff;
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

    private BufferedImage zoomImage(BufferedImage image, int width, int height) {
        var resultingImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        var outputImage = new BufferedImage(width, height, image.getType());
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
