package com.jdwardle.passivemobs;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PlayerManagerStore {
    private static final HashMap<String, PlayerManager> managers = new HashMap<>();

    private PlayerManagerStore() {
    }

    public static void remove(@NotNull UUID playerId) {
        managers.remove(playerId.toString());
    }

    public static void computeIfAbsent(@NotNull UUID playerId, @NotNull PlayerManager manager) {
        managers.put(playerId.toString(), manager);
    }

    public static void replace(@NotNull UUID playerId, @NotNull PlayerManager manager) {
        managers.replace(playerId.toString(), manager);
    }

    public static Optional<PlayerManager> get(@NotNull UUID playerId) {
        return Optional.ofNullable(managers.get(playerId.toString()));
    }
}
