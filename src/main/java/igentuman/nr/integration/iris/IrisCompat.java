package igentuman.nr.integration.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

/** Keeps optional Iris classes out of the vanilla client class-loading path. */
public final class IrisCompat {
    private static Boolean loaded;

    private IrisCompat() {}

    private static boolean isLoaded() {
        if (loaded == null) {
            ModList mods = ModList.get();
            if (mods == null) return false;
            loaded = mods.isLoaded("iris");
        }
        return loaded;
    }

    public static boolean shaderPackInUse() {
        return isLoaded() && Api.shaderPackInUse();
    }

    public static boolean renderingShadowPass() {
        return isLoaded() && Api.renderingShadowPass();
    }

    private static final class Api {
        static boolean shaderPackInUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }

        static boolean renderingShadowPass() {
            return IrisApi.getInstance().isRenderingShadowPass();
        }
    }
}
