package org.Main.simpleAuction;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AuctionManager {
    private final SimpleAuction plugin;
    private final Queue<AuctionItem> items = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Integer> playerItemCount = new HashMap<>();
    private File dataFile;
    private YamlConfiguration data;

    public AuctionManager(SimpleAuction plugin) {
        this.plugin = plugin;
        loadData();
    }

    public boolean addItem(ItemStack item, UUID seller, String sellerName, double price) {
        int max = plugin.getConfig().getInt("settings.max-items-per-player");
        if (playerItemCount.getOrDefault(seller, 0) >= max) {
            return false;
        }

        // Создаем копию предмета с мета-данными
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add("§7Продавец: §e" + sellerName);
        lore.add("§7Цена: §6" + price + "$");
        meta.setLore(lore);
        clone.setItemMeta(meta);

        AuctionItem auctionItem = new AuctionItem(clone, seller, sellerName, price, System.currentTimeMillis());
        items.add(auctionItem);
        playerItemCount.put(seller, playerItemCount.getOrDefault(seller, 0) + 1);
        saveAll();
        return true;
    }

    public void removeItem(AuctionItem item) {
        items.remove(item);
        playerItemCount.put(item.getSeller(), playerItemCount.getOrDefault(item.getSeller(), 1) - 1);
        saveAll();
    }

    public Collection<AuctionItem> getItems() {
        return new ArrayList<>(items);
    }

    public void checkExpiredItems() {
        long expireTime = plugin.getConfig().getLong("settings.expire-time-hours") * 3600000L;
        List<AuctionItem> toRemove = new ArrayList<>();

        for (AuctionItem item : items) {
            if (System.currentTimeMillis() - item.getCreatedTime() > expireTime) {
                toRemove.add(item);
            }
        }

        for (AuctionItem item : toRemove) {
            items.remove(item);
            playerItemCount.put(item.getSeller(), playerItemCount.getOrDefault(item.getSeller(), 1) - 1);
        }

        if (!toRemove.isEmpty()) {
            saveAll();
            if (plugin.getConfig().getString("messages.expired-removed") != null) {
                Bukkit.broadcastMessage(plugin.getConfig().getString("messages.expired-removed").replace("&", "§"));
            }
        }
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(dataFile);
        items.clear();
        playerItemCount.clear();

        if (data.contains("auction-items")) {
            for (String key : data.getConfigurationSection("auction-items").getKeys(false)) {
                try {
                    ItemStack item = data.getItemStack("auction-items." + key + ".item");
                    UUID seller = UUID.fromString(data.getString("auction-items." + key + ".seller"));
                    String name = data.getString("auction-items." + key + ".sellerName");
                    double price = data.getDouble("auction-items." + key + ".price");
                    long time = data.getLong("auction-items." + key + ".time");

                    if (item != null && seller != null && name != null) {
                        items.add(new AuctionItem(item, seller, name, price, time));
                        playerItemCount.put(seller, playerItemCount.getOrDefault(seller, 0) + 1);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка загрузки предмета " + key + ": " + e.getMessage());
                }
            }
        }
    }

    public void saveAll() {
        data = new YamlConfiguration();
        int i = 0;
        for (AuctionItem item : items) {
            data.set("auction-items." + i + ".item", item.getItem());
            data.set("auction-items." + i + ".seller", item.getSeller().toString());
            data.set("auction-items." + i + ".sellerName", item.getSellerName());
            data.set("auction-items." + i + ".price", item.getPrice());
            data.set("auction-items." + i + ".time", item.getCreatedTime());
            i++;
        }

        try {
            data.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}