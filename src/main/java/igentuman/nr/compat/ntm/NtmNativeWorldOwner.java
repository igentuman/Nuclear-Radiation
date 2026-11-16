package igentuman.nr.compat.ntm;

import net.minecraft.server.level.ServerLevel;

/** Access the dimension without linking NTM's package-private WorldRadiationData type. */
public interface NtmNativeWorldOwner {
    ServerLevel nr$level();
}
