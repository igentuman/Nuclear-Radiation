package igentuman.nr.radiation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

import static igentuman.nr.NuclearRadiation.MODID;

public class FluidRadiation {
    protected static HashMap<Fluid, Double> radiationMap = new HashMap<>();
    protected static boolean initialized = false;
    public static HashMap<Fluid, Double> get()
    {
        return radiationMap;
    }

    public static void init()
    {
        if(!radiationMap.isEmpty()) {
            return;
        }

    }


    public static void add(String item, double radiation)
    {
        Fluid toAdd = getFluidByName(item);
        if(toAdd.equals(FluidStack.EMPTY.getFluid())) {
            return;
        }
        radiationMap.put(toAdd, radiation);
    }

    public static void add(Fluid item, double radiation)
    {
        radiationMap.put(item, radiation);
    }

    protected static Fluid getFluidByName(String name)
    {
        if(!name.contains(":")) {
            name = MODID +":" + name;
        }
        ResourceLocation itemKey = new ResourceLocation(name.replace("/", "_"));
        return ForgeRegistries.FLUIDS.getValue(itemKey);
    }

    public static double byFluid(Fluid item) {
        if(!initialized) {
            init();
            initialized = true;
        }
        if(radiationMap.containsKey(item)) {
            return radiationMap.get(item);
        }
        return 0;
    }
}
