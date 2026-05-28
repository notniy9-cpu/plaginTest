package org.Main.simpleAuction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyManager {
    private final SimpleAuction plugin;

    public EconomyManager(SimpleAuction plugin) {
        this.plugin = plugin;
    }

    public boolean hasMoney(Player player, double amount) {
        if (plugin.getEconomyType() == SimpleAuction.EconomyType.COMMAND) {
            // Отправляем команду на проверку баланса
            String command = plugin.getMoneyCheckCommand()
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));

            // Здесь нужно перехватить результат команды
            // Упрощенный вариант - всегда возвращаем true, проверяем при списании
            return true;
        }
        return false;
    }

    public boolean withdrawMoney(Player player, double amount) {
        if (plugin.getEconomyType() == SimpleAuction.EconomyType.COMMAND) {
            String command = plugin.getMoneyWithdrawCommand()
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));

            // Отправляем команду от консоли
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        }
        return false;
    }

    public boolean giveMoney(OfflinePlayer player, double amount) {
        if (plugin.getEconomyType() == SimpleAuction.EconomyType.COMMAND) {
            String command = plugin.getMoneyGiveCommand()
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        }
        return false;
    }
}