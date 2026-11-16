// Development smoke checks: copy into a disposable run directory under kubejs/server_scripts.
// Requires NTM, KubeJS, and the default NR NTM conversion settings. Never installed by the mod.
ServerEvents.loaded(event => {
    const Hbm = Java.loadClass('com.hbm.capability.HbmLivingProps');
    const Props = Java.loadClass('com.hbm.entity.EntityHbmProps');
    const Cow = Java.loadClass('net.minecraft.world.entity.animal.cow.Cow');
    const Type = Java.loadClass('net.minecraft.world.entity.EntityType');
    const NR = Java.loadClass('igentuman.nr.compat.ntm.NtmCompatibility');
    const Handler = Java.loadClass('com.hbm.main.ModEventHandlerMobs');
    const level = event.server.overworld();
    const cow = new Cow(Type.COW, level);
    function check(name, actual, expected) {
        if (Math.abs(actual - expected) > 0.000000001) throw new Error(name + ': ' + actual + ' != ' + expected);
        console.info('NR_NTM_CHECK PASS ' + name);
    }
    const legacy = new Props();
    legacy.setRads(125);
    legacy.nr$bind(cow);
    check('legacy migration', NR.data(cow).svTotalCareer(), 1);
    legacy.nr$bind(cow);
    check('idempotent migration', NR.data(cow).svTotalCareer(), 1);
    Hbm.setRadiation(cow, 200);
    check('API setter', NR.data(cow).svTotalCareer(), 1.6);
    Hbm.incrementRadiation(cow, -10);
    check('medicine', NR.data(cow).svTotalCareer(), 1.52);
    Hbm.incrementRadiation(cow, 5);
    check('exposure', NR.data(cow).svTotalCareer(), 1.56);
    const raw = Hbm.getData(cow);
    raw.setRads(150);
    check('raw setter', NR.data(cow).svTotalCareer(), 1.2);
    NR.data(cow).setSvTotalCareer(2.4);
    check('reverse threshold read', Hbm.getRadiation(cow), 300);
    Handler.onEntityDeath(cow, cow.damageSources().generic());
    check('native death reset suppressed', NR.data(cow).svTotalCareer(), 2.4);
    const Armor = Java.loadClass('igentuman.nr.api.shielding.ArmorProtectionRegistry');
    const Resistance = Java.loadClass('com.hbm.handler.HazmatRegistry');
    const ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');
    const Registries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
    const Id = Java.loadClass('net.minecraft.resources.Identifier');
    const Slot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot');
    const Util = Java.loadClass('com.hbm.util.ContaminationUtil');
    const helmet = new ItemStack(Registries.ITEM.getValue(Id.parse('hbm:ajr_helmet')));
    const expected = 1 - Math.pow(10, -Resistance.getResistance(helmet));
    check('NTM armor conversion', Armor.get(helmet).xray(), expected);
    cow.setItemSlot(Slot.HEAD, helmet);
    check('combined contamination protection', Util.calculateRadiationMod(cow), 1 - Armor.summed(cow).xray());
    const Hazard = Java.loadClass('com.hbm.util.ContaminationUtil$HazardType');
    const Route = Java.loadClass('com.hbm.util.ContaminationUtil$ContaminationType');
    NR.drainExposure(cow);
    NR.drainAmbientExposure(cow);
    let before = NR.data(cow).svTotalCareer();
    Util.contaminate(cow, Hazard.RADIATION, Route.CREATIVE, 1);
    const absorbed = (1 - Armor.summed(cow).xray()) * 0.008;
    check('protected dose added once', NR.data(cow).svTotalCareer(), before + absorbed);
    check('protected absorbed rate', NR.drainExposure(cow), absorbed);
    check('unprotected ambient rate', NR.drainAmbientExposure(cow), 0.008);
    before = NR.data(cow).svTotalCareer();
    Util.contaminate(cow, Hazard.RADIATION, Route.RAD_BYPASS, 1);
    check('armor bypass', NR.data(cow).svTotalCareer(), before + 0.008);
    before = NR.data(cow).svTotalCareer();
    Util.contaminate(cow, Hazard.DIGAMMA, Route.CREATIVE, 0.01);
    check('Digamma does not change NR dose', NR.data(cow).svTotalCareer(), before);
    check('Digamma remains native', Hbm.getDigamma(cow), 0.01);
    const probe = new Cow(Type.COW, level);
    Hbm.setRadiation(probe, 0);
    const Effect = Java.loadClass('com.hbm.entity.EntityHbmProps$ContaminationEffect');
    const effect = new Effect(1, 10, false);
    Hbm.addCont(probe, effect);
    Java.loadClass('com.hbm.handler.EntityEffectHandler').onUpdate(probe);
    check('timed contamination dose', NR.data(probe).svTotalCareer(), 0.008);
    check('timed contamination lifetime', effect.time, 9);
    Hbm.getCont(probe).clear();
    if (Platform.isLoaded('ntmspace')) {
        var ntmActivationApi = Java.loadClass('com.ntmspace.fork.neutron.NeutronActivation');
        Hbm.setRadiation(probe, 0);
        ntmActivationApi.setActivation(probe, 1);
        ntmActivationApi.livingTick(probe);
        if (ntmActivationApi.enabled()) {
            check('Space activation feeds NR', NR.data(probe).svTotalCareer(), 0.0004);
            if (!(ntmActivationApi.getActivation(probe) < 1 && ntmActivationApi.getActivation(probe) > 0.998)) {
                throw new Error('Space activation decay changed');
            }
            console.info('NR_NTM_CHECK PASS Space activation decay');
        } else {
            check('disabled Space activation stays disabled', NR.data(probe).svTotalCareer(), 0);
        }
    }
    Hbm.setRadiation(cow, 50);
    Java.loadClass('com.hbm.handler.EntityEffectHandler').onUpdate(cow);
    if (!cow.isRemoved()) throw new Error('NTM cow transformation was suppressed');
    console.info('NR_NTM_CHECK PASS transformation preserved');
    console.info('NR_NTM_CHECK ALL PASSED');
});
