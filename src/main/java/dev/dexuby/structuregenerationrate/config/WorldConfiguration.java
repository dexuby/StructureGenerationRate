package dev.dexuby.structuregenerationrate.config;

import java.util.Map;

public class WorldConfiguration {

    private final Map<String, StructureConfiguration> structureConfigurations;

    public WorldConfiguration(final Map<String, StructureConfiguration> structureConfigurations) {

        this.structureConfigurations = structureConfigurations;

    }

    public StructureConfiguration getStructureConfiguration(final String name) {

        return this.structureConfigurations.get(name);

    }

    public Map<String, StructureConfiguration> getStructureConfigurations() {

        return this.structureConfigurations;

    }

}
