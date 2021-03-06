package xyz.vsngamer.elevatorid.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import xyz.vsngamer.elevatorid.ElevatorModTab;
import xyz.vsngamer.elevatorid.init.ModConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractElevator extends Block {

    private DyeColor dyeColor;

    AbstractElevator(DyeColor color) {
        super(Block.Properties
                .create(Material.WOOL, color)
                .sound(SoundType.CLOTH)
                .hardnessAndResistance(0.8F));

        setReg(color);
        dyeColor = color;
    }

    abstract void setReg(DyeColor color);

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return ModConfig.GENERAL.mobSpawn.get() && super.canCreatureSpawn(state, world, pos, type, entityType);
    }

    private ElevatorBlockItem item;

    @Nonnull
    @Override
    public Item asItem() {
        if (item == null) {
            item = new ElevatorBlockItem();
        }
        return item;
    }

    public DyeColor getColor(){
        return dyeColor;
    }

    public class ElevatorBlockItem extends BlockItem {
         ElevatorBlockItem() {
            super(AbstractElevator.this, new Item.Properties().group(ElevatorModTab.TAB));
            ResourceLocation name = AbstractElevator.this.getRegistryName();
            if (name != null) {
                setRegistryName(name);
            }
        }
    }
}
