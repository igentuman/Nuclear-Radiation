package igentuman.nr.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NREmiCategory extends EmiRecipeCategory {

    private final Component title;

    public NREmiCategory(Identifier id, EmiRenderable icon, Component title) {
        super(id, icon);
        this.title = title;
    }

    @Override
    public Component getName() {
        return title;
    }
}
