package xyz.vsngamer.elevatorid.network;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.vsngamer.elevatorid.blocks.DirectionalElevatorBlock;
import xyz.vsngamer.elevatorid.init.ModConfig;
import xyz.vsngamer.elevatorid.init.ModSounds;
import xyz.vsngamer.elevatorid.init.ModTags;

import java.util.function.Supplier;

public class TeleportHandler {
    static void handle(TeleportRequest message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = ctx.get().getSender();
        if (player == null) return;

        ServerWorld world = player.getServerWorld();
        BlockPos from = message.getFrom(), to = message.getTo();

        // This ensures the player is still standing on the origin elevator
        final double distanceSq = player.getDistanceSq(new Vec3d(from).add(0, 1, 0));
        if (distanceSq > 4D) return;

//        // Temporarily checking range on server side
//        double dist = from.distanceSq(to.getX(), to.getY(), to.getZ(), false);
//        if (dist > Math.pow(ModConfig.GENERAL.range.get(), 2)) return;

        // this is already validated on the client not sure if it's needed here
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) return;

        BlockState fromState = world.getBlockState(from);
        BlockState toState = world.getBlockState(to);
        Block toElevator = toState.getBlock();

        // Same
        if (!isElevator(fromState) || !isElevator(toState)) return;

        if (!validateTarget(world, to)) return;

        // Check yaw and pitch
        final float yaw, pitch;
        yaw = ModTags.DIRECTIONAL_ELEVATORS_TAG.contains(toElevator) ? toState.get(DirectionalElevatorBlock.FACING).getHorizontalAngle() : player.rotationYaw;
        pitch = (ModConfig.GENERAL.resetPitchNormal.get() && ModTags.NORMAL_ELEVATORS_TAG.contains(toElevator))
                || (ModConfig.GENERAL.resetPitchDirectional.get() && ModTags.DIRECTIONAL_ELEVATORS_TAG.contains(toElevator)) ? 0F : player.rotationPitch;

        // Passed all tests, begin teleport
        ctx.get().enqueueWork(() -> {
            if (ModConfig.GENERAL.precisionTarget.get())
                player.connection.setPlayerLocation(to.getX() + 0.5D, to.getY() + 1D, to.getZ() + 0.5D, yaw, pitch);
            else
                player.connection.setPlayerLocation(player.posX, to.getY() + 1D, player.posZ, yaw, pitch);

            player.setMotion(player.getMotion().mul(new Vec3d(1, 0, 1)));
            world.playSound(null, to, ModSounds.TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
        });
    }

    public static boolean validateTarget(IBlockReader world, BlockPos target) {
        return validateTarget(world.getBlockState(target.up(1))) && validateTarget(world.getBlockState(target.up(2)));
    }

    private static boolean validateTarget(BlockState blockState) {
        return !blockState.getMaterial().isSolid(); // TODO maybe change this
    }

    public static boolean isElevator(BlockState blockState) {
        return ModTags.ALL_ELEVATORS_TAG.contains(blockState.getBlock());
    }
}
