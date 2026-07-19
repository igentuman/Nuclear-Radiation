package igentuman.nr.integration.jei;

import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.recipe.NRRecipes;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.radiation.shielding.world.ShieldingRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
                //new DecayGraphCategory(gh),
                new ArmorProtectionCategory(gh),
                new BlockShieldingCategory(gh),
                new MutationCategory(gh),
                new BlockIrradiationCategory(gh)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<Isotope> isotopes = new ArrayList<>(IsotopeRegistry.all());
        isotopes.sort(Comparator.comparing(Isotope::id));

        registration.addRecipes(NRJeiTypes.ISOTOPE_STATS, isotopes);
        //registration.addRecipes(NRJeiTypes.DECAY_GRAPH, isotopes);

        registration.addRecipes(NRJeiTypes.RADIOACTIVE_ITEMS, collectRadioactiveItems());
        registration.addRecipes(NRJeiTypes.ARMOR_PROTECTION, collectArmorProtection());
        registration.addRecipes(NRJeiTypes.BLOCK_SHIELDING, collectBlockShielding());
        registration.addRecipes(NRJeiTypes.MUTATION, collectMutations());
        registration.addRecipes(NRJeiTypes.BLOCK_IRRADIATION, collectBlockIrradiations());
    }

    private static List<MutationRecipe> collectMutations() {
        Minecraft mc = Minecraft.getInstance();
        List<MutationRecipe> out = new ArrayList<>();
        if (mc.level == null) return out;
        for (RecipeHolder<MutationRecipe> holder : mc.level.getRecipeManager().getAllRecipesFor(NRRecipes.MUTATION_TYPE.get())) {
            out.add(holder.value());
        }
        return out;
    }

    private static List<BlockIrradiationRecipe> collectBlockIrradiations() {
        Minecraft mc = Minecraft.getInstance();
        List<BlockIrradiationRecipe> out = new ArrayList<>();
        if (mc.level == null) return out;
        for (RecipeHolder<BlockIrradiationRecipe> holder : mc.level.getRecipeManager().getAllRecipesFor(NRRecipes.BLOCK_IRRADIATION_TYPE.get())) {
            out.add(holder.value());
        }
        return out;
    }

    private static List<ArmorProtectionEntry> collectArmorProtection() {
        List<ArmorProtectionEntry> out = new ArrayList<>();
        Set<Item> seen = new HashSet<>();

        for (Map.Entry<Item, ArmorProtectionRegistry.Protection> e : ArmorProtectionRegistry.all().entrySet()) {
            ArmorProtectionRegistry.Protection p = e.getValue();
            if (p.xray() <= 0 && p.alpha() <= 0 && p.beta() <= 0 && p.neutron() <= 0) continue;
            if (seen.add(e.getKey())) {
                out.add(new ArmorProtectionEntry(new ItemStack(e.getKey()), p));
            }
        }

        if (!ArmorProtectionRegistry.allTags().isEmpty()) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (!seen.add(item)) continue;
                ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) continue;
                ArmorProtectionRegistry.Protection p = ArmorProtectionRegistry.get(stack);
                if (p == ArmorProtectionRegistry.Protection.NONE) continue;
                if (p.xray() <= 0 && p.alpha() <= 0 && p.beta() <= 0 && p.neutron() <= 0) continue;
                out.add(new ArmorProtectionEntry(stack, p));
            }
        }

        out.sort(Comparator.comparing(en -> BuiltInRegistries.ITEM.getKey(en.stack().getItem()).toString()));
        return out;
    }

    private static List<BlockShieldingEntry> collectBlockShielding() {
        List<BlockShieldingEntry> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ShieldingRegistry.Coeffs coeffs = ShieldingRegistry.get(block.defaultBlockState());
            if (coeffs == null) continue;
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !seen.add(id.toString())) continue;
            Item item = block.asItem();
            if (item == net.minecraft.world.item.Items.AIR) continue;
            out.add(new BlockShieldingEntry(new ItemStack(item), coeffs));
        }
        out.sort(Comparator.comparing(en -> BuiltInRegistries.ITEM.getKey(en.stack().getItem()).toString()));
        return out;
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
