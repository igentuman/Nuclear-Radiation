// Development-only item-radiation bridge checks for NTM core or core + Space, default config.
// Verifies the shared resolver, explicit-NR precedence, transformer/container levels and the
// retired/retained native onUpdate paths. Not a replacement for carried/dropped/client gameplay tests.
ServerEvents.loaded(event => {
    const Bindings = Java.loadClass('igentuman.nr.api.binding.RadiationBindings');
    const Compat = Java.loadClass('igentuman.nr.compat.ntm.NtmCompatibility');
    const Profiles = Java.loadClass('igentuman.nr.compat.ntm.NtmEmissionProfiles');
    const HazardSystem = Java.loadClass('com.hbm.hazard.HazardSystem');
    const HazardRegistry = Java.loadClass('com.hbm.hazard.HazardRegistry');
    const Components = Java.loadClass('com.hbm.items.ModDataComponents');
    const Items = Java.loadClass('net.minecraft.world.item.Items');
    const ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');
    const DataComponents = Java.loadClass('net.minecraft.core.component.DataComponents');
    const DataComponentPatch = Java.loadClass('net.minecraft.core.component.DataComponentPatch');
    const ItemContainerContents = Java.loadClass('net.minecraft.world.item.component.ItemContainerContents');
    const ArrayList = Java.loadClass('java.util.ArrayList');
    const Float = Java.loadClass('java.lang.Float');
    const EntityType = Java.loadClass('net.minecraft.world.entity.EntityType');
    const EntitySpawnReason = Java.loadClass('net.minecraft.world.entity.EntitySpawnReason');
    const BuiltIn = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
    const Identifier = Java.loadClass('net.minecraft.resources.Identifier');
    const SV = 0.008;
    // Rhino cannot choose between the ItemStack/BlockState overloads; select them explicitly.
    const profileOf = item => Bindings['of(net.minecraft.world.item.ItemStack)'](item);
    const explicit = item => Bindings['explicit(net.minecraft.world.item.ItemStack)'](item);

    function stack(id, count) {
        return new ItemStack(BuiltIn.ITEM.getValue(Identifier.parse(id)), count || 1);
    }
    function near(name, actual, expected, tolerance) {
        if (!Number.isFinite(actual) || Math.abs(actual - expected) > (tolerance || Math.abs(expected) * 1e-6 + 1e-12)) {
            throw new Error(name + ': ' + actual + ' != ' + expected);
        }
        console.info('NR_NTM_ITEM_CHECK PASS ' + name);
    }
    function truth(name, value) {
        if (!value) throw new Error(name);
        console.info('NR_NTM_ITEM_CHECK PASS ' + name);
    }
    function ids(profile) {
        var out = [];
        profile.stacks().forEach(s => out.push(String(s.isotope().id())));
        return out.sort().join(',');
    }
    function nativeLevel(item) {
        return HazardSystem.getHazardLevelFromStack(item, HazardRegistry.RADIATION);
    }

    truth('compatibility enabled', Compat.enabled());

    // Curated material: tag c:ingots/pu239 -> Pu-239, dose-equivalent magnitude from the native level.
    var pu = stack('hbm:ingot_pu239');
    near('pu239 native level', nativeLevel(pu), 5);
    var puProfile = profileOf(pu);
    truth('pu239 imported, not explicit', !explicit(pu) && !puProfile.isEmpty());
    truth('pu239 composition ' + ids(puProfile), ids(puProfile) === 'nr:pu_239');
    near('pu239 reference Sv/s', Profiles.unshieldedSvPerSecond(puProfile), 5 * SV);

    // Processed product: fallback mixture.
    var waste = stack('hbm:nuclear_waste');
    var wasteProfile = profileOf(waste);
    truth('waste fallback ' + ids(wasteProfile), ids(wasteProfile) === 'nuclear_radiation:ntm_fallout');
    near('waste reference Sv/s', Profiles.unshieldedSvPerSecond(wasteProfile), nativeLevel(waste) * SV);

    // Explicit NR binding (c:ingots/uranium) wins over the NTM level.
    var uranium = stack('hbm:ingot_uranium');
    truth('uranium explicit NR binding', explicit(uranium));
    truth('uranium uses NR binding ' + ids(profileOf(uranium)), ids(profileOf(uranium)).indexOf('ntm') < 0);

    // Data-driven transformer: hbm:hazard_radiation on an otherwise inert item.
    var stone = new ItemStack(Items.STONE);
    truth('stone inert', profileOf(stone).isEmpty());
    Components.HAZARD_RADIATION.set(stone, Float.valueOf(3.0));
    near('component transformer level', nativeLevel(stone), 3, 1e-6);
    near('component transformer Sv/s', Profiles.unshieldedSvPerSecond(profileOf(stone)), 3 * SV, 1e-8);

    // Container transformer: the shulker's own level includes its contents exactly once.
    var contents = new ArrayList();
    contents.add(stack('hbm:ingot_pu239', 4));
    // Construct with a patch: KubeJS's ItemStack.set wrapper conflicts on this build.
    var patch = DataComponentPatch.builder();
    patch['set(net.minecraft.core.component.DataComponentType,java.lang.Object)'](
        DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
    var shulker = new ItemStack(BuiltIn.ITEM.wrapAsHolder(Items.SHULKER_BOX), 1, patch.build());
    near('shulker contents level', nativeLevel(shulker), 20, 1e-6);
    near('shulker Sv/s', Profiles.unshieldedSvPerSecond(profileOf(shulker)), 20 * SV, 1e-8);

    // Retired carried path vs retained contact/armor-mod path, on a disposable mob.
    var level = event.server.overworld();
    var pig = EntityType.PIG.create(level, EntitySpawnReason.COMMAND);
    pig.setPos(0.5, 320, 0.5);
    level.addFreshEntity(pig);
    var radiationType = HazardRegistry.RADIATION;
    var before = Compat.dose(pig);
    radiationType.onUpdate(pig, 100.0, stack('hbm:ingot_pu239'));
    near('carried onUpdate retired', Compat.dose(pig) - before, 0, 1e-9);
    Compat.retainNativeItemRadiation(() => radiationType.onUpdate(pig, 100.0, stack('hbm:ingot_pu239')));
    truth('retained native path still contaminates', Compat.dose(pig) > before);
    pig.discard();

    console.info('NR_NTM_ITEM_CHECK ALL PASSED');
});
