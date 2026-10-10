package igentuman.nr.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the integration mixins so they only apply when the target mod is present.
 * A mixin is applied iff its target class exists on the classpath, which is exactly
 * the condition that otherwise produces the "Error loading class / @Mixin target was
 * not found" warnings for absent optional dependencies.
 */
public class NRMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isClassPresent(targetClassName);
    }

    private static boolean isClassPresent(String className) {
        String resource = className.replace('.', '/') + ".class";
        ClassLoader[] loaders = {
                Thread.currentThread().getContextClassLoader(),
                NRMixinPlugin.class.getClassLoader()
        };
        for (ClassLoader loader : loaders) {
            if (loader != null && loader.getResource(resource) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
