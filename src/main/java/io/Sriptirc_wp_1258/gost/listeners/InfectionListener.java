package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InfectionListener implements Listener {
    
    private final Gost plugin;
    
    public InfectionListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // 检查是否是玩家攻击玩家
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        if (plugin.getDivineGuardianManager().isDemonHunter(attacker.getUniqueId()) && 
            plugin.getPlayerManager().isGhost(victim.getUniqueId())) {
            if (plugin.getDivineGuardianManager().handleDemonHunterAttack(attacker, victim)) {
                event.setCancelled(true);
            }
            return;
        }
        
        if (!plugin.getPlayerManager().isGhost(attacker.getUniqueId())) {
            return;
        }
        
        if (!plugin.getPlayerManager().isHuman(victim.getUniqueId())) {
            return;
        }
        
        if (plugin.getDivineGuardianManager().handleGhostAttack(attacker, victim)) {
            event.setCancelled(true);
            return;
        }
        
        // 检查一次机会道具
        if (plugin.getSecondChanceListener().checkAndTriggerSecondChance(victim, attacker, event)) {
            // 一次机会触发，取消感染
            event.setCancelled(true);
            return;
        }
        
        // 执行感染
        plugin.getPlayerManager().infectPlayer(victim.getUniqueId(), attacker.getUniqueId());
        
        // 取消伤害（感染不造成伤害）
        event.setCancelled(true);
        
        // 发送消息
        attacker.sendMessage("§a你成功感染了 " + victim.getName() + "！");
        victim.sendMessage("§c你被 " + attacker.getName() + " 感染了！现在你变成了鬼！");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        // 检查是否是玩家右键点击玩家
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }
        
        Player attacker = event.getPlayer();
        Player victim = (Player) event.getRightClicked();
        
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        if (plugin.getDivineGuardianManager().isDemonHunter(attacker.getUniqueId()) && 
            plugin.getPlayerManager().isGhost(victim.getUniqueId())) {
            // 猎魔人右键攻击鬼玩家
            if (plugin.getDivineGuardianManager().handleDemonHunterAttack(attacker, victim)) {
                event.setCancelled(true);
            }
            return;
        }
        
        if (!plugin.getPlayerManager().isGhost(attacker.getUniqueId())) {
            return;
        }
        
        if (!plugin.getPlayerManager().isHuman(victim.getUniqueId())) {
            return;
        }
        
        if (plugin.getDivineGuardianManager().handleGhostAttack(attacker, victim)) {
            event.setCancelled(true);
            return;
        }
        
        // 检查一次机会道具
        if (plugin.getSecondChanceListener().checkAndTriggerSecondChance(victim, attacker, null)) {
            // 一次机会触发，取消感染
            event.setCancelled(true);
            return;
        }
        
        // 执行感染
        plugin.getPlayerManager().infectPlayer(victim.getUniqueId(), attacker.getUniqueId());
        
        // 取消事件（防止其他插件处理）
        event.setCancelled(true);
        
        // 发送消息
        attacker.sendMessage("§a你成功感染了 " + victim.getName() + "！");
        victim.sendMessage("§c你被 " + attacker.getName() + " 感染了！现在你变成了鬼！");
    }
}