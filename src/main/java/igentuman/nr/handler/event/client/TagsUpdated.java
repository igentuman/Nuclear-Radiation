package igentuman.nr.handler.event.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


public class TagsUpdated {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(TagsUpdated::tagsUpated);
    }
    public static void tagsUpated(TagsUpdatedEvent event) {
        if(RecipesUpdated.manager != null) {

        }
    }
}
