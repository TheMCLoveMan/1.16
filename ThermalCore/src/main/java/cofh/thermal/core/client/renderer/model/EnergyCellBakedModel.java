package cofh.thermal.core.client.renderer.model;

import cofh.core.client.renderer.model.BakedQuadRetextured;
import cofh.core.client.renderer.model.ModelUtils;
import cofh.core.util.ComparableItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cofh.core.util.constants.NBTTags.TAG_BLOCK_ENTITY;
import static cofh.core.util.constants.NBTTags.TAG_SIDES;
import static cofh.thermal.core.client.gui.ThermalTextures.*;
import static cofh.thermal.core.common.ThermalConfig.DEFAULT_CELL_SIDES_RAW;
import static net.minecraft.util.Direction.*;

public class EnergyCellBakedModel extends CellBakedModel {

    public EnergyCellBakedModel(IBakedModel originalModel) {

        super(originalModel);
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        List<BakedQuad> quads = super.getQuads(state, side, rand, extraData);
        if (side == null || quads.isEmpty()) {
            return quads;
        }
        byte[] sideConfigRaw = extraData.getData(ModelUtils.SIDES);
        Direction facing = extraData.getData(ModelUtils.FACING);
        if (sideConfigRaw == null) {
            // This shouldn't happen, but playing it safe.
            return quads;
        }
        int sideIndex = side.getIndex();
        // SIDES
        int configHash = Arrays.hashCode(sideConfigRaw);
        BakedQuad[] cachedSideQuads = SIDE_QUAD_CACHE.get(configHash);
        if (cachedSideQuads == null || cachedSideQuads.length < 6) {
            cachedSideQuads = new BakedQuad[6];
        }
        if (cachedSideQuads[sideIndex] == null) {
            cachedSideQuads[sideIndex] = new BakedQuadRetextured(quads.get(0), getTextureRaw(sideConfigRaw[sideIndex]));
            SIDE_QUAD_CACHE.put(configHash, cachedSideQuads);
        }
        quads.add(cachedSideQuads[sideIndex]);
        return quads;
    }

    @Override
    public ItemOverrideList getOverrides() {

        return overrideList;
    }

    protected final ItemOverrideList overrideList = new ItemOverrideList() {

        @Nullable
        @Override
        public IBakedModel getOverrideModel(IBakedModel model, ItemStack stack, @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {

            CompoundNBT tag = stack.getChildTag(TAG_BLOCK_ENTITY);
            byte[] sideConfigRaw = getSideConfigRaw(tag);
            int itemHash = new ComparableItemStack(stack).hashCode();
            int configHash = Arrays.hashCode(sideConfigRaw);

            IBakedModel ret = MODEL_CACHE.get(Arrays.asList(itemHash, configHash));
            if (ret == null) {
                ModelUtils.WrappedBakedModelBuilder builder = new ModelUtils.WrappedBakedModelBuilder(model);
                BakedQuad[] cachedQuads = ITEM_QUAD_CACHE.get(configHash);
                if (cachedQuads == null || cachedQuads.length < 6) {
                    cachedQuads = new BakedQuad[6];

                    cachedQuads[0] = new BakedQuadRetextured(builder.getQuads(DOWN).get(0), getTextureRaw(sideConfigRaw[0]));
                    cachedQuads[1] = new BakedQuadRetextured(builder.getQuads(UP).get(0), getTextureRaw(sideConfigRaw[1]));
                    cachedQuads[2] = new BakedQuadRetextured(builder.getQuads(NORTH).get(0), getTextureRaw(sideConfigRaw[2]));
                    cachedQuads[3] = new BakedQuadRetextured(builder.getQuads(SOUTH).get(0), getTextureRaw(sideConfigRaw[3]));
                    cachedQuads[4] = new BakedQuadRetextured(builder.getQuads(WEST).get(0), getTextureRaw(sideConfigRaw[4]));
                    cachedQuads[5] = new BakedQuadRetextured(builder.getQuads(EAST).get(0), getTextureRaw(sideConfigRaw[5]));
                    ITEM_QUAD_CACHE.put(configHash, cachedQuads);
                }
                builder.addFaceQuad(DOWN, cachedQuads[0]);
                builder.addFaceQuad(UP, cachedQuads[1]);
                builder.addFaceQuad(NORTH, cachedQuads[2]);
                builder.addFaceQuad(SOUTH, cachedQuads[3]);
                builder.addFaceQuad(WEST, cachedQuads[4]);
                builder.addFaceQuad(EAST, cachedQuads[5]);

                ret = builder.build();
                MODEL_CACHE.put(Arrays.asList(itemHash, configHash), ret);
            }
            return ret;
        }
    };

}