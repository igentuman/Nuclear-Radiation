package igentuman.nr.integration.jei;

import igentuman.nr.api.Isotope;
import igentuman.nr.binding.RadiationBindings;
import igentuman.nr.core.RadiationProfile;
import igentuman.nr.registry.IsotopeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return NRJeiTypes.PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gh = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new IsotopeStatsCategory(gh),
                new RadioactiveItemsCategory(gh),
                new DecayGraphCategory(gh)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<Isotope> isotopes = new ArrayList<>(IsotopeRegistry.all());
        isotopes.sort(Comparator.comparing(Isotope::id));

        registration.addRecipes(NRJeiTypes.ISOTOPE_STATS, isotopes);
        registration.addRecipes(NRJeiTypes.DECAY_GRAPH, isotopes);

        registration.addRecipes(NRJeiTypes.RADIOACTIVE_ITEMS, collectRadioactiveItems());
    }

    private static List<RadioactiveItemEntry> collectRadioactiveItems() {
        List<RadioactiveItemEntry> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) continue;
            RadiationProfile profile = RadiationBindings.of(stack);
            if (profile.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !seen.add(id.toString())) continue;
            out.add(new RadioactiveItemEntry(stack, profile));
        }
        return out;
    }
}
