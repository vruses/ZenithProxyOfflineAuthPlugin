package com.vurses.offlineauth.module;

import com.zenith.feature.api.sessionserver.SessionServerApi;
import com.zenith.module.api.Module;
import com.zenith.network.client.ClientSession;
import com.zenith.network.codec.PacketHandlerCodec;
import com.zenith.network.codec.PacketHandlerStateCodec;
import com.zenith.util.config.Config;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundHelloPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundHelloPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundKeyPacket;

import javax.crypto.SecretKey;
import java.util.UUID;

import static com.zenith.Globals.CONFIG;
import static com.zenith.Globals.EXECUTOR;

public class OfflineAuthModule extends Module {

    @Override
    public boolean enabledSetting() {
        return true;
    }

    @Override
    public PacketHandlerCodec registerClientPacketHandlerCodec() {
        return PacketHandlerCodec.clientBuilder()
                .priority(100)
                .id("offline_auth_codec")
                .state(ProtocolState.LOGIN, PacketHandlerStateCodec.clientBuilder()
                        .inbound(ClientboundHelloPacket.class, this::handleClientboundHello)
                        .outbound(ServerboundHelloPacket.class, this::handleServerboundHello)
                        .build())
                .build();
    }

    private ClientboundHelloPacket handleClientboundHello(ClientboundHelloPacket packet, ClientSession session) {
        final GameProfile profile = session.getProfile();
        final String accessToken = session.getAccessToken();

        if (CONFIG.authentication.accountType != Config.Authentication.AccountType.OFFLINE) {
            if (profile == null || accessToken == null) {
                session.disconnect("No Profile or Access Token provided.");
                return null;
            }
        }

        final SecretKey key = SessionServerApi.INSTANCE.generateClientKey();
        if (key == null) {
            session.disconnect("Failed to generate secret key.");
            return null;
        }

        if (packet.isShouldAuthenticate()) {
            EXECUTOR.execute(() -> {
                if (CONFIG.authentication.accountType != Config.Authentication.AccountType.OFFLINE) {
                    try {
                        final String sharedSecret = SessionServerApi.INSTANCE.getSharedSecret(packet.getServerId(), packet.getPublicKey(), key);
                        SessionServerApi.INSTANCE.joinServer(profile.getId(), accessToken, sharedSecret);
                    } catch (Exception e) {
                        session.disconnect("Login failed: Authentication service unavailable.", e);
                        return;
                    }
                }
                session.send(new ServerboundKeyPacket(packet.getPublicKey(), key, packet.getChallenge()),
                        (f) -> session.enableEncryption(key));
            });
        } else {
            session.send(new ServerboundKeyPacket(packet.getPublicKey(), key, packet.getChallenge()));
            session.enableEncryption(key);
        }
        return null;
    }

    private ServerboundHelloPacket handleServerboundHello(ServerboundHelloPacket packet, ClientSession session) {
        if (CONFIG.authentication.accountType == Config.Authentication.AccountType.OFFLINE) {
            UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + CONFIG.authentication.username).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return new ServerboundHelloPacket(packet.getUsername(), offlineUUID);
        }
        return packet;
    }
}
