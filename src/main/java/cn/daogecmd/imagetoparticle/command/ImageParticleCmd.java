package cn.daogecmd.imagetoparticle.command;

import cn.daogecmd.imagetoparticle.Loader;
import cn.daogecmd.imagetoparticle.particle.ParticlePlayPreset;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;

public class ImageParticleCmd extends Command {

    private static final double D_UNIT = 0.1;
    private static final double D_SIZE = 0.075;
    private static final double D_LIFE = 10;
    private static final double D_MOTION = 0;
    private static final double D_SPEED = 0;
    private static final double D_ACCELE = 0;
    private static final double D_ROLL = 0;

    public ImageParticleCmd() {
        super("imageparticle", "made by daoge_cmd");
        setAliases(new String[]{"imgpar", "testimg"});
        setPermission("imageparticle.op");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.isPlayer() || !testPermission(sender)) {
            sender.sendMessage("§cThis command can only be executed by online player!");
            return false;
        }
        var api = Loader.getInstance().getApi();
        var buttons = api.getParticleList().stream().map(name -> new ElementButton(name, new ElementButtonImageData(ElementButtonImageData.IMAGE_DATA_TYPE_PATH, "textures/items/painting.png"))).toList();
        var listForm = new FormWindowSimple("ImageParticle", "Select the particle name to generate the test item", buttons);
        listForm.addHandler(((player, formID) -> {
            var listFormResponse = listForm.getResponse();
            if (listFormResponse == null) return;
            var imageName = listFormResponse.getClickedButton().getText();
            var createTestItemForm = new FormWindowCustom("CreateTestItemForm");

            createTestItemForm.addElement(new ElementInput("§l§bunit §r(0 < value)", "", String.valueOf(D_UNIT)));
            createTestItemForm.addElement(new ElementInput("§l§bsize §r(0 < value < <= 100)", "", String.valueOf(D_SIZE)));
            createTestItemForm.addElement(new ElementInput("§l§blife §r(0 < value <= 1000)", "", String.valueOf(D_LIFE)));
            createTestItemForm.addElement(new ElementSlider("§l§bmotion_x §r(unit: 0.01m) The real value will be subtracted by 100", 0, 200, 1, (float) D_MOTION + 100));
            createTestItemForm.addElement(new ElementSlider("§l§bmotion_y §r(unit: 0.01m) The real value will be subtracted by 100", 0, 200, 1, (float) D_MOTION + 100));
            createTestItemForm.addElement(new ElementSlider("§l§bmotion_z §r(unit: 0.01m) The real value will be subtracted by 100", 0, 200, 1, (float) D_MOTION + 100));
            createTestItemForm.addElement(new ElementInput("§l§bspeed §r(0 ~ 100)", "", String.valueOf(D_SPEED)));
            createTestItemForm.addElement(new ElementInput("§l§baccele §r(-100 ~ 100)", "", String.valueOf(D_ACCELE)));
            createTestItemForm.addElement(new ElementInput("§l§broll §r(If it's less than 0, it's random)", "", String.valueOf(D_ROLL)));

            createTestItemForm.addHandler(((player1, formID1) -> {
                FormResponseCustom CTIFResponse = createTestItemForm.getResponse();
                if (CTIFResponse == null) return;
                player1.getInventory().addItem(Loader.getInstance().getApi().createTestItem(
                        ParticlePlayPreset
                                .builder()
                                .name(imageName)
                                .unit(Float.parseFloat(CTIFResponse.getInputResponse(0)))
                                .size(Float.parseFloat(CTIFResponse.getInputResponse(1)))
                                .life(Float.parseFloat(CTIFResponse.getInputResponse(2)))
                                .motion_x((CTIFResponse.getSliderResponse(3) - 100) / 100f)
                                .motion_y((CTIFResponse.getSliderResponse(4) - 100) / 100f)
                                .motion_z((CTIFResponse.getSliderResponse(5) - 100) / 100f)
                                .speed(Float.parseFloat(CTIFResponse.getInputResponse(6)))
                                .accele(Float.parseFloat(CTIFResponse.getInputResponse(7)))
                                .roll(Float.parseFloat(CTIFResponse.getInputResponse(8)))
                                .build()
                ));
            }));
            player.showFormWindow(createTestItemForm);
        }));
        sender.asPlayer().showFormWindow(listForm);
        return true;
    }
}
