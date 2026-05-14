package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 挑战塔配置
 * 存储挑战塔的各种参数和设置
 */
public class ChallengeTowerConfig {
    
    // 基础配置
    private int maxPlayers = 4;                    // 最大玩家数
    private int maxLevels = 18;                    // 最大层数
    private int wavesPerLevel = 3;                 // 每层波次数
    private int baseMonsterCount = 10;             // 基础怪物数量
    private int incrementPerLevel = 6;             // 每层增加怪物数量
    private int monsterPerPlayer = 3;              // 每多1人增加的怪物数量
    
    // 冷却时间
    private int cooldownSeconds = 300;             // 冷却时间（秒）
    
    // 队列倒计时配置
    private int queueCountdown = 30;               // 队列倒计时秒数
    private int minPlayersToStart = 1;             // 开始挑战所需最少玩家数
    private boolean enableCountdown = true;        // 是否启用倒计时
    
    // 奖励配置
    private int baseExperience = 100;              // 基础经验奖励
    private double experienceMultiplier = 1.5;     // 每层经验倍率
    private Map<Integer, List<ItemStack>> levelRewards = new HashMap<>();  // 层数奖励
    
    // 通关奖金配置
    private double baseReward = 8000.0;            // 基础层奖金（1-5层）
    private double advancedReward = 9000.0;        // 进阶层奖金（6-10层）
    private double eliteReward = 10000.0;          // 精英层奖金（11-15层）
    private double masterReward = 11000.0;         // 大师层奖金（16-18层）
    private boolean enableCompletionRewards = true; // 启用通关奖金
    
    // 怪物配置
    private List<EntityType> monsterTypes = new ArrayList<>();
    private boolean preventNaturalSpawn = true;    // 阻止自然生成
    
    // 区域保护
    private int teleportDelayTicks = 50;           // 传送延迟
    private boolean enableWarning = true;          // 启用警告
    
    // ========== 逐层加强配置 ==========
    
    // 基础属性增强
    private double baseHealthMultiplier = 1.0;     // 基础血量倍率
    private double healthMultiplierPerLevel = 0.5; // 每层血量倍率增加
    private double maxHealthMultiplier = 10.0;     // 最大血量倍率
    
    private double baseDamageMultiplier = 1.0;     // 基础伤害倍率
    private double damageMultiplierPerLevel = 0.4; // 每层伤害倍率增加
    private double maxDamageMultiplier = 8.0;      // 最大伤害倍率
    
    private double baseSpeedMultiplier = 1.0;      // 基础速度倍率
    private double speedMultiplierPerLevel = 0.1;  // 每层速度倍率增加
    private double maxSpeedMultiplier = 3.0;       // 最大速度倍率
    
    private double baseArmorMultiplier = 1.0;      // 基础护甲倍率
    private double armorMultiplierPerLevel = 0.2;  // 每层护甲倍率增加
    private double maxArmorMultiplier = 5.0;       // 最大护甲倍率
    
    // 特殊效果配置
    private int minLevelForEffects = 5;            // 开始出现特殊效果的最小层数
    private double effectChancePerLevel = 0.03;    // 每层特殊效果几率增加
    private double maxEffectChance = 0.5;          // 最大特殊效果几率
    
    // 进化系统配置
    private int minLevelForEvolution = 6;          // 开始出现进化怪物的最小层数（进阶层）
    private double evolutionChancePerLevel = 0.017; // 每层进化几率增加（达到30%）
    private double maxEvolutionChance = 0.3;       // 最大进化几率
    
    // 第四阶段怪物配置
    private int minLevelForStage4 = 11;            // 开始出现第四阶段怪物的最小层数（精英层）
    private double stage4ChancePerLevel = 0.008;   // 每层第四阶段怪物几率增加（达到15%）
    private double maxStage4Chance = 0.15;         // 最大第四阶段怪物几率
    
    // 升格怪物配置
    private double upgradedChancePerLevel = 0.05;  // 每层升格怪物几率增加
    private double maxUpgradedChance = 0.8;        // 最大升格怪物几率
    
    // 特殊效果类型配置
    private List<String> specialEffects = new ArrayList<>();
    
    public ChallengeTowerConfig() {
        // 初始化默认怪物类型
        initializeDefaultMonsterTypes();
        initializeDefaultRewards();
        initializeDefaultSpecialEffects();
    }
    
