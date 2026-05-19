package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import io.Sriptirc_wp_1258.gost.managers.PlayerManager;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
    
    private final Gost plugin;
    
    public PlayerListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，强制离开
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            plugin.getPlayerManager().leaveGame(player);
        }
        
        // 如果玩家在队列中，强制离开队列
        if (plugin.getGameManager().getWaitingPlayersCount() > 0) {
            plugin.getGameManager().leaveQueue(player);
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否是旁观者
        if (plugin.getDivineGuardianManager().isSpectator(playerId)) {
            // 保持旁观模式
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("§c§l[游戏结束] §c你已被淘汰，处于旁观模式");
            return;
        }
        
        // 如果玩家在游戏中，传送到游戏区域
        if (plugin.getPlayerManager().getAllPlayers().contains(playerId)) {
            if (plugin.getConfigManager().isAutoTeleportEnabled()) {
                io.Sriptirc_wp_1258.gost.managers.AreaManager.GameArea selectedArea = plugin.getAreaManager().getSelectedArea();
                if (selectedArea != null) {
                    plugin.getAreaManager().teleportPlayerToArea(player, selectedArea);
                }
            }
            
            // 重新应用游戏状态
            plugin.getPlayerManager().applyGameState(player);
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在游戏中，禁止丢弃物品
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "游戏期间禁止丢弃物品！");
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 如果玩家在游戏中，检查伤害来源
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            // 允许PVP伤害（用于感染判定）
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                // PVP伤害由感染监听器处理
                return;
            }
            
            // 禁止其他类型的伤害
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 如果玩家在游戏中，锁定饱食度
        if (plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }
}