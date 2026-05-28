package org.Main.simpleAuction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AuctionGUI implements Listener {
    private final SimpleAuction plugin;
    private final EconomyManager economyManager;

    public AuctionGUI(SimpleAuction plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Аукцион");
        List<AuctionItem> items = new ArrayList<>(plugin.getAuctionManager().getItems());
        items.sort(Comparator.comparingLong(AuctionItem::getCreatedTime).reversed());

        for (AuctionItem item : items) {
            inv.addItem(item.getItem());
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§6Аукцион")) {
            return;
        }

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Находим предмет в лотах
        AuctionItem target = null;
        for (AuctionItem item : plugin.getAuctionManager().getItems()) {
            if (item.getItem().isSimilar(clicked)) {
                target = item;
                break;
            }
        }

        if (target == null) {
            p.sendMessage(plugin.getConfig().getString("messages.not-found", "&cПредмет не найден!").replace("&", "§"));
            p.closeInventory();
            return;
        }

        // Проверка места в инвентаре
        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(plugin.getConfig().getString("messages.no-space", "&cНет места в инвентаре!").replace("&", "§"));
            return;
        }

        // Проверка и списание денег через команды
        if (plugin.getEconomyType() == SimpleAuction.EconomyType.COMMAND) {
            // Отправляем команду на снятие денег
            String withdrawCmd = plugin.getMoneyWithdrawCommand()
                    .replace("%player%", p.getName())
                    .replace("%amount%", String.valueOf(target.getPrice()));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), withdrawCmd);

            // Выдаем предмет
            p.getInventory().addItem(target.getItem().clone());

            // Отправляем деньги продавцу
            OfflinePlayer seller = Bukkit.getOfflinePlayer(target.getSeller());
            String giveCmd = plugin.getMoneyGiveCommand()
                    .replace("%player%", seller.getName())
                    .replace("%amount%", String.valueOf(target.getPrice()));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), giveCmd);

            // Удаляем из аукциона
            plugin.getAuctionManager().removeItem(target);

            // Сообщения
            p.sendMessage(plugin.getConfig().getString("messages.bought", "&aВы купили предмет за %price%$")
                    .replace("%price%", String.valueOf(target.getPrice()))
                    .replace("&", "§"));

            if (seller.isOnline()) {
                Player onlineSeller = seller.getPlayer();
                if (onlineSeller != null) {
                    onlineSeller.sendMessage(plugin.getConfig().getString("messages.earned", "&aВам зачислено %price%$")
                            .replace("%player%", p.getName())
                            .replace("%price%", String.valueOf(target.getPrice()))
                            .replace("&", "§"));
                }
            }

            // Обновляем GUI
            open(p);
        } else {
            p.sendMessage(plugin.getConfig().getString("messages.economy-error", "&cЭкономика не настроена!").replace("&", "§"));
        }
    }
}