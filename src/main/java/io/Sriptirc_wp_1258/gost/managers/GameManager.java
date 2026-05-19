package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {
    
    public enum GameState {
        WAITING,
        STARTING,
        RUNNING,
        ENDING,
        STOPPED
    }
    
    private final Gost plugin;
    

    private GameState gameState = GameState.STOPPED;
    private UUID currentGameId;
    

    private Set<UUID> waitingPlayers = new HashSet<>();
    

    private long gameStartTime;
    private int gameDuration;
    private int preparationTime;
    private int queueTime;
    private int remainingGameTime;
    private boolean preparationPhase = false;
    

    private BossBar queueBossBar;
    private BossBar gameBossBar;
    

    private BukkitTask queueTask;
    private BukkitTask preparationTask;
    private BukkitTask gameTask;
    private BukkitTask itemDistributionTask;
    private BukkitTask ghostSenseTask;
    private BukkitTask minuteGlowingTask;
    private BukkitTask ghostToHumanTask;
    
    public GameManager(Gost plugin) {
        this.plugin = plugin;
        initializeBossBars();
    }
    
    private void initializeBossBars() {
        // 队列Boss栏
        queueBossBar = Bukkit.createBossBar(
            ChatColor.YELLOW + "等待玩家加入...",
            BarColor.YELLOW,
            BarStyle.SOLID
        );
        queueBossBar.setVisible(false);
        
        // 游戏Boss栏
        gameBossBar = Bukkit.createBossBar(
            ChatColor.GREEN + "游戏进行中",
            BarColor.GREEN,
            BarStyle.SOLID
        );
        gameBossBar.setVisible(false);
    }
    
    // 玩家加入队列
    public boolean joinQueue(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 检查是否已经在游戏中
        if (plugin.getPlayerManager().getAllPlayers().contains(playerId)) {
            player.sendMessage(ChatColor.RED + "你已经在游戏中！");
            return false;
        }
        
        // 检查是否已经在队列中
        if (waitingPlayers.contains(playerId)) {
            player.sendMessage(ChatColor.RED + "你已经在队列中！");
            return false;
        }
        
        // 检查最大玩家数
        if (waitingPlayers.size() >= plugin.getConfigManager().getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "队列已满！");
            return false;
        }
        
        // 检查经济
        if (!plugin.getEconomyManager().chargeEntryFee(player)) {
            player.sendMessage(ChatColor.RED + "金币不足！需要 " + plugin.getConfigManager().getEntryFee() + " 金币");
            return false;
        }
        
        // 加入队列
        waitingPlayers.add(playerId);
        player.sendMessage(ChatColor.GREEN + "你已加入游戏队列！");
        player.sendMessage(ChatColor.YELLOW + "使用命令 /gost leave 可以随时退出队列");
        
        // 广播加入队列消息
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " 加入了游戏队列 (" + waitingPlayers.size() + "/" + plugin.getConfigManager().getMaxPlayers() + ")");
        
        // 更新队列Boss栏
        updateQueueBossBar();
        
        // 如果这是第一个玩家，开始队列倒计时
        if (waitingPlayers.size() == 1) {
            startQueueTimer();
        }
        
        return true;
    }
    
    // 玩家离开队列
    public boolean leaveQueue(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!waitingPlayers.contains(playerId)) {
            player.sendMessage(ChatColor.RED + "你不在队列中！");
            return false;
        }
        
        // 退还金币
        plugin.getEconomyManager().refundEntryFee(player);
        player.sendMessage(ChatColor.GREEN + "入场金币已退还！");
        
        // 移除队列
        waitingPlayers.remove(playerId);
        player.sendMessage(ChatColor.YELLOW + "你已离开游戏队列！");
        
        // 广播离开队列消息
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " 离开了游戏队列 (" + waitingPlayers.size() + "/" + plugin.getConfigManager().getMaxPlayers() + ")");
        
        // 更新队列Boss栏
        updateQueueBossBar();
        
        // 如果队列人数少于最小玩家数，强制清空队列
        if (waitingPlayers.size() < plugin.getConfigManager().getMinPlayers() && queueTask != null) {
            Bukkit.broadcastMessage(ChatColor.RED + "玩家数量不足，队列已取消！");
            
            // 退还所有剩余玩家的金币
            for (UUID remainingPlayerId : new HashSet<>(waitingPlayers)) {
                Player remainingPlayer = Bukkit.getPlayer(remainingPlayerId);
                if (remainingPlayer != null && remainingPlayer.isOnline()) {
                    plugin.getEconomyManager().refundEntryFee(remainingPlayer);
                    remainingPlayer.sendMessage(ChatColor.YELLOW + "队列取消，入场金币已退还！");
                }
            }
            
            // 取消队列任务
            queueTask.cancel();
            queueTask = null;
            queueBossBar.setVisible(false);
            waitingPlayers.clear();
        }
        // 如果队列为空，取消队列任务
        else if (waitingPlayers.isEmpty() && queueTask != null) {
            queueTask.cancel();
            queueTask = null;
            queueBossBar.setVisible(false);
        }
        
        return true;
    }
    
    // 开始队列倒计时
    private void startQueueTimer() {
        queueTime = plugin.getConfigManager().getQueueTime();
        queueBossBar.setVisible(true);
        
        queueTask = new BukkitRunnable() {
            int timeLeft = queueTime;
            boolean matchQueueStarted = false;
            int matchQueueTimeLeft = 0;
            
            @Override
            public void run() {
                if (waitingPlayers.isEmpty()) {
                    queueBossBar.setVisible(false);
                    this.cancel();
                    return;
                }
                
                // 检查是否达到最大玩家数，开始匹配队列倒计时
                if (!matchQueueStarted && waitingPlayers.size() >= plugin.getConfigManager().getMaxPlayers()) {
                    matchQueueStarted = true;
                    matchQueueTimeLeft = plugin.getConfigManager().getMatchQueueTime();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "队列已满员！游戏将在 " + matchQueueTimeLeft + " 秒后开始！");
                }
                
                // 处理匹配队列倒计时
                if (matchQueueStarted) {
                    if (matchQueueTimeLeft > 0) {
                        // 只在最后10秒发送居中大文本倒计时提示
                        if (matchQueueTimeLeft <= 10) {
                            for (UUID playerId : waitingPlayers) {
                                Player player = Bukkit.getPlayer(playerId);
                                if (player != null && player.isOnline()) {
                                    player.sendTitle(ChatColor.GREEN + "游戏即将开始", ChatColor.YELLOW + "倒计时: " + matchQueueTimeLeft + " 秒", 0, 20, 0);
                                }
                            }
                        }
                        
                        // 每10秒或最后10秒发送聊天提示
                        if (matchQueueTimeLeft == 10 || matchQueueTimeLeft == 5 || matchQueueTimeLeft <= 3) {
                            Bukkit.broadcastMessage(ChatColor.GREEN + "游戏开始倒计时: " + matchQueueTimeLeft + " 秒");
                        }
                        
                        matchQueueTimeLeft--;
                    } else {
                        // 匹配队列倒计时结束，开始游戏
                        startGame();
                        this.cancel();
                        return;
                    }
                }
                
                // 更新Boss栏
                double progress = (double) timeLeft / queueTime;
                queueBossBar.setProgress(progress);
                String title = ChatColor.YELLOW + "等待玩家 " + waitingPlayers.size() + "/" + 
                    plugin.getConfigManager().getMaxPlayers() + " | 剩余: " + timeLeft + "秒";
                if (matchQueueStarted) {
                    title += ChatColor.GREEN + " (满员倒计时: " + matchQueueTimeLeft + "秒)";
                }
                queueBossBar.setTitle(title);
                
                // 显示给队列中的玩家
                for (UUID playerId : waitingPlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        queueBossBar.addPlayer(player);
                    }
                }
                
                // 检查是否可以开始游戏（普通队列时间结束）
                if (timeLeft <= 0) {
                    if (waitingPlayers.size() >= plugin.getConfigManager().getMinPlayers()) {
                        startGame();
                    } else {
                        // 玩家不足，取消队列
                        Bukkit.broadcastMessage(ChatColor.RED + "玩家不足，游戏取消！");
                        for (UUID playerId : waitingPlayers) {
                            Player player = Bukkit.getPlayer(playerId);
                            if (player != null) {
                                plugin.getEconomyManager().refundEntryFee(player);
                                player.sendMessage(ChatColor.YELLOW + "游戏取消，金币已退还！");
                            }
                        }
                        waitingPlayers.clear();
                        queueBossBar.setVisible(false);
                    }
                    this.cancel();
                    return;
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    // 开始游戏
    public boolean startGame() {
        if (gameState != GameState.STOPPED && gameState != GameState.WAITING) {
            return false;
        }
        
        // 检查游戏区域 - 自动选择启用的区域
        AreaManager.GameArea selectedArea = plugin.getAreaManager().getSelectedArea();
        if (selectedArea == null) {
            // 尝试自动选择区域
            selectedArea = plugin.getAreaManager().autoSelectArea();
            if (selectedArea == null) {
                Bukkit.broadcastMessage(ChatColor.RED + "没有启用的游戏区域！请管理员先启用至少一个区域。");
                return false;
            }
            Bukkit.broadcastMessage(ChatColor.YELLOW + "已自动选择区域: " + selectedArea.getName());
        } else {
            // 检查选中的区域是否已启用
            if (!plugin.getAreaManager().isAreaEnabled(selectedArea.getName())) {
                Bukkit.broadcastMessage(ChatColor.RED + "选中的区域未启用！请管理员先启用该区域。");
                return false;
            }
        }
        
        // 生成游戏ID
        currentGameId = UUID.randomUUID();
        gameState = GameState.STARTING;
        
        // 获取游戏时长配置
        gameDuration = plugin.getConfigManager().getGameDuration();
        preparationTime = plugin.getConfigManager().getPreparationTime();
        
        // 隐藏队列Boss栏
        queueBossBar.setVisible(false);
        
        // 将所有等待玩家加入游戏
        for (UUID playerId : waitingPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                plugin.getPlayerManager().joinGame(player);
            }
        }
        
        // 清空等待队列
        waitingPlayers.clear();
        
        // 开始准备阶段
        startPreparationPhase();
        
        Bukkit.broadcastMessage(ChatColor.GREEN + "游戏开始！准备阶段 " + preparationTime + " 秒");
        return true;
    }
    
    // 开始准备阶段
    private void startPreparationPhase() {
        preparationPhase = true;
        gameBossBar.setVisible(true);
        gameBossBar.setColor(BarColor.YELLOW);
        
        preparationTask = new BukkitRunnable() {
            int timeLeft = preparationTime;
            
            @Override
            public void run() {
                if (!preparationPhase) {
                    this.cancel();
                    return;
                }
                
                // 更新Boss栏
                double progress = (double) timeLeft / preparationTime;
                gameBossBar.setProgress(progress);
                gameBossBar.setTitle(ChatColor.YELLOW + "准备阶段 " + timeLeft + "秒");
                
                // 显示给所有游戏中的玩家
                for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        gameBossBar.addPlayer(player);
                    }
                }
                
                // 准备阶段结束
                if (timeLeft <= 0) {
                    preparationPhase = false;
                    startGamePhase();
                    this.cancel();
                    return;
                }
                
                // 每5秒提醒一次
                if (timeLeft % 5 == 0 || timeLeft <= 3) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "准备阶段剩余: " + timeLeft + "秒");
                    
                    // 发送准备阶段标题
                    sendPreparationTitle(timeLeft);
                }
                
                // 最后3秒每1秒发送标题
                if (timeLeft <= 3) {
                    sendPreparationTitle(timeLeft);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        // 清理所有玩家的游戏效果，确保公平性
        cleanupPlayerEffects();
        
        // 在准备阶段开始时随机选择母体鬼并启动禁足倒计时
        selectInitialMotherGhost();
    }
    
    // 清理所有玩家的游戏效果，确保公平性
    private void cleanupPlayerEffects() {
        plugin.getLogger().info("清理所有玩家的游戏效果，确保管理员和普通玩家公平游戏...");
        
        for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // 清除所有可能影响游戏平衡的效果
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.removePotionEffect(PotionEffectType.GLOWING);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                
                // 如果是管理员或创造模式玩家，发送提示消息
                boolean isAdmin = player.hasPermission("gost.admin");
                boolean wasCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
                
                if (isAdmin || wasCreative) {
                    player.sendMessage(ChatColor.YELLOW + "⚠ 游戏开始：所有效果已重置，确保公平游戏！");
                    player.sendMessage(ChatColor.YELLOW + "作为" + (isAdmin ? "管理员" : "") + 
                                     (isAdmin && wasCreative ? "且" : "") + 
                                     (wasCreative ? "创造模式玩家" : "") + "，你也会受到所有游戏效果的影响。");
                }
                
                plugin.getLogger().info("已清理玩家效果: " + player.getName() + 
                    (isAdmin ? " (管理员)" : "") + (wasCreative ? " (创造模式)" : ""));
            }
        }
        
        plugin.getLogger().info("游戏效果清理完成！");
    }
    
    // 随机选择初始母体鬼
    private void selectInitialMotherGhost() {
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        if (allPlayers.isEmpty()) {
            plugin.getLogger().warning("无法选择母体鬼：游戏中没有玩家！");
            return;
        }
        
        // 随机选择一名玩家
        Collections.shuffle(allPlayers);
        UUID motherGhostId = allPlayers.get(0);
        
        // 设置该玩家为母体鬼
        plugin.getPlayerManager().setPlayerRole(motherGhostId, PlayerManager.PlayerRole.GHOST_MOTHER);
        
        // 获取玩家对象
        Player motherGhost = Bukkit.getPlayer(motherGhostId);
        if (motherGhost != null && motherGhost.isOnline()) {
            // 广播消息
            Bukkit.broadcastMessage(ChatColor.DARK_RED + motherGhost.getName() + " 被选为母体鬼！");
            
            // 更新队伍显示
            plugin.getTeamManager().setPlayerTeam(motherGhost, PlayerManager.PlayerRole.GHOST_MOTHER);
            
            // 应用母体鬼效果
            plugin.getPlayerManager().applyRoleEffects(motherGhost, PlayerManager.PlayerRole.GHOST_MOTHER);
            
            // 更新玩家背包（母体鬼需要狂暴药水）
            plugin.getPlayerManager().updatePlayerInventory(motherGhost, PlayerManager.PlayerRole.GHOST_MOTHER);
            
            // 启动母体禁足倒计时
            startMotherGhostImmobilizeCountdown(motherGhost);
        }
    }
    
    // 启动母体禁足倒计时
    private void startMotherGhostImmobilizeCountdown(Player motherGhost) {
        int immobilizeDuration = plugin.getConfigManager().getGhostImmobilizeDuration();
        
        new BukkitRunnable() {
            int timeLeft = immobilizeDuration;
            
            @Override
            public void run() {
                if (!motherGhost.isOnline() || 
                    plugin.getPlayerManager().getPlayerRole(motherGhost.getUniqueId()) != PlayerManager.PlayerRole.GHOST_MOTHER) {
                    this.cancel();
                    return;
                }
                
                if (timeLeft > 0) {
                    // 只在最后10秒显示居中大文本倒计时提示
                    if (timeLeft <= 10) {
                        motherGhost.sendTitle(ChatColor.RED + "禁足中", ChatColor.YELLOW + "剩余: " + timeLeft + " 秒", 0, 20, 0);
                    }
                    
                    // 最后10秒发送聊天提示
                    if (timeLeft == 10 || timeLeft == 5 || timeLeft <= 3) {
                        motherGhost.sendMessage(ChatColor.RED + "禁足解除倒计时: " + timeLeft + " 秒");
                    }
                    
                    timeLeft--;
                } else {
                    // 禁足结束
                    motherGhost.sendTitle(ChatColor.GREEN + "禁足解除", ChatColor.YELLOW + "你可以开始行动了！", 10, 40, 10);
                    motherGhost.sendMessage(ChatColor.GREEN + "禁足已解除，你可以开始感染人类了！");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒执行一次
    }
    
    // 开始游戏阶段
    private void startGamePhase() {
        gameState = GameState.RUNNING;
        gameStartTime = System.currentTimeMillis();
        gameBossBar.setColor(BarColor.GREEN);
        
        // 重置神圣守护状态，确保一局游戏只触发一次
        plugin.getDivineGuardianManager().resetGame();
        
        Bukkit.broadcastMessage(ChatColor.GREEN + "游戏正式开始！");
        
        // 注意：母体鬼已经在准备阶段选择并禁足，这里不再重复选择
        
        // 开始游戏倒计时
        startGameTimer();
        
        // 开始道具分发任务（每分钟）
        startItemDistributionTask();
        
        // 开始幽灵感知任务（每分钟）
        startGhostSenseTask();
        
        // 开始道具刷新系统
        plugin.getItemSpawnManager().startSpawning();
        
        // 开始每分钟高亮效果任务
        startMinuteGlowingTask();
        
        // 应用黑暗效果（如果启用）
        plugin.getDarkEffectManager().applyDarkEffectToAllPlayers();
        
        // 开始心跳声效果（如果启用）
        plugin.getHeartbeatManager().startHeartbeat();
        
        // 开始鬼转人类功能（如果启用）
        startGhostToHumanTask();
        
        // 发送游戏开始标题
        sendGameStartTitles();
    }
    
    // 开始游戏倒计时
    private void startGameTimer() {
        remainingGameTime = gameDuration;
        
        gameTask = new BukkitRunnable() {
            int timeLeft = gameDuration;
            
            @Override
            public void run() {
                remainingGameTime = timeLeft;
                
                if (gameState != GameState.RUNNING) {
                    this.cancel();
                    return;
                }
                
                // 更新Boss栏
                double progress = (double) timeLeft / gameDuration;
                gameBossBar.setProgress(progress);
                
                // 格式化时间
                int minutes = timeLeft / 60;
                int seconds = timeLeft % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                
                // 获取游戏统计
                int humanCount = plugin.getPlayerManager().getHumanPlayers().size();
                int ghostCount = plugin.getPlayerManager().getGhostPlayers().size();
                
                gameBossBar.setTitle(ChatColor.GREEN + "游戏时间: " + timeString + 
                    " §a人类: " + humanCount + " §c鬼: " + ghostCount);
                
                // 检查游戏结束条件
                if (timeLeft <= 0) {
                    endGame(true); // 人类胜利
                    this.cancel();
                    return;
                }
                
                // 检查是否所有人类都被感染
                if (humanCount == 0) {
                    endGame(false); // 鬼胜利
                    this.cancel();
                    return;
                }
                
                // 检查鬼数量是否为0
                if (ghostCount == 0) {
                    // 鬼数量为0，人类自动胜利，但不发放奖金，退还入场金
                    endGameWithNoGhosts();
                    this.cancel();
                    return;
                }
                
                if (plugin.getConfigManager().isConversionEnabled() && timeLeft == plugin.getConfigManager().getConversionActivateTime()) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "剩余2分钟，转化功能已激活！");
                }
                
                if (plugin.getConfigManager().isDivineGuardianSystemEnabled()) {
                    plugin.getDivineGuardianManager().checkAndEnterDemonHunterPhase(timeLeft);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    // 开始道具分发任务
    private void startItemDistributionTask() {
        itemDistributionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.RUNNING) {
                    this.cancel();
                    return;
                }
                
                // 分发道具
                plugin.getItemManager().distributeItems();
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // 每分钟执行一次（20 ticks * 60 = 1200）
    }
    
    // 开始幽灵感知任务
    private void startGhostSenseTask() {
        ghostSenseTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.RUNNING) {
                    this.cancel();
                    return;
                }
                
                // 获取所有游戏中的玩家
                List<Player> allPlayers = new ArrayList<>();
                for (UUID playerId : plugin.getPlayerManager().getAllPlayers()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        allPlayers.add(player);
                    }
                }
                
                // 应用幽灵感知效果
                plugin.getItemManager().applyGhostSenseEffect(allPlayers);
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // 每分钟执行一次
    }
    
    // 更新Boss栏统计
    public void updateBossBarStats(int humanCount, int ghostCount) {
        if (gameState == GameState.RUNNING && gameBossBar != null) {
            // 获取剩余时间
            long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000;
            int timeLeft = Math.max(0, gameDuration - (int) elapsed);
            
            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            
            gameBossBar.setTitle(ChatColor.GREEN + "游戏时间: " + timeString + 
                " §a人类: " + humanCount + " §c鬼: " + ghostCount);
        }
    }
    
    // 更新队列Boss栏
    private void updateQueueBossBar() {
        if (queueBossBar != null) {
            queueBossBar.setTitle(ChatColor.YELLOW + "等待玩家: " + waitingPlayers.size() + "/" + 
                plugin.getConfigManager().getMaxPlayers());
        }
    }
    
    // 结束游戏
    public void endGame(boolean humanWin) {
        gameState = GameState.ENDING;
        remainingGameTime = 0;
        
        // 取消所有任务
        if (preparationTask != null) {
            preparationTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (itemDistributionTask != null) {
            itemDistributionTask.cancel();
        }
        if (ghostSenseTask != null) {
            ghostSenseTask.cancel();
        }
        if (minuteGlowingTask != null) {
            minuteGlowingTask.cancel();
        }
        if (ghostToHumanTask != null) {
            ghostToHumanTask.cancel();
        }
        
        // 隐藏Boss栏
        gameBossBar.setVisible(false);
        
        // 广播胜利消息
        if (humanWin) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "游戏结束！人类胜利！");
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "游戏结束！鬼胜利！");
        }
        
        // 发送游戏结束标题
        sendGameEndTitle(humanWin);
        
        // 计算并分发奖金
        plugin.getEconomyManager().distributeRewards(humanWin);
        
        // 停止心跳声效果
        plugin.getHeartbeatManager().stopHeartbeat();
        
        // 清理数据并恢复所有玩家状态
        plugin.getPlayerManager().cleanup();
        plugin.getDivineGuardianManager().cleanup(); // 清理神圣守护数据
        plugin.getDarkEffectManager().cleanup(); // 清理黑暗效果数据
        
        // 重置游戏状态
        gameState = GameState.STOPPED;
        currentGameId = null;
        preparationPhase = false;
        
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏已结束，可以开始新的游戏！");
    }
    
    /**
     * 鬼数量为0时结束游戏（不发放奖金，退还入场金）
     */
    private void endGameWithNoGhosts() {
        gameState = GameState.ENDING;
        remainingGameTime = 0;
        
        // 取消所有任务
        if (preparationTask != null) {
            preparationTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (itemDistributionTask != null) {
            itemDistributionTask.cancel();
        }
        if (ghostSenseTask != null) {
            ghostSenseTask.cancel();
        }
        if (minuteGlowingTask != null) {
            minuteGlowingTask.cancel();
        }
        if (ghostToHumanTask != null) {
            ghostToHumanTask.cancel();
        }
        
        // 隐藏Boss栏
        gameBossBar.setVisible(false);
        
        // 广播消息
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏结束！鬼阵营数量为0！");
        Bukkit.broadcastMessage(ChatColor.RED + "由于鬼阵营数量为0，服务器奖金不发放！");
        Bukkit.broadcastMessage(ChatColor.GREEN + "各玩家的入场金已原路退回！");
        
        // 发送游戏结束标题（人类胜利）
        sendGameEndTitle(true);
        
        // 退还所有玩家的入场金
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                plugin.getEconomyManager().refundEntryFee(player);
                player.sendMessage(ChatColor.YELLOW + "你的入场金已退还！");
            }
        }
        
        // 停止心跳声效果
        plugin.getHeartbeatManager().stopHeartbeat();
        
        // 清理数据并恢复所有玩家状态
        plugin.getPlayerManager().cleanup();
        plugin.getDivineGuardianManager().cleanup(); // 清理神圣守护数据
        plugin.getDarkEffectManager().cleanup(); // 清理黑暗效果数据
        
        // 重置游戏状态
        gameState = GameState.STOPPED;
        currentGameId = null;
        preparationPhase = false;
        
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏已结束，可以开始新的游戏！");
    }
    
    // 强制停止游戏
    public void forceStopGame() {
        remainingGameTime = 0;
        
        if (gameState == GameState.STOPPED) {
            return;
        }
        
        // 取消所有任务
        if (queueTask != null) {
            queueTask.cancel();
        }
        if (preparationTask != null) {
            preparationTask.cancel();
        }
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (itemDistributionTask != null) {
            itemDistributionTask.cancel();
        }
        if (ghostSenseTask != null) {
            ghostSenseTask.cancel();
        }
        if (minuteGlowingTask != null) {
            minuteGlowingTask.cancel();
        }
        if (ghostToHumanTask != null) {
            ghostToHumanTask.cancel();
        }
        
        // 隐藏Boss栏
        queueBossBar.setVisible(false);
        gameBossBar.setVisible(false);
        
        // 退还队列玩家金币
        for (UUID playerId : waitingPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                plugin.getEconomyManager().refundEntryFee(player);
                player.sendMessage(ChatColor.YELLOW + "游戏强制停止，金币已退还！");
            }
        }
        waitingPlayers.clear();
        
        // 先通知所有游戏玩家
        for (UUID playerId : new ArrayList<>(plugin.getPlayerManager().getAllPlayers())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "游戏被管理员强制停止！");
            }
        }
        
        // 停止心跳声效果
        plugin.getHeartbeatManager().stopHeartbeat();
        
        // 然后清理数据并恢复所有玩家状态
        plugin.getPlayerManager().cleanup();
        plugin.getDivineGuardianManager().cleanup(); // 清理神圣守护数据
        plugin.getDarkEffectManager().cleanup(); // 清理黑暗效果数据
        
        // 重置游戏状态
        gameState = GameState.STOPPED;
        currentGameId = null;
        preparationPhase = false;
        
        Bukkit.broadcastMessage(ChatColor.RED + "游戏已被管理员强制停止！");
    }
    
    // 检查游戏是否在进行中
    public boolean isGameRunning() {
        return gameState == GameState.RUNNING || gameState == GameState.STARTING;
    }
    
    // 检查是否有游戏在进行
    public boolean isAnyGameRunning() {
        return gameState != GameState.STOPPED;
    }
    
    // 获取游戏状态
    public GameState getGameState() {
        return gameState;
    }
    
    // 获取等待玩家数量
    public int getWaitingPlayersCount() {
        return waitingPlayers.size();
    }
    
    // 获取队列大小（别名方法）
    public int getQueueSize() {
        return waitingPlayers.size();
    }
    
    // 获取游戏ID
    public UUID getCurrentGameId() {
        return currentGameId;
    }
    
    /**
     * 发送游戏开始标题
     */
    private void sendGameStartTitles() {
        // 获取当前区域名称
        String areaName = plugin.getAreaManager().getSelectedAreaName();
        String welcomeMessage = "";
        
        if (areaName != null && !areaName.isEmpty()) {
            welcomeMessage = ChatColor.GOLD + "欢迎来到：" + ChatColor.YELLOW + areaName;
        }
        
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                PlayerManager.PlayerRole role = plugin.getPlayerManager().getPlayerRole(playerId);
                
                if (role == PlayerManager.PlayerRole.HUMAN) {
                    plugin.getLanguageManager().sendTitle(player, "game.started-human", welcomeMessage);
                } else if (role == PlayerManager.PlayerRole.GHOST_MOTHER || role == PlayerManager.PlayerRole.GHOST_NORMAL) {
                    plugin.getLanguageManager().sendTitle(player, "game.started-ghost", welcomeMessage);
                }
            }
        }
    }
    
    /**
     * 发送感染标题
     */
    public void sendInfectedTitle(Player player) {
        plugin.getLanguageManager().sendTitle(player, "role.infected", "");
    }
    
    /**
     * 发送转化标题
     */
    public void sendConversionTitle(Player player) {
        plugin.getLanguageManager().sendTitle(player, "role.converted", "");
    }
    
    /**
     * 发送游戏结束标题
     */
    public void sendGameEndTitle(boolean humanWin) {
        String titleKey = humanWin ? "broadcast.human-win" : "broadcast.ghost-win";
        String subtitleKey = "game.ended";
        
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                plugin.getLanguageManager().sendTitle(player, titleKey, subtitleKey);
            }
        }
        
        // 同时发送给观战者（暂时取消观战系统）
        // for (UUID spectatorId : plugin.getSpectatorManager().getSpectators()) {
        //     Player spectator = Bukkit.getPlayer(spectatorId);
        //     if (spectator != null && spectator.isOnline()) {
        //         plugin.getLanguageManager().sendTitle(spectator, titleKey, subtitleKey);
        //     }
        // }
    }
    
    /**
     * 发送准备阶段倒计时标题
     */
    public void sendPreparationTitle(int timeLeft) {
        // 获取当前区域名称
        String areaName = plugin.getAreaManager().getSelectedAreaName();
        String welcomeMessage = "";
        
        if (areaName != null && !areaName.isEmpty()) {
            welcomeMessage = ChatColor.GOLD + "欢迎来到：" + ChatColor.YELLOW + areaName + "\n";
        }
        
        String title = plugin.getLanguageManager().getMessage("game.starting");
        String subtitle = welcomeMessage + plugin.getLanguageManager().getMessage("time.preparation", timeLeft);
        
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendTitle(title, subtitle, 10, 70, 20);
            }
        }
    }
    
    /**
     * 获取游戏剩余时间（秒）
     */
    public int getRemainingGameTime() {
        return remainingGameTime;
    }
    
    /**
     * 检查是否在准备阶段
     */
    public boolean isPreparationPhase() {
        return preparationPhase;
    }
    
    /**
     * 开始每分钟高亮效果任务
     */
    private void startMinuteGlowingTask() {
        // 检查是否启用每分钟高亮效果
        if (!plugin.getConfigManager().isMinuteGlowingEnabled()) {
            plugin.getLogger().info("每分钟高亮效果已禁用");
            return;
        }
        
        int interval = plugin.getConfigManager().getMinuteGlowingInterval();
        int duration = plugin.getConfigManager().getMinuteGlowingDuration();
        
        plugin.getLogger().info("开始每分钟高亮效果任务，间隔: " + interval + "秒，持续时间: " + duration + "秒");
        
        minuteGlowingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameState != GameState.RUNNING) {
                    this.cancel();
                    return;
                }
                
                // 给所有玩家应用高亮效果
                applyMinuteGlowingEffect();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L); // 转换为ticks
    }
    
    /**
     * 应用每分钟高亮效果
     */
    private void applyMinuteGlowingEffect() {
        int duration = plugin.getConfigManager().getMinuteGlowingDuration();
        int durationTicks = duration * 20;
        
        // 获取所有游戏中的玩家
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        if (allPlayers.isEmpty()) {
            return;
        }
        
        plugin.getLogger().info("应用每分钟高亮效果，持续时间: " + duration + "秒，玩家数量: " + allPlayers.size());
        
        // 给所有玩家应用高亮效果
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.GLOWING,
                    durationTicks,
                    0, // 等级0
                    true, // 环境效果
                    true // 显示粒子
                ));
                
                // 发送提示消息
                player.sendMessage(ChatColor.YELLOW + "你获得了高亮效果，持续 " + duration + " 秒！");
            }
        }
        
        // 广播提示
        Bukkit.broadcastMessage(ChatColor.GOLD + "✨ 所有玩家获得高亮效果，持续 " + duration + " 秒！");
        
        // 发送屏幕居中字幕
        for (UUID playerId : allPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendTitle(
                    ChatColor.GOLD + "✨ 高亮效果",
                    ChatColor.YELLOW + "持续 " + duration + " 秒",
                    10, 40, 10
                );
            }
        }
    }
    
    /**
     * 开始鬼转人类功能任务
     */
    private void startGhostToHumanTask() {
        // 检查是否启用鬼转人类功能
        if (!plugin.getConfigManager().isGhostToHumanEnabled()) {
            plugin.getLogger().info("鬼转人类功能已禁用");
            return;
        }
        
        int remainingTime = plugin.getConfigManager().getGhostToHumanRemainingTime();
        int gameDuration = plugin.getConfigManager().getGameDuration();
        int triggerTime = gameDuration - remainingTime;
        
        if (triggerTime <= 0) {
            plugin.getLogger().warning("鬼转人类触发时间无效，游戏时长: " + gameDuration + "秒，剩余时间: " + remainingTime + "秒");
            return;
        }
        
        plugin.getLogger().info("开始鬼转人类功能任务，将在游戏剩余 " + remainingTime + " 秒时触发");
        
        ghostToHumanTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 检查游戏是否还在进行中
                if (gameState != GameState.RUNNING) {
                    this.cancel();
                    return;
                }
                
                // 获取游戏剩余时间
                long elapsedTime = (System.currentTimeMillis() - gameStartTime) / 1000;
                long remainingSeconds = gameDuration - elapsedTime;
                
                // 检查是否到达触发时间
                if (remainingSeconds <= remainingTime) {
                    plugin.getLogger().info("触发鬼转人类功能，游戏剩余时间: " + remainingSeconds + "秒");
                    convertGhostsToHumans();
                    this.cancel(); // 只触发一次
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 每秒检查一次
    }
    
    /**
     * 将鬼玩家转换为人类
     */
    private void convertGhostsToHumans() {
        // 获取所有非母体的鬼玩家
        List<UUID> ghostPlayers = new ArrayList<>();
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        
        for (UUID playerId : allPlayers) {
            if (plugin.getPlayerManager().isGhost(playerId) && 
                !plugin.getPlayerManager().isMotherGhost(playerId)) {
                ghostPlayers.add(playerId);
            }
        }
        
        if (ghostPlayers.isEmpty()) {
            plugin.getLogger().info("没有可转换的非母体鬼玩家");
            return;
        }
        
        int convertCount = plugin.getConfigManager().getGhostToHumanCount();
        convertCount = Math.min(convertCount, ghostPlayers.size());
        
        // 随机选择要转换的鬼玩家
        Collections.shuffle(ghostPlayers);
        List<UUID> selectedGhosts = ghostPlayers.subList(0, convertCount);
        
        plugin.getLogger().info("准备转换 " + selectedGhosts.size() + " 名鬼玩家为人类");
        
        for (UUID ghostId : selectedGhosts) {
            Player player = Bukkit.getPlayer(ghostId);
            if (player != null && player.isOnline()) {
                convertGhostToHuman(player);
            }
        }
    }
    
    /**
     * 将单个鬼玩家转换为人类
     */
    private void convertGhostToHuman(Player player) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        
        plugin.getLogger().info("开始转换鬼玩家 " + playerName + " 为人类");
        
        // 1. 将玩家角色设置为人类
        plugin.getPlayerManager().setPlayerRole(playerId, PlayerManager.PlayerRole.HUMAN);
        
        // 2. 发送消息给玩家
        player.sendMessage(ChatColor.GREEN + "════════════════════════════════");
        player.sendMessage(ChatColor.GOLD + "✨ 你被转换回了人类阵容！");
        player.sendMessage(ChatColor.GREEN + "✓ 你获得了肾上腺素道具");
        player.sendMessage(ChatColor.YELLOW + "✓ 其他道具已被清空");
        player.sendMessage(ChatColor.GREEN + "════════════════════════════════");
        
        // 3. 广播消息给所有玩家
        Bukkit.broadcastMessage(ChatColor.YELLOW + "🎮 " + playerName + " 在游戏最后阶段被转换回了人类阵容！");
        
        // 4. 发送屏幕居中字幕
        player.sendTitle(
            ChatColor.GOLD + "✨ 转换回人类",
            ChatColor.YELLOW + "你获得了肾上腺素道具",
            10, 60, 10
        );
        
        plugin.getLogger().info("成功转换鬼玩家 " + playerName + " 为人类");
    }
}