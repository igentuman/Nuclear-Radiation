// Development-only storage checks, including with NTM absent. Uses fabricated pocket
// geometry in a disposable chunk; this does not test native-field migration or exposure.
ServerEvents.loaded(event => {
    const Profiles = Java.loadClass('igentuman.nr.compat.ntm.NtmEmissionProfiles');
    const Fields = Java.loadClass('igentuman.nr.compat.ntm.NtmWorldField');
    const Section = Java.loadClass('igentuman.nr.compat.ntm.NtmWorldField$Section');
    const Pocket = Java.loadClass('igentuman.nr.compat.ntm.NtmWorldField$Pocket');
    const Attachments = Java.loadClass('igentuman.nr.radiation.storage.NRAttachments');
    const Integer = Java.loadClass('java.lang.Integer');
    const Collections = Java.loadClass('java.util.Collections');
    const ArrayList = Java.loadClass('java.util.ArrayList');
    const Pos = Java.loadClass('net.minecraft.core.BlockPos');
    const level = event.server.overworld();
    const chunk = level.getChunkAt(new Pos(0, 64, 0));
    const field = chunk.getData(Attachments.NTM_WORLD_FIELD.get());
    const calibration = Profiles.configured();
    function check(name, actual, expected) {
        if (!Number.isFinite(actual) || Math.abs(actual - expected) > 0.000000001) {
            throw new Error(name + ': ' + actual + ' != ' + expected);
        }
        console.info('NR_NTM_WORLD_CHECK PASS ' + name);
    }
    check('runtime fallback profile', calibration.nativeUnits(calibration.profile(3, 0)), 3);
    check('physical sampling conversion', calibration.nativeWorldReading(
        Profiles.unshieldedSvPerSecond(calibration.profile(3, 0))), 3);
    if (field.migrated()) {
        check('saved fingerprint', field.legacyFingerprint(), 321);
        check('saved pocket activity', field.ownedAt(0, 64, 0, true, calibration), 5);
        console.info('NR_NTM_WORLD_CHECK RESTART PASSED');
    } else {
        var nrWorldGeometry = Collections.nCopies(4096, Integer.valueOf(0));
        var nrWorldPockets = new ArrayList();
        nrWorldPockets.add(Pocket.of(calibration.profile(10, 0)));
        var nrWorldSections = new ArrayList();
        nrWorldSections.add(new Section(4, nrWorldGeometry, nrWorldPockets));
        if (!field.importOnce(nrWorldSections, 321, 0)) throw new Error('Storage import failed');
        field.decrement(0, 64, 0, 5, true, calibration, 0);
        check('owned decrement', field.ownedAt(0, 64, 0, true, calibration), 5);
        check('unloaded read guard', field.ownedAt(0, 64, 0, false, calibration), 0);
        chunk.markUnsaved();
        console.info('NR_NTM_WORLD_CHECK INITIAL SAVE READY');
    }
    if (field.importOnce(new ArrayList(), 999, 0)) throw new Error('Repeated import was accepted');
    console.info('NR_NTM_WORLD_CHECK ALL PASSED');
});
