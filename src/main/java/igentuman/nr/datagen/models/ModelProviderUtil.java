package igentuman.nr.datagen.models;

import net.minecraft.client.renderer.RenderType;

public class ModelProviderUtil
{
	public static String getName(RenderType type)
	{
		if(type==RenderType.solid())
			return "solid";
		else if(type==RenderType.translucent())
			return "translucent";
		else if(type==RenderType.cutout())
			return "cutout";
		else if(type==RenderType.cutoutMipped())
			return "cutout_mipped";
		else
			throw new RuntimeException("Unknown render type: "+type);
	}
}
