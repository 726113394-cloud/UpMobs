package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DarkEffectManager {
    
    private final Gost plugin;
    private BukkitTask sprintCheckTask;
    private final Map<UUID, AttributeModifier> sprintModifiers = new HashMap<>();
    private final Map<UUID, Boolean> wasSprinting = new HashMap<>();
    
    public DarkEffectManager(Gost plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载配置
     */
    public void loadConfig() {
        plugin.getLogger().info("黑暗效果管理器已加载（疾跑功能已修复）");
    }
    
    /**
     * 给所有游戏中的玩家应用黑暗效果
     */
    public synchronized void applyDarkEffectToAllPlayers() {
        if (!plugin.getConfigManager().isDarkEffectEnabled()) {
            return;
        }
        
        int duration = plugin.getConfigManager().getDarkEffectDuration();
        int amplifier = plugin.getConfigManager().getDarkEffectAmplifier();
        
        // 检查是否在准备阶段
        boolean isPreparationPhase = plugin.getGameManager().isPreparationPhase();
        
        for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // 如果在准备阶段，只给鬼应用黑暗效果
                if (isPreparationPhase) {
                    if (plugin.getPlayerManager().isGhost(playerId)) {
                        applyDarkEffectWithSprintFix(player, duration, amplifier, true);
                        
                        // 如果是管理员或创造模式玩家，发送提示消息
                        boolean isAdmin = player.hasPermission("gost.admin");
                        boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
                        
                        if (isAdmin || wasCreative) {
                            player.sendMessage(ChatColor.YELLOW + "⚠ 注意：作为" + (isAdmin ? "管理员" : "") + 
                                             (isAdmin && wasCreative ? "且" : "") + 
                                             (wasCreative ? "创造模式玩家" : "") + "参与游戏，你也会受到黑暗效果的影响！");
                        }
                    }
                } else {
                    // 游戏阶段，给所有玩家应用黑暗效果
                    applyDarkEffectWithSprintFix(player, duration, amplifier, true);
                    
                    // 如果是管理员或创造模式玩家，发送提示消息
                    boolean isAdmin = player.hasPermission("gost.admin");
                    boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
                    
                    if (isAdmin || wasCreative) {
                        player.sendMessage(ChatColor.YELLOW + "⚠ 注意：作为" + (isAdmin ? "管理员" : "") + 
                                         (isAdmin && wasCreative ? "且" : "") + 
                                         (wasCreative ? "创造模式玩家" : "") + "参与游戏，你也会受到黑暗效果的影响！");
                    }
                }
            }
        }
        
        // 启动疾跑检查任务
        startSprintCheckTask();
        
        plugin.getLogger().info("黑暗效果已应用给所有玩家（疾跑功能已修复）");
    }
    
    /**
     * 应用黑暗效果并修复疾跑问题
     */
    private synchronized void applyDarkEffectWithSprintFix(Player player, int duration, int amplifier, boolean force) {
        // 首先清除可能存在的黑暗效果
        if (force) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            // 尝试移除DARKNESS效果（如果存在）
            try {
                PotionEffectType darknessType = PotionEffectType.getByName("DARKNESS");
                if (darknessType != null) {
                    player.removePotionEffect(darknessType);
                }
            } catch (Exception e) {
                // 如果DARKNESS效果不存在，忽略错误
            }
        }
        
        // 尝试使用DARKNESS效果（1.19+版本）
        PotionEffectType effectType = null;
        try {
            effectType = PotionEffectType.getByName("DARKNESS");
        } catch (Exception e) {
            // 如果DARKNESS效果不存在，回退到BLINDNESS
            effectType = PotionEffectType.BLINDNESS;
        }
        
        // 创建黑暗效果
        PotionEffect darkEffect = new PotionEffect(
            effectType,
            duration * 20, // 转换为tick（1秒=20tick）
            amplifier,
            true, // 环境效果
            false, // 不显示粒子
            false // 不显示图标
        );
        
        // 强制应用效果
        player.addPotionEffect(darkEffect, force);
        
        // 为玩家添加疾跑修复
        applySprintFix(player);
        
        if (force && player.hasPermission("gost.admin")) {
            plugin.getLogger().info("应用黑暗效果给管理员: " + player.getName() + "，使用效果类型: " + effectType.getName() + "（疾跑已修复）");
        }
    }
    
    /**
     * 为玩家应用疾跑修复
     * 通过修改移动速度属性来抵消DARKNESS效果对疾跑的影响
     */
    private synchronized void applySprintFix(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 移除现有的疾跑修复（如果有）
        removeSprintFix(player);
        
        // 创建属性修改器来增加移动速度，抵消DARKNESS效果的影响
        AttributeModifier sprintModifier = new AttributeModifier(
            UUID.randomUUID(), // 唯一ID
            "gost_darkness_sprint_fix", // 名称
            0.3, // 增加30%移动速度，足够抵消DARKNESS效果的影响
            AttributeModifier.Operation.ADD_SCALAR // 按比例增加
        );
        
        // 获取玩家的移动速度属性
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null) {
            // 添加修改器
            movementSpeed.addModifier(sprintModifier);
            sprintModifiers.put(playerId, sprintModifier);
            
            // 初始化疾跑状态
            wasSprinting.put(playerId, player.isSprinting());
            
            plugin.getLogger().fine("为玩家 " + player.getName() + " 应用疾跑修复");
        }
    }
    
    /**
     * 移除玩家的疾跑修复
     */
    private synchronized void removeSprintFix(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (sprintModifiers.containsKey(playerId)) {
            AttributeModifier modifier = sprintModifiers.get(playerId);
            AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            
            if (movementSpeed != null) {
                movementSpeed.removeModifier(modifier);
            }
            
            sprintModifiers.remove(playerId);
            wasSprinting.remove(playerId);
            
            plugin.getLogger().fine("移除玩家 " + player.getName() + " 的疾跑修复");
        }
    }
    
    /**
     * 启动疾跑检查任务
     * 定期检查玩家是否在尝试疾跑，确保他们可以正常疾跑
     */
    private synchronized void startSprintCheckTask() {
        if (sprintCheckTask != null && !sprintCheckTask.isCancelled()) {
            sprintCheckTask.cancel();
        }
        
        sprintCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getGameManager().isGameRunning()) {
                    return;
                }
                
                if (!plugin.getConfigManager().isDarkEffectEnabled()) {
                    return;
                }
                
                // 检查所有有黑暗效果的玩家
                for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        checkAndFixSprint(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // 每10 ticks检查一次
        
        plugin.getLogger().info("疾跑检查任务已启动");
    }
    
    /**
     * 检查并修复玩家的疾跑状态
     */
    private synchronized void checkAndFixSprint(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否有黑暗效果
        boolean hasDarkness = false;
        try {
            PotionEffectType darknessType = PotionEffectType.getByName("DARKNESS");
            if (darknessType != null) {
                hasDarkness = player.hasPotionEffect(darknessType);
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        boolean hasBlindness = player.hasPotionEffect(PotionEffectType.BLINDNESS);
        
        if (!hasDarkness && !hasBlindness) {
            // 玩家没有黑暗效果，移除疾跑修复
            if (sprintModifiers.containsKey(playerId)) {
                removeSprintFix(player);
            }
            return;
        }
        
        // 玩家有黑暗效果，确保有疾跑修复
        if (!sprintModifiers.containsKey(playerId)) {
            applySprintFix(player);
        }
        
        // 检查玩家是否在尝试疾跑但被阻止了
        boolean isSprinting = player.isSprinting();
        boolean wasSprintingBefore = wasSprinting.getOrDefault(playerId, false);
        
        // 如果玩家之前在疾跑但现在不是，但W键仍然被按住，帮助他们恢复疾跑
        if (wasSprintingBefore && !isSprinting) {
            // 检查玩家是否仍然按着前进键（通过速度判断）
            if (player.getVelocity().length() > 0.1) {
                // 强制恢复疾跑状态
                player.setSprinting(true);
                plugin.getLogger().fine("帮助玩家 " + player.getName() + " 恢复疾跑");
            }
        }
        
        // 更新疾跑状态
        wasSprinting.put(playerId, isSprinting);
    }
    
    /**
     * 给指定玩家应用黑暗效果
     * @param player 玩家
     */
    public synchronized void applyDarkEffect(Player player) {
        if (!plugin.getConfigManager().isDarkEffectEnabled()) {
            return;
        }
        
        int duration = plugin.getConfigManager().getDarkEffectDuration();
        int amplifier = plugin.getConfigManager().getDarkEffectAmplifier();
        
        // 检查是否在准备阶段
        boolean isPreparationPhase = plugin.getGameManager().isPreparationPhase();
        
        // 如果在准备阶段，只给鬼应用黑暗效果
        if (isPreparationPhase) {
            UUID playerId = player.getUniqueId();
            if (plugin.getPlayerManager().isGhost(playerId)) {
                applyDarkEffectWithSprintFix(player, duration, amplifier, true);
                
                // 如果是管理员或创造模式玩家，发送提示消息
                boolean isAdmin = player.hasPermission("gost.admin");
                boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
                
                if (isAdmin || wasCreative) {
                    player.sendMessage(ChatColor.YELLOW + "⚠ 注意：作为" + (isAdmin ? "管理员" : "") + 
                                     (isAdmin && wasCreative ? "且" : "") + 
                                     (wasCreative ? "创造模式玩家" : "") + "参与游戏，你也会受到黑暗效果的影响！");
                }
            }
        } else {
            // 游戏阶段，给所有玩家应用黑暗效果
            applyDarkEffectWithSprintFix(player, duration, amplifier, true);
            
            // 如果是管理员或创造模式玩家，发送提示消息
            boolean isAdmin = player.hasPermission("gost.admin");
            boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
            
            if (isAdmin || wasCreative) {
                player.sendMessage(ChatColor.YELLOW + "⚠ 注意：作为" + (isAdmin ? "管理员" : "") + 
                                 (isAdmin && wasCreative ? "且" : "") + 
                                 (wasCreative ? "创造模式玩家" : "") + "参与游戏，你也会受到黑暗效果的影响！");
            }
        }
    }
    
    /**
     * 移除所有玩家的黑暗效果
     */
    public synchronized void removeDarkEffectFromAllPlayers() {
        for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeDarkEffect(player);
            }
        }
        
        // 停止疾跑检查任务
        if (sprintCheckTask != null && !sprintCheckTask.isCancelled()) {
            sprintCheckTask.cancel();
            sprintCheckTask = null;
        }
        
        // 清理所有疾跑修复
        sprintModifiers.clear();
        wasSprinting.clear();
    }
    
    /**
     * 移除指定玩家的黑暗效果
     * @param player 玩家
     */
    public synchronized void removeDarkEffect(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        
        // 尝试移除DARKNESS效果（如果存在）
        try {
            PotionEffectType darknessType = PotionEffectType.getByName("DARKNESS");
            if (darknessType != null) {
                player.removePotionEffect(darknessType);
            }
        } catch (Exception e) {
            // 如果DARKNESS效果不存在，忽略错误
        }
        
        // 移除疾跑修复
        removeSprintFix(player);
    }
    
    /**
     * 切换黑暗效果开关
     * @param enabled 是否启用
     */
    public synchronized void toggleDarkEffect(boolean enabled) {
        plugin.getConfigManager().setDarkEffectEnabled(enabled);
        
        if (enabled) {
            // 如果启用，给所有玩家应用黑暗效果
            applyDarkEffectToAllPlayers();
            Bukkit.broadcastMessage("§8[§cGost§8] §7黑暗效果已启用！玩家可以正常疾跑。");
        } else {
            // 如果禁用，移除所有玩家的黑暗效果
            removeDarkEffectFromAllPlayers();
            Bukkit.broadcastMessage("§8[§cGost§8] §7黑暗效果已禁用！");
        }
    }
    
    /**
     * 清理所有数据
     */
    public synchronized void cleanup() {
        // 移除所有玩家的疾跑修复
        // 使用副本遍历，避免ConcurrentModificationException
        List<UUID> playerIds = new ArrayList<>(sprintModifiers.keySet());
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removeSprintFix(player);
            }
        }
        
        sprintModifiers.clear();
        wasSprinting.clear();
        
        // 停止疾跑检查任务
        if (sprintCheckTask != null && !sprintCheckTask.isCancelled()) {
            sprintCheckTask.cancel();
            sprintCheckTask = null;
        }
    }
    
    /**
     * 检查黑暗效果是否启用
     * @return 是否启用
     */
    public boolean isDarkEffectEnabled() {
        return plugin.getConfigManager().isDarkEffectEnabled();
    }
}