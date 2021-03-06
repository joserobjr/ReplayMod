package com.replaymod.recording.gui;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.recording.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.replaymod.core.ReplayMod.TEXTURE;
import static com.replaymod.core.ReplayMod.TEXTURE_SIZE;
import static com.replaymod.core.versions.MCVer.getFontRenderer;
import static com.replaymod.core.versions.MCVer.getType;

/**
 * Renders overlay during recording.
 */
public class GuiRecordingOverlay {
    private final Minecraft mc;
    private final SettingsRegistry settingsRegistry;

    public GuiRecordingOverlay(Minecraft mc, SettingsRegistry settingsRegistry) {
        this.mc = mc;
        this.settingsRegistry = settingsRegistry;
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    /**
     * Render the recording icon and text in the top left corner of the screen.
     * @param event Rendered post game overlay
     */
    @SubscribeEvent
    public void renderRecordingIndicator(RenderGameOverlayEvent.Post event) {
        if (getType(event) != RenderGameOverlayEvent.ElementType.ALL) return;
        if (settingsRegistry.get(Setting.INDICATOR)) {
            FontRenderer fontRenderer = getFontRenderer(mc);
            fontRenderer.drawString(I18n.format("replaymod.gui.recording").toUpperCase(), 30, 18 - (fontRenderer.FONT_HEIGHT / 2), 0xffffffff);
            mc.renderEngine.bindTexture(TEXTURE);
            GlStateManager.resetColor();
            GlStateManager.enableAlpha();
            Gui.drawModalRectWithCustomSizedTexture(10, 10, 58, 20, 16, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
