package org.Main.simpleAuction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleAuction extends JavaPlugin {
    private static SimpleAuction instance;
    private AuctionManager auctionManager;
    private EconomyType economyType = EconomyType.COMMAND;
    private String moneyWithdrawCommand = "money take %player% %amount%";
    private String moneyGiveCommand = "money give %player% %amount%";
    private String moneyCheckCommand = "money balance %player%";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Загружаем настройки экономики из конфига
        loadEconomySettings();

        auctionManager = new AuctionManager(this);

        getCommand("ah").setExecutor(new AuctionCommand(this));
        getServer().getPluginManager().registerEvents(new AuctionGUI(this), this);

        // Запускаем проверку просроченных предметов каждый час
        Bukkit.getScheduler().runTaskTimer(this, () -> auctionManager.checkExpiredItems(), 20L * 60 * 60, 20L * 60 * 60);

        getLogger().info("SimpleAuction enabled - Тип экономики: " + economyType.name());
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.saveAll();
        }
        getLogger().info("SimpleAuction disabled");
    }

    private void loadEconomySettings() {
        String type = getConfig().getString("economy.type", "COMMAND");
        try {
            economyType = EconomyType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            economyType = EconomyType.COMMAND;
        }

        moneyWithdrawCommand = getConfig().getString("economy.withdraw-command", "money take %player% %amount%");
        moneyGiveCommand = getConfig().getString("economy.give-command", "money give %player% %amount%");
        moneyCheckCommand = getConfig().getString("economy.check-command", "money balance %player%");
    }

    public static SimpleAuction getInstance() {
        return instance;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public EconomyType getEconomyType() {
        return economyType;
    }

    public String getMoneyWithdrawCommand() {
        return moneyWithdrawCommand;
    }

    public String getMoneyGiveCommand() {
        return moneyGiveCommand;
    }

    public String getMoneyCheckCommand() {
        return moneyCheckCommand;
    }

    public enum EconomyType {
        COMMAND, VAULT, NONE
    }
}