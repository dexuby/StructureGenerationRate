package dev.dexuby.structuregenerationrate;

import com.mojang.serialization.Lifecycle;
import dev.dexuby.structuregenerationrate.config.Configuration;
import dev.dexuby.structuregenerationrate.config.StructureConfiguration;
import dev.dexuby.structuregenerationrate.config.WorldConfiguration;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class StructureGenerationRate extends JavaPlugin implements CommandExecutor {

    private final Map<String, StructureConfiguration> cachedDefaultValues = new HashMap<>();
    private Configuration configuration;
    private boolean initial = true;

    @Override
    public void onEnable() {

        super.saveDefaultConfig();
        this.loadConfig();

        super.getCommand("sgr").setExecutor(this);
        this.applyConfiguration();

    }

    private void loadConfig() {

        final boolean cacheDefaultValues = getConfig().getBoolean("cache-default-values");
        final Map<String, WorldConfiguration> worldConfigurations = new HashMap<>();
        for (final String key : getConfig().getConfigurationSection("worlds").getKeys((false))) {
            final Map<String, StructureConfiguration> structureConfigurations = new HashMap<>();
            for (final String subKey : getConfig().getConfigurationSection("worlds." + key + ".structures").getKeys(false)) {
                final String path = "worlds." + key + ".structures." + subKey;
                final int spacing = getConfig().getInt(path + ".spacing", -1);
                final int separation = getConfig().getInt(path + ".separation", -1);
                final RandomSpreadType spreadType = RandomSpreadType.byName(getConfig().getString(path + ".spread-type", "linear"));
                final int salt = getConfig().getInt(path + ".salt", -1);
                structureConfigurations.put(subKey, new StructureConfiguration(spacing, separation, spreadType, salt));
            }
            worldConfigurations.put(key, new WorldConfiguration(structureConfigurations));
        }

        this.configuration = new Configuration(cacheDefaultValues, worldConfigurations);

    }

    private void applyConfiguration() {

        this.cachedDefaultValues.clear();

        for (final World world : this.getServer().getWorlds()) {
            if (!(world instanceof CraftWorld)) continue;
            final ChunkGenerator chunkGenerator = ((CraftWorld) world).getHandle().getChunkSource().getGenerator();
            final MappedRegistry<StructureSet> mappedRegistry = (MappedRegistry<StructureSet>) chunkGenerator.structureSets;
            if (initial) {
                try {
                    final Field frozenField = MappedRegistry.class.getDeclaredField("bL");
                    frozenField.setAccessible(true);
                    frozenField.setBoolean(mappedRegistry, false);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }

            for (final Map.Entry<ResourceKey<StructureSet>, StructureSet> entry : mappedRegistry.entrySet()) {
                final String name = chunkGenerator.structureSets.getKey(entry.getValue()).getPath();
                final StructurePlacement structurePlacement = entry.getValue().placement();
                if (!(structurePlacement instanceof RandomSpreadStructurePlacement)) continue;

                final RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) structurePlacement;
                if (this.configuration.doCacheDefaultValues() && this.initial) {
                    if (!this.cachedDefaultValues.containsKey(name))
                        this.cachedDefaultValues.put(name, StructureConfiguration.fromRandomSpreadStructurePlacement(placement));
                }

                final WorldConfiguration worldConfig = this.configuration.getWorldConfiguration(world);
                if (worldConfig == null) continue;

                final StructureConfiguration structureConfig = worldConfig.getStructureConfiguration(name);
                if (structureConfig == null) continue;

                final RandomSpreadStructurePlacement updatedPlacement = new RandomSpreadStructurePlacement(
                        structureConfig.getSpacing() == -1 ? placement.spacing() : structureConfig.getSpacing(),
                        structureConfig.getSeparation() == -1 ? placement.separation() : structureConfig.getSeparation(),
                        structureConfig.getSpreadType() == null ? placement.spreadType() : structureConfig.getSpreadType(),
                        structureConfig.getSalt() == -1 ? placement.salt() : structureConfig.getSalt(),
                        placement.locateOffset()
                );

                mappedRegistry.registerOrOverride(OptionalInt.empty(), entry.getKey(), new StructureSet(entry.getValue().structures(), updatedPlacement), Lifecycle.stable());
                super.getLogger().info(String.format(" [%s] Updated values of %s (spacing, separation, spread type, salt): %s", world.getName(), name, this.generateDifferenceString(placement, updatedPlacement)));
            }
        }

        this.initial = false;

    }

    private String generateDifferenceString(final RandomSpreadStructurePlacement before, final RandomSpreadStructurePlacement after) {

        return before.spacing() + " -> " + after.spacing() + ", " +
                before.separation() + " -> " + after.separation() + ", " +
                before.spreadType().name() + " -> " + after.spreadType().name() + ", " +
                before.salt() + " -> " + after.salt();

    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String cmdName, final String[] args) {

        if (args.length == 0) return false;

        final String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("reload")) {
            super.reloadConfig();
            this.loadConfig();
            this.applyConfiguration();
            return true;
        } else if (subCommand.equalsIgnoreCase("defaultvalue")) {
            if (args.length < 2) return false;
            final String name = args[1];
            if (this.cachedDefaultValues.containsKey(name)) {
                sender.sendMessage(this.cachedDefaultValues.get(name).toString());
            } else {
                sender.sendMessage("Invalid key.");
            }
            return true;
        }

        return false;

    }

}
