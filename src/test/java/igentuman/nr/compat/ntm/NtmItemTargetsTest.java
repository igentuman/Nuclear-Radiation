package igentuman.nr.compat.ntm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Item-radiation mixins are @Pseudo with defaultRequire=1: a renamed pinned target fails only at game start.
 * Read the compile-only NTM bytecode as resources (never loading/initializing it) and pin every target.
 */
class NtmItemTargetsTest {
    private static ClassNode node(String name) throws IOException {
        try (InputStream in = NtmItemTargetsTest.class.getClassLoader()
                .getResourceAsStream(name.replace('.', '/') + ".class")) {
            assertNotNull(in, "pinned NTM class missing: " + name);
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    private static MethodNode method(ClassNode owner, String name, String desc) {
        return owner.methods.stream()
                .filter(m -> m.name.equals(name) && (desc == null || m.desc.equals(desc)))
                .findFirst().orElseThrow(() -> new AssertionError(owner.name + "." + name + desc));
    }

    private static long calls(MethodNode method, String owner, String name, String desc) {
        long count = 0;
        for (var insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.owner.equals(owner)
                    && call.name.equals(name) && call.desc.equals(desc)) count++;
        }
        return count;
    }

    @Test
    void hazardTypeRadiationTargets() throws IOException {
        ClassNode type = node("com.hbm.hazard.type.HazardTypeRadiation");
        method(type, "onUpdate", "(Lnet/minecraft/world/entity/LivingEntity;DLnet/minecraft/world/item/ItemStack;)V");
        method(type, "addHazardInformation",
                "(Lnet/minecraft/world/entity/player/Player;Ljava/util/List;DLnet/minecraft/world/item/ItemStack;Ljava/util/List;)V");
        ClassNode system = node("com.hbm.hazard.HazardSystem");
        assertEquals(1, calls(method(system, "addHazardInfo",
                        "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Ljava/util/List;)V"),
                "com/hbm/handler/HazmatRegistry", "getResistance", "(Lnet/minecraft/world/item/ItemStack;)D"));
    }

    @Test
    void retainedNativeContactAndArmorModTargets() throws IOException {
        String applyBlock = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/entity/LivingEntity;)V";
        ClassNode block = node("com.hbm.blocks.generic.BlockHazard");
        assertEquals(1, calls(method(block, "stepOn", null), "com/hbm/hazard/HazardSystem", "applyHazards", applyBlock));
        assertEquals(1, calls(method(block, "onEntityInside", null), "com/hbm/hazard/HazardSystem", "applyHazards", applyBlock));
        ClassNode mods = node("com.hbm.handler.ArmorModEvents");
        assertEquals(1, calls(method(mods, "onLivingUpdate", "(Lnet/minecraft/world/entity/LivingEntity;)V"),
                "com/hbm/hazard/HazardSystem", "applyHazards",
                "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"));
    }

    @Test
    void placedAndStoredEmissionTargets() throws IOException {
        String tick = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;"
                + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V";
        String increment = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;D)V";
        ClassNode hazard = node("com.hbm.blocks.generic.BlockHazard");
        assertEquals(1, calls(method(hazard, "tick", tick),
                "com/hbm/handler/radiation/ChunkRadiationManager$ProxyClass", "incrementRad", increment));
        // Imported block level/area accessors used by NtmBlockBridge.
        method(hazard, "getRadMax", "()F");
        method(hazard, "getRad3d", "()F");
        assertEquals(1, calls(method(node("com.hbm.blocks.generic.BlockHazardFalling"), "tick", tick),
                "com/hbm/handler/radiation/ChunkRadiationManager$ProxyClass", "incrementRad", increment));
        ClassNode drum = node("com.hbm.tileentity.machine.TileEntityStorageDrum");
        assertTrue(drum.interfaces.contains("net/minecraft/world/WorldlyContainer"), "NR container scan must see drums");
        assertEquals(1, calls(method(drum, "serverTick", null), "com/hbm/util/ContaminationUtil", "radiate",
                "(Lnet/minecraft/world/level/Level;DDDDF)V"));
    }

    @Test
    void heldCrateMenusExposeHeldSlot() throws IOException {
        for (String menu : new String[]{"com.hbm.inventory.container.MenuCrate",
                "com.hbm.inventory.container.ContainerCrateBase"}) {
            assertTrue(node(menu).fields.stream().anyMatch(f -> f.name.equals("heldSlot") && f.desc.equals("I")), menu);
        }
    }
}
