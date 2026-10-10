package igentuman.nr.integration.jei;

import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.binding.RadiationBindings;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.shielding.ArmorProtectionRegistry;
import igentuman.nr.config.GeneralConfig;
import igentuman.nr.network.ClientRecipeCache;
import igentuman.nr.radiation.shielding.world.ShieldingRegistry;
import igentuman.nr.recipe.BlockIrradiationRecipe;
import igentuman.nr.recipe.MutationRecipe;
import igentuman.nr.recipe.ShieldingUpgradeRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    private IJeiRuntime runtime;
    private List<Isotope> displayedIsotopes = List.of();
    private List<RadioactiveItemEntry> displayedRadioactiveItems = List.of();
    private List<ArmorProtectionEntry> displayedArmorProtection = List.of();
    private List<BlockShieldingEntry> displayedBlockShielding = List.of();
    private List<MutationRecipe> displayedMutations = List.of();
    private List<BlockIrradiationRecipe> displayedBlockIrradiations = List.of();
    private int registeredRecipeGeneration;

    @Override
    public Identifier getPluginUid() {
        return NRJeiTypes.PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (!GeneralConfig.RADIATION_ENABLED.get()) return;
        IGuiHelper gh = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new IsotopeStatsCategory(gh),
                new RadioactiveItemsCategory(gh),
                new DecayGraphCategory(gh),
                new ArmorProtectionCategory(gh),
                new BlockShieldingCategory(gh),
                new MutationCategory(gh),
                new BlockIrradiationCategory(gh)
        );
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (!GeneralConfig.RADIATION_ENABLED.get()) return;
        registration.getSmithingCategory().addExtension(ShieldingUpgradeRecipe.class, new ShieldingUpgradeJeiExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!GeneralConfig.RADIATION_ENABLED.get()) return;
        displayedIsotopes = collectIsotopes();
        displayedRadioactiveItems = collectRadioactiveItems();
        displayedArmorProtection = collectArmorProtection();
        displayedBlockShielding = collectBlockShielding();
        registration.addRecipes(NRJeiTypes.ISOTOPE_STATS, displayedIsotopes);
        registration.addRecipes(NRJeiTypes.DECAY_GRAPH, displayedIsotopes);
        registration.addRecipes(NRJeiTypes.RADIOACTIVE_ITEMS, displayedRadioactiveItems);
        registration.addRecipes(NRJeiTypes.ARMOR_PROTECTION, displayedArmorProtection);
        registration.addRecipes(NRJeiTypes.BLOCK_SHIELDING, displayedBlockShielding);
        displayedMutations = ClientRecipeCache.mutations();
        displayedBlockIrradiations = ClientRecipeCache.blockIrradiations();
        registeredRecipeGeneration = ClientRecipeCache.generation();
        registration.addRecipes(NRJeiTypes.MUTATION, displayedMutations);
        registration.addRecipes(NRJeiTypes.BLOCK_IRRADIATION, displayedBlockIrradiations);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        ClientRecipeCache.setListener(this::refreshRecipes);
        if (registeredRecipeGeneration != ClientRecipeCache.generation()) {
            refreshRecipes(ClientRecipeCache.mutations(), ClientRecipeCache.blockIrradiations());
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        ClientRecipeCache.setListener(null);
        runtime = null;
        displayedIsotopes = List.of();
        displayedRadioactiveItems = List.of();
        displayedArmorProtection = List.of();
        displayedBlockShielding = List.of();
        displayedMutations = List.of();
        displayedBlockIrradiations = List.of();
    }

    private void refreshRecipes(List<MutationRecipe> mutations, List<BlockIrradiationRecipe> blockIrradiations) {
        if (runtime == null || !GeneralConfig.RADIATION_ENABLED.get()) return;
        var recipes = runtime.getRecipeManager();
        recipes.hideRecipes(NRJeiTypes.ISOTOPE_STATS, displayedIsotopes);
        recipes.hideRecipes(NRJeiTypes.DECAY_GRAPH, displayedIsotopes);
        recipes.hideRecipes(NRJeiTypes.RADIOACTIVE_ITEMS, displayedRadioactiveItems);
        recipes.hideRecipes(NRJeiTypes.ARMOR_PROTECTION, displayedArmorProtection);
        recipes.hideRecipes(NRJeiTypes.BLOCK_SHIELDING, displayedBlockShielding);
        recipes.hideRecipes(NRJeiTypes.MUTATION, displayedMutations);
        recipes.hideRecipes(NRJeiTypes.BLOCK_IRRADIATION, displayedBlockIrradiations);
        displayedIsotopes = collectIsotopes();
        displayedRadioactiveItems = collectRadioactiveItems();
        displayedArmorProtection = collectArmorProtection();
        displayedBlockShielding = collectBlockShielding();
        recipes.addRecipes(NRJeiTypes.ISOTOPE_STATS, displayedIsotopes);
        recipes.addRecipes(NRJeiTypes.DECAY_GRAPH, displayedIsotopes);
        recipes.addRecipes(NRJeiTypes.RADIOACTIVE_ITEMS, displayedRadioactiveItems);
        recipes.addRecipes(NRJeiTypes.ARMOR_PROTECTION, displayedArmorProtection);
        recipes.addRecipes(NRJeiTypes.BLOCK_SHIELDING, displayedBlockShielding);
        recipes.addRecipes(NRJeiTypes.MUTATION, mutations);
        recipes.addRecipes(NRJeiTypes.BLOCK_IRRADIATION, blockIrradiations);
        displayedMutations = mutations;
        displayedBlockIrradiations = blockIrradiations;
        registeredRecipeGeneration = ClientRecipeCache.generation();
    }

    private static List<Isotope> collectIsotopes() {
        List<Isotope> isotopes = new ArrayList<>(IsotopeRegistry.all());
        isotopes.sort(Comparator.comparing(Isotope::id));
        return isotopes;
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
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
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
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !seen.add(id.toString())) continue;
            out.add(new RadioactiveItemEntry(stack, profile));
        }
        return out;
    }
}
