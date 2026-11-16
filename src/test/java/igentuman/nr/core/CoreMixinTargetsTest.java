package igentuman.nr.core;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/** Core mixins use defaultRequire=1: pin their Minecraft targets so a renamed descriptor fails here, not at game start. */
class CoreMixinTargetsTest {
    @Test
    void levelChunkSetBlockStateTarget() throws IOException {
        try (InputStream in = CoreMixinTargetsTest.class.getClassLoader()
                .getResourceAsStream("net/minecraft/world/level/chunk/LevelChunk.class")) {
            assertNotNull(in, "LevelChunk missing from test classpath");
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, ClassReader.SKIP_CODE);
            String desc = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)"
                    + "Lnet/minecraft/world/level/block/state/BlockState;";
            assertTrue(node.methods.stream().anyMatch(m -> m.name.equals("setBlockState") && m.desc.equals(desc)),
                    "LevelChunk.setBlockState" + desc);
        }
    }
}
