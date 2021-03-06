package com.replaymod.compat.bettersprinting;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.events.ReplayChatMessageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.Restriction;
import net.minecraftforge.fml.common.versioning.VersionRange;

//#if MC>=10904
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nullable;
//#else
//$$ import net.minecraft.util.BlockPos;
//$$ import net.minecraft.world.IWorldAccess;
//#endif

import java.util.Collections;

import static com.replaymod.compat.ReplayModCompat.LOGGER;
import static com.replaymod.core.versions.MCVer.*;

/**
 * Old Better Sprinting versions replace the vanilla player with their own, overridden instance (replacing the camera entity).
 *
 * See: https://github.com/chylex/Better-Sprinting/blob/1.8/src/main/java/chylex/bettersprinting/client/player/impl/LogicImplOverride.java
 */
public class DisableBetterSprinting {
    private static final VersionRange OLD_VERSION = VersionRange.newRange(null,
            Collections.singletonList(new Restriction(null, false, new DefaultArtifactVersion("2.0.0"), false)));
    private static final String LOGIC_CLASS_NAME = "chylex.bettersprinting.client.player.impl.LogicImplOverride";
    private static final String CONTROLLER_OVERRIDE_CLASS_NAME = LOGIC_CLASS_NAME + "$PlayerControllerMPOverride";

    public static void register() {
        Loader.instance().getModList().stream()
                .filter(mod -> mod.getModId().equalsIgnoreCase("bettersprinting"))
                .findFirst()
                .map(ModContainer::getProcessedVersion).filter(OLD_VERSION::containsVersion)
                .ifPresent($_ -> MinecraftForge.EVENT_BUS.register(new DisableBetterSprinting()));
    }

    private DisableBetterSprinting() {
        LOGGER.info("BetterSprinting workaround enabled");
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private PlayerControllerMP originalController;
    private BetterSprintingWorldAccess worldAccessHook = new BetterSprintingWorldAccess();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void beforeGuiOpenEvent(GuiOpenEvent event) {
        if (ReplayModReplay.instance.getReplayHandler() != null && world(mc) != null) {
            // During replay, get ready to revert BetterSprinting's overwritten playerController
            originalController = mc.playerController;
            //#if MC>=10904
            world(mc).addEventListener(worldAccessHook);
            //#else
            //$$ world(mc).addWorldAccess(worldAccessHook);
            //#endif
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void afterGuiOpenEvent(GuiOpenEvent event) {
        if (ReplayModReplay.instance.getReplayHandler() != null && world(mc) != null) {
            //#if MC>=10904
            world(mc).addEventListener(worldAccessHook);
            //#else
            //$$ world(mc).addWorldAccess(worldAccessHook);
            //#endif
        }
    }

    @SubscribeEvent
    public void onReplayChatMessage(ReplayChatMessageEvent event) {
        // Suppress this message if it's the Better Sprinting warning message
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            if (LOGIC_CLASS_NAME.equals(elem.getClassName())) {
                LOGGER.info("BetterSprinting warning message suppressed.");
                event.setCanceled(true);
                return;
            }
        }
    }

    private class BetterSprintingWorldAccess
            //#if MC>=10904
            implements IWorldEventListener
            //#else
            //$$ implements IWorldAccess
            //#endif
    {
        @Override
        public void onEntityRemoved(Entity entityIn) {
            if (mc.playerController != null && mc.playerController.getClass().getName().equals(CONTROLLER_OVERRIDE_CLASS_NAME)) {
                // Someone has secretly swapped out the player controller and is about to substitute their own player entity.
                // This is the right time to destroy their plan.
                LOGGER.info("Preventing player controller {} from being replaced by BetterSprinting with {}.", originalController, mc.playerController);
                mc.playerController = originalController;
            }
        }

        @Override public void notifyLightSet(BlockPos pos) {}
        @Override public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}
        @Override public void spawnParticle(int p_180442_1_, boolean p_180442_2_, double p_180442_3_, double p_180442_5_, double p_180442_7_, double p_180442_9_, double p_180442_11_, double p_180442_13_, int... p_180442_15_) {}
        @Override public void onEntityAdded(Entity entityIn) {}
        @Override public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_) {}
        @Override public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
        //#if MC>=10904
        @Override public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {}
        @Override public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {}
        @Override public void playRecord(SoundEvent soundIn, BlockPos pos) {}
        @Override public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}
        //#if MC>=11100
        //#if MC>=11102
        @Override public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_, double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_, int... p_190570_16_) {}
        //#else
        //$$ @Override public void func_190570_a(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_, double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_, int... p_190570_16_) {}
        //#endif
        //#endif
        //#else
        //$$ @Override public void markBlockForUpdate(BlockPos pos) {}
        //$$ @Override public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {}
        //$$ @Override public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch) {}
        //$$ @Override public void playRecord(String recordName, BlockPos blockPosIn) {}
        //#if MC>=10809
        //$$ @Override public void playAuxSFX(EntityPlayer p_180439_1_, int p_180439_2_, BlockPos blockPosIn, int p_180439_4_) {}
        //#else
        //$$ @Override public void playAusSFX(EntityPlayer p_180439_1_, int p_180439_2_, BlockPos blockPosIn, int p_180439_4_) {}
        //#endif
        //#endif
    }
}
