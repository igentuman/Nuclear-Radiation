package igentuman.nr.events;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

/** Keeps the existing binding JSON parsers usable with the codec-based reload listener API. */
final class ReloadJsonCodec {
    static final Codec<JsonElement> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json));

    private ReloadJsonCodec() {}
}
