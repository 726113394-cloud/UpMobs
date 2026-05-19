package io.Sriptirc_wp_1258.upmobs.challengetower;

import io.Sriptirc_wp_1258.upmobs.Upmobs;
import io.Sriptirc_wp_1258.upmobs.MobManager;
import io.Sriptirc_wp_1258.upmobs.EvolutionManager;
import io.Sriptirc_wp_1258.upmobs.EconomyRewardManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 通天层实例
 * 管理单个通天层的运行状态
 */
public class SkyTowerInstance {
    
    private final Upmobs plugin;
    private final ChallengeTower tower;
    private final SkyTowerConfig config;
    private final MobManager mobManager;
    private final EvolutionManager evolutionManager;
    private final EconomyRewardManager economyManager;
    private final ChallengeTowerMonsterEnhancer monsterEnhancer;
    
    // 玩家管理
    private final UUID playerId;
    private final String playerName;
    private Location entryLocation;
    
    // 游戏状态
    private boolean isActive = false;
    private boolean isCompleted = false;
    private long startTime;
    private long elapsedTime; // 已过时间（毫秒）
    private int currentMonsterCount = 0;
    private int totalMonstersKilled = 0;
    private int milestoneReached = 0;
    private int currentWave = 1;            // 当前波次（从1开始，越高越难）
    private int killsInCurrentWave = 0;     // 当前波次已击杀数
    
    // 怪物管理
    private final List<UUID> spawnedMonsters = new ArrayList<>();
    private final Map<UUID, String> monsterTypes = new HashMap<>();
    
    // 任务管理
    private BukkitTask mainTask;
    private BukkitTask monsterCheckTask;
    private BukkitTask statusUpdateTask;
    
    // 统计
    private double totalRewards = 0.0;
    
    public SkyTowerInstance(Upmobs plugin, ChallengeTower tower, Player player) {
        this.plugin = plugin;
        this.tower = tower;
        this.config = tower.getSkyTowerConfig();
        this.mobManager = plugin.getMobManager();
        this.evolutionManager = plugin.getEvolutionManager();
        this.economyManager = plugin.getEconomyRewardManager();
        this.monsterEnhancer = new ChallengeTowerMonsterEnhancer(tower.getConfig(), mobManager, evolutionManager);
        
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.entryLocation = player.getLocation().clone();
    }
    
    /**
     * 开始通天层挑战
     */
    public boolean startChallenge() {
        if (isActive) {
            return false;
        }
        
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // 检查是否满足解锁要求
        if (!checkUnlockRequirement(player)) {
            player.sendMessage(ChatColor.RED + "§c你尚未解锁通天层！需要先通关挑战塔所有18层。");
            return false;
        }
        
        // 保存进入位置
        entryLocation = player.getLocation().clone();
        
        // 广播开始消息
        broadcastMessage(ChatColor.GOLD + "§l=== 通天层挑战开始 ===");
        broadcastMessage(ChatColor.YELLOW + "玩家: " + ChatColor.WHITE + playerName);
        broadcastMessage(ChatColor.YELLOW + "时间限制: " + ChatColor.WHITE + config.getTimeLimitMinutes() + "分钟");
        broadcastMessage(ChatColor.YELLOW + "怪物数量: " + ChatColor.WHITE + config.getBaseMonsterCount() + "只第四阶段升格怪物");
        broadcastMessage(ChatColor.YELLOW + "难度: " + ChatColor.WHITE + "随时间逐渐增强");
        broadcastMessage(ChatColor.YELLOW + "奖励: " + ChatColor.WHITE + "根据击杀数量和存活时间计算");
        
        // 传送玩家到通天层区域
        teleportPlayerToSkyTower(player);
        
        // 设置活动状态
        isActive = true;
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        
        // 生成初始怪物
        spawnInitialMonsters();
        
        // 开始主任务（计时和难度更新）
        startMainTask();
        
        // 开始怪物检查任务
        startMonsterCheckTask();
        
        // 开始状态更新任务
        if (config.isEnablePlayerStatusDisplay()) {
            startStatusUpdateTask();
        }
        
        return true;
    }
    
    /**
     * 检查解锁要求
     */
    private boolean checkUnlockRequirement(Player player) {
        String requirement = config.getUnlockRequirement();
        
        if ("complete_tower".equals(requirement)) {
            // 检查是否通关挑战塔所有18层
            ChallengeTowerPlayerProgress progress = tower.getManager().getPlayerProgress(player.getUniqueId());
            return progress != null && progress.getCurrentLevel() >= 19; // 19表示已通关18层
        }
        
        // 默认返回true（用于测试）
        return true;
    }
    