    /**
     * 初始化默认怪物类型
     */
    private void initializeDefaultMonsterTypes() {
        monsterTypes.add(EntityType.ZOMBIE);
        monsterTypes.add(EntityType.SKELETON);
        monsterTypes.add(EntityType.CREEPER);
        monsterTypes.add(EntityType.SPIDER);
        monsterTypes.add(EntityType.ENDERMAN);
        monsterTypes.add(EntityType.WITCH);
        monsterTypes.add(EntityType.BLAZE);
        monsterTypes.add(EntityType.PIGLIN);
        monsterTypes.add(EntityType.HUSK);
        monsterTypes.add(EntityType.STRAY);
    }
    
    /**
     * 初始化默认奖励
     */
    private void initializeDefaultRewards() {
        // 第5层奖励
        levelRewards.put(5, Arrays.asList(
            new ItemStack(Material.IRON_INGOT, 5),
            new ItemStack(Material.GOLD_INGOT, 3),
            new ItemStack(Material.DIAMOND, 1)
        ));
        
        // 第10层奖励
        levelRewards.put(10, Arrays.asList(
            new ItemStack(Material.DIAMOND, 3),
            new ItemStack(Material.EMERALD, 5),
            new ItemStack(Material.NETHERITE_SCRAP, 1)
        ));
        
        // 第15层奖励
        levelRewards.put(15, Arrays.asList(
            new ItemStack(Material.NETHERITE_INGOT, 1),
            new ItemStack(Material.DIAMOND_BLOCK, 1),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1)
        ));
        
