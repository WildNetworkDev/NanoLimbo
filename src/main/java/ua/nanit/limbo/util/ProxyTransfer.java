/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ua.nanit.limbo.LimboConstants;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.play.PacketPluginMessage;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@UtilityClass
public class ProxyTransfer {

    public static byte[] buildConnectPayload(@NonNull String serverName) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        return bytes.toByteArray();
    }

    public static void sendConnect(@NonNull ClientConnection conn, @NonNull String serverName) throws IOException {
        if (!conn.isConnected() || conn.getState() != State.PLAY) {
            return;
        }

        Version version = conn.getClientVersion();
        if (version == null) {
            return;
        }

        PacketPluginMessage message = new PacketPluginMessage();
        message.setChannel(version.moreOrEqual(Version.V1_13)
                ? LimboConstants.BUNGEECORD_CHANNEL
                : LimboConstants.BUNGEECORD_CHANNEL_LEGACY);
        message.setData(buildConnectPayload(serverName));
        conn.sendPacket(message);
    }

}
