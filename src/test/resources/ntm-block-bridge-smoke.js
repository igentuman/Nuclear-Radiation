// Development-only placed hazard-block import checks for NTM core or core + Space, default config,
// fresh disposable directory. Verifies the block resolver fallback, explicit-NR precedence, sustained
// non-contaminating NR block sources, stale-source removal and suppression of the native per-block
// world release. Registration and removal go through real setBlock calls, exercising the core
// LevelChunkBlockChangeMixin -> BlockSourceChangeProcessor path for imported NTM blocks and for an
// explicit NR block binding above the world-source floor (picked at runtime), whose removal only that
// hook can perform; explicit checks are skipped, with a logged reason, if no such block is registered.
// Not a replacement for client, shielding or gameplay-balance tests.
const NR_BLOCK_POS = [8, 300, 8];
const NR_EXPLICIT_POS = [10, 300, 8];
let nrBlockCheckTicks = -1;
let nrBlockActivity = 0;
let nrExplicitBlock = null;
let nrWorldSourceMin = null;

function nrBlockFixture() {
    return {
        Bindings: Java.loadClass('igentuman.nr.api.binding.RadiationBindings'),
        Compat: Java.loadClass('igentuman.nr.compat.ntm.NtmCompatibility'),
        Profiles: Java.loadClass('igentuman.nr.compat.ntm.NtmEmissionProfiles'),
        Registry: Java.loadClass('igentuman.nr.radiation.source.WorldSourceRegistry'),
        Source: Java.loadClass('igentuman.nr.radiation.source.BlockRadSource'),
        UUID: Java.loadClass('java.util.UUID'),
        BuiltIn: Java.loadClass('net.minecraft.core.registries.BuiltInRegistries'),
        Identifier: Java.loadClass('net.minecraft.resources.Identifier'),
        BlockPos: Java.loadClass('net.minecraft.core.BlockPos'),
        Blocks: Java.loadClass('net.minecraft.world.level.block.Blocks'),
        Level: Java.loadClass('net.minecraft.world.level.Level'),
        Config: Java.loadClass('igentuman.nr.config.RadiationConfig'),
    };
}

function nrBlockState(f, id) {
    return f.BuiltIn.BLOCK.getValue(f.Identifier.parse(id)).defaultBlockState();
}

function nrBlockNear(name, actual, expected, tolerance) {
    if (!Number.isFinite(actual) || Math.abs(actual - expected) > (tolerance || Math.abs(expected) * 1e-6 + 1e-12)) {
        throw new Error(name + ': ' + actual + ' != ' + expected);
    }
    console.info('NR_NTM_BLOCK_CHECK PASS ' + name);
}

function nrBlockTruth(name, value) {
    if (!value) throw new Error(name);
    console.info('NR_NTM_BLOCK_CHECK PASS ' + name);
}

function nrBlockIds(profile) {
    var out = [];
    profile.stacks().forEach(s => out.push(String(s.isotope().id())));
    return out.sort().join(',');
}

