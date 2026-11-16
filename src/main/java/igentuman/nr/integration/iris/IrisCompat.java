package igentuman.nr.integration.iris;

import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/** Optional Iris API bridge without a hard dependency on a version-specific Iris artifact. */
public final class IrisCompat {
    private static boolean checked;
    private static Object api;
    private static Method shadowPass;

    private IrisCompat() {}

    public static boolean renderingShadowPass() {
        if (!checked) {
            ModList mods = ModList.get();
            if (mods == null) return false;
            checked = true;
            if (mods.isLoaded("iris")) {
                try {
                    Class<?> type = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    api = type.getMethod("getInstance").invoke(null);
                    shadowPass = type.getMethod("isRenderingShadowPass");
                } catch (ReflectiveOperationException | LinkageError ignored) {
                    api = null;
                }
            }
        }
        if (api == null) return false;
        try {
            return (boolean) shadowPass.invoke(api);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            api = null;
            return false;
        }
    }
}
