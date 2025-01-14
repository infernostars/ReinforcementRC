package codes.zucker.ReinforcementRC;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import codes.zucker.ReinforcementRC.util.LangYaml;
import codes.zucker.ReinforcementRC.util.Utils;

public class Commands {
    
    protected static List<Player> rvToggle = new ArrayList<>();
    protected static List<Player> reToggle = new ArrayList<>();
    protected static List<Player> rAdminModeToggle = new ArrayList<>();
    
    public static boolean rvCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;

        if (rvToggle.contains(player)) {
            rvToggle.remove(player);
            Utils.sendMessage(player, LangYaml.getString("reinforcement_visibility_off"));
        }
        else {
            rvToggle.add(player);
            Utils.sendMessage(player, LangYaml.getString("reinforcement_visibility_on"));
        }
        return true;
    }

    private Commands() {

    }

    public static boolean reCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;

        if (reToggle.contains(player)) {
            reToggle.remove(player);
            Utils.sendMessage(player, LangYaml.getString("reinforcement_mode_off"));
        }
        else {
            reToggle.add(player);
            Utils.sendMessage(player, LangYaml.getString("reinforcement_mode_on"));
        }
        return true;
    }


    public static boolean rAdminCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;

        if (rAdminModeToggle.contains(player)) {
            rAdminModeToggle.remove(player);
            Utils.sendMessage(player, LangYaml.getString("admin_mode_off"));
        }
        else {
            rAdminModeToggle.add(player);
            Utils.sendMessage(player, LangYaml.getString("admin_mode_on"));
        }
        return true;
    }
}