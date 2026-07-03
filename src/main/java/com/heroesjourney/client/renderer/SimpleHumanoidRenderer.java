package com.heroesjourney.client.renderer;

import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Reused for every humanoid-shaped mob we add (quest NPCs, guards, ninjas, Ken, Scarecrow,
 * Henri): only the texture differs, so there is no need for six near-identical renderer classes.
 */
public class SimpleHumanoidRenderer<T extends Mob> extends MobRenderer<T, HumanoidModel<T>> {

    private final Function<T, ResourceLocation> textureFunction;

    public SimpleHumanoidRenderer(EntityRendererProvider.Context context, Function<T, ResourceLocation> textureFunction, float shadowRadius) {
        super(context, new HumanoidModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER)), shadowRadius);
        this.textureFunction = textureFunction;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return textureFunction.apply(entity);
    }
}
