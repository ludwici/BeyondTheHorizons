package dev.ludwici.beyondthehorizons.neoforge.thermal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class ThermalZoneManager {

    private static final Map<ServerLevel, ZoneIndex> ZONES = new WeakHashMap<>();

    private ThermalZoneManager() {
    }

    @Nullable
    public static Short getTemperature(ServerLevel level, BlockPos pos) {
        ZoneIndex index = ZONES.get(level);
        if (index == null) {
            return null;
        }

        Claim claim = index.positions.get(pos.asLong());
        return claim == null ? null : claim.temperature();
    }

    public static void update(ServerLevel level, BlockPos owner, Set<BlockPos> positions, short temperature) {
        ZoneIndex index = ZONES.computeIfAbsent(level, unused -> new ZoneIndex());
        long ownerKey = owner.asLong();
        clear(index, ownerKey);
        Set<Long> claims = new HashSet<>(positions.size());

        for (BlockPos pos : positions) {
            long key = pos.asLong();
            Claim existing = index.positions.get(key);
            if (existing != null && existing.owner() < ownerKey) {
                continue;
            }

            index.positions.put(key, new Claim(ownerKey, temperature));
            claims.add(key);
        }

        index.byOwner.put(ownerKey, claims);
    }

    public static void clear(ServerLevel level, BlockPos owner) {
        ZoneIndex index = ZONES.get(level);
        if (index != null) {
            clear(index, owner.asLong());
            if (index.positions.isEmpty()) {
                ZONES.remove(level);
            }
        }
    }

    private static void clear(ZoneIndex index, long owner) {
        Set<Long> old = index.byOwner.remove(owner);
        if (old == null) {
            return;
        }

        for (long key : old) {
            Claim current = index.positions.get(key);
            if (current != null && current.owner() == owner) {
                index.positions.remove(key);
            }
        }
    }

    private record Claim(long owner, short temperature) {
    }

    private static final class ZoneIndex {
        private final Map<Long, Claim> positions = new HashMap<>();
        private final Map<Long, Set<Long>> byOwner = new HashMap<>();
    }
}
