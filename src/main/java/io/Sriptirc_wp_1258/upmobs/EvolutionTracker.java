package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 进化追踪器
 * 记录每个怪物的进化状态和攻击数据
 */
public class EvolutionTracker {
    
    // 怪物进化状态
    public static class MobEvolutionData {
        private final LivingEntity mob;
        private int attackCount = 0;          // 攻击玩家次数
        private double totalDamage = 0.0;     // 对玩家造成的总伤害
        private int evolutionStage = 0;       // 当前进化阶段 (0=未进化)
        private long lastEvolutionTime = 0;   // 上次进化时间
        private boolean isEvolving = false;   // 是否正在进化中
        
        public MobEvolutionData(LivingEntity mob) {
            this.mob = mob;
        }
        
        public LivingEntity getMob() {
            return mob;
        }
        
        public int getAttackCount() {
            return attackCount;
        }
        
        public void incrementAttackCount() {
            this.attackCount++;
        }
        
        public double getTotalDamage() {
            return totalDamage;
        }
        
        public void addDamage(double damage) {
            this.totalDamage += damage;
        }
        
        public int getEvolutionStage() {
            return evolutionStage;
        }
        
        public void setEvolutionStage(int stage) {
            this.evolutionStage = stage;
            this.lastEvolutionTime = System.currentTimeMillis();
        }
        
        public long getLastEvolutionTime() {
            return lastEvolutionTime;
        }
        
        public boolean isEvolving() {
            return isEvolving;
        }
        
        public void setEvolving(boolean evolving) {
            isEvolving = evolving;
        }
        
        public boolean canEvolve(long cooldownMillis) {
            if (isEvolving) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            long timeSinceLastEvolution = currentTime - lastEvolutionTime;
            
            return timeSinceLastEvolution >= cooldownMillis;
        }
        
        public void reset() {
            attackCount = 0;
            totalDamage = 0.0;
            // 不清除进化阶段，只重置攻击数据
        }
    }
    
    private final Map<UUID, MobEvolutionData> mobDataMap = new HashMap<>();
    private final Map<UUID, Set<UUID>> playerTargets = new HashMap<>(); // 玩家被哪些怪物攻击过
    
    // 统计信息
    private int totalEvolutions = 0;      // 总进化次数
    private int maxEvolutionStage = 0;    // 最高进化阶段
    
    /**
     * 记录怪物攻击玩家
     */
    public void recordAttack(LivingEntity mob, Player player, double damage) {
        UUID mobId = mob.getUniqueId();
        UUID playerId = player.getUniqueId();
        
        // 获取或创建怪物数据
        MobEvolutionData data = mobDataMap.computeIfAbsent(mobId, k -> new MobEvolutionData(mob));
        
        // 更新攻击数据
        data.incrementAttackCount();
        data.addDamage(damage);
        
        // 记录玩家被攻击
        playerTargets.computeIfAbsent(playerId, k -> new HashSet<>()).add(mobId);
    }
    
    /**
     * 获取怪物进化数据
     */
    public MobEvolutionData getMobData(LivingEntity mob) {
        return mobDataMap.get(mob.getUniqueId());
    }
    
    /**
     * 获取怪物进化数据（如果不存在则创建）
     */
    public MobEvolutionData getOrCreateMobData(LivingEntity mob) {
        return mobDataMap.computeIfAbsent(mob.getUniqueId(), k -> new MobEvolutionData(mob));
    }
    
    /**
     * 检查怪物是否满足进化条件
     */
    public boolean checkEvolutionConditions(MobEvolutionData data, 
                                           int requiredAttacks, 
                                           double requiredDamage, 
                                           double healthThresholdPercent) {
        if (data == null) {
            return false;
        }
        
        // 检查攻击次数
        if (data.getAttackCount() < requiredAttacks) {
            return false;
        }
        
        // 检查总伤害
        if (data.getTotalDamage() < requiredDamage) {
            return false;
        }
        
        // 检查血量阈值（血量低于一定百分比时更容易进化）
        LivingEntity mob = data.getMob();
        double healthPercent = mob.getHealth() / mob.getMaxHealth() * 100;
        if (healthPercent > healthThresholdPercent) {
            return false; // 血量太高，不进化
        }
        
        return true;
    }
    
