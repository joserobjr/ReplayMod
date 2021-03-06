package com.replaymod.replay.mixin;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=11200
import net.minecraft.stats.RecipeBook;
//#endif
//#if MC>=10904
import net.minecraft.stats.StatisticsManager;
//#else
//$$ import net.minecraft.stats.StatFileWriter;
//#endif

import static com.replaymod.core.versions.MCVer.*;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Shadow
    private Minecraft mc;

    @Shadow
    //#if MC>=10904
    private NetHandlerPlayClient connection;
    //#else
    //$$ private NetHandlerPlayClient netClientHandler;
    //#endif

    //#if MC>=11200
    @Inject(method = "func_192830_a", at=@At("HEAD"), cancellable = true)
    private void replayModReplay_createReplayCamera(World worldIn, StatisticsManager statisticsManager, RecipeBook recipeBook, CallbackInfoReturnable<EntityPlayerSP> ci) {
        if (ReplayModReplay.instance.getReplayHandler() != null) {
            ci.setReturnValue(new CameraEntity(mc, worldIn, connection, statisticsManager, recipeBook));
    //#else
    //#if MC>=10904
    //$$ @Inject(method = "createClientPlayer", at=@At("HEAD"), cancellable = true)
    //$$ private void replayModReplay_createReplayCamera(World worldIn, StatisticsManager statisticsManager, CallbackInfoReturnable<EntityPlayerSP> ci) {
    //$$     if (ReplayModReplay.instance.getReplayHandler() != null) {
    //$$         ci.setReturnValue(new CameraEntity(mc, worldIn, connection, statisticsManager));
    //#else
    //$$ @Inject(method = "func_178892_a", at=@At("HEAD"), cancellable = true)
    //$$ private void replayModReplay_createReplayCamera(World worldIn, StatFileWriter statFileWriter, CallbackInfoReturnable<EntityPlayerSP> ci) {
    //$$     if (ReplayModReplay.instance.getReplayHandler() != null) {
    //$$         ci.setReturnValue(new CameraEntity(mc, worldIn, netClientHandler, statFileWriter));
    //#endif
    //#endif
            ci.cancel();
        }
    }

    @Inject(method = "isSpectator", at=@At("HEAD"), cancellable = true)
    private void replayModReplay_isSpectator(CallbackInfoReturnable<Boolean> ci) {
        if (player(mc) instanceof CameraEntity) { // this check should in theory not be required
            ci.setReturnValue(player(mc).isSpectator());
        }
    }
}
