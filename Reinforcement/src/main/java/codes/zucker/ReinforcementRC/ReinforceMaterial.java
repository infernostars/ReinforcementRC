package codes.zucker.ReinforcementRC;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class ReinforceMaterial {
    protected static List<ReinforceMaterial> entries = new ArrayList<>();

    private Material material;
    private int breaksPerReinforce;
    private int maxAllowedBreaks;
    private double explosiveMultiplier;

    public ReinforceMaterial(Material material, int breaksPerReinforce, int maxAllowedBreaks, double explosiveMultiplier) {
        this.material = material;
        this.breaksPerReinforce = breaksPerReinforce;
        this.maxAllowedBreaks = maxAllowedBreaks;
        this.explosiveMultiplier = explosiveMultiplier;
    }

    public Material getMaterial() {
        return material;
    }

    public int getBreaksPerReinforce() {
        return breaksPerReinforce;
    }

    public int getMaxAllowedBreaks() {
        return maxAllowedBreaks;
    }

    public static ReinforceMaterial getFromMaterial(Material material) {
        for(ReinforceMaterial reinforceMaterial : entries) {
            if (reinforceMaterial.material.name().equals(material.name()))
                return reinforceMaterial;
        }
        return null;
    }

    public static ReinforceMaterial getFromMaterial(String material) {
        Material m = Material.getMaterial(material);
        return getFromMaterial(m);
    }

    public double getExplosiveMultiplier() {return explosiveMultiplier;}
}