    /**
     * 传送玩家到通天层区域
     */
    private void teleportPlayerToSkyTower(Player player) {
        ChallengeTowerArea area = tower.getArea();
        if (area != null) {
            Location center = area.getCenter();
            player.teleport(center);
            player.sendMessage(ChatColor.GREEN + "§a已传送至通天层挑战区域！");
        }
    }
    
    /**
     * 生成初始怪物
     */
    private void spawnInitialMonsters() {
        int count = config.getBaseMonsterCount();
        spawnMonsters(count);
        currentMonsterCount = count;
    }
    
    /**
     * 生成怪物
     */
    private void spawnMonsters(int count) {
        Random random = new Random();
        ChallengeTowerArea area = tower.getArea();
        
        // 根据波次决定怪物阶段（波次越高怪物越强）
        // 波次1~3：普通升格怪物
        // 波次4~6：进化怪物（第一阶段）
        // 波次7~9：高阶段进化怪物（第二~三阶段）
        // 波次10+：混合出怪，第四阶段占20%，其余为高阶段进化怪物
        int monsterStage;
        if (currentWave <= 3) {
            monsterStage = 0; // 普通升格
        } else if (currentWave <= 6) {
            monsterStage = 1; // 第一阶段进化
        } else if (currentWave <= 9) {
            monsterStage = 2 + random.nextInt(2); // 第二或第三阶段
        } else {
            // 波次10+：20%概率出第四阶段，80%出高阶段进化
            if (random.nextDouble() < 0.2) {
                monsterStage = 4; // 第四阶段（低概率）
            } else {
                monsterStage = 2 + random.nextInt(2); // 第二或第三阶段
            }
        }
        
        for (int i = 0; i < count; i++) {
            // 获取随机位置（在区域内部的地面上）
            Location spawnLocation = area.getRandomGroundLocation();
            
            // 获取随机怪物类型（从配置中获取）
            List<String> monsterTypeNames = config.getMonsterTypes();
            String monsterTypeName = monsterTypeNames.get(random.nextInt(monsterTypeNames.size()));
            
            LivingEntity monster;
            
            // 根据怪物阶段生成不同强度的怪物
            if (monsterStage == 0) {
                // 普通升格怪物（原版属性，有进化资格）
                monster = createNormalMonster(spawnLocation, monsterTypeName);
                if (monster != null) {
                    monster.addScoreboardTag("upgraded_mob");
                    applySkyTowerEnhancements(monster);
                }
            } else if (monsterStage == 4) {
                // 第四阶段怪物（最强）
                monster = createStage4Monster(spawnLocation, monsterTypeName);
                if (monster != null) {
                    applySkyTowerEnhancements(monster);
                }
            } else {
                // 进化怪物（指定阶段）
                monster = createStageMonster(spawnLocation, monsterTypeName, monsterStage);
                if (monster != null) {
                    applySkyTowerEnhancements(monster);
                }
            }
            
            if (monster == null) continue;
            
            // 添加通天层标签
            monster.addScoreboardTag("sky_tower_monster");
            monster.addScoreboardTag("wave_" + currentWave);
            
            // 设置目标为玩家
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && monster instanceof Mob) {
                ((Mob) monster).setTarget(player);
            }
            
            // 记录怪物
            spawnedMonsters.add(monster.getUniqueId());
            monsterTypes.put(monster.getUniqueId(), monsterTypeName);
            
            // 播放生成效果（根据阶段不同）
            if (monsterStage >= 4) {
                spawnLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, spawnLocation, 20, 0.5, 0.5, 0.5, 0);
                spawnLocation.getWorld().playSound(spawnLocation, Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.8f);
            } else {
                spawnLocation.getWorld().spawnParticle(Particle.SMOKE, spawnLocation, 10, 0.5, 0.5, 0.5, 0);
                spawnLocation.getWorld().playSound(spawnLocation, Sound.ENTITY_ZOMBIE_AMBIENT, 0.5f, 1.0f);
            }
        }
    }
    
    /**
     * 创建普通怪物（原版属性）
     */
    private LivingEntity createNormalMonster(Location location, String monsterTypeName) {
        String baseTypeName = monsterTypeName.replace("STAGE4_", "");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(baseTypeName);
        } catch (Exception e) {
            return null;
        }
        
        return (LivingEntity) location.getWorld().spawnEntity(location, entityType);
    }
    
    /**
     * 创建指定阶段的进化怪物
     */
    private LivingEntity createStageMonster(Location location, String monsterTypeName, int stage) {
        String baseTypeName = monsterTypeName.replace("STAGE4_", "");
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(baseTypeName);
        } catch (Exception e) {
            return null;
        }
        
        LivingEntity monster = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        if (monster == null) return null;
        
        // 标记为升格怪物
        monster.addScoreboardTag("upgraded_mob");
        
        // 应用进化效果
        if (plugin.getEvolutionEffects() != null) {
            plugin.getEvolutionEffects().applyEvolutionEffects(monster, stage);
        }
        
        return monster;
    }
    
    /**
     * 创建第四阶段怪物
     */
    private LivingEntity createStage4Monster(Location location, String monsterTypeName) {
        // 解析怪物类型名称
        EntityType entityType = null;
        
        try {
            // 移除STAGE4_前缀
            String baseTypeName = monsterTypeName.replace("STAGE4_", "");
            entityType = EntityType.valueOf(baseTypeName);
        } catch (IllegalArgumentException e) {
            // 如果解析失败，使用默认的僵尸
            entityType = EntityType.ZOMBIE;
        }
        
        // 生成怪物
        LivingEntity monster = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        
        // 应用第四阶段增强
        if (mobManager != null) {
            // 先应用升格
            mobManager.upgradeMob(monster);
            
            // 添加第四阶段标签
            monster.addScoreboardTag("stage4");
            
            // 应用第四阶段属性增强
            org.bukkit.attribute.AttributeInstance maxHealth = monster.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(maxHealth.getBaseValue() * 4.0); // 第四阶段4倍血量
                monster.setHealth(maxHealth.getValue());
            }
            
            org.bukkit.attribute.AttributeInstance attackDamage = monster.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(attackDamage.getBaseValue() * 3.0); // 第四阶段3倍伤害
            }
            
            // 添加发光效果
            monster.setGlowing(true);
        }
        
        return monster;
    }
    
    /**
     * 应用通天层增强效果
     */
    private void applySkyTowerEnhancements(LivingEntity monster) {
        // 计算当前难度倍率
        int elapsedMinutes = (int) (elapsedTime / (60 * 1000));
        double difficultyMultiplier = config.getDifficultyMultiplierForTime(elapsedMinutes);
        
        // 应用难度增强
        org.bukkit.attribute.AttributeInstance maxHealth = monster.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * difficultyMultiplier);
            monster.setHealth(maxHealth.getValue());
        }
        
        org.bukkit.attribute.AttributeInstance attackDamage = monster.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(attackDamage.getBaseValue() * difficultyMultiplier);
        }
    }
    
    /**
     * 开始主任务
     */
    private void startMainTask() {
        mainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                // 更新已过时间
                elapsedTime = System.currentTimeMillis() - startTime;
                
                // 检查时间限制
                checkTimeLimit();
                
                // 检查里程碑
                checkMilestones();
                
                // 显示倒计时（如果启用）
                if (config.isShowTimer()) {
                    showTimer();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    /**
     * 开始怪物检查任务
     */
    private void startMonsterCheckTask() {
        monsterCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                // 检查并清理死亡怪物
                checkDeadMonsters();
                
                // 保持怪物数量
                maintainMonsterCount();
            }
        }.runTaskTimer(plugin, 0L, 40L); // 每2秒执行一次
    }
    
    /**
     * 开始状态更新任务
     */
    private void startStatusUpdateTask() {
        statusUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    cancel();
                    return;
                }
                
                // 更新玩家状态显示
                updatePlayerStatus();
            }
        }.runTaskTimer(plugin, 0L, config.getStatusUpdateIntervalSeconds() * 20L);
    }
    
    /**
     * 检查时间限制
     */
    private void checkTimeLimit() {
        long timeLimitMillis = config.getTimeLimitMinutes() * 60 * 1000L;
        
        if (elapsedTime >= timeLimitMillis) {
            // 时间到，完成挑战
            completeChallenge(true);
        } else if (elapsedTime >= timeLimitMillis - (config.getWarningTimeSeconds() * 1000L)) {
            // 警告时间
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                long remainingSeconds = (timeLimitMillis - elapsedTime) / 1000;
                if (remainingSeconds % 10 == 0) { // 每10秒显示一次
                    player.sendTitle(ChatColor.RED + "剩余时间", ChatColor.YELLOW + String.valueOf(remainingSeconds) + "秒", 0, 20, 0);
                }
            }
        }
    }
    

    
    /**
     * 检查里程碑
     */
    private void checkMilestones() {
        int elapsedMinutes = (int) (elapsedTime / (60 * 1000));
        int newMilestone = elapsedMinutes / config.getMilestoneIntervalMinutes();
        
        if (newMilestone > milestoneReached) {
            milestoneReached = newMilestone;
            awardMilestoneBonus();
        }
    }
    
    /**
     * 奖励里程碑
     */
    private void awardMilestoneBonus() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        
        double bonus = config.getMilestoneBonus() * milestoneReached;
        totalRewards += bonus;
        
        // 发送奖励消息
        player.sendMessage(ChatColor.GOLD + "§l=== 里程碑达成 ===");
        player.sendMessage(ChatColor.YELLOW + "存活时间: " + ChatColor.WHITE + (milestoneReached * config.getMilestoneIntervalMinutes()) + "分钟");
        player.sendMessage(ChatColor.YELLOW + "里程碑奖励: " + ChatColor.GREEN + String.format("%.1f", bonus) + " 游戏币");
        
        // 播放庆祝效果
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 30, 0.5, 1, 0.5, 0);
    }
    
    /**
     * 显示倒计时
     */
    private void showTimer() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        
        long remainingMillis = (config.getTimeLimitMinutes() * 60 * 1000L) - elapsedTime;
        long remainingMinutes = remainingMillis / (60 * 1000);
        long remainingSeconds = (remainingMillis % (60 * 1000)) / 1000;
        
        // 在ActionBar显示倒计时
        String timeText = String.format("§e剩余时间: §f%02d:%02d", remainingMinutes, remainingSeconds);
        player.sendActionBar(timeText);
    }
    
    /**
     * 检查并清理死亡怪物
     */
    private void checkDeadMonsters() {
        Iterator<UUID> iterator = spawnedMonsters.iterator();
        while (iterator.hasNext()) {
            UUID monsterId = iterator.next();
            Entity entity = Bukkit.getEntity(monsterId);
            
            if (entity == null || entity.isDead()) {
                iterator.remove();
                monsterTypes.remove(monsterId);
                currentMonsterCount--;
                totalMonstersKilled++;
                killsInCurrentWave++;
                
                // 每击杀10只怪物进入下一波
                if (killsInCurrentWave >= 10) {
                    currentWave++;
                    killsInCurrentWave = 0;
                    broadcastMessage(ChatColor.GOLD + "§l⚔ 进入第 " + currentWave + " 波！怪物强度提升！");
                }
                
                // 奖励击杀
                awardKillReward();
                
                // 如果启用刷新，安排刷新怪物
                if (config.isRefreshOnDeath()) {
                    scheduleMonsterRefresh();
                }
            }
        }
    }
    
    /**
     * 保持怪物数量
     */
    private void maintainMonsterCount() {
        int targetCount = config.getBaseMonsterCount();
        
        if (currentMonsterCount < targetCount) {
            int toSpawn = targetCount - currentMonsterCount;
            spawnMonsters(toSpawn);
            currentMonsterCount += toSpawn;
        }
    }
    
    /**
     * 安排怪物刷新
     */
    private void scheduleMonsterRefresh() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isActive) {
                    spawnMonsters(1);
                    currentMonsterCount++;
                }
            }
        }.runTaskLater(plugin, config.getRefreshDelaySeconds() * 20L);
    }
    
    /**
     * 奖励击杀
     */
    private void awardKillReward() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        
        int elapsedMinutes = (int) (elapsedTime / (60 * 1000));
        double rewardMultiplier = config.getRewardMultiplierForTime(elapsedMinutes);
        double reward = config.getBaseRewardPerKill() * rewardMultiplier;
        
        totalRewards += reward;
        
        // 发送经济奖励
        if (economyManager != null && economyManager.isEnabled()) {
            economyManager.giveReward(player, reward, "通天层击杀");
        }
        
        // 发送击杀消息（每10只显示一次）
        if (totalMonstersKilled % 10 == 0) {
            player.sendMessage(ChatColor.GREEN + "§l击杀统计: " + ChatColor.WHITE + totalMonstersKilled + "只怪物");
            player.sendMessage(ChatColor.GREEN + "§l累计奖励: " + ChatColor.WHITE + String.format("%.1f", totalRewards) + " 游戏币");
        }
    }
    
    /**
     * 更新玩家状态显示
     */
    private void updatePlayerStatus() {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        
        // 在副标题显示状态
        String status = String.format("§e怪物: §f%d/%d §7| §e击杀: §f%d §7| §e奖励: §f%.1f",
            currentMonsterCount, config.getBaseMonsterCount(),
            totalMonstersKilled, totalRewards);
        
        player.sendTitle("", status, 0, 40, 0);
    }
    
    /**
     * 广播消息
     */
    private void broadcastMessage(String message) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage(message);
        }
    }
    
    /**
     * 完成挑战
     */
    public void completeChallenge(boolean success) {
        if (!isActive) return;
        
        isActive = false;
        isCompleted = true;
        
        // 取消所有任务
        if (mainTask != null) mainTask.cancel();
        if (monsterCheckTask != null) monsterCheckTask.cancel();
        if (statusUpdateTask != null) statusUpdateTask.cancel();
        
        // 清理所有怪物
        cleanupMonsters();
        
        // 传送玩家回入口位置
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.teleport(entryLocation);
            
            // 发送完成消息
            if (success) {
                player.sendMessage(ChatColor.GOLD + "§l=== 通天层挑战完成 ===");
                player.sendMessage(ChatColor.YELLOW + "存活时间: " + ChatColor.WHITE + String.format("%.1f", elapsedTime / 60000.0) + "分钟");
                player.sendMessage(ChatColor.YELLOW + "击杀怪物: " + ChatColor.WHITE + totalMonstersKilled + "只");
                player.sendMessage(ChatColor.YELLOW + "总奖励: " + ChatColor.GREEN + String.format("%.1f", totalRewards) + " 游戏币");
                player.sendMessage(ChatColor.GREEN + "§a恭喜你成功完成通天层挑战！");
                
                // 播放完成音效
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } else {
                player.sendMessage(ChatColor.RED + "§l=== 通天层挑战失败 ===");
                player.sendMessage(ChatColor.YELLOW + "存活时间: " + ChatColor.WHITE + String.format("%.1f", elapsedTime / 60000.0) + "分钟");
                player.sendMessage(ChatColor.YELLOW + "击杀怪物: " + ChatColor.WHITE + totalMonstersKilled + "只");
                player.sendMessage(ChatColor.YELLOW + "获得奖励: " + ChatColor.GREEN + String.format("%.1f", totalRewards) + " 游戏币");
                player.sendMessage(ChatColor.RED + "§c不要气馁，下次再来挑战！");
            }
            
            // 保存排行榜数据
            if (config.isEnableLeaderboard() && config.isSavePlayerStats()) {
                saveLeaderboardData();
            }
        }
        
        // 通知挑战塔管理器
        tower.getManager().onSkyTowerComplete(this);
    }
    
    /**
     * 清理所有怪物
     */
    private void cleanupMonsters() {
        for (UUID monsterId : spawnedMonsters) {
            Entity entity = Bukkit.getEntity(monsterId);
            if (entity != null) {
                entity.remove();
            }
        }
        spawnedMonsters.clear();
        monsterTypes.clear();
    }
    
    /**
     * 保存排行榜数据
     */
    private void saveLeaderboardData() {
        // 这里可以保存到文件或数据库
        // 暂时只打印日志
        plugin.getLogger().info(String.format(
            "通天层排行榜数据: 玩家=%s, 时间=%.1f分钟, 击杀=%d, 奖励=%.1f",
            playerName, elapsedTime / 60000.0, totalMonstersKilled, totalRewards
        ));
    }
    
    /**
     * 处理玩家死亡
     */
    public void onPlayerDeath() {
        completeChallenge(false);
    }
    
    /**
     * 检查玩家是否在线
     */
    public boolean isPlayerOnline() {
        Player player = Bukkit.getPlayer(playerId);
        return player != null && player.isOnline();
    }
    
    /**
     * 强制结束挑战
     */
    public void forceEnd() {
        completeChallenge(false);
    }
    
    // ========== Getter方法 ==========
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public long getElapsedTime() {
        return elapsedTime;
    }
    
    public int getTotalMonstersKilled() {
        return totalMonstersKilled;
    }
    
    public double getTotalRewards() {
        return totalRewards;
    }
    
    public int getMilestoneReached() {
        return milestoneReached;
    }
}