package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarManager {
    
    private final Gost plugin;
    private final Map<UUID, String> actionBarMessages = new HashMap<>();
    private final Map<UUID, Integer> actionBarTasks = new HashMap<>();
    
    public ActionBarManager(Gost plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 发送ActionBar消息给玩家
     * @param player 目标玩家
     * @param message 消息内容
     * @param duration 持续时间（秒），0表示永久显示直到被覆盖
     */
    public void sendActionBar(Player player, String message, int duration) {
        UUID playerId = player.getUniqueId();
        
        // 取消之前的任务
        cancelActionBar(playerId);
        
        // 设置消息
        actionBarMessages.put(playerId, message);
        
        // 立即发送一次
        sendActionBarNow(player, message);
        
        // 如果需要持续显示，创建定时任务
        if (duration > 0) {
            int taskId = new BukkitRunnable() {
                int remaining = duration;
                
                @Override
                public void run() {
                    if (!player.isOnline() || !actionBarMessages.containsKey(playerId)) {
                        cancel();
                        actionBarTasks.remove(playerId);
                        return;
                    }
                    
                    // 每秒发送一次
                    sendActionBarNow(player, actionBarMessages.get(playerId));
                    
                    remaining--;
                    if (remaining <= 0) {
                        cancel();
                        actionBarMessages.remove(playerId);
                        actionBarTasks.remove(playerId);
                    }
                }
            }.runTaskTimer(plugin, 20L, 20L).getTaskId();
            
            actionBarTasks.put(playerId, taskId);
        }
    }
    
    /**
     * 立即发送ActionBar消息（不存储）
     */
    private void sendActionBarNow(Player player, String message) {
        try {
            // 使用反射发送ActionBar消息
            player.sendActionBar(message);
        } catch (Exception e) {
            // 如果sendActionBar方法不存在，尝试使用其他方式
            try {
                // 使用Title API发送ActionBar（1.20.x支持）
                player.sendTitle("", message, 0, 20, 0);
            } catch (Exception ex) {
                // 如果都不行，发送普通消息
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 取消玩家的ActionBar显示
     */
    public void cancelActionBar(UUID playerId) {
        if (actionBarTasks.containsKey(playerId)) {
            Bukkit.getScheduler().cancelTask(actionBarTasks.get(playerId));
            actionBarTasks.remove(playerId);
        }
        actionBarMessages.remove(playerId);
    }
    
    /**
     * 发送道具使用提示
     */
    public void sendItemUsageHint(Player player, String itemName, String usage) {
        String message = ChatColor.YELLOW + "道具: " + ChatColor.GOLD + itemName + 
                        ChatColor.YELLOW + " - " + ChatColor.GRAY + usage;
        sendActionBar(player, message, 5); // 显示5秒
    }
    
    /**
     * 发送肾上腺素使用提示
     */
    public void sendAdrenalineHint(Player player) {
        sendItemUsageHint(player, "肾上腺素", "右键使用获得速度提升");
    }
    
    /**
     * 发送狂暴药水使用提示
     */
    public void sendFrenzyPotionHint(Player player) {
        sendItemUsageHint(player, "狂暴药水", "右键使用获得速度提升");
    }
    
    /**
     * 发送凝冰球使用提示
     */
    public void sendIceBallHint(Player player) {
        sendItemUsageHint(player, "凝冰球", "右键投掷减速鬼玩家");
    }
    
    /**
     * 发送控魂术使用提示
     */
    public void sendSoulControlHint(Player player) {
        sendItemUsageHint(player, "控魂术", "右键使用冻结所有鬼");
    }
    
    /**
     * 发送灵魂探测器使用提示
     */
    public void sendSoulDetectorHint(Player player) {
        sendItemUsageHint(player, "灵魂探测器", "右键使用使所有玩家发光25秒");
    }
    
    public void sendStinkySteakHint(Player player) {
        sendItemUsageHint(player, "臭牛排", "右键食用获得速度II效果和发光效果");
    }
    
    public void sendTeleportPearlHint(Player player) {
        sendItemUsageHint(player, "传送珍珠", "右键投掷进行传送");
    }
    
    /**
     * 清理所有ActionBar
     */
    public void cleanup() {
        for (UUID playerId : actionBarTasks.keySet()) {
            Bukkit.getScheduler().cancelTask(actionBarTasks.get(playerId));
        }
        actionBarTasks.clear();
        actionBarMessages.clear();
    }
}