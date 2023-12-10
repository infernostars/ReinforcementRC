package codes.zucker.ReinforcementRC;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import codes.zucker.ReinforcementRC.entity.Hologram;
import codes.zucker.ReinforcementRC.util.CommandHelper;
import codes.zucker.ReinforcementRC.util.ConfigurationYaml;
import codes.zucker.ReinforcementRC.util.DataYaml;
import codes.zucker.ReinforcementRC.util.LOG;
import codes.zucker.ReinforcementRC.util.LangYaml;

public class ReinforcementPlugin extends JavaPlugin
{

    @Override
    public void onEnable() {
        ConfigurationYaml.loadConfigurationFile();

        ConfigurationYaml.getList("reinforcement_blocks").forEach(i -> {
            List<?> entry = ((ArrayList<?>)i);
            Material material = Material.getMaterial((String)entry.get(0));
            int breaks = (int)entry.get(1);
            int max = (int)entry.get(2);
            double explosiveMultiplier = 1 - (double)entry.get(3);
            ReinforceMaterial reinforceMaterial = new ReinforceMaterial(material, breaks, max, explosiveMultiplier);
            ReinforceMaterial.entries.add(reinforceMaterial);
        });

        LangYaml.loadLang();
        DataYaml.loadDataFile();

        getServer().getPluginManager().registerEvents(new Events(), this);

        CommandHelper.registerCommand("rv", "rvCommand");
        CommandHelper.registerCommand("re", "reCommand");
        CommandHelper.registerCommand("radmin", "rAdminCommand");
    }
    
    @Override
    public void onDisable() {
        DataYaml.saveDataFile();
        Hologram.getHolograms().forEach(Hologram::destroyHologram);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (Map.Entry<String, Method> map : CommandHelper.commands.entrySet()) {
            if (cmd.getName().equalsIgnoreCase(map.getKey())) {
                try {
                    // Here's some dodgy reflection stuff...
                    Method method = map.getValue();
                    method.invoke(null, sender, cmd, label, args);
                    return true;
                } catch (Exception e) {
                    LOG.severe("{}", e);
                }
                return true;
            }
        }

        return false;
    }
}
