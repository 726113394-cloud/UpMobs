package io.Sriptirc_wp_1258.upmobs.challengetower;

import io.Sriptirc_wp_1258.upmobs.MobManager;
import io.Sriptirc_wp_1258.upmobs.EvolutionManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.Random;

/**
 * 挑战塔怪物增强器
 * 负责根据层数应用逐层加强效果
 */
public class ChallengeTowerMonsterEnhancer {
    
    private final ChallengeTowerConfig config;
    private final MobManager mobManager;
    private final EvolutionManager evolutionManager;
    private final Random random;
    
    public ChallengeTowerMonsterEnhancer(ChallengeTowerConfig config, MobManager mobManager, EvolutionManager evolutionManager) {
        this.config = config;
        this.mobManager = mobManager;
        this.evolutionManager = evolutionManager;
        this.random = new Random();
    }
    
    /**
     * 增强怪物（根据层数应用所有增强效果）
     */
    public void enhanceMonster(LivingEntity monster, int level) {
        if (monster == null || level < 1) return;
        
        // 1. 应用基础属性增强
        applyAttributeEnhancements(monster, level);
        
        // 2. 应用特殊效果
        applySpecialEffects(monster, level);
        
        // 3. 应用进化系统
        applyEvolutionSystem(monster, level);
        
        // 4. 应用第四阶段怪物
        applyStage4Enhancement(monster, level);
        
        // 5. 应用升格怪物
        applyUpgradedEnhancement(monster, level);
        
        // 6. 添加挑战塔标签
        addChallengeTags(monster, level);
    }
    
    /**
     * 应用基础属性增强
     */
    private void applyAttributeEnhancements(LivingEntity monster, int level) {
        // 获取当前层数的属性倍率
        double healthMultiplier = config.getHealthMultiplierForLevel(level);
        double damageMultiplier = config.getDamageMultiplierForLevel(level);
        double speedMultiplier = config.getSpeedMultiplierForLevel(level);
        double armorMultiplier = config.getArmorMultiplierForLevel(level);
        
        // 应用血量增强
        AttributeInstance maxHealth = monster.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = maxHealth.getBaseValue();
            maxHealth.setBaseValue(baseHealth * healthMultiplier);
            monster.setHealth(maxHealth.getValue());
        }
        
