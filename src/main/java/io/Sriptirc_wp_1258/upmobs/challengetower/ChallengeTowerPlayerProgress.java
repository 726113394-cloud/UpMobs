package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家挑战塔进度
 * 跟踪每个玩家在挑战塔中的进度和统计信息
 */
public class ChallengeTowerPlayerProgress {
    
    private final UUID playerId;
    private final String playerName;
    
    // 进度数据
    private int highestLevel = 0;                  // 达到的最高层数
    private int currentLevel = 1;                  // 当前挑战层数（从上次未完成的层开始）
    private int totalCompletions = 0;              // 总完成次数
    private int totalMonstersKilled = 0;           // 总击杀怪物数
    private int totalExperienceEarned = 0;         // 总获得经验
    private long lastChallengeTime = 0;            // 上次挑战时间（用于冷却）
    
    // 当前挑战数据
    private int currentWave = 1;                   // 当前波次
    private int monstersRemaining = 0;             // 剩余怪物数
    private boolean inChallenge = false;           // 是否在挑战中
    private String currentTowerId = null;          // 当前挑战塔ID
    
    // 统计数据
    private Map<Integer, Integer> levelCompletions = new HashMap<>();  // 每层完成次数
    private Map<String, Integer> monsterKillsByType = new HashMap<>(); // 按怪物类型统计
    
