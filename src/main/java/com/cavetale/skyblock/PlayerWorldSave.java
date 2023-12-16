package com.cavetale.skyblock;

import com.cavetale.inventory.storage.InventoryStorage;
import com.cavetale.inventory.storage.ItemStorage;
import com.cavetale.inventory.storage.PlayerStatusStorage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerWorldSave implements Serializable {
    protected InventoryStorage inventory;
    protected InventoryStorage enderChest;
    protected ItemStorage cursor;
    protected PlayerStatusStorage status;

    public void store(Player player) {
        this.inventory = InventoryStorage.of(player.getInventory());
        this.enderChest = InventoryStorage.of(player.getEnderChest());
        this.status = PlayerStatusStorage.of(player);
        final ItemStack cursorItem = player.getOpenInventory().getCursor();
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            this.cursor = ItemStorage.of(cursorItem);
        }
        //
        inventory.clear(player.getInventory());
        enderChest.clear(player.getEnderChest());
        status.clear(player);
        player.getOpenInventory().setCursor(null);
    }

    public void restore(Player player) {
        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(inventory.restore(player.getInventory(), player.getName() + " inventory"));
        drops.addAll(enderChest.restore(player.getEnderChest(), player.getName() + " ender chest"));
        if (cursor != null) drops.add(cursor.toItemStack());
        status.restore(player);
        for (ItemStack drop : drops) {
            player.getWorld().dropItem(player.getEyeLocation(), drop).setPickupDelay(0);
        }
    }
}
