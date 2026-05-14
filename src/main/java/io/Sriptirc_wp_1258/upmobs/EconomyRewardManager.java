package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

/**
 * 经济奖励管理器
 * 处理击杀升格怪物的游戏币奖励
 */
public class EconomyRewardManager {
    
    private final JavaPlugin plugin;
    private Economy economy = null;
    private boolean economyEnabled = false;
    
    /**
     * 构造函数
     */
    public EconomyRewardManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("未找到Vault插件，经济奖励系统已禁用");
            economyEnabled = false;
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("未找到经济服务提供者，经济奖励系统已禁用");
            economyEnabled = false;
            return;
        }
        
        economy = rsp.getProvider();
        economyEnabled = true;
        plugin.getLogger().info("经济奖励系统已启用，使用: " + economy.getName());
    }
    
    /**
     * 检查经济系统是否可用
     */
    public boolean isEconomyEnabled() {
        return economyEnabled && economy != null;
    }
    
    /**
     * 给玩家奖励游戏币
     */
    public void rewardPlayer(Player player, double amount, String reason) {
        if (!isEconomyEnabled() || player == null || amount <= 0) {
            return;
        }
        
        // 检查是否启用奖励
        if (!plugin.getConfig().getBoolean("economy_rewards.enabled", true)) {
            return;
        }
        
        // 检查权限要求
        if (plugin.getConfig().getBoolean("economy_rewards.require_permission", false)) {
            String permissionNode = plugin.getConfig().getString("economy_rewards.permission_node", "upmobs.rewards");
            if (!player.hasPermission(permissionNode)) {
                return;
            }
        }
        
        // 检查最大奖励限制
        double maxReward = plugin.getConfig().getDouble("economy_rewards.max_reward_per_kill", 200.0);
        if (amount > maxReward) {
            amount = maxReward;
        }
        
        try {
            economy.depositPlayer(player, amount);
            
            // 发送奖励消息
            if (plugin.getConfig().getBoolean("economy_rewards.message_enabled", true)) {
                String messageFormat = plugin.getConfig().getString("economy_rewards.message_format", 
                    "§a§l+ §f{amount} 游戏币 §7({reason})");
                String formattedAmount = String.format("%.2f", amount);
                String message = messageFormat
                    .replace("{amount}", formattedAmount)
                    .replace("{reason}", reason);
                player.sendMessage(message);
            }
            
            plugin.getLogger().fine("奖励玩家 " + player.getName() + " " + amount + " 游戏币: " + reason);
        } catch (Exception e) {
            plugin.getLogger().warning("奖励玩家游戏币时出错: " + e.getMessage());
        }
    }
    
    /**
     * 获取经济系统实例
     */
    public Economy getEconomy() {
        return economy;
    }
    
    /**
     * 重新加载经济系统
     */
    public void reload() {
        setupEconomy();
    }
    
    /**
     * 检查经济系统是否启用
     */
    public boolean isEnabled() {
        return economyEnabled && economy != null;
    }
    
    /**
     * 给予玩家奖励
     */
    public void giveReward(Player player, double amount, String reason) {
        if (!isEnabled()) {
            plugin.getLogger().warning("经济系统未启用，无法给予奖励");
            return;
        }
        
        try {
            economy.depositPlayer(player, amount);
            
            // 发送奖励消息
            if (plugin.getConfig().getBoolean("economy_rewards.message_enabled", true)) {
                String messageFormat = plugin.getConfig().getString("economy_rewards.message_format", 
                    "§a§l+ §f{amount} 游戏币 §7({reason})");
                String formattedAmount = String.format("%.2f", amount);
                String message = messageFormat
                    .replace("{amount}", formattedAmount)
                    .replace("{reason}", reason);
                player.sendMessage(message);
            }
            
            plugin.getLogger().fine("奖励玩家 " + player.getName() + " " + amount + " 游戏币: " + reason);
        } catch (Exception e) {
            plugin.getLogger().warning("奖励玩家游戏币时出错: " + e.getMessage());
        }
    }
}