package dev.dexuby.structuregenerationrate.config;

import org.bukkit.World;

import java.util.Map;

public class Configuration {

    private final boolean cacheDefaultValues;
    private final Map<String, WorldConfiguration> worldConfigurations;

    public Configuration(final boolean cacheDefaultValues,
                         final Map<String, WorldConfiguration> worldConfigurations) {

        this.cacheDefaultValues = cacheDefaultValues;
        this.worldConfigurations = worldConfigurations;

    }

    public boolean doCacheDefaultValues() {

        return this.cacheDefaultValues;

    }

    public WorldConfiguration getWorldConfiguration(final World world) {

        return this.worldConfigurations.get(world.getName());

    }

    public Map<String, WorldConfiguration> getWorldConfigurations() {

        return this.worldConfigurations;

    }

}
