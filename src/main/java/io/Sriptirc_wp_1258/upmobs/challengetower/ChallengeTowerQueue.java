package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * 挑战塔排队系统
 * 支持多队列，每个挑战塔有独立的队列
 */
public class ChallengeTowerQueue {
    
    private final ChallengeTowerManager manager;
    
    // 队列数据结构：塔ID -> 玩家UUID列表
    private final Map<String, List<UUID>> towerQueues = new HashMap<>();
    
    // 队列状态：塔ID -> 队列状态
    private final Map<String, QueueStatus> queueStatus = new HashMap<>();
    
    // 玩家状态：玩家UUID -> 所在队列ID
    private final Map<UUID, String> playerQueues = new HashMap<>();
    
    // 倒计时：塔ID -> 倒计时秒数
    private final Map<String, Integer> countdownTimers = new HashMap<>();
    
    // 倒计时任务ID
    private final Map<String, Integer> countdownTaskIds = new HashMap<>();
    
    // 队列检查任务
    private BukkitRunnable queueCheckTask;
    
    public ChallengeTowerQueue(ChallengeTowerManager manager) {
        this.manager = manager;
        startQueueCheckTask();
    }
    
    /**
     * 启动队列检查任务
     */
    private void startQueueCheckTask() {
        queueCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkAllQueues();
            }
        };
        queueCheckTask.runTaskTimer(manager.getPlugin(), 20L, 20L); // 每秒检查一次
    }
    
    /**
     * 停止队列检查任务
     */
    public void stop() {
        if (queueCheckTask != null) {
            queueCheckTask.cancel();
            queueCheckTask = null;
        }
        
        // 取消所有倒计时任务
        for (Integer taskId : countdownTaskIds.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        countdownTaskIds.clear();
        countdownTimers.clear();
    }
    
    /**
     * 玩家加入队列
     */
    public boolean joinQueue(Player player, String towerId) {
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否已在队列中
        if (playerQueues.containsKey(playerId)) {
            String currentQueue = playerQueues.get(playerId);
            if (currentQueue.equals(towerId)) {
                player.sendMessage(ChatColor.YELLOW + "你已经在该挑战塔的队列中");
                return false;
            } else {
                leaveQueue(player);
            }
        }
        
        // 检查挑战塔是否存在
        ChallengeTower tower = manager.getTower(towerId);
        if (tower == null) {
            player.sendMessage(ChatColor.RED + "挑战塔不存在: " + towerId);
            return false;
        }
        
        // 检查玩家是否在冷却中
        ChallengeTowerPlayerProgress progress = manager.getPlayerProgress(playerId);
        if (progress.isOnCooldown(tower.getConfig().getCooldownSeconds())) {
            int remaining = progress.getRemainingCooldown(tower.getConfig().getCooldownSeconds());
            player.sendMessage(ChatColor.RED + "冷却时间中，请等待 " + remaining + " 秒");
            return false;
        }
        
        // 获取或创建队列
        List<UUID> queue = towerQueues.computeIfAbsent(towerId, k -> new ArrayList<>());
        
        // 检查队列是否已满或正在进行中
        QueueStatus status = queueStatus.getOrDefault(towerId, QueueStatus.WAITING);
        if (status == QueueStatus.IN_PROGRESS) {
            player.sendMessage(ChatColor.RED + "该挑战塔正在进行中，请稍后再试");
            return false;
        }
        
        // 检查队列人数
        if (queue.size() >= tower.getConfig().getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "队列已满，最大" + tower.getConfig().getMaxPlayers() + "人");
            return false;
        }
        
        // 加入队列
        queue.add(playerId);
        playerQueues.put(playerId, towerId);
        
        // 发送加入消息
        player.sendMessage(ChatColor.GREEN + "§l✓ 已加入挑战塔队列");
        player.sendMessage(ChatColor.YELLOW + "队列位置: " + ChatColor.WHITE + queue.size() + "/" + tower.getConfig().getMaxPlayers());
        player.sendMessage(ChatColor.YELLOW + "当前队列状态: " + ChatColor.WHITE + status.getDisplayName());
        
        // 广播队列更新
        broadcastQueueUpdate(towerId);
        
        return true;
    }
    
    /**
     * 玩家离开队列
     */
    public boolean leaveQueue(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerQueues.containsKey(playerId)) {
            player.sendMessage(ChatColor.YELLOW + "你不在任何队列中");
            return false;
        }
        
        String towerId = playerQueues.get(playerId);
        List<UUID> queue = towerQueues.get(towerId);
        
        if (queue != null) {
            queue.remove(playerId);
            if (queue.isEmpty()) {
                towerQueues.remove(towerId);
                queueStatus.remove(towerId);
            }
        }
        
        playerQueues.remove(playerId);
        player.sendMessage(ChatColor.YELLOW + "已离开挑战塔队列");
        
        // 广播队列更新
        broadcastQueueUpdate(towerId);
        
        // 检查倒计时是否需要取消
        checkCountdownOnPlayerLeave(towerId);
        
        return true;
    }
    
    /**
     * 强制移除玩家（管理员用）
     */
    public boolean forceRemovePlayer(UUID playerId) {
        return forceRemovePlayer(playerId, true);
    }
    
    /**
     * 强制移除玩家（可控制是否发送消息）
     */
    public boolean forceRemovePlayer(UUID playerId, boolean sendMessage) {
        if (!playerQueues.containsKey(playerId)) {
            return false;
        }
        
        String towerId = playerQueues.get(playerId);
        List<UUID> queue = towerQueues.get(towerId);
        
        if (queue != null) {
            queue.remove(playerId);
            if (queue.isEmpty()) {
                towerQueues.remove(towerId);
                queueStatus.remove(towerId);
            }
        }
        
        playerQueues.remove(playerId);
        
        if (sendMessage) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.RED + "你已被移出挑战塔队列");
            }
        }
        
        // 广播队列更新
        broadcastQueueUpdate(towerId);
        
        // 检查倒计时是否需要取消
        checkCountdownOnPlayerLeave(towerId);
        
        return true;
    }
    
    /**
     * 检查所有队列
     */
    private void checkAllQueues() {
        for (Map.Entry<String, List<UUID>> entry : towerQueues.entrySet()) {
            String towerId = entry.getKey();
            List<UUID> queue = entry.getValue();
            QueueStatus status = queueStatus.getOrDefault(towerId, QueueStatus.WAITING);
            
            // 跳过进行中的队列
            if (status == QueueStatus.IN_PROGRESS) {
                continue;
            }
            
            ChallengeTower tower = manager.getTower(towerId);
            if (tower == null) {
                continue;
            }
            
            // 检查队列是否已满
            if (queue.size() >= tower.getConfig().getMaxPlayers()) {
                // 队列已满，立即开始挑战
                startChallenge(towerId, new ArrayList<>(queue));
                continue;
            }
            
            // 检查是否有倒计时在进行中
            if (countdownTimers.containsKey(towerId)) {
                // 倒计时进行中，跳过
                continue;
            }
            
            // 检查是否满足开始条件（从配置读取最少玩家数）
            int minPlayers = tower.getConfig().getMinPlayersToStart();
            if (queue.size() >= minPlayers) {
                // 开始倒计时
                startCountdown(towerId);
            }
        }
    }
    
    /**
     * 开始挑战
     */
    private void startChallenge(String towerId, List<UUID> playerIds) {
        ChallengeTower tower = manager.getTower(towerId);
        if (tower == null) {
            return;
        }
        
        // 更新队列状态
        queueStatus.put(towerId, QueueStatus.IN_PROGRESS);
        
        // 从队列中移除这些玩家
        List<UUID> queue = towerQueues.get(towerId);
        if (queue != null) {
            queue.removeAll(playerIds);
            if (queue.isEmpty()) {
                towerQueues.remove(towerId);
            }
        }
        
        // 从玩家状态中移除
        for (UUID playerId : playerIds) {
            playerQueues.remove(playerId);
        }
        
        // 获取玩家对象
        List<Player> players = new ArrayList<>();
        for (UUID playerId : playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        
        if (players.isEmpty()) {
            // 没有在线玩家，重置队列状态
            queueStatus.put(towerId, QueueStatus.WAITING);
            return;
        }
        
        // 开始挑战
        boolean started = tower.startChallenge(players);
        if (!started) {
            // 挑战启动失败，重置队列状态
            queueStatus.put(towerId, QueueStatus.WAITING);
            
            // 通知玩家
            for (Player player : players) {
                player.sendMessage(ChatColor.RED + "挑战塔启动失败，请稍后再试");
            }
        }
    }
    
    /**
     * 挑战完成，重置队列状态
     */
    public void onChallengeComplete(String towerId) {
        // 确保倒计时被取消
        cancelCountdown(towerId);
        
        queueStatus.put(towerId, QueueStatus.WAITING);
        
        // 检查是否还有人在队列中
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            towerQueues.remove(towerId);
            queueStatus.remove(towerId);
        }
    }
    
    /**
     * 广播队列更新
     */
    private void broadcastQueueUpdate(String towerId) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        ChallengeTower tower = manager.getTower(towerId);
        if (tower == null) {
            return;
        }
        
        String message = ChatColor.YELLOW + "挑战塔队列更新: " + 
                        ChatColor.WHITE + queue.size() + "/" + tower.getConfig().getMaxPlayers() + 
                        ChatColor.YELLOW + " 人";
        
        // 发送给队列中的所有玩家
        for (UUID playerId : queue) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 获取玩家所在队列
     */
    public String getPlayerQueue(UUID playerId) {
        return playerQueues.get(playerId);
    }
    
    /**
     * 获取队列信息
     */
    public QueueInfo getQueueInfo(String towerId) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null) {
            return null;
        }
        
        QueueStatus status = queueStatus.getOrDefault(towerId, QueueStatus.WAITING);
        List<String> playerNames = new ArrayList<>();
        
        for (UUID playerId : queue) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                playerNames.add(player.getName());
            } else {
                playerNames.add("未知玩家");
            }
        }
        
        return new QueueInfo(towerId, queue.size(), playerNames, status);
    }
    
    /**
     * 获取所有队列信息
     */
    public List<QueueInfo> getAllQueueInfo() {
        List<QueueInfo> infoList = new ArrayList<>();
        
        for (String towerId : towerQueues.keySet()) {
            QueueInfo info = getQueueInfo(towerId);
            if (info != null) {
                infoList.add(info);
            }
        }
        
        return infoList;
    }
    
    /**
     * 清理空队列
     */
    public void cleanupEmptyQueues() {
        Iterator<Map.Entry<String, List<UUID>>> iterator = towerQueues.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<UUID>> entry = iterator.next();
            if (entry.getValue().isEmpty()) {
                iterator.remove();
                queueStatus.remove(entry.getKey());
            }
        }
    }
    
    /**
     * 队列状态枚举
     */
    public enum QueueStatus {
        WAITING("等待中"),
        IN_PROGRESS("进行中"),
        PREPARING("准备中");
        
        private final String displayName;
        
        QueueStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 队列信息类
     */
    public static class QueueInfo {
        private final String towerId;
        private final int playerCount;
        private final List<String> playerNames;
        private final QueueStatus status;
        
        public QueueInfo(String towerId, int playerCount, List<String> playerNames, QueueStatus status) {
            this.towerId = towerId;
            this.playerCount = playerCount;
            this.playerNames = playerNames;
            this.status = status;
        }
        
        public String getTowerId() {
            return towerId;
        }
        
        public int getPlayerCount() {
            return playerCount;
        }
        
        public List<String> getPlayerNames() {
            return playerNames;
        }
        
        public QueueStatus getStatus() {
            return status;
        }
        
        @Override
        public String toString() {
            return String.format("挑战塔: %s, 状态: %s, 玩家: %d/%d", 
                towerId, status.getDisplayName(), playerCount, playerNames.size());
        }
    }
    
    /**
     * 开始倒计时
     */
    private void startCountdown(String towerId) {
        ChallengeTower tower = manager.getTower(towerId);
        if (tower == null) {
            return;
        }
        
        // 检查是否启用倒计时
        if (!tower.getConfig().isEnableCountdown()) {
            // 不启用倒计时，直接开始挑战
            List<UUID> queue = towerQueues.get(towerId);
            if (queue != null && !queue.isEmpty()) {
                startChallenge(towerId, new ArrayList<>(queue));
            }
            return;
        }
        
        // 从配置读取倒计时秒数
        int countdownSeconds = tower.getConfig().getQueueCountdown();
        countdownTimers.put(towerId, countdownSeconds);
        queueStatus.put(towerId, QueueStatus.PREPARING);
        
        // 广播倒计时开始
        broadcastCountdownStart(towerId, countdownSeconds);
        
        // 启动倒计时任务
        int taskId = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), new Runnable() {
            @Override
            public void run() {
                updateCountdown(towerId);
            }
        }, 20L, 20L).getTaskId(); // 每秒执行一次
        
        countdownTaskIds.put(towerId, taskId);
    }
    
    /**
     * 更新倒计时
     */
    private void updateCountdown(String towerId) {
        if (!countdownTimers.containsKey(towerId)) {
            return;
        }
        
        int remaining = countdownTimers.get(towerId) - 1;
        countdownTimers.put(towerId, remaining);
        
        // 检查队列是否还有人
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            // 队列空了，取消倒计时
            cancelCountdown(towerId);
            return;
        }
        
        // 检查是否满员
        ChallengeTower tower = manager.getTower(towerId);
        if (tower != null && queue.size() >= tower.getConfig().getMaxPlayers()) {
            // 满员了，立即开始挑战
            cancelCountdown(towerId);
            startChallenge(towerId, new ArrayList<>(queue));
            return;
        }
        
        // 广播倒计时更新
        if (remaining > 0) {
            // 每5秒或最后10秒广播一次
            if (remaining % 5 == 0 || remaining <= 10) {
                broadcastCountdownUpdate(towerId, remaining);
            }
        } else {
            // 倒计时结束，开始挑战
            cancelCountdown(towerId);
            startChallenge(towerId, new ArrayList<>(queue));
        }
    }
    
    /**
     * 取消倒计时
     */
    private void cancelCountdown(String towerId) {
        // 取消任务
        Integer taskId = countdownTaskIds.remove(towerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        // 清理倒计时数据
        countdownTimers.remove(towerId);
        queueStatus.put(towerId, QueueStatus.WAITING);
        
        // 广播倒计时取消
        broadcastCountdownCancel(towerId);
    }
    
    /**
     * 广播倒计时开始
     */
    private void broadcastCountdownStart(String towerId, int seconds) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        String message = ChatColor.GOLD + "§l⚔ 挑战塔倒计时开始！";
        message += ChatColor.YELLOW + "\n倒计时: " + ChatColor.WHITE + seconds + "秒";
        message += ChatColor.YELLOW + "\n当前人数: " + ChatColor.WHITE + queue.size() + "人";
        message += ChatColor.GREEN + "\n\n倒计时结束后将自动开始挑战！";
        
        for (UUID playerId : queue) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 广播倒计时更新
     */
    private void broadcastCountdownUpdate(String towerId, int remaining) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        String message = ChatColor.YELLOW + "挑战塔倒计时: " + ChatColor.WHITE + remaining + "秒";
        
        for (UUID playerId : queue) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 广播倒计时取消
     */
    private void broadcastCountdownCancel(String towerId) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        
        String message = ChatColor.RED + "挑战塔倒计时已取消";
        
        for (UUID playerId : queue) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 玩家离开队列时检查倒计时
     */
    private void checkCountdownOnPlayerLeave(String towerId) {
        List<UUID> queue = towerQueues.get(towerId);
        if (queue == null || queue.isEmpty()) {
            // 队列空了，取消倒计时
            cancelCountdown(towerId);
            return;
        }
        
        // 检查是否满足最少玩家数要求
        ChallengeTower tower = manager.getTower(towerId);
        if (tower != null) {
            int minPlayers = tower.getConfig().getMinPlayersToStart();
            if (queue.size() < minPlayers) {
                // 人数不足，取消倒计时
                cancelCountdown(towerId);
            }
        }
    }
}