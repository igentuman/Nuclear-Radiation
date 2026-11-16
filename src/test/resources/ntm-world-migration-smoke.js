// Development only: install in an OFFLINE-SEEDED disposable core/Space run directory.
// See ntm-seed-legacy-world.py. Run twice, stopping normally between launches.
ServerEvents.loaded(event => {
    var Native = Java.loadClass('com.hbm.handler.radiation.RadiationSystemNT');
    var Bridge = Java.loadClass('igentuman.nr.compat.ntm.NtmWorldBridge');
    var Reader = Java.loadClass('igentuman.nr.compat.ntm.NtmLegacyWorldReader');
    var Profiles = Java.loadClass('igentuman.nr.compat.ntm.NtmEmissionProfiles');
    var Attachments = Java.loadClass('igentuman.nr.radiation.storage.NRAttachments');
    var Archive = Java.loadClass('igentuman.nr.compat.ntm.NtmMigrationArchive');
    var JsonIO = Java.loadClass('dev.latvian.mods.kubejs.util.JsonIO');
    var Pos = Java.loadClass('net.minecraft.core.BlockPos');
    var level = event.server.overworld();
    var calibration = Profiles.configured();
    var Sampler = Java.loadClass('igentuman.nr.radiation.simulation.RadiationFieldSampler');
    var physicalUnit = Sampler.profile(calibration.profile(1, 0)).total();
    var readingUnit = calibration.nativeWorldReading(physicalUnit);
    function check(name, actual, expected) {
        if (!Number.isFinite(actual) || Math.abs(actual - expected) > 1e-7) {
            throw new Error(name + ': ' + actual + ' != ' + expected);
        }
        console.info('NR_NTM_MIGRATION_CHECK PASS ' + name);
    }
    function owned(chunk, pos) {
        return Bridge.field(level, chunk).ownedAt(pos.getX(), pos.getY(), pos.getZ(), true, calibration);
    }
    var legacyPos = new Pos(48 * 16, 300, 49 * 16);
    var legacyChunk = level.getChunkAt(legacyPos);
    var prepared = Reader.prepare(level, legacyChunk);
    var dimension = prepared.recoveryRecord().getParent().getParent().getParent().getParent();
    var phasePath = String(dimension) + '/../../../nr_ntm_migration_smoke_seeded';
    var phase = JsonIO.readJson(phasePath).getAsJsonObject();
    var archive = new Archive(dimension);
    // Exact original compressed SavedData remains recoverable while the native drain is suppressed.
    archive.read(phase.get('source_sha256').getAsString());
    var previouslyMigrated = legacyChunk.getData(Attachments.NTM_WORLD_FIELD.get()).migrated();
    var imported = Bridge.field(level, legacyChunk);
    if (!imported.migrated()) throw new Error('Live chunk migration marker missing');
    if (!phase.get('validated').getAsBoolean()) {
        if (!previouslyMigrated) {
            check('later-loaded old SavedData imported', owned(legacyChunk, legacyPos), 42);
        } else {
            var resumed = owned(legacyChunk, legacyPos);
            if (resumed < 0 || resumed > 42) throw new Error('Invalid resumed legacy activity: ' + resumed);
            check('frozen native recovery source', prepared.candidate().ownedAt(768, 300, 784, true, calibration), 42);
        }
        phase.addProperty('fingerprint', imported.legacyFingerprint());
    } else {
        check('saved migration fingerprint retained', imported.legacyFingerprint(), phase.get('fingerprint').getAsLong());
        // First run clears the old release. Native storage still contains 42, and must never resurrect it.
        check('restart does not reimport native field', owned(legacyChunk, legacyPos), 0);
        check('recoverable native source remains intact', prepared.candidate().ownedAt(768, 300, 784, true, calibration), 42);
        console.info('NR_NTM_MIGRATION_CHECK RESTART PASSED');
    }

    var pos = new Pos(128, 300, 128);
    var chunk = level.getChunkAt(pos);
    Native.setRadForCoord(level, pos, 12.5);
    check('live set owns a profile', owned(chunk, pos), 12.5);
    var emptyPos = new Pos(144, 300, 128);
    level.getChunkAt(emptyPos);
    var baseline = Native.getRadForCoord(level, emptyPos);
    check('world instrument reads physical field', Native.getRadForCoord(level, pos) - baseline, 12.5 * readingUnit);
    Native.incrementRad(level, pos, 3, 14);
    check('increment cap', owned(chunk, pos), 14);
    Native.decrementRad(level, pos, 2);
    check('owned decrement', owned(chunk, pos), 12);

    var Leftover = Java.loadClass('igentuman.nr.radiation.source.LeftOverRadSource');
    var Registry = Java.loadClass('igentuman.nr.radiation.source.WorldSourceRegistry');
    var registry = Registry.get(level);
    var unrelated = new Leftover(level, pos, calibration.profile(4, prepared.candidate().importTick()), prepared.candidate().importTick(), false);
    registry.register(unrelated);
    check('world read includes unrelated NR source', Native.getRadForCoord(level, pos) - baseline, 16 * readingUnit);
    Native.decrementRad(level, pos, 100);
    check('decrement leaves unrelated source', Native.getRadForCoord(level, pos) - baseline, 4 * readingUnit);
    Native.setRadForCoord(level, pos, 12.5);
    registry.remove(unrelated.getId());

    var NR = Java.loadClass('igentuman.nr.compat.ntm.NtmCompatibility');
    var Cow = Java.loadClass('net.minecraft.world.entity.animal.cow.Cow');
    var Type = Java.loadClass('net.minecraft.world.entity.EntityType');
    var Hbm = Java.loadClass('com.hbm.capability.HbmLivingProps');
    var Effects = Java.loadClass('com.hbm.handler.EntityEffectHandler');
    var Processor = Java.loadClass('igentuman.nr.radiation.simulation.entity.EntityDoseProcessor');
    var cow = new Cow(Type.COW, level);
    cow.setPos(128.5, 300, 128.5);
    Hbm.setRadiation(cow, 0);
    Effects.onUpdate(cow);
    check('native world intake retired', NR.data(cow).svTotalCareer(), 0);
    Processor.tick(level, cow, prepared.candidate().importTick(), 20);
    var General = Java.loadClass('igentuman.nr.config.GeneralConfig');
    var Config = Java.loadClass('igentuman.nr.config.RadiationConfig');
    var recoveryHours = General.ENTITY_DECAY_MULTIPLIER.get() / (5 * 3600);
    var expectedDose = 12.5 * physicalUnit * Math.exp(-Math.LN2 * recoveryHours)
        - Config.BASE_DECAY_SV_PER_HOUR.get() * recoveryHours;
    check('NR world intake applied once', NR.data(cow).svTotalCareer(), expectedDose);

    // Exercise the actual native tick entry point: neither retention nor diffusion may run.
    var frozen = Reader.prepare(level, chunk).candidate();
    var nativeBefore = frozen.ownedAt(128, 300, 128, true, calibration);
    for (var i = 0; i < 100; i++) Native.tickServer(event.server);
    check('native workers remain frozen', Reader.prepare(level, chunk).candidate().ownedAt(128, 300, 128, true, calibration), nativeBefore);
    check('repeated native ticks do not rewrite NR atoms', owned(chunk, pos), 12.5);
    Native.flushForSave(level, true);
    Native.closeWorld(level);
    check('native close/reopen does not lose NR ownership', Native.getRadForCoord(level, pos) - baseline, 12.5 * readingUnit);

    var fogMaximum = 0;
    Native.forEachChunkMax(level, (x, z, value) => { if (x === 8 && z === 8) fogMaximum = value; });
    if (fogMaximum < 12.5 * readingUnit) throw new Error('Fog enumeration lost migrated volume');
    console.info('NR_NTM_MIGRATION_CHECK PASS NR-backed fog enumeration');

    // An initially air-filled source volume reaches a subsequently placed surface block.
    // Native decayBlock chooses the actual conversion; this checks the redirected queue and its consumer.
    var Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks');
    var NativeConfig = Java.loadClass('com.hbm.config.RadiationConfig');
    var effectPos = new Pos(130, 300, 130);
    level.setBlock(effectPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
    Native.setRadForCoord(level, pos, 1000);
    var previousEffects = NativeConfig.worldRadEffects;
    NativeConfig.worldRadEffects = true;
    try {
        var tries = 0;
        while (String(level.getBlockState(effectPos).getBlock()) === String(Blocks.GRASS_BLOCK) && tries++ < 10000) {
            Bridge.queueWorldEffects(event.server);
            Bridge.drainWorldEffects(level);
        }
        if (String(level.getBlockState(effectPos).getBlock()) === String(Blocks.GRASS_BLOCK)) throw new Error('NR-backed block decay did not run');
        console.info('NR_NTM_MIGRATION_CHECK PASS NR-backed native block conversion');
    } finally {
        NativeConfig.worldRadEffects = previousEffects;
        level.setBlock(effectPos, Blocks.AIR.defaultBlockState(), 3);
        Native.setRadForCoord(level, pos, 12.5);
    }

    // Loaded guards must not generate/load a new Minecraft chunk.
    var absent = new Pos(1600000, 300, 1600000);
    Native.setRadForCoord(level, absent, 9);
    check('unloaded world read', Native.getRadForCoord(level, absent), 0);
    if (level.getChunkSource().getChunkNow(100000, 100000) !== null) throw new Error('Adapter loaded missing chunk');

    registry.register(unrelated);
    Native.clearWorld(level);
    check('clear removes owned release', owned(chunk, pos), 0);
    check('clear preserves unrelated source', Native.getRadForCoord(level, pos) - baseline, 4 * readingUnit);
    check('clear retains migration marker', imported.migrated() ? 1 : 0, 1);
    var later = new Pos(50 * 16, 300, 51 * 16);
    var laterChunk = level.getChunkAt(later);
    check('clear suppresses later-loaded legacy release', owned(laterChunk, later), 0);
    registry.remove(unrelated.getId());
    Native.setRadForCoord(level, pos, 3);
    check('new release survives clear-generation synchronization', owned(chunk, pos), 3);
    // A dimension owns its watermark; an overworld clear must not clear Nether releases.
    var nether = event.server['getLevel(net.minecraft.resources.ResourceKey)'](Java.loadClass('net.minecraft.world.level.Level').NETHER);
    var netherPos = new Pos(128, 100, 128);
    var netherChunk = nether.getChunkAt(netherPos);
    nether.setBlock(netherPos, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState(), 3);
    Native.setRadForCoord(nether, netherPos, 6);
    check('dimension-independent release', Bridge.field(nether, netherChunk).ownedAt(128, 100, 128, true, calibration), 6);
    Native.clearWorld(level);
    check('world clear is dimension-local', Bridge.field(nether, netherChunk).ownedAt(128, 100, 128, true, calibration), 6);
    var togglePos = new Pos(160, 300, 160);
    level.getChunkAt(togglePos);
    Native.setRadForCoord(level, togglePos, 25); // Sentinel for optional toggle fixture.
    phase.addProperty('validated', true);
    JsonIO.write(phasePath, phase);
    console.info('NR_NTM_MIGRATION_CHECK ALL PASSED');
});
