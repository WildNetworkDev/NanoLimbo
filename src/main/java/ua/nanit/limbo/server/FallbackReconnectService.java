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

package ua.nanit.limbo.server;

import io.netty.channel.EventLoopGroup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.server.data.FallbackReconnectSettings;
import ua.nanit.limbo.util.ProxyTransfer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class FallbackReconnectService {

    private final LimboServer server;
    private ScheduledFuture<?> tickTask;

    public void start(@NonNull EventLoopGroup workerGroup) {
        tickTask = workerGroup.scheduleAtFixedRate(this::tick, 1L, 1L, TimeUnit.SECONDS);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel(true);
        }
    }

    private void tick() {
        FallbackReconnectSettings settings = server.getConfig().getFallbackReconnect();
        if (!settings.isEnabled()) {
            return;
        }

        for (ClientConnection connection : server.getConnections().getAllConnections()) {
            if (connection.getState() != State.PLAY) {
                continue;
            }
            if (connection.isFallbackReconnectDue()) {
                startCycle(connection);
            }
        }
    }

    private void startCycle(@NonNull ClientConnection connection) {
        connection.setFallbackReconnectInProgress(true);
        tryServer(connection, 0);
    }

    private void tryServer(@NonNull ClientConnection connection, int index) {
        FallbackReconnectSettings settings = server.getConfig().getFallbackReconnect();
        List<String> servers = settings.getServers();

        if (!connection.isConnected()) {
            connection.setFallbackReconnectInProgress(false);
            return;
        }

        if (index >= servers.size()) {
            connection.scheduleFallbackReconnectCycle(settings.getIntervalSeconds() * 1000L);
            connection.setFallbackReconnectInProgress(false);
            return;
        }

        String serverName = servers.get(index);
        try {
            if (Log.isDebug()) {
                Log.debug("Fallback reconnect: sending %s to %s", connection.getUsername(), serverName);
            }
            ProxyTransfer.sendConnect(connection, serverName);
        } catch (IOException exception) {
            Log.warning("Failed to send Connect to %s for %s", exception, serverName, connection.getUsername());
        }

        connection.getChannel().eventLoop().schedule(
                () -> tryServer(connection, index + 1),
                settings.getAttemptTimeoutMs(),
                TimeUnit.MILLISECONDS
        );
    }

}
