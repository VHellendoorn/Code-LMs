package fr.arthurbambou.fdlink.compat_1_7_10;

import fr.arthurbambou.fdlink.api.minecraft.Message;
import fr.arthurbambou.fdlink.api.minecraft.MessagePacket;
import fr.arthurbambou.fdlink.api.minecraft.MinecraftServer;
import fr.arthurbambou.fdlink.api.minecraft.PlayerEntity;
import fr.arthurbambou.fdlink.api.minecraft.style.TextColor;
import net.minecraft.class_1981;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinecraftServer1_7_10 implements MinecraftServer {

    private final net.minecraft.server.MinecraftServer minecraftServer;

    public MinecraftServer1_7_10(net.minecraft.server.MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    @Override
    public String getMotd() {
        return this.minecraftServer.getServerMotd();
    }

    @Override
    public int getPlayerCount() {
        return this.minecraftServer.getCurrentPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return this.minecraftServer.getMaxPlayerCount();
    }

    @Override
    public List<PlayerEntity> getPlayers() {
        List<PlayerEntity> list = new ArrayList<>();
        for (Object playerEntity : this.minecraftServer.getPlayerManager().players) {
            list.add(new PlayerEntity1_7_10((ServerPlayerEntity) playerEntity));
        }
        return list;
    }

    @Override
    public PlayerEntity getPlayerFromUsername(String username) {
        return new PlayerEntity1_7_10(this.minecraftServer.getPlayerManager().getPlayer(username));
    }

    @Override
    public String getUsernameFromUUID(UUID uuid) {
        String username = "";
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (playerEntity.getUUID().equals(uuid)) {
                username = playerEntity.getPlayerName();
                break;
            }
        }
        return username;
    }

    @Override
    public void sendMessageToAll(MessagePacket messagePacket) {
        Message message = messagePacket.getMessage();
        Text text = null;
        if (message.getType() == Message.MessageObjectType.STRING) {
            text = new LiteralText(message.getMessage());
        } else {
            if (message.getTextType() == Message.TextType.LITERAL) {
                text = new LiteralText(message.getMessage());
            } else if (message.getTextType() == Message.TextType.TRANSLATABLE) {
                text = new TranslatableText(message.getKey(), message.getArgs());
            }
        }
        Style vanillaStyle = new Style();
        fr.arthurbambou.fdlink.api.minecraft.style.Style compatStyle = message.getStyle();
        vanillaStyle = vanillaStyle
                .setBold(compatStyle.isBold())
                .setFormatting(Formatting.byName(TextColor.toFormatting(compatStyle.getColor()).getName()))
                .setItalic(compatStyle.isItalic())
                .setUnderline(compatStyle.isUnderlined())
                .setObfuscated(compatStyle.isObfuscated())
                .setStrikethrough(compatStyle.isStrikethrough());
        if (compatStyle.getClickEvent() != null) {
            vanillaStyle.setClickEvent(new ClickEvent(class_1981.method_7464(compatStyle.getClickEvent().getAction().getName()),
                    compatStyle.getClickEvent().getValue()));
        }
        this.minecraftServer.getPlayerManager().sendToAll(text);
    }

    @Override
    public String getIp() {
        return this.minecraftServer.getServerIp();
    }

    @Override
    public File getIcon() {
        return this.minecraftServer.getFile("icon.png");
    }
}
