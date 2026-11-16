// Development only, AFTER the main world-migration fixture has passed and saved normally.
// Remove the entity/migration fixtures, install this one, set enabled=false and launch/stop.
// Set enabled=true, restart the SAME disposable core/Space directory and check REENABLED PASSED.
ServerEvents.loaded(event => {
    var NR = Java.loadClass('igentuman.nr.compat.ntm.NtmCompatibility');
    var Native = Java.loadClass('com.hbm.handler.radiation.RadiationSystemNT');
    var Profiles = Java.loadClass('igentuman.nr.compat.ntm.NtmEmissionProfiles');
    var Attachments = Java.loadClass('igentuman.nr.radiation.storage.NRAttachments');
    var Pos = Java.loadClass('net.minecraft.core.BlockPos');
    var JsonIO = Java.loadClass('dev.latvian.mods.kubejs.util.JsonIO');
    var JsonObject = Java.loadClass('com.google.gson.JsonObject');
    var Resource = Java.loadClass('net.minecraft.world.level.storage.LevelResource');
    var path = event.server.getWorldPath(Resource.ROOT).resolve('nr_ntm_toggle.json');
    var level = event.server.overworld();
    var sentinel = new Pos(160, 300, 160);
    var freshLevel = event.server['getLevel(net.minecraft.resources.ResourceKey)'](Java.loadClass('net.minecraft.world.level.Level').NETHER);
    var fresh = new Pos(336, 100, 336);
    var clearedFresh = new Pos(320, 300, 320);
    var chunk = level.getChunkAt(sentinel);
    var stored = chunk.getData(Attachments.NTM_WORLD_FIELD.get());
    var calibration = Profiles.configured();
    if (!stored.migrated()) throw new Error('Run and normally save the main migration fixture first');
    function check(name, actual, expected) {
        if (!Number.isFinite(actual) || Math.abs(actual - expected) > Math.abs(expected) * 1e-12 + 1e-7) {
            throw new Error(name + ': ' + actual + ' != ' + expected);
        }
        console.info('NR_NTM_TOGGLE_CHECK PASS ' + name);
    }
    if (!NR.enabled()) {
        // Freeze native diffusion only in this fixture, so a new sidecar release has a deterministic value.
        var NativeConfig = Java.loadClass('com.hbm.config.RadiationConfig');
        NativeConfig.radTickRate = 1000000;
        NativeConfig.worldRadEffects = false;
        var record = new JsonObject();
        record.addProperty('activity', stored.profileAt(160, 300, 160, true).totalActivityBq());
        record.addProperty('fingerprint', String(stored.legacyFingerprint()));
        if (calibration.nativeUnits(stored.profileAt(160, 300, 160, true)) <= 0) throw new Error('Missing NR sentinel');
        Native.setRadForCoord(level, sentinel, 999);
        var freshChunk = freshLevel.getChunkAt(fresh);
        freshLevel.setBlock(fresh, Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState(), 3);
        Native.setRadForCoord(freshLevel, fresh, 19);
        Native.flushForSave(freshLevel, true);
        var clearedChunk = level.getChunkAt(clearedFresh);
        Native.setRadForCoord(level, clearedFresh, 21);
        if (clearedChunk.getData(Attachments.NTM_WORLD_FIELD.get()).migrated()) throw new Error('Cleared-dimension fixture already migrated');
        Native.flushForSave(level, true);
        check('disabled native writer remains active', Native.getRadForCoord(level, sentinel), 999);
        check('disabled bridge leaves NR inventory unchanged', stored.profileAt(160, 300, 160, true).totalActivityBq(),
            record.get('activity').getAsDouble());
        if (freshChunk.getData(Attachments.NTM_WORLD_FIELD.get()).migrated()) throw new Error('Disabled bridge imported a chunk');
        JsonIO.write(path, record);
        console.info('NR_NTM_TOGGLE_CHECK DISABLED PASSED');
    } else {
        var record = JsonIO.readJson(path).getAsJsonObject();
        check('saved NR atoms retained while disabled', stored.profileAt(160, 300, 160, true).totalActivityBq(),
            record.get('activity').getAsDouble());
        if (String(stored.legacyFingerprint()) !== record.get('fingerprint').getAsString()) throw new Error('Migration marker changed');
        var Bridge = Java.loadClass('igentuman.nr.compat.ntm.NtmWorldBridge');
        var Sampler = Java.loadClass('igentuman.nr.radiation.simulation.RadiationFieldSampler');
        var original = Sampler.profile(stored.profileAt(160, 300, 160, true)).total();
        var resumed = Sampler.profile(NR.worldProfile(level, sentinel)).total();
        if (resumed <= 0 || resumed >= original) throw new Error('Elapsed-game-time decay was not resumed');
        var freshChunk = freshLevel.getChunkAt(fresh);
        check('previously unmigrated native sidecar imported once',
            Bridge.field(freshLevel, freshChunk).ownedAt(336, 100, 336, true, calibration), 19);
        if (Native.getRadForCoord(level, sentinel) >= calibration.nativeWorldReading(original) * 2) {
            throw new Error('Intervening native 999 overwrote migrated NR state');
        }
        var clearedChunk = level.getChunkAt(clearedFresh);
        var Reader = Java.loadClass('igentuman.nr.compat.ntm.NtmLegacyWorldReader');
        check('post-clear disabled-session release is recoverable',
            Reader.prepare(level, clearedChunk).candidate().ownedAt(320, 300, 320, true, calibration), 21);
        check('NR clear takes precedence over disabled-session release',
            Bridge.field(level, clearedChunk).ownedAt(320, 300, 320, true, calibration), 0);
        console.info('NR_NTM_TOGGLE_CHECK REENABLED PASSED');
    }
});
