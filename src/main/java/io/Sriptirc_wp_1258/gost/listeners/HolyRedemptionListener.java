package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 神之救赎道具监听器
 * 处理救赎者使用神之救赎道具转化鬼玩家
 */
public class HolyRedemptionListener implements Listener {
    
    private final Gost plugin;
    
    public HolyRedemptionListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 检查右键点击的是否是玩家
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        
        Player redeemer = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        
        // 检查救赎者手中是否有神之救赎道具
        ItemStack itemInHand = redeemer.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            return;
        }
        
        // 检查是否是神之救赎道具
        if (!isHolyRedemptionItem(itemInHand)) {
            return;
        }
        
        // 检查玩家是否为救赎者
        if (!plugin.getDivineGuardianManager().isRedeemer(redeemer.getUniqueId())) {
            return;
        }
        
        // 取消事件（防止其他插件处理）
        event.setCancelled(true);
        
        // 使用神之救赎道具
        boolean success = plugin.getDivineGuardianManager().useHolyRedemption(redeemer, target);
        
        if (!success) {
            // 如果使用失败，可能是因为冷却时间或目标不是鬼玩家
            // 这些错误消息已经在useHolyRedemption方法中发送了
            return;
        }
    }
    
    /**
     * 检查物品是否是神之救赎道具
     */
    private boolean isHolyRedemptionItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        // 检查两种可能的显示名称格式（§6§l神之救赎 或 §6神之救赎）
        return displayName.equals("§6§l神之救赎") || displayName.equals(ChatColor.GOLD + "神之救赎");
    }
}