    public ChallengeTowerPlayerProgress(Player player) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
    }
    
    public ChallengeTowerPlayerProgress(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    /**
     * 开始新挑战
     */
    public void startChallenge(String towerId, int startingLevel) {
        this.currentTowerId = towerId;
        this.currentLevel = Math.max(1, startingLevel);
        this.currentWave = 1;
        this.monstersRemaining = 0;
        this.inChallenge = true;
        this.lastChallengeTime = System.currentTimeMillis();
    }
    
    /**
     * 完成一波怪物
     */
    public void completeWave() {
        currentWave++;
        if (currentWave > 3) {  // 默认每层3波
            completeLevel();
        }
    }
    
    /**
     * 完成一层
     */
    public void completeLevel() {
        // 更新最高层数
        if (currentLevel > highestLevel) {
            highestLevel = currentLevel;
        }
        
        // 更新完成次数
        levelCompletions.put(currentLevel, levelCompletions.getOrDefault(currentLevel, 0) + 1);
        totalCompletions++;
        
        // 进入下一层
        currentLevel++;
        currentWave = 1;
        
        // 保存进度
        saveProgress();
    }
    
    /**
     * 挑战失败
     */
    public void failChallenge() {
        // 保持当前层数（下次从同一层开始）
        inChallenge = false;
        currentTowerId = null;
        currentWave = 1;
        monstersRemaining = 0;
        
        // 保存进度
        saveProgress();
    }
    
    /**
     * 完成整个挑战（主动退出或完成最高层）
     */
    public void completeChallenge() {
        inChallenge = false;
        currentTowerId = null;
        currentWave = 1;
        monstersRemaining = 0;
        
        // 保存进度
        saveProgress();
    }
    
    /**
     * 添加击杀统计
     */
    public void addMonsterKill(String monsterType) {
        totalMonstersKilled++;
        monsterKillsByType.put(monsterType, 
            monsterKillsByType.getOrDefault(monsterType, 0) + 1);
    }
    
    /**
     * 添加经验奖励
     */
    public void addExperience(int experience) {
        totalExperienceEarned += experience;
    }
    
    /**
     * 检查冷却时间
     */
    public boolean isOnCooldown(int cooldownSeconds) {
        if (lastChallengeTime == 0) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastChallengeTime) / 1000;
        
        return elapsedSeconds < cooldownSeconds;
    }
    
    /**
     * 获取剩余冷却时间（秒）
     */
    public int getRemainingCooldown(int cooldownSeconds) {
        if (lastChallengeTime == 0) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastChallengeTime) / 1000;
        
        if (elapsedSeconds >= cooldownSeconds) {
            return 0;
        }
        
        return (int) (cooldownSeconds - elapsedSeconds);
    }
    
    /**
     * 重置进度
     */
    public void resetProgress() {
        highestLevel = 0;
        currentLevel = 1;
        totalCompletions = 0;
        totalMonstersKilled = 0;
        totalExperienceEarned = 0;
        levelCompletions.clear();
        monsterKillsByType.clear();
        inChallenge = false;
        currentTowerId = null;
        currentWave = 1;
        monstersRemaining = 0;
    }
    
    /**
     * 保存进度（在实际实现中会保存到文件或数据库）
     */
    public void saveProgress() {
        // 这里应该将进度保存到文件或数据库
        // 为了简化，我们只打印日志
        System.out.println("保存玩家进度: " + playerName + " 最高层数: " + highestLevel);
    }
    
    /**
     * 从数据加载进度
     */
    public void loadFromData(Map<String, Object> data) {
        if (data == null) return;
        
        highestLevel = (int) data.getOrDefault("highest_level", 0);
        currentLevel = (int) data.getOrDefault("current_level", 1);
        totalCompletions = (int) data.getOrDefault("total_completions", 0);
        totalMonstersKilled = (int) data.getOrDefault("total_monsters_killed", 0);
        totalExperienceEarned = (int) data.getOrDefault("total_experience_earned", 0);
        lastChallengeTime = (long) data.getOrDefault("last_challenge_time", 0L);
        
        // 加载层完成次数
        if (data.containsKey("level_completions")) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> completions = (Map<String, Integer>) data.get("level_completions");
            for (Map.Entry<String, Integer> entry : completions.entrySet()) {
                levelCompletions.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }
        }
        
        // 加载怪物击杀统计
        if (data.containsKey("monster_kills_by_type")) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> kills = (Map<String, Integer>) data.get("monster_kills_by_type");
            monsterKillsByType.putAll(kills);
        }
    }
    
    /**
     * 转换为数据以便保存
     */
    public Map<String, Object> toData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("player_id", playerId.toString());
        data.put("player_name", playerName);
        data.put("highest_level", highestLevel);
        data.put("current_level", currentLevel);
        data.put("total_completions", totalCompletions);
        data.put("total_monsters_killed", totalMonstersKilled);
        data.put("total_experience_earned", totalExperienceEarned);
        data.put("last_challenge_time", lastChallengeTime);
        
        // 转换层完成次数
        Map<String, Integer> completions = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : levelCompletions.entrySet()) {
            completions.put(entry.getKey().toString(), entry.getValue());
        }
        data.put("level_completions", completions);
        
        // 转换怪物击杀统计
        data.put("monster_kills_by_type", new HashMap<>(monsterKillsByType));
        
        return data;
    }
    
    // ========== Getter方法 ==========
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getHighestLevel() {
        return highestLevel;
    }
    
    public void setHighestLevel(int highestLevel) {
        this.highestLevel = highestLevel;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(1, currentLevel);
    }
    
    public int getTotalCompletions() {
        return totalCompletions;
    }
    
    public int getTotalMonstersKilled() {
        return totalMonstersKilled;
    }
    
    public int getTotalExperienceEarned() {
        return totalExperienceEarned;
    }
    
    public long getLastChallengeTime() {
        return lastChallengeTime;
    }
    
    public int getCurrentWave() {
        return currentWave;
    }
    
    public void setCurrentWave(int currentWave) {
        this.currentWave = Math.max(1, currentWave);
    }
    
    public int getMonstersRemaining() {
        return monstersRemaining;
    }
    
    public void setMonstersRemaining(int monstersRemaining) {
        this.monstersRemaining = Math.max(0, monstersRemaining);
    }
    
    public boolean isInChallenge() {
        return inChallenge;
    }
    
    public String getCurrentTowerId() {
        return currentTowerId;
    }
    
    public Map<Integer, Integer> getLevelCompletions() {
        return new HashMap<>(levelCompletions);
    }
    
    public Map<String, Integer> getMonsterKillsByType() {
        return new HashMap<>(monsterKillsByType);
    }
    
    /**
     * 获取进度摘要
     */
    public String getProgressSummary() {
        return String.format(
            "玩家: %s, 最高层数: %d, 当前层数: %d, 总完成: %d次, 总击杀: %d只, 总经验: %d",
            playerName, highestLevel, currentLevel, totalCompletions, 
            totalMonstersKilled, totalExperienceEarned
        );
    }
    
    /**
     * 获取当前挑战状态
     */
    public String getCurrentChallengeStatus() {
        if (!inChallenge) {
            return "不在挑战中";
        }
        
        return String.format("挑战中: 塔%s, 第%d层, 第%d波, 剩余怪物: %d",
            currentTowerId, currentLevel, currentWave, monstersRemaining);
    }
}