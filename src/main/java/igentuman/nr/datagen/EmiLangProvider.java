package igentuman.nr.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

import static igentuman.nr.setup.registration.NCFluids.NC_MATERIALS;
import static igentuman.nr.setup.registration.NCItems.*;
import static igentuman.nr.util.TextUtils.convertToName;

public class EmiLangProvider extends LanguageProvider {

    public EmiLangProvider(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), "emi", locale);
    }

    @Override
    protected void addTranslations() {

        for(String name: NC_MATERIALS.keySet()) {
            add("tag.fluid.c."+name, convertToName(name));
        }
        for(String name: NC_INGOTS.keySet()) {
            add("tag.item.c."+name+"_ingots", convertToName(name)+" Ingots");
            add("tag.item.c.ingots."+name, convertToName(name)+" Ingots");
        }
        for(String name: NC_NUGGETS.keySet()) {
            add("tag.item.c."+name+"_nuggets", convertToName(name)+" Nuggets");
            add("tag.item.c.nuggets."+name, convertToName(name)+" Nuggets");
        }
        for(String name: NC_DUSTS.keySet()) {
            add("tag.item.c."+name+"_dusts", convertToName(name)+" Dusts");
            add("tag.item.c.dusts."+name, convertToName(name)+" Dusts");
        }
        for(String name: NC_PLATES.keySet()) {
            add("tag.item.c."+name+"_plates", convertToName(name)+" Plates");
            add("tag.item.c.plates."+name, convertToName(name)+" Plates");
        }
        for(String name: NC_CHUNKS.keySet()) {
            add("tag.item.c.raw_material."+name, convertToName(name)+" Raw");
        }
    }
}