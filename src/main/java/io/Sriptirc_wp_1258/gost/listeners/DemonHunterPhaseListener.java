package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import io.Sriptirc_wp_1258.gost.managers.DivineGuardianManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class DemonHunterPhaseListener implements Listener {
    
    private final Gost plugin;
    
    public DemonHunterPhaseListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        // 只处理玩家回血事件
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // 检查是否在猎魔人阶段且禁止回血
        DivineGuardianManager divineGuardianManager = plugin.getDivineGuardianManager();
        if (divineGuardianManager != null && 
            divineGuardianManager.isInDemonHunterPhase() && 
            plugin.getConfigManager().isNoHealingInDemonHunterPhase()) {
            
            // 检查玩家是否是猎魔人或鬼玩家
            if (divineGuardianManager.isDemonHunter(player.getUniqueId()) || 
                plugin.getPlayerManager().isGhost(player.getUniqueId())) {
                
                // 取消回血事件
                event.setCancelled(true);
                
                // 发送提示消息（只在玩家主动回血时显示）
                if (event.getRegainReason() == RegainReason.SATIATED || 
                    event.getRegainReason() == RegainReason.REGEN || 
                    event.getRegainReason() == RegainReason.EATING) {
                    player.sendActionBar("§c猎魔人阶段禁止回血");
                }
            }
        }
    }
}