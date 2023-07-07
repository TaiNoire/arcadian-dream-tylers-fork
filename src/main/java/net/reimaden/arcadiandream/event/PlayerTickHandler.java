package net.reimaden.arcadiandream.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.reimaden.arcadiandream.util.IEntityDataSaver;
import net.reimaden.arcadiandream.util.StaminaHelper;

public class PlayerTickHandler implements ServerTickEvents.StartTick {

    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            IEntityDataSaver dataPlayer = ((IEntityDataSaver) player);
            dataPlayer.getPersistentData();
            if (StaminaHelper.getStamina((IEntityDataSaver) dataPlayer) < StaminaHelper.getMaxStamina((IEntityDataSaver) dataPlayer)){
                        StaminaHelper.changeStamina((IEntityDataSaver) player, StaminaHelper.getStaminaRegenerationFactor((IEntityDataSaver) player));
                }



        }
    }
}