ServerEvents.loaded(event => {
    const f = nrBlockFixture();
    const SV = 0.008;
    // Rhino cannot choose between the ItemStack/BlockState overloads; select them explicitly.
    const profileOf = state => f.Bindings['of(net.minecraft.world.level.block.state.BlockState)'](state);
    const explicit = state => f.Bindings['explicit(net.minecraft.world.level.block.state.BlockState)'](state);

    nrBlockTruth('compatibility enabled', f.Compat.enabled());

    // Value-map block, fallback mixture: hbm:radiation_emission block_waste = 150.
    var waste = nrBlockState(f, 'hbm:block_waste');
    var wasteProfile = profileOf(waste);
    nrBlockTruth('waste imported, not explicit', !explicit(waste) && !wasteProfile.isEmpty());
    nrBlockTruth('waste fallback ' + nrBlockIds(wasteProfile), nrBlockIds(wasteProfile) === 'nuclear_radiation:ntm_fallout');
    nrBlockNear('waste reference Sv/s', f.Profiles.unshieldedSvPerSecond(wasteProfile), 150 * SV);
    nrBlockTruth('waste emission owned by NR', f.Compat.nrOwnsBlockEmission(waste));

    // Curated material through the block item: block_pu239 = 50.
    var pu = nrBlockState(f, 'hbm:block_pu239');
    var puProfile = profileOf(pu);
    if (explicit(pu)) {
        nrBlockTruth('pu239 block explicit NR binding wins', nrBlockIds(puProfile).indexOf('ntm') < 0);
    } else {
        nrBlockTruth('pu239 block composition ' + nrBlockIds(puProfile), nrBlockIds(puProfile) === 'nr:pu_239');
        nrBlockNear('pu239 block reference Sv/s', f.Profiles.unshieldedSvPerSecond(puProfile), 50 * SV);
    }

    // Inert blocks stay empty and keep no ownership.
    var stone = f.Blocks.STONE.defaultBlockState();
    nrBlockTruth('stone inert', profileOf(stone).isEmpty() && !f.Compat.nrOwnsBlockEmission(stone));

    // Native per-block release is suppressed: owned NTM world inventory does not change.
    var level = event.server.overworld();
    var pos = new f.BlockPos(NR_BLOCK_POS[0], NR_BLOCK_POS[1], NR_BLOCK_POS[2]);
    // No players: keep the chunk loaded, otherwise unload legitimately removes its block sources.
    level.setChunkForced(NR_BLOCK_POS[0] >> 4, NR_BLOCK_POS[2] >> 4, true);
    level.setBlock(pos.below(), f.Blocks.STONE.defaultBlockState(), 3);
    level.setBlock(pos, waste, 3);
    var ownedBefore = f.Compat.worldProfile(level, pos).totalActivityBq();
    for (var i = 0; i < 5; i++) waste.getBlock().tick(waste, level, pos, level.getRandom());
    nrBlockNear('native tick adds no NTM world release',
        f.Compat.worldProfile(level, pos).totalActivityBq() - ownedBefore, 0, 1e-6);

    // Detached source: sustained, non-contaminating, never decays or expires.
    var detached = f.Source.forState(f.UUID.randomUUID(), f.Level.OVERWORLD, pos, waste, profileOf(waste), 0);
    nrBlockTruth('imported block source sustained', detached.sustained());
    nrBlockTruth('imported block source does not contaminate area', !detached.contaminatesArea());
    var activity = detached.activityBq();
    detached.advanceDecay(100000000);
    nrBlockNear('sustained source does not decay', detached.activityBq(), activity);
    nrBlockTruth('sustained source never expires', detached.expiryGameTime() > 9.0e18);

    // Explicit NR block binding placed next to it; its source is never dropped by the decay-tick fallback.
    // Only blocks above the world-source floor become sources, so pick one at runtime. Default floors exclude
    // every bundled explicit block binding; then lower world_source_min_bq in this disposable config.
    var pickExplicit = floor => {
        f.BuiltIn.BLOCK.forEach(block => {
            if (nrExplicitBlock != null) return;
            var state = block.defaultBlockState();
            if (state.hasBlockEntity() || !explicit(state)) return;
            var p = profileOf(state);
            if (!p.isEmpty() && p.totalActivityBq() >= floor) nrExplicitBlock = state;
        });
    };
    var floor = Math.max(f.Config.ACTIVITY_FLOOR_BQ.get(), f.Config.WORLD_SOURCE_MIN_BQ.get());
    pickExplicit(floor);
    if (nrExplicitBlock == null) {
        nrWorldSourceMin = f.Config.WORLD_SOURCE_MIN_BQ.get();
        f.Config.WORLD_SOURCE_MIN_BQ.set(0.0);
        console.info('NR_NTM_BLOCK_CHECK no explicit block above ' + floor + ' Bq; world_source_min_bq lowered for this check');
        pickExplicit(Math.max(f.Config.ACTIVITY_FLOOR_BQ.get(), 1.0));
    }
    if (nrExplicitBlock == null) {
        console.info('NR_NTM_BLOCK_CHECK SKIP explicit NR block: no explicit block binding registered');
    } else {
        console.info('NR_NTM_BLOCK_CHECK using explicit NR block ' + nrExplicitBlock.getBlock());
        var explicitPos = new f.BlockPos(NR_EXPLICIT_POS[0], NR_EXPLICIT_POS[1], NR_EXPLICIT_POS[2]);
        level.setBlock(explicitPos.below(), f.Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(explicitPos, nrExplicitBlock, 3);
    }

    // Live sources are registered by the block-change hook (no manual registry.register).
    nrBlockActivity = activity;
    nrBlockCheckTicks = 0;
});

ServerEvents.tick(event => {
    if (nrBlockCheckTicks < 0) return;
    nrBlockCheckTicks++;
    const f = nrBlockFixture();
    var level = event.server.overworld();
    var pos = new f.BlockPos(NR_BLOCK_POS[0], NR_BLOCK_POS[1], NR_BLOCK_POS[2]);
    var explicitPos = new f.BlockPos(NR_EXPLICIT_POS[0], NR_EXPLICIT_POS[1], NR_EXPLICIT_POS[2]);
    var checkTick = nrBlockCheckTicks;
    // Any failed assertion below stops the fixture: never print ALL PASSED after an error.
    nrBlockCheckTicks = -1;
    if (checkTick === 100) {
        var source = f.Registry.get(level).atBlock(pos);
        nrBlockTruth('setBlock registered imported source', source != null);
        nrBlockTruth('placed sustained source survives decay ticks', source.sustained());
        nrBlockNear('placed sustained source keeps activity', source.activityBq(), nrBlockActivity);
        if (nrExplicitBlock != null) {
            var explicitSource = f.Registry.get(level).atBlock(explicitPos);
            nrBlockTruth('setBlock registered explicit NR source', explicitSource != null && !explicitSource.sustained());
        }
        level.setBlock(pos, f.Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(explicitPos, f.Blocks.AIR.defaultBlockState(), 3);
        nrBlockCheckTicks = checkTick;
    } else if (checkTick === 140) {
        level.setChunkForced(NR_BLOCK_POS[0] >> 4, NR_BLOCK_POS[2] >> 4, false);
        if (nrWorldSourceMin != null) f.Config.WORLD_SOURCE_MIN_BQ.set(nrWorldSourceMin);
        nrBlockTruth('imported source dropped after block removal', f.Registry.get(level).atBlock(pos) == null);
        if (nrExplicitBlock != null) {
            nrBlockTruth('explicit source dropped by block-change hook', f.Registry.get(level).atBlock(explicitPos) == null);
        }
        console.info('NR_NTM_BLOCK_CHECK ALL PASSED');
    } else {
        nrBlockCheckTicks = checkTick;
    }
});
