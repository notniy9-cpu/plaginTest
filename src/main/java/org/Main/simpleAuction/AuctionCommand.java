package org.Main.simpleAuction;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
    private final SimpleAuction plugin;

    public AuctionCommand(SimpleAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0) {
            new AuctionGUI(plugin).open(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell") && args.length == 2) {
            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cЦена должна быть числом!");
                return true;
            }

            double min = plugin.getConfig().getDouble("settings.min-price");
            double max = plugin.getConfig().getDouble("settings.max-price");

            if (price < min) {
                p.sendMessage(plugin.getConfig().getString("messages.min-price-error")
                        .replace("%min%", String.valueOf(min))
                        .replace("&", "§"));
                return true;
            }

            if (price > max) {
                p.sendMessage(plugin.getConfig().getString("messages.max-price-error")
                        .replace("%max%", String.valueOf(max))
                        .replace("&", "§"));
                return true;
            }

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                p.sendMessage("§cВы ничего не держите в руке!");
                return true;
            }

            if (plugin.getAuctionManager().addItem(item, p.getUniqueId(), p.getName(), price)) {
                p.getInventory().setItemInMainHand(null);
                p.sendMessage(plugin.getConfig().getString("messages.sold").replace("&", "§"));
            } else {
                p.sendMessage(plugin.getConfig().getString("messages.sell-limit").replace("&", "§"));
            }
            return true;
        }

        p.sendMessage("§cИспользование: /ah - открыть аукцион, /ah sell [цена] - выставить предмет");
        return false;
    }
}