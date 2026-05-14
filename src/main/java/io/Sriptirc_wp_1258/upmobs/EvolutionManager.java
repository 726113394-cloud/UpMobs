package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * 进化管理器
 * 处理怪物的吸血进化逻辑
 */
public class EvolutionManager {
    
    private final Upmobs plugin;
    private final ConfigManager configManager;
    private final MobManager mobManager;
    private final EvolutionTracker tracker;
    private final EvolutionEffects effects;
    
    // 进化配置
    private int requiredAttacks = 3;          // 所需攻击次数
    private double requiredDamage = 10.0;     // 所需总伤害
    private double healthThreshold = 70.0;    // 血量阈值百分比（低于此值才进化）
    private long evolutionCooldown = 30000;   // 进化冷却时间（毫秒）
    private int maxEvolutionStages = 4;       // 最大进化阶段
    private int evolutionDuration = 60;       // 进化持续时间（tick）
    
    public EvolutionManager(Upmobs plugin, ConfigManager configManager, MobManager mobManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mobManager = mobManager;
        this.tracker = new EvolutionTracker();
        this.effects = new EvolutionEffects(plugin);
        
        loadConfig();
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        requiredAttacks = plugin.getConfig().getInt("evolution.required_attacks", 3);
        requiredDamage = plugin.getConfig().getDouble("evolution.required_damage", 10.0);
        healthThreshold = plugin.getConfig().getDouble("evolution.health_threshold", 70.0);
        evolutionCooldown = plugin.getConfig().getLong("evolution.cooldown", 30000);
        maxEvolutionStages = plugin.getConfig().getInt("evolution.max_stages", 3);
        evolutionDuration = plugin.getConfig().getInt("evolution.duration", 60);
    }
    
    /**
     * 记录怪物攻击玩家
     */
    public void recordAttack(LivingEntity mob, Player player, double damage) {
        if (mob == null || player == null || damage <= 0) {
            return;
        }
        
        // 记录攻击
        tracker.recordAttack(mob, player, damage);
        
        // 检查是否满足进化条件
        EvolutionTracker.MobEvolutionData data = tracker.getMobData(mob);
        if (data != null && data.canEvolve(evolutionCooldown)) {
            checkAndStartEvolution(mob, player, data);
        }
    }
    
    /**
     * 检查并开始进化
     */
    private void checkAndStartEvolution(LivingEntity mob, Player player, EvolutionTracker.MobEvolutionData data) {
        // 检查进化条件
        boolean canEvolve = tracker.checkEvolutionConditions(data, requiredAttacks, requiredDamage, healthThreshold);
        
        if (!canEvolve) {
            return;
        }
        
        // 检查是否达到最大进化阶段
        int currentStage = data.getEvolutionStage();
        if (currentStage >= maxEvolutionStages) {
            return; // 已达到最大进化阶段
        }
        
        // 开始进化
        startEvolution(mob, player, data, currentStage + 1);
    }
    
    /**
     * 开始进化过程
     */
    private void startEvolution(LivingEntity mob, Player player, EvolutionTracker.MobEvolutionData data, int newStage) {
        // 标记为正在进化
        tracker.startEvolution(data);
        
        // 播放进化开始效果
        effects.playEvolutionStartEffects(mob, player, newStage);
        
        // 开始渐进式进化（简化版）
        startSimpleEvolution(mob, player, newStage);
    }
    
