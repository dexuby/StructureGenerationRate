package dev.dexuby.structuregenerationrate.config;

import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class StructureConfiguration {

    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;
    private final int salt;

    public StructureConfiguration(final int spacing,
                                  final int separation,
                                  final RandomSpreadType spreadType,
                                  final int salt) {

        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
        this.salt = salt;

    }

    public int getSpacing() {

        return this.spacing;

    }

    public int getSeparation() {

        return this.separation;

    }

    public RandomSpreadType getSpreadType() {

        return this.spreadType;

    }

    public int getSalt() {

        return this.salt;

    }

    @Override
    public String toString() {

        return String.format("(spacing: %d, separation: %d, spread-type: %s, salt: %d)", this.spacing, this.separation, this.spreadType.name(), this.salt);

    }

    public static StructureConfiguration fromRandomSpreadStructurePlacement(final RandomSpreadStructurePlacement placement) {

        return new StructureConfiguration(placement.spacing(), placement.separation(), placement.spreadType(), placement.salt());

    }

}
