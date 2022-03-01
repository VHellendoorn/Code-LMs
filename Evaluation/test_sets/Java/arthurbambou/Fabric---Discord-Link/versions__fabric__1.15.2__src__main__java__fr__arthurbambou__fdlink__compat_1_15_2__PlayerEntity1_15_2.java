package fr.arthurbambou.fdlink.compat_1_15_2;

import fr.arthurbambou.fdlink.api.minecraft.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class PlayerEntity1_15_2 implements PlayerEntity {

    private final ServerPlayerEntity playerEntity;

    public PlayerEntity1_15_2(ServerPlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    @Override
    public String getPlayerName() {
        return this.playerEntity.getName().getString();
    }

    @Override
    public UUID getUUID() {
        return this.playerEntity.getUuid();
    }
}
