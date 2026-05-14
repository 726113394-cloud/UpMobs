package io.Sriptirc_wp_1258.upmobs.challengetower;

import io.Sriptirc_wp_1258.upmobs.Upmobs;
import io.Sriptirc_wp_1258.upmobs.MobManager;
import io.Sriptirc_wp_1258.upmobs.EconomyRewardManager;
import io.Sriptirc_wp_1258.upmobs.EvolutionManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * 挑战塔实例
 * 管理单个挑战塔的运行状态
 */
public class ChallengeTowerInstance {
    
    private final Upmobs plugin;
    private final ChallengeTower tower;
    private final ChallengeTowerConfig config;
    private final MobManager mobManager;
    private final EvolutionManager evolutionManager;
    private final ChallengeTowerMonsterEnhancer monsterEnhancer;
    
    // 玩家管理
    private final List<UUID> playerIds = new ArrayList<>();
    private final Map<UUID, Location> entryLocations = new HashMap<>();
    private final Map<UUID, ChallengeTowerPlayerProgress> playerProgress = new HashMap<>();
    
    // 游戏状态
    private boolean isActive = false;
    private boolean isPaused = false;
    private int currentLevel = 1;
    private int currentWave = 1;
    private int monstersRemaining = 0;
    private int totalMonstersSpawned = 0;
    
    // 怪物管理
    private final List<UUID> spawnedMonsters = new ArrayList<>();
    private final Map<UUID, String> monsterTypes = new HashMap<>();
    
    // 任务管理
    private BukkitTask waveTask;
    private BukkitTask checkTask;
    private BukkitTask teleportTask;
    
    // 时间管理
    private long startTime;
    private long waveStartTime;
    
    // 挑战塔管理器引用
    private ChallengeTowerManager challengeTowerManager;
    
    public ChallengeTowerInstance(Upmobs plugin, ChallengeTower tower) {
        this.plugin = plugin;
        this.tower = tower;
        this.config = tower.getConfig();
        this.mobManager = plugin.getMobManager();
        this.evolutionManager = plugin.getEvolutionManager();
        this.monsterEnhancer = new ChallengeTowerMonsterEnhancer(config, mobManager, evolutionManager);
    }
    
    /**
     * 设置挑战塔管理器引用
     */
    public void setChallengeTowerManager(ChallengeTowerManager manager) {
        this.challengeTowerManager = manager;
    }
    
    /**
     * 开始挑战
     */
    public boolean startChallenge(List<Player> players) {
        if (isActive) {
            return false;
        }
        
        if (players.isEmpty()) {
            return false;
        }
        
        // 保存玩家进入位置
        for (Player player : players) {
            playerIds.add(player.getUniqueId());
            entryLocations.put(player.getUniqueId(), player.getLocation().clone());
            
            // 获取或创建玩家进度
            ChallengeTowerPlayerProgress progress = tower.getManager().getPlayerProgress(player.getUniqueId());
            playerProgress.put(player.getUniqueId(), progress);
            
            // 设置最低层数（多人挑战时以最低层级的玩家为基准）
            if (currentLevel == 1) {
                currentLevel = progress.getCurrentLevel();
            } else {
                currentLevel = Math.min(currentLevel, progress.getCurrentLevel());
            }
        }
        
        // 广播开始消息
        broadcastMessage(ChatColor.GOLD + "§l=== 挑战塔开始 ===");
        broadcastMessage(ChatColor.YELLOW + "挑战塔: " + ChatColor.WHITE + tower.getName());
        broadcastMessage(ChatColor.YELLOW + "玩家数量: " + ChatColor.WHITE + players.size());
        broadcastMessage(ChatColor.YELLOW + "起始层数: " + ChatColor.WHITE + currentLevel);
        
        // 传送玩家到挑战塔区域
        teleportPlayersToTower();
        
        // 设置活动状态
        isActive = true;
        startTime = System.currentTimeMillis();
        
        // 开始第一波
        startNextWave();
        
        // 启动检查任务
        startCheckTask();
        
        return true;
    }
    
