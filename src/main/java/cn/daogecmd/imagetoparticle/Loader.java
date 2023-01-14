package cn.daogecmd.imagetoparticle;

import cn.daogecmd.imagetoparticle.command.ImageParticleCmd;
import cn.daogecmd.imagetoparticle.particle.CustomParticle;
import cn.daogecmd.imagetoparticle.particle.EulerAngle;
import cn.daogecmd.imagetoparticle.particle.ImageParticleAPI;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.util.UUID;

import static cn.nukkit.math.NukkitMath.round;

public final class Loader extends PluginBase implements Listener {

    @Getter
    private static Loader instance;
    private final String IMAGE_PATH = "image";
    private final String PACK_NAME = "CustomDust.mcpack";
    private final UUID PACK_UUID = UUID.fromString("5f45648d-99f6-3768-8397-3690e77ea0e2");
    private ImageParticleAPI api;

    {
        instance = this;
    }

    public ImageParticleAPI getApi() {
        return api;
    }

    @Override
    public void onLoad() {
        this.api = new ImageParticleAPI();
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        var resourcePackManager = this.getServer().getResourcePackManager();
        //lack resource pack
        if (resourcePackManager.getPackById(PACK_UUID) == null) {
            this.getLogger().warning("Specific resource pack is missing!");
        }
        //check folder
        this.saveResource("images.yml", false);
        var folder = this.getDataFolder();
        var imgPath = folder.toPath().resolve(IMAGE_PATH);
        if (!Files.isDirectory(imgPath))
            Files.createDirectory(imgPath);
        //register images
        var config = new Config(folder.toPath().resolve("images.yml").toFile());
        for (var name : config.getAll().keySet()) {
            api.registerImage(
                    name,
                    imgPath.resolve(config.getString(name + ".file")).toString()
            );
        }

        var server = getServer();
        server.getCommandMap().register("", new ImageParticleCmd());
        server.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItem();
        if (!api.isTestItem(item))
            return;
        event.setCancelled();
        var player = event.getPlayer();
        var info = (CompoundTag) item.getNamedTag().get(ImageParticleAPI.TEST_PARTICLE_TAG);
        var location = player.getLocation().add(0, player.getEyeHeight());
        var centerVector = location.add(player.getDirectionVector().multiply(4));

        var yaw = location.getYaw();
        var pitch = location.getPitch();
        var roll = info.getFloat("roll");
        if (roll < 0) roll = Utils.rand(0, 3600) / 10.0F;

        api.sendParticle(
                info.getString("name"),
                EulerAngle.from(
                        centerVector,
                        location.getLevel(),
                        (float) yaw,
                        (float) pitch,
                        roll
                ),
                new CustomParticle(
                        info.getFloat("size"),
                        info.getFloat("life"),
                        new Vector3(
                                info.getFloat("motion_x"),
                                info.getFloat("motion_y"),
                                info.getFloat("motion_z")
                        ),
                        info.getFloat("speed"),
                        info.getFloat("accele")
                ),
                0,
                info.getFloat("unit"),
                true
        );
        player.sendPopup("§l§b" + round(yaw, 3) + " §f: §c" + round(pitch, 3) + " §f: §a" + round(roll, 3));
    }
}