        // 应用攻击伤害增强
        AttributeInstance attackDamage = monster.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage != null) {
            double baseDamage = attackDamage.getBaseValue();
            attackDamage.setBaseValue(baseDamage * damageMultiplier);
        }
        
        // 应用移动速度增强
        AttributeInstance movementSpeed = monster.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            double baseSpeed = movementSpeed.getBaseValue();
            movementSpeed.setBaseValue(baseSpeed * speedMultiplier);
        }
        
        // 应用护甲增强
        AttributeInstance armor = monster.getAttribute(Attribute.ARMOR);
        if (armor != null) {
            double baseArmor = armor.getBaseValue();
            armor.setBaseValue(baseArmor * armorMultiplier);
        }
        
        // 应用击退抗性增强（随层数增加）
        AttributeInstance knockbackResistance = monster.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackResistance != null) {
            double baseResistance = knockbackResistance.getBaseValue();
            double resistanceBonus = Math.min(0.8, (level - 1) * 0.05); // 每层增加5%，最大80%
            knockbackResistance.setBaseValue(baseResistance + resistanceBonus);
        }
    }
    
    /**
     * 应用特殊效果
     */
    private void applySpecialEffects(LivingEntity monster, int level) {
        // 检查是否达到特殊效果的最小层数
        if (level < config.getMinLevelForEffects()) return;
        
        // 计算特殊效果几率
        double effectChance = config.getEffectChanceForLevel(level);
        if (random.nextDouble() > effectChance) return;
        
        // 获取随机特殊效果
        String effectName = config.getRandomSpecialEffect(random);
        if (effectName == null) return;
        
        // 应用特殊效果
        try {
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType != null) {
                // 根据层数设置效果等级和持续时间
                int amplifier = Math.min(2, (level - config.getMinLevelForEffects()) / 3); // 每3层增加1级，最大2级
                int duration = 20 * 60 * 10; // 10分钟（挑战塔内永久有效）
                
                monster.addPotionEffect(new PotionEffect(effectType, duration, amplifier, true, true));
                
                // 添加效果标签
                monster.addScoreboardTag("tower_effect_" + effectName.toLowerCase());
            }
        } catch (IllegalArgumentException e) {
            // 忽略无效的效果类型
        }
    }
    
    /**
     * 应用进化系统
     */
    private void applyEvolutionSystem(LivingEntity monster, int level) {
        // 检查是否达到进化的最小层数
        if (level < config.getMinLevelForEvolution()) return;
        
        // 计算进化几率
        double evolutionChance = config.getEvolutionChanceForLevel(level);
        if (random.nextDouble() > evolutionChance) return;
        
        // 应用进化效果
        if (evolutionManager != null) {
            // 计算进化阶段（根据层数）
            int maxStages = 3;
            int evolutionStage = Math.min(maxStages, (level - config.getMinLevelForEvolution()) / 2 + 1);
            
            // 应用进化
            evolutionManager.applyEvolution(monster, evolutionStage);
            
            // 添加进化标签
            monster.addScoreboardTag("tower_evolution_stage_" + evolutionStage);
            monster.addScoreboardTag("tower_evolved");
        }
    }
    
    /**
     * 应用第四阶段怪物增强
     */
    private void applyStage4Enhancement(LivingEntity monster, int level) {
        // 检查是否达到第四阶段怪物的最小层数
        if (level < config.getMinLevelForStage4()) return;
        
        // 计算第四阶段怪物几率
        double stage4Chance = config.getStage4ChanceForLevel(level);
        if (random.nextDouble() > stage4Chance) return;
        
        // 应用第四阶段增强
        if (mobManager != null) {
            // 应用第四阶段属性增强
            monster.addScoreboardTag("stage4");
            monster.addScoreboardTag("tower_stage4");
            
            // 添加发光效果
            monster.setGlowing(true);
            
            // 添加精英标签
            monster.addScoreboardTag("elite");
            
            // 应用额外的属性增强（第四阶段额外增强）
            AttributeInstance maxHealth = monster.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(maxHealth.getBaseValue() * 1.5); // 额外50%血量
                monster.setHealth(maxHealth.getValue());
            }
            
            AttributeInstance attackDamage = monster.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(attackDamage.getBaseValue() * 1.3); // 额外30%伤害
            }
        }
    }
    
    /**
     * 应用升格怪物增强
     */
    private void applyUpgradedEnhancement(LivingEntity monster, int level) {
        // 计算升格怪物几率
        double upgradedChance = config.getUpgradedChanceForLevel(level);
        if (random.nextDouble() > upgradedChance) return;
        
        // 应用升格增强
        if (mobManager != null) {
            mobManager.upgradeMob(monster);
            monster.addScoreboardTag("tower_upgraded");
        }
    }
    
    /**
     * 添加挑战塔标签
     */
    private void addChallengeTags(LivingEntity monster, int level) {
        monster.addScoreboardTag("challenge_tower_monster");
        monster.addScoreboardTag("tower_level_" + level);
        
        // 根据层数添加难度标签
        if (level <= 5) {
            monster.addScoreboardTag("tower_difficulty_basic");
        } else if (level <= 10) {
            monster.addScoreboardTag("tower_difficulty_advanced");
        } else if (level <= 15) {
            monster.addScoreboardTag("tower_difficulty_elite");
        } else {
            monster.addScoreboardTag("tower_difficulty_master");
        }
    }
    
    /**
     * 获取层数增强信息（用于显示）
     */
    public String getLevelEnhancementInfo(int level) {
        StringBuilder info = new StringBuilder();
        info.append("§6=== 第").append(level).append("层增强信息 ===\n");
        
        // 属性增强
        info.append("§e属性增强:\n");
        info.append("§7- 血量倍率: §f").append(String.format("%.1f", config.getHealthMultiplierForLevel(level))).append("x\n");
        info.append("§7- 伤害倍率: §f").append(String.format("%.1f", config.getDamageMultiplierForLevel(level))).append("x\n");
        info.append("§7- 速度倍率: §f").append(String.format("%.1f", config.getSpeedMultiplierForLevel(level))).append("x\n");
        info.append("§7- 护甲倍率: §f").append(String.format("%.1f", config.getArmorMultiplierForLevel(level))).append("x\n");
        
        // 特殊效果
        if (level >= config.getMinLevelForEffects()) {
            double effectChance = config.getEffectChanceForLevel(level);
            info.append("§e特殊效果: §f").append(String.format("%.1f", effectChance * 100)).append("%\n");
        }
        
        // 进化系统
        if (level >= config.getMinLevelForEvolution()) {
            double evolutionChance = config.getEvolutionChanceForLevel(level);
            info.append("§e进化怪物: §f").append(String.format("%.1f", evolutionChance * 100)).append("%\n");
        }
        
        // 第四阶段怪物
        if (level >= config.getMinLevelForStage4()) {
            double stage4Chance = config.getStage4ChanceForLevel(level);
            info.append("§e第四阶段: §f").append(String.format("%.1f", stage4Chance * 100)).append("%\n");
        }
        
        // 升格怪物
        double upgradedChance = config.getUpgradedChanceForLevel(level);
        info.append("§e升格怪物: §f").append(String.format("%.1f", upgradedChance * 100)).append("%\n");
        
        return info.toString();
    }
}