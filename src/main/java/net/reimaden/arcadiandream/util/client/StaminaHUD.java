package net.reimaden.arcadiandream.util.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.reimaden.arcadiandream.ArcadianDream;
import net.reimaden.arcadiandream.util.IEntityDataSaver;
import net.reimaden.arcadiandream.util.ModTags;
import net.reimaden.arcadiandream.util.StaminaHelper;

public class StaminaHUD implements HudRenderCallback {

    private static final Identifier STAMINA_BAR = new Identifier(ArcadianDream.MOD_ID,
            "textures/ui/stambar.png");

    MinecraftClient client = MinecraftClient.getInstance();




    private void renderCurrentStaminaBar(DrawContext matrices, int x, int y){
        IEntityDataSaver player = (IEntityDataSaver) client.player;
        double currentStamina = (StaminaHelper.getStamina(player));
        double currentMax = (StaminaHelper.getMaxStamina(player));
        double adjustedWidth = ((182 / currentMax) * currentStamina);
        matrices.drawTexture(STAMINA_BAR, x - 91, y - 29, 0, 0, (int) adjustedWidth, 5, 182, 5);

    }

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if (client != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            int x = width / 2;
            int y = height;

            if (client.player.getMainHandStack().isIn(ModTags.Items.STAMINA_BASED_ITEM) && !client.player.isCreative() && !client.player.isSpectator())  {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, STAMINA_BAR);
                renderCurrentStaminaBar(drawContext, x, y);


            }
        }
    }
}

