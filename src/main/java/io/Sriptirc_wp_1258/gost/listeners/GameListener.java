package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GameListener implements Listener {
    
    private final Gost plugin;
    
    public GameListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，禁止破坏方块
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏期间禁止破坏方块！");
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，禁止放置方块
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏期间禁止放置方块！");
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 如果玩家在游戏中，禁止移动物品
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏期间禁止移动物品！");
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，检查是否离开游戏区域
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            // 检查游戏区域
            io.Sriptirc_wp_1258.gost.managers.AreaManager.GameArea selectedArea = plugin.getAreaManager().getSelectedArea();
            if (selectedArea != null) {
                if (!selectedArea.contains(player.getLocation())) {
                    // 将玩家传送回区域中心
                    plugin.getAreaManager().teleportPlayerToArea(player, selectedArea);
                    player.sendMessage(ChatColor.RED + "请不要离开游戏区域！");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，检查物品使用
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            // 物品使用由ItemListener处理
            return;
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 这个事件由InfectionListener处理
        return;
    }
}