    /**
     * 开始进化
     */
    public void startEvolution(MobEvolutionData data) {
        if (data != null) {
            data.setEvolving(true);
        }
    }
    
    /**
     * 完成进化
     */
    public void completeEvolution(MobEvolutionData data, int newStage) {
        if (data != null) {
            data.setEvolutionStage(newStage);
            data.setEvolving(false);
            data.reset(); // 重置攻击数据，为下一次进化准备
        }
    }
    
    /**
     * 移除怪物数据（当怪物死亡时）
     */
    public void removeMobData(LivingEntity mob) {
        mobDataMap.remove(mob.getUniqueId());
        
        // 从所有玩家的目标列表中移除
        for (Set<UUID> mobIds : playerTargets.values()) {
            mobIds.remove(mob.getUniqueId());
        }
    }
    
    /**
     * 获取攻击过玩家的所有怪物
     */
    public Set<LivingEntity> getMobsAttackingPlayer(Player player) {
        Set<LivingEntity> mobs = new HashSet<>();
        Set<UUID> mobIds = playerTargets.get(player.getUniqueId());
        
        if (mobIds != null) {
            for (UUID mobId : mobIds) {
                MobEvolutionData data = mobDataMap.get(mobId);
                if (data != null && data.getMob() != null && !data.getMob().isDead()) {
                    mobs.add(data.getMob());
                }
            }
        }
        
        return mobs;
    }
    
    /**
     * 清理无效数据
     */
    public void cleanup() {
        // 清理死亡的怪物
        Iterator<Map.Entry<UUID, MobEvolutionData>> iterator = mobDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MobEvolutionData> entry = iterator.next();
            MobEvolutionData data = entry.getValue();
            if (data.getMob() == null || data.getMob().isDead()) {
                iterator.remove();
                
                // 从玩家目标列表中移除
                for (Set<UUID> mobIds : playerTargets.values()) {
                    mobIds.remove(entry.getKey());
                }
            }
        }
        
        // 清理离线玩家的数据
        Iterator<Map.Entry<UUID, Set<UUID>>> playerIterator = playerTargets.entrySet().iterator();
        while (playerIterator.hasNext()) {
            Map.Entry<UUID, Set<UUID>> entry = playerIterator.next();
            if (entry.getValue().isEmpty()) {
                playerIterator.remove();
            }
        }
    }
    
    /**
     * 获取所有正在进化的怪物
     */
    public List<LivingEntity> getEvolvingMobs() {
        List<LivingEntity> evolvingMobs = new ArrayList<>();
        for (MobEvolutionData data : mobDataMap.values()) {
            if (data.isEvolving() && data.getMob() != null && !data.getMob().isDead()) {
                evolvingMobs.add(data.getMob());
            }
        }
        return evolvingMobs;
    }
    
    /**
     * 获取统计数据
     */
    public String getStats() {
        int totalMobs = mobDataMap.size();
        int evolvingMobs = getEvolvingMobs().size();
        int evolvedMobs = 0;
        
        for (MobEvolutionData data : mobDataMap.values()) {
            if (data.getEvolutionStage() > 0) {
                evolvedMobs++;
            }
        }
        
        return String.format("怪物总数: %d, 正在进化: %d, 已进化: %d", 
                           totalMobs, evolvingMobs, evolvedMobs);
    }
    
    /**
     * 记录怪物进化
     */
    public void recordEvolution(LivingEntity monster, int stage) {
        if (monster == null || stage < 1) return;
        
        UUID mobId = monster.getUniqueId();
        MobEvolutionData data = mobDataMap.get(mobId);
        
        if (data == null) {
            data = new MobEvolutionData(monster);
            mobDataMap.put(mobId, data);
        }
        
        // 更新进化阶段
        data.evolutionStage = stage;
        data.lastEvolutionTime = System.currentTimeMillis();
        data.isEvolving = false; // 进化完成
        
        // 记录统计
        totalEvolutions++;
        if (stage > maxEvolutionStage) {
            maxEvolutionStage = stage;
        }
    }
}