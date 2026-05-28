package org.Main.simpleAuction;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class AuctionItem {
    private final ItemStack item;
    private final UUID seller;
    private final String sellerName;
    private final double price;
    private final long createdTime;

    public AuctionItem(ItemStack item, UUID seller, String sellerName, double price, long createdTime) {
        this.item = item;
        this.seller = seller;
        this.sellerName = sellerName;
        this.price = price;
        this.createdTime = createdTime;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public UUID getSeller() {
        return seller;
    }

    public String getSellerName() {
        return sellerName;
    }

    public double getPrice() {
        return price;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}