        // 第18层奖励
        levelRewards.put(18, Arrays.asList(
            new ItemStack(Material.NETHERITE_BLOCK, 1),
            new ItemStack(Material.DRAGON_EGG, 1),
            new ItemStack(Material.TOTEM_OF_UNDYING, 1)
        ));
    }
    
    /**
     * 初始化默认特殊效果
     */
    private void initializeDefaultSpecialEffects() {
        specialEffects.add("FIRE_RESISTANCE");      // 火焰抗性
        specialEffects.add("KNOCKBACK_RESISTANCE"); // 击退抗性
        specialEffects.add("REGENERATION");         // 生命恢复
        specialEffects.add("INCREASE_DAMAGE");      // 力量效果
    }
    
    /**
     * 从配置节加载配置
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        maxPlayers = config.getInt("max_players", maxPlayers);
        maxLevels = config.getInt("max_levels", maxLevels);
        wavesPerLevel = config.getInt("waves_per_level", wavesPerLevel);
        baseMonsterCount = config.getInt("base_monster_count", baseMonsterCount);
        incrementPerLevel = config.getInt("increment_per_level", incrementPerLevel);
        monsterPerPlayer = config.getInt("monster_per_player", monsterPerPlayer);
        
        cooldownSeconds = config.getInt("cooldown_seconds", cooldownSeconds);
        
        baseExperience = config.getInt("base_experience", baseExperience);
        experienceMultiplier = config.getDouble("experience_multiplier", experienceMultiplier);
        
        // 加载通关奖金配置
        baseReward = config.getDouble("completion_rewards.base_reward", baseReward);
        advancedReward = config.getDouble("completion_rewards.advanced_reward", advancedReward);
        eliteReward = config.getDouble("completion_rewards.elite_reward", eliteReward);
        masterReward = config.getDouble("completion_rewards.master_reward", masterReward);
        enableCompletionRewards = config.getBoolean("completion_rewards.enabled", enableCompletionRewards);
        
        teleportDelayTicks = config.getInt("teleport_delay_ticks", teleportDelayTicks);
        enableWarning = config.getBoolean("enable_warning", enableWarning);
        preventNaturalSpawn = config.getBoolean("prevent_natural_spawn", preventNaturalSpawn);
        
        // 加载怪物类型
        if (config.contains("monster_types")) {
            monsterTypes.clear();
            for (String typeName : config.getStringList("monster_types")) {
                try {
                    EntityType type = EntityType.valueOf(typeName.toUpperCase());
                    monsterTypes.add(type);
                } catch (IllegalArgumentException e) {
                    // 忽略无效的怪物类型
                }
            }
        }
        
        // 如果怪物类型为空，使用默认的
        if (monsterTypes.isEmpty()) {
            initializeDefaultMonsterTypes();
        }
        
        // ========== 加载逐层加强配置 ==========
        
        // 基础属性增强
        baseHealthMultiplier = config.getDouble("level_scaling.base_health_multiplier", baseHealthMultiplier);
        healthMultiplierPerLevel = config.getDouble("level_scaling.health_multiplier_per_level", healthMultiplierPerLevel);
        maxHealthMultiplier = config.getDouble("level_scaling.max_health_multiplier", maxHealthMultiplier);
        
        baseDamageMultiplier = config.getDouble("level_scaling.base_damage_multiplier", baseDamageMultiplier);
        damageMultiplierPerLevel = config.getDouble("level_scaling.damage_multiplier_per_level", damageMultiplierPerLevel);
        maxDamageMultiplier = config.getDouble("level_scaling.max_damage_multiplier", maxDamageMultiplier);
        
        baseSpeedMultiplier = config.getDouble("level_scaling.base_speed_multiplier", baseSpeedMultiplier);
        speedMultiplierPerLevel = config.getDouble("level_scaling.speed_multiplier_per_level", speedMultiplierPerLevel);
        maxSpeedMultiplier = config.getDouble("level_scaling.max_speed_multiplier", maxSpeedMultiplier);
        
        baseArmorMultiplier = config.getDouble("level_scaling.base_armor_multiplier", baseArmorMultiplier);
        armorMultiplierPerLevel = config.getDouble("level_scaling.armor_multiplier_per_level", armorMultiplierPerLevel);
        maxArmorMultiplier = config.getDouble("level_scaling.max_armor_multiplier", maxArmorMultiplier);
        
        // 特殊效果配置
        minLevelForEffects = config.getInt("level_scaling.min_level_for_effects", minLevelForEffects);
        effectChancePerLevel = config.getDouble("level_scaling.effect_chance_per_level", effectChancePerLevel);
        maxEffectChance = config.getDouble("level_scaling.max_effect_chance", maxEffectChance);
        
        // 进化系统配置
        minLevelForEvolution = config.getInt("level_scaling.min_level_for_evolution", minLevelForEvolution);
        evolutionChancePerLevel = config.getDouble("level_scaling.evolution_chance_per_level", evolutionChancePerLevel);
        maxEvolutionChance = config.getDouble("level_scaling.max_evolution_chance", maxEvolutionChance);
        
        // 第四阶段怪物配置
        minLevelForStage4 = config.getInt("level_scaling.min_level_for_stage4", minLevelForStage4);
        stage4ChancePerLevel = config.getDouble("level_scaling.stage4_chance_per_level", stage4ChancePerLevel);
        maxStage4Chance = config.getDouble("level_scaling.max_stage4_chance", maxStage4Chance);
        
        // 升格怪物配置
        upgradedChancePerLevel = config.getDouble("level_scaling.upgraded_chance_per_level", upgradedChancePerLevel);
        maxUpgradedChance = config.getDouble("level_scaling.max_upgraded_chance", maxUpgradedChance);
        
        // 特殊效果类型
        if (config.contains("level_scaling.special_effects")) {
            specialEffects.clear();
            specialEffects.addAll(config.getStringList("level_scaling.special_effects"));
        }
        
        // 如果特殊效果为空，使用默认的
        if (specialEffects.isEmpty()) {
            initializeDefaultSpecialEffects();
        }
    }
    
    /**
     * 保存配置到配置节
     */
    public void saveToConfig(YamlConfiguration config, String path) {
        config.set(path + ".max_players", maxPlayers);
        config.set(path + ".max_levels", maxLevels);
        config.set(path + ".waves_per_level", wavesPerLevel);
        config.set(path + ".base_monster_count", baseMonsterCount);
        config.set(path + ".increment_per_level", incrementPerLevel);
        config.set(path + ".monster_per_player", monsterPerPlayer);
        
        config.set(path + ".cooldown_seconds", cooldownSeconds);
        
        config.set(path + ".base_experience", baseExperience);
        config.set(path + ".experience_multiplier", experienceMultiplier);
        
        // 保存通关奖金配置
        config.set(path + ".completion_rewards.enabled", enableCompletionRewards);
        config.set(path + ".completion_rewards.base_reward", baseReward);
        config.set(path + ".completion_rewards.advanced_reward", advancedReward);
        config.set(path + ".completion_rewards.elite_reward", eliteReward);
        config.set(path + ".completion_rewards.master_reward", masterReward);
        
        config.set(path + ".teleport_delay_ticks", teleportDelayTicks);
        config.set(path + ".enable_warning", enableWarning);
        config.set(path + ".prevent_natural_spawn", preventNaturalSpawn);
        
        // 保存怪物类型
        List<String> monsterTypeNames = new ArrayList<>();
        for (EntityType type : monsterTypes) {
            monsterTypeNames.add(type.name());
        }
        config.set(path + ".monster_types", monsterTypeNames);
        
        // ========== 保存逐层加强配置 ==========
        
        // 基础属性增强
        config.set(path + ".level_scaling.base_health_multiplier", baseHealthMultiplier);
        config.set(path + ".level_scaling.health_multiplier_per_level", healthMultiplierPerLevel);
        config.set(path + ".level_scaling.max_health_multiplier", maxHealthMultiplier);
        
        config.set(path + ".level_scaling.base_damage_multiplier", baseDamageMultiplier);
        config.set(path + ".level_scaling.damage_multiplier_per_level", damageMultiplierPerLevel);
        config.set(path + ".level_scaling.max_damage_multiplier", maxDamageMultiplier);
        
        config.set(path + ".level_scaling.base_speed_multiplier", baseSpeedMultiplier);
        config.set(path + ".level_scaling.speed_multiplier_per_level", speedMultiplierPerLevel);
        config.set(path + ".level_scaling.max_speed_multiplier", maxSpeedMultiplier);
        
        config.set(path + ".level_scaling.base_armor_multiplier", baseArmorMultiplier);
        config.set(path + ".level_scaling.armor_multiplier_per_level", armorMultiplierPerLevel);
        config.set(path + ".level_scaling.max_armor_multiplier", maxArmorMultiplier);
        
        // 特殊效果配置
        config.set(path + ".level_scaling.min_level_for_effects", minLevelForEffects);
        config.set(path + ".level_scaling.effect_chance_per_level", effectChancePerLevel);
        config.set(path + ".level_scaling.max_effect_chance", maxEffectChance);
        
        // 进化系统配置
        config.set(path + ".level_scaling.min_level_for_evolution", minLevelForEvolution);
        config.set(path + ".level_scaling.evolution_chance_per_level", evolutionChancePerLevel);
        config.set(path + ".level_scaling.max_evolution_chance", maxEvolutionChance);
        
        // 第四阶段怪物配置
        config.set(path + ".level_scaling.min_level_for_stage4", minLevelForStage4);
        config.set(path + ".level_scaling.stage4_chance_per_level", stage4ChancePerLevel);
        config.set(path + ".level_scaling.max_stage4_chance", maxStage4Chance);
        
        // 升格怪物配置
        config.set(path + ".level_scaling.upgraded_chance_per_level", upgradedChancePerLevel);
        config.set(path + ".level_scaling.max_upgraded_chance", maxUpgradedChance);
        
        // 特殊效果类型
        config.set(path + ".level_scaling.special_effects", specialEffects);
    }
    
    // ========== Getter方法 ==========
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public int getMaxLevels() {
        return maxLevels;
    }
    
    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }
    
    public int getWavesPerLevel() {
        return wavesPerLevel;
    }
    
    public void setWavesPerLevel(int wavesPerLevel) {
        this.wavesPerLevel = wavesPerLevel;
    }
    
    public int getBaseMonsterCount() {
        return baseMonsterCount;
    }
    
    public void setBaseMonsterCount(int baseMonsterCount) {
        this.baseMonsterCount = baseMonsterCount;
    }
    
    public int getIncrementPerLevel() {
        return incrementPerLevel;
    }
    
    public void setIncrementPerLevel(int incrementPerLevel) {
        this.incrementPerLevel = incrementPerLevel;
    }
    
    public int getMonsterPerPlayer() {
        return monsterPerPlayer;
    }
    
    public void setMonsterPerPlayer(int monsterPerPlayer) {
        this.monsterPerPlayer = monsterPerPlayer;
    }
    
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
    
    // ========== 队列倒计时配置 Getter方法 ==========
    
    public int getQueueCountdown() {
        return queueCountdown;
    }
    
    public void setQueueCountdown(int queueCountdown) {
        this.queueCountdown = queueCountdown;
    }
    
    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }
    
    public void setMinPlayersToStart(int minPlayersToStart) {
        this.minPlayersToStart = minPlayersToStart;
    }
    
    public boolean isEnableCountdown() {
        return enableCountdown;
    }
    
    public void setEnableCountdown(boolean enableCountdown) {
        this.enableCountdown = enableCountdown;
    }
    
    public int getBaseExperience() {
        return baseExperience;
    }
    
    public void setBaseExperience(int baseExperience) {
        this.baseExperience = baseExperience;
    }
    
    public double getExperienceMultiplier() {
        return experienceMultiplier;
    }
    
    public void setExperienceMultiplier(double experienceMultiplier) {
        this.experienceMultiplier = experienceMultiplier;
    }
    
    // ========== 通关奖金配置 Getter方法 ==========
    
    public double getBaseReward() {
        return baseReward;
    }
    
    public void setBaseReward(double baseReward) {
        this.baseReward = baseReward;
    }
    
    public double getAdvancedReward() {
        return advancedReward;
    }
    
    public void setAdvancedReward(double advancedReward) {
        this.advancedReward = advancedReward;
    }
    
    public double getEliteReward() {
        return eliteReward;
    }
    
    public void setEliteReward(double eliteReward) {
        this.eliteReward = eliteReward;
    }
    
    public double getMasterReward() {
        return masterReward;
    }
    
    public void setMasterReward(double masterReward) {
        this.masterReward = masterReward;
    }
    
    public boolean isEnableCompletionRewards() {
        return enableCompletionRewards;
    }
    
    public void setEnableCompletionRewards(boolean enableCompletionRewards) {
        this.enableCompletionRewards = enableCompletionRewards;
    }
    
    /**
     * 获取指定层数的通关奖金
     */
    public double getCompletionRewardForLevel(int level) {
        if (!enableCompletionRewards) {
            return 0.0;
        }
        
        if (level >= 1 && level <= 5) {
            return baseReward;          // 基础层：8000
        } else if (level >= 6 && level <= 10) {
            return advancedReward;      // 进阶层：9000
        } else if (level >= 11 && level <= 15) {
            return eliteReward;         // 精英层：10000
        } else if (level >= 16 && level <= 18) {
            return masterReward;        // 大师层：11000
        }
        
        return 0.0;  // 其他层数（如通天层）没有奖金
    }
    
    public int getTeleportDelayTicks() {
        return teleportDelayTicks;
    }
    
    public void setTeleportDelayTicks(int teleportDelayTicks) {
        this.teleportDelayTicks = teleportDelayTicks;
    }
    
    public boolean isEnableWarning() {
        return enableWarning;
    }
    
    public void setEnableWarning(boolean enableWarning) {
        this.enableWarning = enableWarning;
    }
    
    public boolean isPreventNaturalSpawn() {
        return preventNaturalSpawn;
    }
    
    public void setPreventNaturalSpawn(boolean preventNaturalSpawn) {
        this.preventNaturalSpawn = preventNaturalSpawn;
    }
    
    public List<EntityType> getMonsterTypes() {
        return new ArrayList<>(monsterTypes);
    }
    
    public void setMonsterTypes(List<EntityType> monsterTypes) {
        this.monsterTypes = new ArrayList<>(monsterTypes);
    }
    
    // ========== 逐层加强计算方法 ==========
    
    /**
     * 获取指定层数的血量倍率
     */
    public double getHealthMultiplierForLevel(int level) {
        if (level < 1) level = 1;
        double multiplier = baseHealthMultiplier + (healthMultiplierPerLevel * (level - 1));
        return Math.min(multiplier, maxHealthMultiplier);
    }
    
    /**
     * 获取指定层数的伤害倍率
     */
    public double getDamageMultiplierForLevel(int level) {
        if (level < 1) level = 1;
        double multiplier = baseDamageMultiplier + (damageMultiplierPerLevel * (level - 1));
        return Math.min(multiplier, maxDamageMultiplier);
    }
    
    /**
     * 获取指定层数的速度倍率
     */
    public double getSpeedMultiplierForLevel(int level) {
        if (level < 1) level = 1;
        double multiplier = baseSpeedMultiplier + (speedMultiplierPerLevel * (level - 1));
        return Math.min(multiplier, maxSpeedMultiplier);
    }
    
    /**
     * 获取指定层数的护甲倍率
     */
    public double getArmorMultiplierForLevel(int level) {
        if (level < 1) level = 1;
        double multiplier = baseArmorMultiplier + (armorMultiplierPerLevel * (level - 1));
        return Math.min(multiplier, maxArmorMultiplier);
    }
    
    /**
     * 获取指定层数的进化几率
     */
    public double getEvolutionChanceForLevel(int level) {
        if (level < minLevelForEvolution) return 0.0;
        double chance = evolutionChancePerLevel * (level - minLevelForEvolution + 1);
        return Math.min(chance, maxEvolutionChance);
    }
    
    /**
     * 获取指定层数的第四阶段怪物几率
     */
    public double getStage4ChanceForLevel(int level) {
        if (level < minLevelForStage4) return 0.0;
        double chance = stage4ChancePerLevel * (level - minLevelForStage4 + 1);
        return Math.min(chance, maxStage4Chance);
    }
    
    /**
     * 获取指定层数的特殊效果几率
     */
    public double getEffectChanceForLevel(int level) {
        if (level < minLevelForEffects) return 0.0;
        double chance = effectChancePerLevel * (level - minLevelForEffects + 1);
        return Math.min(chance, maxEffectChance);
    }
    
    /**
     * 获取指定层数的升格怪物几率
     */
    public double getUpgradedChanceForLevel(int level) {
        double chance = upgradedChancePerLevel * level;
        return Math.min(chance, maxUpgradedChance);
    }
    
    /**
     * 获取随机特殊效果
     */
    public String getRandomSpecialEffect(Random random) {
        if (specialEffects.isEmpty()) return null;
        return specialEffects.get(random.nextInt(specialEffects.size()));
    }
    
    /**
     * 获取指定层数的怪物数量
     */
    public int getMonsterCountForLevel(int level, int playerCount) {
        if (level < 1) level = 1;
        if (playerCount < 1) playerCount = 1;
        
        // 基础怪物数量 + 层数增加 + 玩家数量增加
        int baseCount = baseMonsterCount + (incrementPerLevel * (level - 1));
        int playerBonus = monsterPerPlayer * (playerCount - 1);
        
        return baseCount + playerBonus;
    }
    
    /**
     * 获取指定层数的经验奖励
     */
    public int getExperienceForLevel(int level) {
        return (int) (baseExperience * Math.pow(experienceMultiplier, level - 1));
    }
    
    /**
     * 获取指定层数的奖励物品
     */
    public List<ItemStack> getRewardsForLevel(int level) {
        // 查找最接近的奖励层
        int closestLevel = 0;
        for (int rewardLevel : levelRewards.keySet()) {
            if (rewardLevel <= level && rewardLevel > closestLevel) {
                closestLevel = rewardLevel;
            }
        }
        
        if (closestLevel > 0) {
            List<ItemStack> rewards = levelRewards.get(closestLevel);
            if (rewards != null) {
                List<ItemStack> cloned = new ArrayList<>();
                for (ItemStack item : rewards) {
                    cloned.add(item.clone());
                }
                return cloned;
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 获取随机怪物类型
     */
    public EntityType getRandomMonsterType(Random random) {
        if (monsterTypes.isEmpty()) {
            return EntityType.ZOMBIE;
        }
        return monsterTypes.get(random.nextInt(monsterTypes.size()));
    }
    
    /**
     * 获取开始出现特殊效果的最小层数
     */
    public int getMinLevelForEffects() {
        return minLevelForEffects;
    }
    
    /**
     * 获取开始出现进化怪物的最小层数
     */
    public int getMinLevelForEvolution() {
        return minLevelForEvolution;
    }
    
    /**
     * 获取开始出现第四阶段怪物的最小层数
     */
    public int getMinLevelForStage4() {
        return minLevelForStage4;
    }
    
    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        return String.format(
            "挑战塔配置: 最大玩家=%d, 最大层数=%d, 每层波次=%d, 基础怪物=%d, 每层增加=%d, 每玩家增加=%d, 冷却=%d秒\n" +
            "逐层加强: 血量倍率=%.1f-%.1f, 伤害倍率=%.1f-%.1f, 速度倍率=%.1f-%.1f, 护甲倍率=%.1f-%.1f\n" +
            "特殊效果: 最小层数=%d, 最大几率=%.1f%%, 效果数量=%d\n" +
            "进化系统: 最小层数=%d, 最大几率=%.1f%%\n" +
            "第四阶段: 最小层数=%d, 最大几率=%.1f%%",
            maxPlayers, maxLevels, wavesPerLevel, baseMonsterCount, 
            incrementPerLevel, monsterPerPlayer, cooldownSeconds,
            baseHealthMultiplier, maxHealthMultiplier,
            baseDamageMultiplier, maxDamageMultiplier,
            baseSpeedMultiplier, maxSpeedMultiplier,
            baseArmorMultiplier, maxArmorMultiplier,
            minLevelForEffects, maxEffectChance * 100, specialEffects.size(),
            minLevelForEvolution, maxEvolutionChance * 100,
            minLevelForStage4, maxStage4Chance * 100
        );
    }
}