    /**
     * 传送玩家到挑战塔
     */
    private void teleportPlayersToTower() {
        Location spawnLocation = tower.getArea().getSafeSpawnLocation();
        
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.teleport(spawnLocation);
                player.sendMessage(ChatColor.GREEN + "已传送至挑战塔区域");
                
                // 播放传送效果
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0);
            }
        }
    }
    
    /**
     * 开始下一波怪物
     */
    private void startNextWave() {
        if (!isActive || isPaused) {
            return;
        }
        
        // 取消之前的波次任务
        if (waveTask != null) {
            waveTask.cancel();
        }
        
        // 计算怪物数量
        int monsterCount = config.getMonsterCountForLevel(currentLevel, playerIds.size());
        monstersRemaining = monsterCount;
        totalMonstersSpawned = 0;
        
        // 广播波次信息
        broadcastMessage(ChatColor.GOLD + "§l=== 第" + currentLevel + "层 第" + currentWave + "波 ===");
        broadcastMessage(ChatColor.YELLOW + "怪物数量: " + ChatColor.WHITE + monsterCount);
        
        // 显示层数增强信息（只在第一波显示）
        if (currentWave == 1 && monsterEnhancer != null) {
            String enhancementInfo = monsterEnhancer.getLevelEnhancementInfo(currentLevel);
            for (String line : enhancementInfo.split("\n")) {
                broadcastMessage(line);
            }
        }
        
        broadcastMessage(ChatColor.YELLOW + "倒计时: " + ChatColor.WHITE + "5秒后开始");
        
        // 播放准备音效
        playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        
        // 5秒后开始生成怪物
        waveTask = new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    // 倒计时
                    broadcastMessage(ChatColor.YELLOW + "准备开始: " + ChatColor.WHITE + countdown + "秒");
                    playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                    countdown--;
                } else {
                    // 开始生成怪物
                    this.cancel();
                    startMonsterSpawning();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒一次
        
        waveStartTime = System.currentTimeMillis();
    }
    
    /**
     * 开始生成怪物
     */
    private void startMonsterSpawning() {
        if (!isActive || isPaused) {
            return;
        }
        
        broadcastMessage(ChatColor.RED + "§l怪物来袭！");
        playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 0.8f);
        
        // 分批生成怪物（避免一次性生成太多造成卡顿）
        int batchSize = Math.min(5, monstersRemaining);
        int delayBetweenBatches = 20; // 1秒
        
        new BukkitRunnable() {
            int batchesSpawned = 0;
            
            @Override
            public void run() {
                if (!isActive || isPaused || monstersRemaining <= 0) {
                    this.cancel();
                    return;
                }
                
                // 生成一批怪物
                int toSpawn = Math.min(batchSize, monstersRemaining);
                spawnMonsters(toSpawn);
                
                batchesSpawned++;
                monstersRemaining -= toSpawn;
                totalMonstersSpawned += toSpawn;
                
                // 如果还有怪物需要生成，继续下一批
                if (monstersRemaining > 0) {
                    // 继续下一批
                } else {
                    // 所有怪物都已生成
                    this.cancel();
                    broadcastMessage(ChatColor.GREEN + "所有怪物已生成，开始战斗！");
                }
            }
        }.runTaskTimer(plugin, 0L, delayBetweenBatches);
    }
    
    /**
     * 生成怪物
     */
    private void spawnMonsters(int count) {
        Random random = new Random();
        ChallengeTowerArea area = tower.getArea();
        
        for (int i = 0; i < count; i++) {
            // 获取随机位置（在区域内部的地面上）
            Location spawnLocation = area.getRandomGroundLocation();
            
            // 获取随机怪物类型
            EntityType monsterType = config.getRandomMonsterType(random);
            
            // 生成怪物
            LivingEntity monster = (LivingEntity) spawnLocation.getWorld().spawnEntity(spawnLocation, monsterType);
            
            // 应用逐层加强效果
            if (monsterEnhancer != null) {
                monsterEnhancer.enhanceMonster(monster, currentLevel);
            }
            
            // 添加挑战塔标签
            monster.addScoreboardTag("challenge_tower_monster");
            monster.addScoreboardTag("tower_" + tower.getId());
            monster.addScoreboardTag("level_" + currentLevel);
            monster.addScoreboardTag("wave_" + currentWave);
            
            // 设置目标为最近的玩家
            Player nearestPlayer = getNearestPlayer(spawnLocation);
            if (nearestPlayer != null && monster instanceof Mob) {
                ((Mob) monster).setTarget(nearestPlayer);
            }
            
            // 记录怪物
            spawnedMonsters.add(monster.getUniqueId());
            monsterTypes.put(monster.getUniqueId(), monsterType.name());
            
            // 播放生成效果
            spawnLocation.getWorld().spawnParticle(Particle.SMOKE, spawnLocation, 10, 0.5, 0.5, 0.5, 0);
            spawnLocation.getWorld().playSound(spawnLocation, Sound.ENTITY_ZOMBIE_AMBIENT, 0.5f, 1.0f);
        }
    }
    
    /**
     * 获取最近玩家
     */
    private Player getNearestPlayer(Location location) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline() && player.getWorld().equals(location.getWorld())) {
                double distance = player.getLocation().distance(location);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = player;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * 怪物死亡处理
     */
    public void onMonsterDeath(LivingEntity monster) {
        if (!spawnedMonsters.contains(monster.getUniqueId())) {
            return;
        }
        
        // 从列表中移除
        spawnedMonsters.remove(monster.getUniqueId());
        String monsterType = monsterTypes.remove(monster.getUniqueId());
        
        // 更新玩家进度
        Player killer = monster.getKiller();
        if (killer != null) {
            Player player = killer;
            ChallengeTowerPlayerProgress progress = playerProgress.get(player.getUniqueId());
            if (progress != null) {
                progress.addMonsterKill(monsterType != null ? monsterType : "UNKNOWN");
            }
        }
        
        // 检查是否所有怪物都已死亡
        checkWaveCompletion();
    }
    
    /**
     * 检查波次完成
     */
    private void checkWaveCompletion() {
        if (spawnedMonsters.isEmpty() && monstersRemaining == 0) {
            // 所有怪物都已死亡或生成
            completeWave();
        }
    }
    
    /**
     * 完成一波
     */
    private void completeWave() {
        broadcastMessage(ChatColor.GREEN + "§l✓ 第" + currentWave + "波完成！");
        playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // 更新玩家进度
        for (ChallengeTowerPlayerProgress progress : playerProgress.values()) {
            progress.completeWave();
        }
        
        // 检查是否完成当前层
        if (currentWave >= config.getWavesPerLevel()) {
            completeLevel();
        } else {
            // 进入下一波
            currentWave++;
            broadcastMessage(ChatColor.YELLOW + "准备第" + currentWave + "波，10秒后开始...");
            
            // 10秒后开始下一波
            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextWave();
                }
            }.runTaskLater(plugin, 200L); // 10秒
        }
    }
    
    /**
     * 完成一层
     */
    private void completeLevel() {
        broadcastMessage(ChatColor.GOLD + "§l🎉 第" + currentLevel + "层完成！");
        playSound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        
        // 给予奖励
        giveLevelRewards();
        
        // 更新玩家进度
        for (ChallengeTowerPlayerProgress progress : playerProgress.values()) {
            progress.completeLevel();
        }
        
        // 检查是否完成所有层
        if (currentLevel >= config.getMaxLevels()) {
            completeChallenge();
        } else {
            // 进入下一层
            currentLevel++;
            currentWave = 1;
            
            broadcastMessage(ChatColor.YELLOW + "准备进入第" + currentLevel + "层，15秒后开始...");
            
            // 15秒后开始下一层
            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextWave();
                }
            }.runTaskLater(plugin, 300L); // 15秒
        }
    }
    
    /**
     * 给予层数奖励
     */
    private void giveLevelRewards() {
        // 经验奖励
        int experience = config.getExperienceForLevel(currentLevel);
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.giveExp(experience);
                
                ChallengeTowerPlayerProgress progress = playerProgress.get(playerId);
                if (progress != null) {
                    progress.addExperience(experience);
                }
            }
        }
        
        broadcastMessage(ChatColor.YELLOW + "经验奖励: " + ChatColor.WHITE + experience + "点");
        
        // 通关奖金
        double completionReward = config.getCompletionRewardForLevel(currentLevel);
        if (completionReward > 0) {
            // 检查玩家是否首次达到该层
            for (UUID playerId : playerIds) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    ChallengeTowerPlayerProgress progress = playerProgress.get(playerId);
                    if (progress != null) {
                        // 检查玩家是否首次达到该层
                        if (currentLevel > progress.getHighestLevel()) {
                            // 首次达到该层，发放奖金
                            giveCompletionReward(player, completionReward, currentLevel);
                        }
                    }
                }
            }
        }
        
        // 物品奖励
        List<ItemStack> rewards = config.getRewardsForLevel(currentLevel);
        if (!rewards.isEmpty()) {
            for (UUID playerId : playerIds) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    for (ItemStack reward : rewards) {
                        player.getWorld().dropItemNaturally(player.getLocation(), reward.clone());
                    }
                }
            }
            
            broadcastMessage(ChatColor.YELLOW + "已发放物品奖励");
        }
    }
    
    /**
     * 发放通关奖金
     */
    private void giveCompletionReward(Player player, double reward, int level) {
        // 获取经济奖励管理器
        EconomyRewardManager economyManager = plugin.getEconomyRewardManager();
        if (economyManager != null && economyManager.isEnabled()) {
            // 发放奖金
            economyManager.giveReward(player, reward, "挑战塔第" + level + "层通关奖金");
            
            // 发送消息
            String tierName = getTierNameForLevel(level);
            player.sendMessage(ChatColor.GOLD + "§l=== 通关奖金 ===");
            player.sendMessage(ChatColor.YELLOW + "层数: " + ChatColor.WHITE + "第" + level + "层 (" + tierName + ")");
            player.sendMessage(ChatColor.YELLOW + "奖金: " + ChatColor.GREEN + String.format("%.1f", reward) + " 游戏币");
            player.sendMessage(ChatColor.GREEN + "§a恭喜首次通关该层！");
            
            // 播放音效
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.YELLOW + "通关奖金: " + ChatColor.GREEN + String.format("%.1f", reward) + " 游戏币");
            player.sendMessage(ChatColor.RED + "§c注意：经济系统未启用，奖金未实际发放");
        }
    }
    
    /**
     * 获取层数对应的层级名称
     */
    private String getTierNameForLevel(int level) {
        if (level >= 1 && level <= 5) {
            return "基础层";
        } else if (level >= 6 && level <= 10) {
            return "进阶层";
        } else if (level >= 11 && level <= 15) {
            return "精英层";
        } else if (level >= 16 && level <= 18) {
            return "大师层";
        }
        return "未知层";
    }
    
    /**
     * 完成整个挑战
     */
    private void completeChallenge() {
        broadcastMessage(ChatColor.GOLD + "§l🎊 挑战塔完成！");
        broadcastMessage(ChatColor.YELLOW + "恭喜完成所有" + config.getMaxLevels() + "层挑战");
        
        // 最终奖励
        giveFinalRewards();
        
        // 更新玩家进度
        for (ChallengeTowerPlayerProgress progress : playerProgress.values()) {
            progress.completeChallenge();
        }
        
        // 结束挑战
        endChallenge(true);
    }
    
    /**
     * 给予最终奖励
     */
    private void giveFinalRewards() {
        // 额外的经验奖励
        int bonusExperience = config.getExperienceForLevel(config.getMaxLevels()) * 3;
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.giveExp(bonusExperience);
                
                ChallengeTowerPlayerProgress progress = playerProgress.get(playerId);
                if (progress != null) {
                    progress.addExperience(bonusExperience);
                }
            }
        }
        
        broadcastMessage(ChatColor.YELLOW + "最终奖励: " + ChatColor.WHITE + bonusExperience + "点经验");
        
        // 播放庆祝效果
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Location loc = player.getLocation();
                World world = loc.getWorld();
                
                // 烟花效果
                for (int i = 0; i < 20; i++) {
                    world.spawnParticle(Particle.FIREWORK, loc, 10, 0.5, 1, 0.5, 0);
                }
                
                // 音效
                player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
            }
        }
    }
    
    /**
     * 开始检查任务
     */
    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) {
                    this.cancel();
                    return;
                }
                
                // 检查玩家状态
                checkPlayerStatus();
                
                // 检查怪物状态
                checkMonsterStatus();
                
                // 检查时间限制（可选）
                checkTimeLimit();
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒检查一次
    }
    
    /**
     * 检查玩家状态
     */
    private void checkPlayerStatus() {
        List<UUID> toRemove = new ArrayList<>();
        
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            
            if (player == null || !player.isOnline()) {
                // 玩家离线
                toRemove.add(playerId);
                continue;
            }
            
            // 检查玩家是否在区域外
            if (!tower.getArea().contains(player)) {
                // 玩家离开区域，传送回挑战区域内的安全位置
                Location safeLocation = tower.getArea().getSafeSpawnLocation();
                if (safeLocation != null) {
                    player.teleport(safeLocation);
                    player.sendMessage(ChatColor.RED + "你离开了挑战区域，已传送回挑战区域");
                } else {
                    // 如果找不到安全位置，使用入口位置作为备选
                    Location entryLoc = entryLocations.get(playerId);
                    if (entryLoc != null) {
                        player.teleport(entryLoc);
                        player.sendMessage(ChatColor.RED + "你离开了挑战区域，已传送回入口");
                    }
                }
            }
            
            // 检查玩家是否死亡
            if (player.isDead()) {
                // 玩家死亡，强制离开挑战塔队列
                toRemove.add(playerId);
                player.sendMessage(ChatColor.RED + "§c你已死亡，已强制离开挑战塔");
                
                // 从队列中移除
                challengeTowerManager.getQueue().forceRemovePlayer(playerId);
            }
        }
        
        // 移除离线玩家
        for (UUID playerId : toRemove) {
            playerIds.remove(playerId);
            entryLocations.remove(playerId);
            playerProgress.remove(playerId);
        }
        
        // 如果所有玩家都离线，结束挑战
        if (playerIds.isEmpty() && isActive) {
            broadcastMessage(ChatColor.RED + "所有玩家已离开，挑战结束");
            endChallenge(false);
        }
    }
    
    /**
     * 检查怪物状态
     */
    private void checkMonsterStatus() {
        // 清理已死亡的怪物
        Iterator<UUID> iterator = spawnedMonsters.iterator();
        while (iterator.hasNext()) {
            UUID monsterId = iterator.next();
            Entity monster = Bukkit.getEntity(monsterId);
            if (monster == null || monster.isDead()) {
                iterator.remove();
                monsterTypes.remove(monsterId);
            }
        }
    }
    
    /**
     * 检查时间限制
     */
    private void checkTimeLimit() {
        // 可选：添加时间限制
        // long currentTime = System.currentTimeMillis();
        // long elapsedMinutes = (currentTime - startTime) / (1000 * 60);
        // if (elapsedMinutes > 30) { // 30分钟限制
        //     broadcastMessage(ChatColor.RED + "时间到，挑战结束");
        //     endChallenge(false);
        // }
    }
    
    /**
     * 结束挑战
     */
    public void endChallenge(boolean success) {
        if (!isActive) {
            return;
        }
        
        isActive = false;
        
        // 取消所有任务
        if (waveTask != null) {
            waveTask.cancel();
            waveTask = null;
        }
        
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        
        if (teleportTask != null) {
            teleportTask.cancel();
            teleportTask = null;
        }
        
        // 清理所有怪物
        cleanupMonsters();
        
        // 传送玩家回入口
        teleportPlayersBack();
        
        // 广播结束消息
        if (success) {
            broadcastMessage(ChatColor.GREEN + "§l挑战成功完成！");
        } else {
            broadcastMessage(ChatColor.RED + "§l挑战结束");
        }
        
        // 更新队列状态
        tower.getManager().getQueue().onChallengeComplete(tower.getId());
        
        // 保存玩家进度
        for (ChallengeTowerPlayerProgress progress : playerProgress.values()) {
            progress.saveProgress();
        }
        
        // 重置状态
        reset();
    }
    
    /**
     * 清理怪物
     */
    private void cleanupMonsters() {
        for (UUID monsterId : spawnedMonsters) {
            Entity monster = Bukkit.getEntity(monsterId);
            if (monster != null) {
                monster.remove();
            }
        }
        spawnedMonsters.clear();
        monsterTypes.clear();
    }
    
    /**
     * 传送玩家回入口
     */
    private void teleportPlayersBack() {
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Location entryLoc = entryLocations.get(playerId);
                if (entryLoc != null) {
                    player.teleport(entryLoc);
                    player.sendMessage(ChatColor.YELLOW + "已传送回挑战塔入口");
                }
            }
        }
    }
    
    /**
     * 重置实例
     */
    private void reset() {
        playerIds.clear();
        entryLocations.clear();
        playerProgress.clear();
        spawnedMonsters.clear();
        monsterTypes.clear();
        
        isActive = false;
        isPaused = false;
        currentLevel = 1;
        currentWave = 1;
        monstersRemaining = 0;
        totalMonstersSpawned = 0;
        
        startTime = 0;
        waveStartTime = 0;
    }
    
    /**
     * 暂停挑战
     */
    public void pauseChallenge() {
        if (!isActive || isPaused) {
            return;
        }
        
        isPaused = true;
        
        // 暂停怪物AI
        for (UUID monsterId : spawnedMonsters) {
            Entity monster = Bukkit.getEntity(monsterId);
            if (monster instanceof LivingEntity) {
                ((LivingEntity) monster).setAI(false);
            }
        }
        
        broadcastMessage(ChatColor.YELLOW + "挑战已暂停");
    }
    
    /**
     * 恢复挑战
     */
    public void resumeChallenge() {
        if (!isActive || !isPaused) {
            return;
        }
        
        isPaused = false;
        
        // 恢复怪物AI
        for (UUID monsterId : spawnedMonsters) {
            Entity monster = Bukkit.getEntity(monsterId);
            if (monster instanceof LivingEntity) {
                ((LivingEntity) monster).setAI(true);
            }
        }
        
        broadcastMessage(ChatColor.YELLOW + "挑战已恢复");
    }
    
    /**
     * 广播消息给所有玩家
     */
    private void broadcastMessage(String message) {
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 播放音效给所有玩家
     */
    private void playSound(Sound sound, float volume, float pitch) {
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }
    
    // ========== Getter方法 ==========
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public int getMonstersRemaining() {
        return monstersRemaining;
    }
    
    public int getTotalMonstersSpawned() {
        return totalMonstersSpawned;
    }
    
    public List<UUID> getPlayerIds() {
        return new ArrayList<>(playerIds);
    }
    
    public List<UUID> getSpawnedMonsters() {
        return new ArrayList<>(spawnedMonsters);
    }
    
    public ChallengeTower getTower() {
        return tower;
    }
    
    /**
     * 获取挑战状态信息
     */
    public String getStatusInfo() {
        if (!isActive) {
            return "未激活";
        }
        
        return String.format("第%d层 第%d波 | 剩余怪物: %d | 玩家: %d/%d",
            currentLevel, currentWave, spawnedMonsters.size() + monstersRemaining,
            playerIds.size(), config.getMaxPlayers());
    }
}