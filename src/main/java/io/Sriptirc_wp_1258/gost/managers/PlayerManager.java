package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerManager {
    
    public enum PlayerRole {
        HUMAN,          // 人类
        GHOST_MOTHER,   // 母体鬼
        GHOST_NORMAL    // 普通鬼
    }
    
    private final Gost plugin;
    
    // 玩家数据存储
    private final Map<UUID, PlayerRole> playerRoles = new HashMap<>();
    private final Map<UUID, PlayerState> savedStates = new HashMap<>();
    private final Map<UUID, Integer> infectionCounts = new HashMap<>();
    private final Map<UUID, Long> survivalTimes = new HashMap<>();
    private final Map<UUID, Long> roleStartTimes = new HashMap<>();
    private final Map<UUID, Long> ghostAccumulatedTimes = new HashMap<>(); // 累计鬼时间（毫秒）
    private final Set<UUID> convertedByRedemption = new HashSet<>(); // 被神之救赎转化的玩家
    
    public PlayerManager(Gost plugin) {
        this.plugin = plugin;
    }
    
    // 玩家加入游戏
    public boolean joinGame(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (playerRoles.containsKey(playerId)) {
            return false; // 已经在游戏中
        }
        
        // 保存玩家原始状态
        savePlayerState(player);
        
        // 初始化为人类
        setPlayerRole(playerId, PlayerRole.HUMAN);
        
        // 重置游戏内统计
        infectionCounts.put(playerId, 0);
        survivalTimes.put(playerId, 0L);
        roleStartTimes.put(playerId, System.currentTimeMillis());
        
        // 应用游戏状态
        applyGameState(player);
        
        plugin.getLogger().info("玩家 " + player.getName() + " 加入游戏");
        return true;
    }
    
    // 玩家离开游戏
    public boolean leaveGame(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerRoles.containsKey(playerId)) {
            return false; // 不在游戏中
        }
        
        // 恢复玩家原始状态
        restorePlayerState(player);
        
        // 清理数据
        playerRoles.remove(playerId);
        savedStates.remove(playerId);
        infectionCounts.remove(playerId);
        survivalTimes.remove(playerId);
        roleStartTimes.remove(playerId);
        
        // 从队伍中移除
        plugin.getTeamManager().removeFromAllTeams(player);
        
        player.sendMessage("§a你已离开游戏！");
        plugin.getLogger().info("玩家 " + player.getName() + " 离开游戏");
        return true;
    }
    
    // 设置玩家角色
    public void setPlayerRole(UUID playerId, PlayerRole role) {
        Player player = Bukkit.getPlayer(playerId);
        PlayerRole oldRole = playerRoles.get(playerId);
        
        // 更新角色开始时间
        if (oldRole != null) {
            updateSurvivalTime(playerId);
            
            // 如果从鬼转换为人类，记录累计鬼时间
            if ((oldRole == PlayerRole.GHOST_MOTHER || oldRole == PlayerRole.GHOST_NORMAL) 
                && role == PlayerRole.HUMAN) {
                recordGhostAccumulatedTime(playerId);
                plugin.getLogger().info("玩家 " + playerId + " 从鬼转换为人类，记录累计鬼时间");
            }
        }
        roleStartTimes.put(playerId, System.currentTimeMillis());
        
        // 更新角色
        playerRoles.put(playerId, role);
        
        if (player != null && player.isOnline()) {
            // 更新队伍
            plugin.getTeamManager().setPlayerTeam(player, role);
            
            // 更新背包
            updatePlayerInventory(player, role);
            
            // 应用角色特定效果
            applyRoleEffects(player, role);
            
            // 发送角色变更消息
            String roleName = plugin.getTeamManager().getTeamName(role);
            ChatColor color = plugin.getTeamManager().getTeamColor(role);
            player.sendMessage(color + "你的角色已变更为: " + roleName);
            
            // 更新鬼玩家粒子效果数据
            updateGhostParticleData(playerId, role);
        } else {
            // 玩家离线，仍然更新粒子数据
            updateGhostParticleData(playerId, role);
        }
    }
    
    // 更新鬼玩家粒子效果数据
    private void updateGhostParticleData(UUID playerId, PlayerRole role) {
        if (plugin.getGhostParticleManager() != null) {
            // 将PlayerRole转换为GhostParticleManager的PlayerRole
            GhostParticleManager.PlayerRole particleRole;
            switch (role) {
                case GHOST_MOTHER:
                    particleRole = GhostParticleManager.PlayerRole.GHOST_MOTHER;
                    break;
                case GHOST_NORMAL:
                    particleRole = GhostParticleManager.PlayerRole.GHOST_NORMAL;
                    break;
                default:
                    particleRole = GhostParticleManager.PlayerRole.HUMAN;
                    break;
            }
            plugin.getGhostParticleManager().updatePlayerParticleData(playerId, particleRole);
        }
    }
    
    // 获取玩家角色
    public PlayerRole getPlayerRole(UUID playerId) {
        PlayerRole role = playerRoles.get(playerId);
        if (role == null) {
            plugin.getLogger().warning("玩家 " + playerId + " 的角色为null，可能未加入游戏");
        }
        return role;
    }
    
    // 检查是否是鬼
    public boolean isGhost(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        return role != null && (role == PlayerRole.GHOST_MOTHER || role == PlayerRole.GHOST_NORMAL);
    }
    
    // 将鬼玩家转换为人类（用于救赎者系统）
    public boolean convertGhostToHuman(UUID playerId) {
        if (!isGhost(playerId)) {
            return false; // 不是鬼玩家，无法转换
        }
        
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return false; // 玩家不在线
        }
        
        // 设置玩家角色为人类
        setPlayerRole(playerId, PlayerRole.HUMAN);
        
        // 标记为被神之救赎转化
        convertedByRedemption.add(playerId);
        
        // 应用人类效果
        applyRoleEffects(player, PlayerRole.HUMAN);
        
        // 更新存活时间
        updateSurvivalTime(playerId);
        
        plugin.getLogger().info("鬼玩家 " + player.getName() + " 被转换为人类");
        return true;
    }
    
    // 检查是否是母体鬼
    public boolean isGhostMother(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        return role != null && role == PlayerRole.GHOST_MOTHER;
    }
    
    // 检查是否是人类
    public boolean isHuman(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        return role != null && role == PlayerRole.HUMAN;
    }
    
    // 检查是否是母体鬼
    public boolean isMotherGhost(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        return role != null && role == PlayerRole.GHOST_MOTHER;
    }
    
    // 感染玩家
    public void infectPlayer(UUID victimId, UUID infectorId) {
        PlayerRole currentRole = getPlayerRole(victimId);
        
        if (currentRole == null) {
            plugin.getLogger().warning("感染失败：玩家 " + victimId + " 角色为null");
            return;
        }
        
        if (currentRole != PlayerRole.HUMAN) {
            plugin.getLogger().info("感染失败：玩家 " + victimId + " 不是人类，当前角色: " + currentRole);
            return; // 只能感染人类
        }
        
        // 更新感染统计
        if (infectorId != null) {
            boolean infectorIsGhost = isGhost(infectorId);
            plugin.getLogger().info("感染者 " + infectorId + " 是鬼: " + infectorIsGhost);
            if (infectorIsGhost) {
                int count = infectionCounts.getOrDefault(infectorId, 0);
                infectionCounts.put(infectorId, count + 1);
            }
        }
        
        // 决定新鬼的类型
        PlayerRole newRole = PlayerRole.GHOST_NORMAL;
        
        // 检查是否还有母体鬼
        boolean hasMother = playerRoles.values().stream()
            .anyMatch(role -> role == PlayerRole.GHOST_MOTHER);
        
        plugin.getLogger().info("检查是否有母体鬼: " + hasMother + " (总玩家数: " + playerRoles.size() + ")");
        
        if (!hasMother) {
            newRole = PlayerRole.GHOST_MOTHER;
            plugin.getLogger().info("没有母体鬼，将玩家 " + victimId + " 设为母体鬼");
        } else {
            plugin.getLogger().info("已有母体鬼，将玩家 " + victimId + " 设为普通鬼");
        }
        
        plugin.getLogger().info("玩家 " + victimId + " 被感染，新角色: " + newRole);
        
        // 设置新角色
        setPlayerRole(victimId, newRole);
        
        // 更新存活时间
        updateSurvivalTime(victimId);
        
        // 发送全局消息
        Player victim = Bukkit.getPlayer(victimId);
        Player infector = infectorId != null ? Bukkit.getPlayer(infectorId) : null;
        
        if (victim != null) {
            String infectorName = infector != null ? infector.getName() : "未知";
            Bukkit.broadcastMessage("§c" + victim.getName() + " 被 " + infectorName + " 感染了！");
            
            // 显示感染效果
            showInfectionEffects(victim);
            
            // 发送感染标题
            plugin.getGameManager().sendInfectedTitle(victim);
        }
        
        // 更新游戏统计
        updateGameStats();
        
        // 记录当前鬼玩家数量
        long ghostCount = playerRoles.values().stream()
            .filter(role -> role == PlayerRole.GHOST_MOTHER || role == PlayerRole.GHOST_NORMAL)
            .count();
        plugin.getLogger().info("感染完成，当前鬼玩家数量: " + ghostCount);
        
        // 检查神圣守护状态
        checkDivineGuardianStatus();
    }
    
    // 检查神圣守护状态
    private void checkDivineGuardianStatus() {
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        // 获取当前人类玩家列表
        List<UUID> humanPlayers = getHumanPlayers();
        
        // 检查神圣守护
        plugin.getDivineGuardianManager().checkAndActivateHolyGuardian(humanPlayers);
    }
    
    // 显示感染效果
    private void showInfectionEffects(Player player) {
        // 闪电效果
        if (plugin.getConfigManager().isInfectionLightningEnabled()) {
            player.getWorld().strikeLightningEffect(player.getLocation());
        }
        
        // 音效
        if (plugin.getConfigManager().isInfectionSoundEnabled()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
        
        // 粒子效果
        player.getWorld().spawnParticle(org.bukkit.Particle.FLAME, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
    }
    
    // 更新游戏统计
    private void updateGameStats() {
        int humanCount = 0;
        int ghostCount = 0;
        
        for (PlayerRole role : playerRoles.values()) {
            if (role == PlayerRole.HUMAN) {
                humanCount++;
            } else {
                ghostCount++;
            }
        }
        
        // 更新Boss栏
        plugin.getGameManager().updateBossBarStats(humanCount, ghostCount);
        
        // 广播统计
        Bukkit.broadcastMessage("§a剩余人类: " + humanCount + " §c鬼: " + ghostCount);
    }
    
    // 更新存活时间
    public void updateSurvivalTime(UUID playerId) {
        Long startTime = roleStartTimes.get(playerId);
        if (startTime != null) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;
            
            Long currentSurvival = survivalTimes.getOrDefault(playerId, 0L);
            survivalTimes.put(playerId, currentSurvival + elapsed);
            
            PlayerRole role = getPlayerRole(playerId);
            plugin.getLogger().info("更新玩家 " + playerId + " 存活时间，角色: " + role + 
                ", 本次存活: " + (elapsed / 1000) + "秒, 累计: " + ((currentSurvival + elapsed) / 1000) + "秒");
        } else {
            plugin.getLogger().warning("更新存活时间失败：玩家 " + playerId + " 没有角色开始时间");
        }
    }
    
    // 获取存活时间（秒）
    public long getSurvivalTime(UUID playerId) {
        updateSurvivalTime(playerId);
        long survivalTime = survivalTimes.getOrDefault(playerId, 0L) / 1000;
        PlayerRole role = getPlayerRole(playerId);
        plugin.getLogger().info("获取玩家 " + playerId + " 存活时间，角色: " + role + ", 存活: " + survivalTime + "秒");
        return survivalTime;
    }
    
    // 获取鬼的存活时间（秒）- 从成为鬼开始计算
    public long getGhostTime(UUID playerId) {
        PlayerRole role = getPlayerRole(playerId);
        
        // 如果不是鬼，返回0
        if (role != PlayerRole.GHOST_MOTHER && role != PlayerRole.GHOST_NORMAL) {
            return 0;
        }
        
        // 获取角色开始时间
        Long startTime = roleStartTimes.get(playerId);
        if (startTime == null) {
            return 0;
        }
        
        // 计算从成为鬼开始到现在的存活时间
        long currentTime = System.currentTimeMillis();
        long ghostTime = currentTime - startTime;
        
        plugin.getLogger().info("获取玩家 " + playerId + " 鬼存活时间，角色: " + role + ", 鬼存活: " + (ghostTime / 1000) + "秒");
        return ghostTime / 1000; // 转换为秒
    }
    
    // 记录累计鬼时间（当鬼转换为人类时调用）
    private void recordGhostAccumulatedTime(UUID playerId) {
        Long startTime = roleStartTimes.get(playerId);
        if (startTime == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long ghostTime = currentTime - startTime;
        
        // 累加到累计鬼时间
        long accumulatedTime = ghostAccumulatedTimes.getOrDefault(playerId, 0L);
        accumulatedTime += ghostTime;
        ghostAccumulatedTimes.put(playerId, accumulatedTime);
        
        plugin.getLogger().info("记录玩家 " + playerId + " 累计鬼时间: " + (accumulatedTime / 1000) + "秒");
    }
    
    // 获取累计鬼时间（秒）- 用于奖金计算
    public long getGhostAccumulatedTime(UUID playerId) {
        return ghostAccumulatedTimes.getOrDefault(playerId, 0L) / 1000; // 转换为秒
    }
    
    // 获取感染次数
    public int getInfectionCount(UUID playerId) {
        return infectionCounts.getOrDefault(playerId, 0);
    }
    
    // 应用游戏状态
    public void applyGameState(Player player) {
        plugin.getLogger().info("应用游戏状态给玩家: " + player.getName());
        
        // 检查玩家是否处于创造模式，如果是则强制切换并提示
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            GameMode originalMode = player.getGameMode();
            player.setGameMode(GameMode.SURVIVAL);
            
            // 发送提示消息
            player.sendMessage(ChatColor.YELLOW + "⚠ 注意：创造/旁观模式已自动切换为生存模式！");
            player.sendMessage(ChatColor.YELLOW + "游戏期间所有玩家必须使用生存模式以确保公平性。");
            
            plugin.getLogger().info("强制切换玩家 " + player.getName() + " 从 " + originalMode + " 到 SURVIVAL 模式");
        } else {
            // 设置游戏模式为生存模式
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        // 设置生命值（使用配置文件中的值）
        double maxHealth = plugin.getConfigManager().getMaxHealth();
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealth(maxHealth);
        plugin.getLogger().info("设置玩家 " + player.getName() + " 生命值为" + maxHealth + "点");
        
        // 清除所有药水效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // 设置饱食度
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // 传送到游戏区域
        if (plugin.getConfigManager().isAutoTeleportEnabled()) {
            plugin.getAreaManager().teleportPlayerToArea(player, plugin.getAreaManager().getSelectedArea());
        }
    }
    
    // 应用角色特定效果
    public void applyRoleEffects(Player player, PlayerRole role) {
        plugin.getLogger().info("应用角色效果给玩家: " + player.getName() + ", 角色: " + role);
        
        // 无论玩家是否是管理员或创造模式，都强制应用游戏效果
        // 这是为了确保游戏平衡性，所有玩家参与游戏时应该受到相同的影响
        
        boolean isAdmin = player.hasPermission("gost.admin");
        boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
        
        switch (role) {
            case GHOST_MOTHER:
                String playerType = "";
                if (isAdmin) playerType = "管理员";
                if (wasCreative) playerType += (playerType.isEmpty() ? "" : "且") + "创造模式玩家";
                if (playerType.isEmpty()) playerType = "玩家";
                
                plugin.getLogger().info("应用母体鬼效果给" + playerType + ": " + player.getName() + " (强制应用效果)");
                
                // 首先清除可能存在的免疫效果
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                
                // 母体鬼失明效果 - 强制应用
                int blindnessDuration = plugin.getConfigManager().getMotherGhostBlindnessDuration() * 20;
                PotionEffect blindnessEffect = new PotionEffect(
                    PotionEffectType.BLINDNESS,
                    blindnessDuration,
                    0,
                    true,
                    true
                );
                // 使用 forceAddPotionEffect 确保效果被应用
                player.addPotionEffect(blindnessEffect, true); // true 表示强制覆盖现有效果
                plugin.getLogger().info("强制应用失明效果给" + playerType + ": " + player.getName() + ", 持续时间: " + blindnessDuration + " ticks");
                
                // 母体鬼20秒无法移动效果（神鬼药水效果） - 强制应用
                int immobilizeDuration = plugin.getConfigManager().getGhostImmobilizeDuration() * 20;
                PotionEffect slowEffect = new PotionEffect(
                    PotionEffectType.SLOW,
                    immobilizeDuration,
                    255, // 最大等级，几乎无法移动
                    true,
                    true
                );
                player.addPotionEffect(slowEffect, true); // 强制覆盖
                plugin.getLogger().info("强制应用减速效果给" + playerType + ": " + player.getName() + ", 持续时间: " + immobilizeDuration + " ticks");
                
                // 同时添加挖掘疲劳效果，确保完全无法移动 - 强制应用
                PotionEffect miningEffect = new PotionEffect(
                    PotionEffectType.SLOW_DIGGING,
                    immobilizeDuration,
                    255,
                    true,
                    true
                );
                player.addPotionEffect(miningEffect, true); // 强制覆盖
                plugin.getLogger().info("强制应用挖掘疲劳效果给" + playerType + ": " + player.getName() + ", 持续时间: " + immobilizeDuration + " ticks");
                
                // 发送提示消息
                if (isAdmin || wasCreative) {
                    player.sendMessage(ChatColor.YELLOW + "⚠ 注意：作为" + (isAdmin ? "管理员" : "") + 
                                     (isAdmin && wasCreative ? "且" : "") + 
                                     (wasCreative ? "创造模式玩家" : "") + "参与游戏，你也会受到母体鬼禁足效果的影响！");
                    player.sendMessage(ChatColor.YELLOW + "禁足时间: " + plugin.getConfigManager().getGhostImmobilizeDuration() + "秒");
                }
                
                plugin.getLanguageManager().sendMessage(player, "role.ghost-disabled-prep");
                break;
        }
    }
    
    // 更新玩家背包
    public void updatePlayerInventory(Player player, PlayerRole role) {
        plugin.getLogger().info("更新玩家背包: " + player.getName() + ", 角色: " + role);
        PlayerInventory inventory = player.getInventory();
        
        // 清空背包
        inventory.clear();
        plugin.getLogger().info("清空玩家 " + player.getName() + " 的背包");
        
        // 根据角色给予初始物品
        switch (role) {
            case HUMAN:
                plugin.getLogger().info("给予人类 " + player.getName() + " 肾上腺素");
                // 人类获得肾上腺素
                inventory.addItem(plugin.getItemManager().getAdrenaline());
                break;
            case GHOST_MOTHER:
                plugin.getLogger().info("给予母体鬼 " + player.getName() + " 狂暴药水");
                // 母体鬼获得狂暴药水
                inventory.addItem(plugin.getItemManager().getFrenzyPotion());
                break;
            case GHOST_NORMAL:
                plugin.getLogger().info("给予普通鬼 " + player.getName() + " 狂暴药水");
                // 普通鬼获得狂暴药水
                inventory.addItem(plugin.getItemManager().getFrenzyPotion());
                break;
        }
        
        player.updateInventory();
        plugin.getLogger().info("玩家 " + player.getName() + " 背包更新完成");
    }
    
    // 保存玩家原始状态
    public void savePlayerState(Player player) {
        PlayerState state = new PlayerState(player);
        savedStates.put(player.getUniqueId(), state);
    }
    
    // 恢复玩家原始状态
    public void restorePlayerState(Player player) {
        PlayerState state = savedStates.get(player.getUniqueId());
        if (state != null) {
            state.restore(player, plugin);
            savedStates.remove(player.getUniqueId());
        }
        
        // 清除所有药水效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // 移除黑暗效果和疾跑修复
        plugin.getDarkEffectManager().removeDarkEffect(player);
        
        // 恢复最大血量
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.setHealth(20.0);
        
        // 从队伍中移除
        plugin.getTeamManager().removeFromAllTeams(player);
    }
    
    // 获取所有游戏中的玩家
    public List<UUID> getAllPlayers() {
        return new ArrayList<>(playerRoles.keySet());
    }
    
    // 获取所有人类玩家
    public List<UUID> getHumanPlayers() {
        List<UUID> humans = new ArrayList<>();
        for (Map.Entry<UUID, PlayerRole> entry : playerRoles.entrySet()) {
            if (entry.getValue() == PlayerRole.HUMAN) {
                humans.add(entry.getKey());
            }
        }
        return humans;
    }
    
    // 获取所有鬼玩家
    public List<UUID> getGhostPlayers() {
        List<UUID> ghosts = new ArrayList<>();
        for (Map.Entry<UUID, PlayerRole> entry : playerRoles.entrySet()) {
            if (entry.getValue() == PlayerRole.GHOST_MOTHER || entry.getValue() == PlayerRole.GHOST_NORMAL) {
                ghosts.add(entry.getKey());
            }
        }
        return ghosts;
    }
    
    // 检查玩家是否被神之救赎转化
    public boolean isConvertedByRedemption(UUID playerId) {
        return convertedByRedemption.contains(playerId);
    }
    
    // 清理所有玩家数据
    public void cleanup() {
        // 首先恢复所有在线玩家的状态
        for (UUID playerId : new ArrayList<>(playerRoles.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                restorePlayerState(player);
                player.sendMessage(ChatColor.RED + "游戏已结束，你的状态已恢复！");
            }
        }
        
        // 清理所有数据
        playerRoles.clear();
        savedStates.clear();
        infectionCounts.clear();
        survivalTimes.clear();
        roleStartTimes.clear();
        ghostAccumulatedTimes.clear();
        convertedByRedemption.clear();
        
        // 清理玩家数据完成
        plugin.getLogger().info("清理玩家数据完成");
    }
    
    // 玩家状态存储类
    private static class PlayerState {
        private final GameMode gameMode;
        private final Location location;
        private final ItemStack[] inventory;
        private final ItemStack[] armor;
        private final double health;
        private final int foodLevel;
        private final float saturation;
        private final int level;
        private final float exp;
        private final boolean allowFlight;
        private final boolean flying;
        
        public PlayerState(Player player) {
            this.gameMode = player.getGameMode();
            this.location = player.getLocation();
            this.inventory = player.getInventory().getContents();
            this.armor = player.getInventory().getArmorContents();
            // 安全地保存生命值，考虑当前服务器的限制
            double currentHealth = player.getHealth();
            double maxHealth = player.getMaxHealth();
            this.health = Math.min(currentHealth, maxHealth);
            
            this.foodLevel = player.getFoodLevel();
            this.saturation = player.getSaturation();
            this.level = player.getLevel();
            this.exp = player.getExp();
            this.allowFlight = player.getAllowFlight();
            this.flying = player.isFlying();
        }
        
        public void restore(Player player, Gost plugin) {
            player.setGameMode(gameMode);
            player.teleport(location);
            player.getInventory().setContents(inventory);
            player.getInventory().setArmorContents(armor);
            
            // 安全地设置生命值
            try {
                double maxHealth = player.getMaxHealth();
                double safeHealth = Math.min(health, maxHealth);
                player.setHealth(safeHealth);
            } catch (IllegalArgumentException e) {
                // 如果设置失败，使用默认值
                plugin.getLogger().warning("无法设置玩家 " + player.getName() + " 的生命值: " + e.getMessage());
                player.setHealth(player.getMaxHealth());
            }
            
            player.setFoodLevel(foodLevel);
            player.setSaturation(saturation);
            player.setLevel(level);
            player.setExp(exp);
            player.setAllowFlight(allowFlight);
            player.setFlying(flying);
        }
    }
    
    // 检查玩家是否已达到最大道具种类数量
    public boolean hasReachedMaxItemTypes(Player player) {
        int maxItemTypes = plugin.getConfigManager().getMaxItemTypesPerPlayer();
        Set<String> itemTypes = new HashSet<>();
        
        // 检查玩家背包中的道具
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    
                    // 检查是否是自定义道具
                    if (displayName.contains("臭牛排") || 
                        displayName.contains("传送珍珠") || 
                        displayName.contains("灵魂探测器") ||
                        displayName.contains("一次机会")) {
                        
                        // 获取道具类型（去除颜色代码）
                        String itemType = ChatColor.stripColor(displayName);
                        itemTypes.add(itemType);
                    }
                }
            }
        }
        
        return itemTypes.size() >= maxItemTypes;
    }
    
    // 获取玩家当前拥有的道具种类数量
    public int getPlayerItemTypesCount(Player player) {
        Set<String> itemTypes = new HashSet<>();
        
        // 检查玩家背包中的道具
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    
                    // 检查是否是自定义道具
                    if (displayName.contains("臭牛排") || 
                        displayName.contains("传送珍珠") || 
                        displayName.contains("灵魂探测器") ||
                        displayName.contains("一次机会")) {
                        
                        // 获取道具类型（去除颜色代码）
                        String itemType = ChatColor.stripColor(displayName);
                        itemTypes.add(itemType);
                    }
                }
            }
        }
        
        return itemTypes.size();
    }
}