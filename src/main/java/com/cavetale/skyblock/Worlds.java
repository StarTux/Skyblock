package com.cavetale.skyblock;

import com.cavetale.core.util.Json;
import com.winthier.creative.BuildWorld;
import com.winthier.creative.file.Files;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.SpawnCategory;
import static com.cavetale.skyblock.SkyblockPlugin.plugin;

public final class Worlds {
    protected final Map<String, LoadedWorld> loadedWorlds = new HashMap<>();

    protected void enable() {
        Bukkit.getScheduler().runTaskTimer(plugin(), this::tick, 1L, 1L);
        for (World world : Bukkit.getWorlds()) {
            final UUID uuid;
            try {
                uuid = UUID.fromString(world.getName());
            } catch (IllegalArgumentException iae) {
                continue;
            }
            load(uuid, world);
        }
    }

    protected void disable() {
        for (LoadedWorld loadedWorld : List.copyOf(loadedWorlds.values())) {
            unload(loadedWorld);
        }
    }

    protected LoadedWorld get(UUID uuid) {
        return loadedWorlds.get(uuid.toString());
    }

    protected LoadedWorld in(World world) {
        return loadedWorlds.get(world.getName());
    }

    protected LoadedWorld load(UUID uuid) {
        WorldCreator creator = WorldCreator.name(uuid.toString());
        creator.generator("VoidGenerator");
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);
        creator.seed(0L);
        creator.type(WorldType.NORMAL);
        creator.keepSpawnLoaded(TriState.FALSE);
        return load(uuid, creator.createWorld());
    }

    protected LoadedWorld load(UUID uuid, World world) {
        LoadedWorld loadedWorld = new LoadedWorld(world, uuid);
        loadedWorld.load();
        applyWorld(loadedWorld, world);
        loadedWorlds.put(uuid.toString(), loadedWorld);
        return loadedWorld;
    }

    protected LoadedWorld create(UUID uuid, World world, SkyblockDifficulty difficulty) {
        LoadedWorld loadedWorld = new LoadedWorld(world, uuid);
        loadedWorld.tag = new WorldTag();
        loadedWorld.tag.difficulty = difficulty;
        applyWorld(loadedWorld, world);
        loadedWorlds.put(uuid.toString(), loadedWorld);
        return loadedWorld;
    }

    protected boolean unload(LoadedWorld loadedWorld) {
        loadedWorld.save();
        if (!loadedWorld.world.getPlayers().isEmpty()) return false;
        final boolean save = true;
        if (!Bukkit.unloadWorld(loadedWorld.world, save)) return false;
        loadedWorlds.remove(loadedWorld.uuid.toString());
        return true;
    }

    private void applyWorld(LoadedWorld loadedWorld, World world) {
        final SkyblockDifficulty difficulty = loadedWorld.tag.difficulty != null
            ? loadedWorld.tag.difficulty
            : SkyblockDifficulty.NORMAL;
        world.setDifficulty(difficulty.difficulty);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, true);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DISABLE_RAIDS, false); // ?
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true);
        world.setGameRule(GameRule.DO_FIRE_TICK, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_LIMITED_CRAFTING, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, true);
        world.setGameRule(GameRule.DO_TILE_DROPS, true);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, true); // ?
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true); // ?
        world.setGameRule(GameRule.DROWNING_DAMAGE, true);
        world.setGameRule(GameRule.FALL_DAMAGE, true);
        world.setGameRule(GameRule.FIRE_DAMAGE, true);
        world.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
        world.setGameRule(GameRule.FREEZE_DAMAGE, true);
        world.setGameRule(GameRule.KEEP_INVENTORY, difficulty.keepInventory);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);
        world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 1);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 10);
        world.setGameRule(GameRule.MOB_GRIEFING, true);
        world.setGameRule(GameRule.NATURAL_REGENERATION, difficulty.naturalRegeneration);
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 101);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.UNIVERSAL_ANGER, false);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
        world.getWorldBorder().setSize(8192.0);
        world.getWorldBorder().setDamageAmount(20.0);
        world.getWorldBorder().setDamageBuffer(0.0);
        world.getWorldBorder().setWarningDistance(4);
        world.getWorldBorder().setWarningTime(5);
        // Defaults, I hope
        world.setSpawnFlags(true, true);
        world.setSpawnLimit(SpawnCategory.MONSTER, 70);
        world.setSpawnLimit(SpawnCategory.ANIMAL, 10);
        world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 5);
        world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, 20);
        world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, 5);
        world.setSpawnLimit(SpawnCategory.AMBIENT, 15);
        world.setSpawnLimit(SpawnCategory.AXOLOTL, 5);
        world.setTicksPerSpawns(SpawnCategory.MONSTER, 1);
        world.setTicksPerSpawns(SpawnCategory.ANIMAL, 400);
        world.setTicksPerSpawns(SpawnCategory.WATER_ANIMAL, 1);
        world.setTicksPerSpawns(SpawnCategory.WATER_AMBIENT, 1);
        world.setTicksPerSpawns(SpawnCategory.WATER_UNDERGROUND_CREATURE, 1);
        world.setTicksPerSpawns(SpawnCategory.AMBIENT, 1);
        world.setTicksPerSpawns(SpawnCategory.AXOLOTL, 1);
    }

    public LoadedWorld create(UUID uuid, SkyblockDifficulty difficulty) {
        BuildWorld buildWorld = BuildWorld.findWithPath("skyblock");
        if (buildWorld == null) throw new IllegalStateException("BuildWorld not found: skyblock");
        World world = buildWorld.makeLocalCopy(uuid.toString());
        LoadedWorld loadedWorld = create(uuid, world, difficulty);
        loadedWorld.tag.owner = uuid;
        loadedWorld.tag.creationTime = System.currentTimeMillis();
        loadedWorld.save();
        return loadedWorld;
    }

    public boolean delete(UUID uuid) {
        if (loadedWorlds.containsKey(uuid.toString())) return false;
        if (Bukkit.getWorld(uuid.toString()) != null) return false;
        File file = new File(Bukkit.getWorldContainer(), uuid.toString());
        if (!file.exists()) return false;
        Files.deleteFileStructure(file);
        return true;
    }

    public World getLobbyWorld() {
        return Bukkit.getWorlds().get(0);
    }

    private void tick() {
        for (LoadedWorld loadedWorld : List.copyOf(loadedWorlds.values())) {
            if (loadedWorld.world.getPlayers().isEmpty()) {
                loadedWorld.emptyTicks += 1;
                if (loadedWorld.emptyTicks > 20 * 10) {
                    unload(loadedWorld);
                }
            } else {
                loadedWorld.emptyTicks = 0;
            }
            loadedWorld.tick();
        }
    }

    public WorldTag loadTag(UUID uuid) {
        LoadedWorld loadedWorld = loadedWorlds.get(uuid.toString());
        if (loadedWorld != null) return loadedWorld.tag;
        File folder = new File(Bukkit.getWorldContainer(), uuid.toString());
        if (!folder.isDirectory()) return null;
        File file = new File(folder, "skyblock.json");
        if (!file.exists()) return null;
        return Json.load(file, WorldTag.class, () -> null);
    }
}
