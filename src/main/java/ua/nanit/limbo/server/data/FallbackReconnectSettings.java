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

package ua.nanit.limbo.server.data;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
public final class FallbackReconnectSettings {

    private final boolean enabled;
    private final int intervalSeconds;
    private final long attemptTimeoutMs;
    private final List<String> servers;

    public FallbackReconnectSettings(boolean enabled,
                                     int intervalSeconds,
                                     long attemptTimeoutMs,
                                     @NonNull List<String> servers) {
        this.enabled = enabled;
        this.intervalSeconds = intervalSeconds;
        this.attemptTimeoutMs = attemptTimeoutMs;
        this.servers = List.copyOf(servers);
    }

    public static FallbackReconnectSettings disabled() {
        return new FallbackReconnectSettings(false, 30, 2500L, List.of());
    }

}
