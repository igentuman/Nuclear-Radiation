package igentuman.nr.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import igentuman.nr.api.Isotope;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.binding.RadiationBindings;
import igentuman.nr.integration.jei.ArmorProtectionEntry;
import igentuman.nr.integration.jei.BlockShieldingEntry;
import igentuman.nr.integration.jei.RadioactiveItemEntry;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.recipe.NRRecipes;
import igentuman.nr.registry.IsotopeRegistry;
import igentuman.nr.shielding.ArmorProtectionRegistry;
import igentuman.nr.shielding.ShieldingRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EmiEntrypoint
public class ModEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(NREmiCategories.ISOTOPE_STATS);
        registry.addCategory(NREmiCategories.RADIOACTIVE_ITEMS);
        registry.addCategory(NREmiCategories.ARMOR_PROTECTION);
        registry.addCategory(NREmiCategories.BLOCK_SHIELDING);
        registry.addCategory(NREmiCategories.MUTATION);
        registry.addCategory(NREmiCategories.BLOCK_IRRADIATION);

        List<Isotope> isotopes = new ArrayList<>(IsotopeRegistry.all());
        isotopes.sort(Comparator.comparing(Isotope::id));
        for (Isotope iso : isotopes) {
            registry.addRecipe(new IsotopeStatsEmiRecipe(iso));
        }

        for (RadioactiveItemEntry e : collectRadioactiveItems()) {
            registry.addRecipe(new RadioactiveItemEmiRecipe(e));
        }
        for (ArmorProtectionEntry e : collectArmorProtection()) {
            registry.addRecipe(new ArmorProtectionEmiRecipe(e));
        }
        for (BlockShieldingEntry e : collectBlockShielding()) {
            registry.addRecipe(new BlockShieldingEmiRecipe(e));
        }

        for (RecipeHolder<MutationRecipe> holder : registry.getRecipeManager().getAllRecipesFor(NRRecipes.MUTATION_TYPE.get())) {
            registry.addRecipe(new MutationEmiRecipe(holder.id(), holder.value()));
        }
        for (RecipeHolder<BlockIrradiationRecipe> holder : registry.getRecipeManager().getAllRecipesFor(NRRecipes.BLOCK_IRRADIATION_TYPE.get())) {
            registry.addRecipe(new BlockIrradiationEmiRecipe(holder.id(), holder.value()));
        }
    }

    private static List<ArmorProtectionEntry> collectArmorProtection() {
        List<ArmorProtectionEntry> out = new ArrayList<>();
        for (Map.Entry<Item, ArmorProtectionRegistry.Protection> e : ArmorProtectionRegistry.all().entrySet()) {
            ArmorProtectionRegistry.Protection p = e.getValue();
            if (p.xray() <= 0 && p.alpha() <= 0 && p.beta() <= 0 && p.neutron() <= 0) continue;
            out.add(new ArmorProtectionEntry(new ItemStack(e.getKey()), p));
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
            if (item == Items.AIR) continue;
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
