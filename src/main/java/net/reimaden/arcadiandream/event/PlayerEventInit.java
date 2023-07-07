package net.reimaden.arcadiandream.event;

import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.reimaden.arcadiandream.util.IEntityDataSaver;
import net.reimaden.arcadiandream.util.StaminaHelper;

import java.util.List;

public class PlayerEventInit implements S2CPlayChannelEvents.Register {

    @Override
    public void onChannelRegister(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server, List<Identifier> channels) {
            IEntityDataSaver player = (IEntityDataSaver) handler.player;
            StaminaHelper.changeStamina(player, -1);
    }
}




