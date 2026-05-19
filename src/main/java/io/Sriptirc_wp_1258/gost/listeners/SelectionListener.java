package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import io.Sriptirc_wp_1258.gost.managers.SelectionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 选区监听器
 * 监听玩家使用选区工具的操作
 */
public class SelectionListener implements Listener {
    
    private final Gost plugin;
    
    public SelectionListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 检查是否持有选区工具
        if (item == null || !isSelectionTool(item)) {
            return;
        }
        
        // 取消默认行为
        event.setCancelled(true);
        
        // 检查是否点击了方块
        if (event.getClickedBlock() == null) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        
        // 根据点击类型设置不同的点
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // 左键设置第一个点
            plugin.getSelectionManager().setPos1(player, clickedBlock.getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // 右键设置第二个点
            plugin.getSelectionManager().setPos2(player, clickedBlock.getLocation());
        }
    }
    
    /**
     * 检查物品是否是选区工具
     */
    private boolean isSelectionTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        // 获取配置的选区工具物品
        Material configMaterial;
        try {
            configMaterial = plugin.getSelectionManager().getSelectionTool().getType();
        } catch (Exception e) {
            // 如果选区管理器未完全初始化，使用默认值
            configMaterial = Material.MAGMA_CREAM;
        }
        
        // 检查物品类型
        if (item.getType() != configMaterial) {
            return false;
        }
        
        // 检查物品名称（可选）
        if (item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            return displayName.contains("区域选择工具");
        }
        
        return true;
    }
}