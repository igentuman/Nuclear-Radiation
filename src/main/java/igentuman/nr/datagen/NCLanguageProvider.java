package igentuman.nr.datagen;

import igentuman.nr.setup.registration.*;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.List;

import static igentuman.nr.NuclearRadiation.MODID;
import static igentuman.nr.setup.registration.NCItems.*;

public class NCLanguageProvider extends LanguageProvider {

    public NCLanguageProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + MODID+"_items", "NuclearRadiation Items");
        add("itemGroup." + MODID+"_blocks", "NuclearRadiation Blocks");
        add("itemGroup." + MODID+"_fission_reactor", "NuclearRadiation Fission Reactor");
        add("itemGroup." + MODID+"_fusion_reactor", "NuclearRadiation Fusion Reactor");
        add("itemGroup." + MODID+"_fluids", "NuclearRadiation Fluids");
        add("itemGroup." + MODID+"_turbine_controller", "NuclearRadiation Turbine");
        parts();
        armor();
        tools();
        items();
        sounds();
    }

    private void messages() {
        add("message.nr.player_radiation_contamination", "Radiation Dose: %s");
        add("message.nr.geiger_radiation_measure", "Radiation Level: %s");
        add("death.attack.radiation", "Died of Radiation Poisoning");
        add("nc.message.patrons", "Special thanks to patrons: marcin212, PersonBelowRocks, tomdodd4598, ethantabler, endleon201, sancho.lucky, Cerusvi, tocix9730 and others...");
    }
    private void sounds() {

        add("sound_event.nuclearradiation.item.geiger_1", "Geiger Counter Ticks Level 1 Intensity");
        add("sound_event.nuclearradiation.item.geiger_2", "Geiger Counter Ticks Level 2 Intensity");
        add("sound_event.nuclearradiation.item.geiger_3", "Geiger Counter Ticks Level 3 Intensity");
        add("sound_event.nuclearradiation.item.geiger_4", "Geiger Counter Ticks Level 4 Intensity");
        add("sound_event.nuclearradiation.item.geiger_5", "Geiger Counter Ticks Level 5 Intensity");

        add("sound_event.nuclearradiation.fusion.ready", "Fusion Reactor Ready");
        add("sound_event.nuclearradiation.fusion.running", "Fusion Reactor Running");
        add("sound_event.nuclearradiation.fusion.charging", "Fusion Reactor Charging");
        add("sound_event.nuclearradiation.fusion.switch", "Fusion Reactor Switch");

        add("sound_event.nuclearradiation.fission_reactor", "Fission Reactor Ticking");
    }

    private void shielding() {
        for(String name: NCItems.NC_SHIELDING.keySet()) {
           // add(NCItems.NC_SHIELDING.get(name).get(), convertToName(name)+" Shielding");
        }
    }

    private void tooltips() {

        add("effect.nuclearradiation.radiation_resistance","Radiation Resistance");
        add("tooltip.nr.rad_shielding","Rad Shielding LVL: %s");
        add("tooltip.nr.radiation","Radiation: %s");
        add("tooltip.nr.radiation_removal","Removes Radiation: %s");
    }


    private void items() {
        for(String name: NCItems.NC_ITEMS.keySet()) {
          //  add(NCItems.NC_ITEMS.get(name).get(), convertToName(name));
        }
    }


    private void tools()
    {
        add(GEIGER_COUNTER.get(), "Geiger Counter");
    }

    private void armor() {

        add(HAZMAT_MASK.get(), "Hazmat Mask");
        add(HAZMAT_PANTS.get(), "Hazmat Pants");
        add(HAZMAT_BOOTS.get(), "Hazmat Boots");
        add(HAZMAT_CHEST.get(), "Hazmat Chest");
    }


    private void parts() {
        for(String name: NCItems.NC_PARTS.keySet()) {
           // add(NCItems.NC_PARTS.get(name).get(), convertToName(name));
        }
    }

}