    /**
     * 开始简化版进化
     */
    private void startSimpleEvolution(LivingEntity mob, Player player, int stage) {
        new BukkitRunnable() {
            int tick = 0;
            final int totalTicks = evolutionDuration;
            
            @Override
            public void run() {
                if (mob.isDead() || !mob.isValid()) {
                    cancelEvolution(mob);
                    this.cancel();
                    return;
                }
                
                float progress = (float) tick / totalTicks;
                
                // 播放进度效果
                effects.playEvolutionProgressEffects(mob, stage, progress);
                
                tick++;
                
                if (tick >= totalTicks) {
                    // 进化完成
                    completeEvolution(mob, player, stage);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * 完成进化
     */
    private void completeEvolution(LivingEntity mob, Player player, int stage) {
        // 应用进化属性 - 增强版
        boolean upgraded = mobManager.upgradeMob(mob);
        
        // 播放进化完成效果
        effects.playEvolutionCompleteEffects(mob, player, stage);
        
        // 播放进化成功音效
        playEvolutionSuccessSound(mob.getLocation(), stage);
        
        // 更新追踪器
        EvolutionTracker.MobEvolutionData data = tracker.getMobData(mob);
        if (data != null) {
            tracker.completeEvolution(data, stage);
        }
        
        // 记录进化日志
        plugin.getLogger().info(String.format("怪物 %s 已完成第 %d 阶段进化 (升级结果: %s)", 
            mob.getType().name(), stage));
    }
    
    /**
     * 取消进化
     */
    private void cancelEvolution(LivingEntity mob) {
        EvolutionTracker.MobEvolutionData data = tracker.getMobData(mob);
        if (data != null) {
            data.setEvolving(false);
        }
    }
    
    /**
     * 获取生物当前属性（简化版）
     */
    private MobAttributes getCurrentAttributes(LivingEntity mob) {
        return new MobAttributes(mob.getType());
    }
    
    /**
     * 清理数据
     */
    public void cleanup() {
        tracker.cleanup();
    }
    
    /**
     * 播放进化成功音效
     */
    private void playEvolutionSuccessSound(Location location, int stage) {
        org.bukkit.World world = location.getWorld();
        if (world == null) return;
        
        org.bukkit.Sound sound;
        float pitch;
        
        switch (stage) {
            case 1:
                sound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 1.0f;
                break;
            case 2:
                sound = org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL;
                pitch = 0.8f;
                break;
            case 3:
                sound = org.bukkit.Sound.ENTITY_WITHER_SPAWN;
                pitch = 0.6f;
                break;
            default:
                sound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 0.9f;
        }
        
        world.playSound(location, sound, 1.5f, pitch);
    }
    
    /**
     * 自然刷新升格怪物（从配置读取几率）
     */
    public void spawnUpgradedMobNaturally(org.bukkit.Location location, org.bukkit.entity.EntityType entityType) {
        if (location == null || entityType == null) return;
        
        // 检查是否启用自然刷新
        boolean enabled = plugin.getConfig().getBoolean("natural_upgrade.enabled", true);
        if (!enabled) return;
        
        // 从配置读取刷新几率
        double spawnChance = plugin.getConfig().getDouble("natural_upgrade.spawn_chance", 30.0) / 100.0;
        
        // 检查是否为第四阶段怪物（从配置读取几率）
        double stage4Chance = plugin.getConfig().getDouble("stage4.spawn_chance", 8.0) / 100.0;
        
        if (Math.random() < stage4Chance) {
            // 生成第四阶段怪物
            spawnStage4Mob(location, entityType);
        } else if (Math.random() < spawnChance) {
            // 按配置几率生成普通升格怪物
            try {
                org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) location.getWorld().spawnEntity(location, entityType);
                
                // 立即应用升级效果
                boolean upgraded = mobManager.upgradeMob(mob);
                
                if (upgraded) {
                    // 播放生成音效
                    location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.8f, 0.7f);
                    
                    // 生成粒子效果
                    for (int i = 0; i < 10; i++) {
                        double offsetX = Math.random() * 2 - 1;
                        double offsetY = Math.random() * 2;
                        double offsetZ = Math.random() * 2 - 1;
                        
                        location.getWorld().spawnParticle(
                            org.bukkit.Particle.ANGRY_VILLAGER,
                            location.clone().add(offsetX, offsetY, offsetZ),
                            1, 0, 0, 0, 0
                        );
                    }
                    
                    // 获取中文名称
                    String mobName = getMobDisplayName(mob);
                    plugin.getLogger().info(String.format("自然刷新升格怪物: %s (%s) 在 %s", 
                        mobName, entityType.name(), location));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("自然刷新升格怪物失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成第四阶段怪物
     */
    private void spawnStage4Mob(org.bukkit.Location location, org.bukkit.entity.EntityType entityType) {
        try {
            org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) location.getWorld().spawnEntity(location, entityType);
            
            // 应用第四阶段属性增强
            applyStage4Attributes(mob);
            
            // 应用第四阶段特效
            applyStage4Effects(mob);
            
            // 从配置读取音效设置
            String soundName = plugin.getConfig().getString("stage4.sound.spawn", "ENTITY_WITHER_SPAWN");
            float soundVolume = (float) plugin.getConfig().getDouble("stage4.sound.volume", 1.0);
            float soundPitch = (float) plugin.getConfig().getDouble("stage4.sound.pitch", 0.8);
            
            // 播放第四阶段生成音效
            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
                location.getWorld().playSound(location, sound, soundVolume, soundPitch);
            } catch (Exception e) {
                // 如果音效名称无效，使用默认音效
                location.getWorld().playSound(location, org.bukkit.Sound.ENTITY_WITHER_SPAWN, soundVolume, soundPitch);
            }
            
            // 从配置读取粒子设置
            String particleName = plugin.getConfig().getString("stage4.particles.type", "DRAGON_BREATH");
            int particleCount = plugin.getConfig().getInt("stage4.particles.count", 20);
            double particleSpread = plugin.getConfig().getDouble("stage4.particles.spread", 3.0);
            
            // 生成第四阶段粒子效果
            try {
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleName);
                for (int i = 0; i < particleCount; i++) {
                    double offsetX = Math.random() * particleSpread - (particleSpread / 2);
                    double offsetY = Math.random() * particleSpread;
                    double offsetZ = Math.random() * particleSpread - (particleSpread / 2);
                    
                    location.getWorld().spawnParticle(
                        particle,
                        location.clone().add(offsetX, offsetY, offsetZ),
                        2, 0, 0, 0, 0.1
                    );
                }
            } catch (Exception e) {
                // 如果粒子类型无效，使用默认粒子
                for (int i = 0; i < 20; i++) {
                    double offsetX = Math.random() * 3 - 1.5;
                    double offsetY = Math.random() * 3;
                    double offsetZ = Math.random() * 3 - 1.5;
                    
                    location.getWorld().spawnParticle(
                        org.bukkit.Particle.DRAGON_BREATH,
                        location.clone().add(offsetX, offsetY, offsetZ),
                        2, 0, 0, 0, 0.1
                    );
                }
            }
            
            // 获取中文名称
            String mobName = getMobDisplayName(mob);
            plugin.getLogger().info(String.format("自然刷新第四阶段怪物: %s (%s) 在 %s", 
                mobName, entityType.name(), location));
                
        } catch (Exception e) {
            plugin.getLogger().warning("生成第四阶段怪物失败: " + e.getMessage());
        }
    }
    
    /**
     * 应用第四阶段属性增强
     */
    private void applyStage4Attributes(org.bukkit.entity.LivingEntity mob) {
        if (mob == null || mob.isDead()) return;
        
        try {
            // 从配置读取第四阶段属性倍率
            double healthMultiplier = plugin.getConfig().getDouble("stage4.health_multiplier", 4.0);
            double damageMultiplier = plugin.getConfig().getDouble("stage4.damage_multiplier", 3.0);
            double speedMultiplier = plugin.getConfig().getDouble("stage4.speed_multiplier", 1.5);
            double armorMultiplier = plugin.getConfig().getDouble("stage4.armor_multiplier", 2.0);
            double knockbackResistance = plugin.getConfig().getDouble("stage4.knockback_resistance", 0.8);
            double attackSpeedMultiplier = plugin.getConfig().getDouble("stage4.attack_speed_multiplier", 1.5);
            
            // 第四阶段属性增强（比第三阶段有大的跨度）
            // 血量增强
            if (mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) != null) {
                double baseHealth = mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue();
                mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(baseHealth * healthMultiplier);
                mob.setHealth(mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
            }
            
            // 伤害增强
            if (mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE) != null) {
                double baseDamage = mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE).getBaseValue();
                mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE).setBaseValue(baseDamage * damageMultiplier);
            }
            
            // 速度增强
            if (mob.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED) != null) {
                double baseSpeed = mob.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED).getBaseValue();
                mob.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
            }
            
            // 护甲增强
            if (mob.getAttribute(org.bukkit.attribute.Attribute.ARMOR) != null) {
                double baseArmor = mob.getAttribute(org.bukkit.attribute.Attribute.ARMOR).getBaseValue();
                mob.getAttribute(org.bukkit.attribute.Attribute.ARMOR).setBaseValue(baseArmor * armorMultiplier);
            }
            
            // 击退抗性
            if (mob.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null) {
                mob.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
            }
            
            // 攻击速度增强
            if (mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED) != null) {
                double baseAttackSpeed = mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED).getBaseValue();
                mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED).setBaseValue(baseAttackSpeed * attackSpeedMultiplier);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("应用第四阶段属性时出错: " + e.getMessage());
        }
    }
    
    /**
     * 应用第四阶段特效
     */
    private void applyStage4Effects(org.bukkit.entity.LivingEntity mob) {
        if (mob == null || mob.isDead()) return;
        
        try {
            // 从配置读取特效设置
            boolean glowingEnabled = plugin.getConfig().getBoolean("stage4.effects.glowing", true);
            boolean strengthEnabled = plugin.getConfig().getBoolean("stage4.effects.strength", true);
            boolean resistanceEnabled = plugin.getConfig().getBoolean("stage4.effects.resistance", true);
            boolean speedEnabled = plugin.getConfig().getBoolean("stage4.effects.speed", true);
            boolean regenerationEnabled = plugin.getConfig().getBoolean("stage4.effects.regeneration", true);
            
            // 添加永久发光效果
            if (glowingEnabled) {
                mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.GLOWING, 
                    Integer.MAX_VALUE, 0, true, false));
            }
            
            // 添加力量效果
            if (strengthEnabled) {
                mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    PotionEffectType.STRENGTH, 
                    Integer.MAX_VALUE, 1, true, false));
            }
            
            // 添加抗性提升效果
            if (resistanceEnabled) {
                mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    PotionEffectType.RESISTANCE, 
                    Integer.MAX_VALUE, 1, true, false));
            }
            
            // 添加速度效果
            if (speedEnabled) {
                mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, 
                    Integer.MAX_VALUE, 1, true, false));
            }
            
            // 添加再生效果
            if (regenerationEnabled) {
                mob.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION, 
                    Integer.MAX_VALUE, 0, true, false));
            }
            
            // 设置第四阶段名称
            String mobName = getMobDisplayName(mob);
            mob.setCustomName("§4§l[第四阶段] §c§l" + mobName);
            mob.setCustomNameVisible(true);
            
            // 添加第四阶段标签
            mob.addScoreboardTag("upmobs_stage4");
            mob.addScoreboardTag("upmobs_elite");
            
            // 设置元数据标记
            mob.setMetadata("upmobs_stage", new org.bukkit.metadata.FixedMetadataValue(plugin, 4));
            
        } catch (Exception e) {
            plugin.getLogger().warning("应用第四阶段特效时出错: " + e.getMessage());
        }
    }
    
    /**
     * 获取生物显示名称
     */
    private String getMobDisplayName(org.bukkit.entity.LivingEntity mob) {
        if (mob == null) return "未知生物";
        
        // 优先使用自定义名称
        if (mob.getCustomName() != null && !mob.getCustomName().isEmpty()) {
            return mob.getCustomName();
        }
        
        // 将实体类型转换为中文名
        org.bukkit.entity.EntityType type = mob.getType();
        
        // 中文映射（与EvolutionEffects.java保持一致）
        switch (type) {
            case ZOMBIE: return "僵尸";
            case SKELETON: return "骷髅";
            case CREEPER: return "苦力怕";
            case SPIDER: return "蜘蛛";
            case ENDERMAN: return "末影人";
            case WITCH: return "女巫";
            case BLAZE: return "烈焰人";
            case WITHER_SKELETON: return "凋零骷髅";
            case GUARDIAN: return "守卫者";
            case SHULKER: return "潜影贝";
            case RAVAGER: return "劫掠兽";
            case PIGLIN: return "猪灵";
            case HOGLIN: return "疣猪兽";
            case PHANTOM: return "幻翼";
            case DROWNED: return "溺尸";
            case HUSK: return "尸壳";
            case STRAY: return "流浪者";
            case VEX: return "恼鬼";
            case EVOKER: return "唤魔者";
            case VINDICATOR: return "卫道士";
            case PILLAGER: return "掠夺者";
            case ILLUSIONER: return "幻术师";
            case WITHER: return "凋零";
            case ENDER_DRAGON: return "末影龙";
            case ZOMBIFIED_PIGLIN: return "僵尸猪灵";
            // PIG_ZOMBIE 在1.16+已改为 ZOMBIFIED_PIGLIN
            case GHAST: return "恶魂";
            case MAGMA_CUBE: return "岩浆怪";
            case SLIME: return "史莱姆";
            case CAVE_SPIDER: return "洞穴蜘蛛";
            case SILVERFISH: return "蠹虫";
            case ENDERMITE: return "末影螨";
            case GIANT: return "巨人";
            case ZOMBIE_VILLAGER: return "僵尸村民";
            case VILLAGER: return "村民";
            case IRON_GOLEM: return "铁傀儡";
            case SNOW_GOLEM: return "雪傀儡";
            case WOLF: return "狼";
            case CAT: return "猫";
            case OCELOT: return "豹猫";
            case FOX: return "狐狸";
            case PANDA: return "熊猫";
            case POLAR_BEAR: return "北极熊";
            case DOLPHIN: return "海豚";
            case TURTLE: return "海龟";
            case COD: return "鳕鱼";
            case SALMON: return "鲑鱼";
            case PUFFERFISH: return "河豚";
            case TROPICAL_FISH: return "热带鱼";
            case SQUID: return "鱿鱼";
            case GLOW_SQUID: return "发光鱿鱼";
            case AXOLOTL: return "美西螈";
            case GOAT: return "山羊";
            case STRIDER: return "炽足兽";
            case BEE: return "蜜蜂";
            case PARROT: return "鹦鹉";
            case BAT: return "蝙蝠";
            case CHICKEN: return "鸡";
            case COW: return "牛";
            case MOOSHROOM: return "哞菇";
            case PIG: return "猪";
            case SHEEP: return "羊";
            case HORSE: return "马";
            case DONKEY: return "驴";
            case MULE: return "骡";
            case LLAMA: return "羊驼";
            case TRADER_LLAMA: return "行商羊驼";
            case CAMEL: return "骆驼";
            case RABBIT: return "兔子";
            default: 
                // 将下划线转换为空格并首字母大写作为备选
                String typeName = type.name();
                String[] words = typeName.toLowerCase().split("_");
                StringBuilder displayName = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        displayName.append(Character.toUpperCase(word.charAt(0)))
                                  .append(word.substring(1))
                                  .append(" ");
                    }
                }
                return displayName.toString().trim();
        }
    }
    
    /**
     * 获取进化追踪器
     */
    public EvolutionTracker getTracker() {
        return tracker;
    }
    
    /**
     * 获取进化效果管理器
     */
    public EvolutionEffects getEffects() {
        return effects;
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 获取统计数据
     */
    public String getStats() {
        return tracker.getStats();
    }
    
    /**
     * 应用进化效果到怪物
     */
    public void applyEvolution(org.bukkit.entity.LivingEntity monster, int stage) {
        if (monster == null || stage < 1) return;
        
        // 限制最大阶段
        if (stage > maxEvolutionStages) {
            stage = maxEvolutionStages;
        }
        
        // 应用进化效果
        effects.applyEvolutionEffects(monster, stage);
        
        // 记录进化
        tracker.recordEvolution(monster, stage);
        
        plugin.getLogger().fine("应用进化阶段 " + stage + " 到怪物: " + getMobDisplayName(monster));
    }
}