package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * 生物管理器
 * 负责将属性配置应用到实际生物
 */
public class MobManager {
    
    private final ConfigManager configManager;
    private final Random random;
    private final Upmobs plugin;
    
    public MobManager(ConfigManager configManager, Upmobs plugin) {
        this.configManager = configManager;
        this.random = new Random();
        this.plugin = plugin;
    }
    
    /**
     * 升级生物
     * @param entity 要升级的生物
     * @return 是否成功升级
     */
    public boolean upgradeMob(LivingEntity entity) {
        // 检查升格总开关
        if (!configManager.getConfig().getBoolean("upgrade_enabled", true)) {
            return false;
        }
        
        if (entity == null || entity.isDead()) {
            return false;
        }
        
        // 获取生物类型
        EntityType entityType = entity.getType();
        
        // 检查是否为自定义生物
        String customName = entity.getCustomName();
        MobAttributes attributes = null;
        
        if (customName != null && !customName.isEmpty()) {
            attributes = configManager.getCustomMobAttributes(customName);
        }
        
        // 如果不是自定义生物，获取原版生物配置
        if (attributes == null) {
            attributes = configManager.getMobAttributes(entityType);
        }
        
        // 如果没有配置，不进行升级
        if (attributes == null) {
            return false;
        }
        
        // 检查随机个体升级
        if (configManager.isRandomIndividualUpgrade()) {
            double weight = attributes.getSpawnWeight();
            if (weight < 1.0 && random.nextDouble() > weight) {
                return false;  // 根据权重跳过升级
            }
        }
        
        // 升格怪物与原版无异，只是标记为具有升格资格
        // 不应用属性增强，保持原版属性
        
        // 添加升格标记（用于识别升格怪物）
        entity.addScoreboardTag("upgraded_mob");
        
        // 应用升格特效（视觉标记）
        applyUpgradeEffects(entity);
        
        return true;
    }
    
    /**
     * 应用属性到生物
     */
    private void applyAttributes(LivingEntity entity, MobAttributes attributes) {
        // 获取基础值（用于百分比计算）
        double baseHealth = entity.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        double baseSpeed = entity.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
        double baseArmor = entity.getAttribute(Attribute.ARMOR).getBaseValue();
        double baseDamage = 2.0;  // 默认基础伤害
        
        // 应用全局倍率 + 升格增强（从配置读取）
        double upgradeStrength = configManager.getConfig().getDouble("natural_upgrade.upgrade_strength", 1.5);
        double globalMultiplier = configManager.getGlobalMultiplier() * upgradeStrength;
        
        // 血量
        double newHealth = attributes.getHealth().calculate(baseHealth) * globalMultiplier;
        entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newHealth);
        entity.setHealth(newHealth);
        
        // 速度
        double newSpeed = attributes.getSpeed().calculate(baseSpeed) * globalMultiplier;
        entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(newSpeed);
        
        // 护甲
        double newArmor = attributes.getArmor().calculate(baseArmor) * globalMultiplier;
        entity.getAttribute(Attribute.ARMOR).setBaseValue(newArmor);
        
        // 伤害（需要特殊处理，不同生物伤害属性不同）
        double newDamage = attributes.getDamage().calculate(baseDamage) * globalMultiplier;
        applyDamageAttribute(entity, newDamage);
        
        // 击退抗性
        double newKnockbackResistance = attributes.getKnockbackResistance().calculate(0) * globalMultiplier;
        entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(Math.min(newKnockbackResistance, 1.0));
        
        // 跟随范围
        double newFollowRange = attributes.getFollowRange().calculate(16.0) * globalMultiplier;
        entity.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(newFollowRange);
        
        // 攻击速度
        double newAttackSpeed = attributes.getAttackSpeed().calculate(4.0) * globalMultiplier;
        entity.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(newAttackSpeed);
    }
    
    /**
     * 应用伤害属性（不同生物类型使用不同的伤害属性）
     */
    private void applyDamageAttribute(LivingEntity entity, double damage) {
        if (entity instanceof Zombie) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Skeleton) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Spider) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Creeper) {
            // 苦力怕的爆炸伤害需要特殊处理
            ((Creeper) entity).setExplosionRadius((int) (damage / 2));
        } else if (entity instanceof Enderman) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Witch) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Blaze) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof WitherSkeleton) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Guardian) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Shulker) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Ravager) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Piglin) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Hoglin) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Phantom) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Drowned) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Husk) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Stray) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Vex) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Evoker) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Vindicator) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Pillager) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else if (entity instanceof Ravager) {
            entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        } else {
            // 默认使用通用攻击伤害属性
            if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
            }
        }
    }
    
    /**
     * 应用额外效果
     */
    private void applyEffects(LivingEntity entity, MobAttributes attributes) {
        // 火焰抗性
        if (attributes.isFireResistant()) {
            entity.setFireTicks(0);
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
        }
        
        // 隐身
        if (attributes.isInvisible()) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
        }
        
        // 发光
        if (attributes.isGlowing()) {
            entity.setGlowing(true);
        }
        
        // 静音
        if (attributes.isSilent()) {
            entity.setSilent(true);
        }
        
        // 随机添加其他效果（根据权重）
        if (random.nextDouble() < 0.3) {  // 30%几率添加随机效果
            addRandomEffect(entity);
        }
        
        // 启动粒子效果
        startParticleEffect(entity);
    }
    
    /**
     * 添加随机效果
     */
    private void addRandomEffect(LivingEntity entity) {
        // 1.20.x兼容的效果类型
        PotionEffectType[] effects = {
            PotionEffectType.SPEED,
            PotionEffectType.REGENERATION,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.WATER_BREATHING
        };
        
        PotionEffectType effect = effects[random.nextInt(effects.length)];
        int duration = 20 * 60 * (1 + random.nextInt(10));  // 1-10分钟
        int amplifier = random.nextInt(2);  // 0-1级
        
        entity.addPotionEffect(new PotionEffect(effect, duration, amplifier, true, false));
    }
    
    /**
     * 启动粒子效果
     */
    private void startParticleEffect(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        // 使用插件实例
        if (plugin == null || !plugin.isEnabled()) {
            return;
        }
        
        // 根据生物类型选择粒子效果
        Particle particle = getParticleForMob(entity);
        if (particle == null) {
            return;
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                // 检查生物是否仍然存活
                if (entity == null || entity.isDead() || !entity.isValid()) {
                    this.cancel();
                    return;
                }
                
                // 播放粒子效果
                Location loc = entity.getLocation().clone().add(0, 1, 0); // 在生物中心上方
                entity.getWorld().spawnParticle(
                    particle,
                    loc,
                    3, // 数量
                    0.3, // X偏移
                    0.5, // Y偏移
                    0.3, // Z偏移
                    0 // 速度
                );
            }
        }.runTaskTimer(plugin, 0L, 5L); // 每5tick（0.25秒）一次
    }
    
    /**
     * 根据生物类型获取对应的粒子效果
     */
    private Particle getParticleForMob(LivingEntity entity) {
        EntityType type = entity.getType();
        
        // 僵尸类 - 使用灰色粒子
        if (type == EntityType.ZOMBIE || 
            type == EntityType.ZOMBIE_VILLAGER || 
            type == EntityType.HUSK || 
            type == EntityType.DROWNED ||
            type == EntityType.ZOMBIFIED_PIGLIN) {
            return Particle.ASH; // 灰色灰烬粒子
        }
        
        // 骷髅类 - 使用白色粒子
        if (type == EntityType.SKELETON || 
            type == EntityType.STRAY || 
            type == EntityType.WITHER_SKELETON) {
            return Particle.WHITE_ASH; // 白色灰烬粒子
        }
        
        // 蜘蛛类 - 使用紫色粒子
        if (type == EntityType.SPIDER || 
            type == EntityType.CAVE_SPIDER) {
            return Particle.WITCH; // 紫色女巫粒子
        }
        
        // 苦力怕 - 使用电火花粒子
        if (type == EntityType.CREEPER) {
            return Particle.ELECTRIC_SPARK; // 电火花粒子
        }
        
        // 末影人 - 使用传送门粒子
        if (type == EntityType.ENDERMAN) {
            return Particle.PORTAL; // 末影传送门粒子
        }
        
        // 女巫 - 使用紫色法术粒子
        if (type == EntityType.WITCH) {
            return Particle.WITCH; // 紫色女巫粒子
        }
        
        // 烈焰人 - 使用火焰粒子
        if (type == EntityType.BLAZE) {
            return Particle.FLAME; // 火焰粒子
        }
        
        // 守卫者 - 使用水泡粒子
        if (type == EntityType.GUARDIAN || 
            type == EntityType.ELDER_GUARDIAN) {
            return Particle.BUBBLE; // 水泡粒子
        }
        
        // 末影螨 - 使用龙息粒子
        if (type == EntityType.ENDERMITE) {
            return Particle.DRAGON_BREATH; // 龙息粒子
        }
        
        // 幻翼 - 使用烟雾粒子
        if (type == EntityType.PHANTOM) {
            return Particle.SMOKE; // 烟雾粒子
        }
        
        // 掠夺者阵营 - 使用魔法暴击粒子
        if (type == EntityType.PILLAGER || 
            type == EntityType.VINDICATOR || 
            type == EntityType.EVOKER || 
            type == EntityType.ILLUSIONER) {
            return Particle.ENCHANTED_HIT; // 魔法暴击粒子
        }
        
        // 劫掠兽 - 使用方块碎裂粒子
        if (type == EntityType.RAVAGER) {
            return Particle.BLOCK; // 方块碎裂粒子
        }
        
        // 猪灵 - 使用发光粒子
        if (type == EntityType.PIGLIN || 
            type == EntityType.PIGLIN_BRUTE || 
            type == EntityType.ZOMBIFIED_PIGLIN) {
            return Particle.GLOW; // 发光粒子
        }
        
        // 疣猪兽 - 使用红石粒子
        if (type == EntityType.HOGLIN || 
            type == EntityType.ZOGLIN) {
            return Particle.DUST; // 红石粒子
        }
        
        // 蠹虫 - 使用暴击粒子
        if (type == EntityType.SILVERFISH || 
            type == EntityType.ENDERMITE) {
            return Particle.CRIT; // 暴击粒子
        }
        
        // 监守者（1.19+） - 使用幽匿灵魂粒子
        if (type.name().equals("WARDEN")) {
            return Particle.SCULK_SOUL; // 幽匿灵魂粒子
        }
        
        // 其他生物使用默认粒子 - 附魔台粒子
        return Particle.ENCHANT;
    }
    
    /**
     * 创建自定义生物
     */
    public LivingEntity createCustomMob(Location location, String customName) {
        MobAttributes attributes = configManager.getCustomMobAttributes(customName);
        if (attributes == null) {
            return null;
        }
        
        // 获取基础生物类型（从配置中获取或使用默认）
        EntityType baseType = attributes.getEntityType();
        if (baseType == null) {
            baseType = EntityType.ZOMBIE;  // 默认使用僵尸
        }
        
        // 生成生物
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, baseType);
        
        // 设置自定义名称
        entity.setCustomName(customName);
        entity.setCustomNameVisible(true);
        
        // 应用属性
        upgradeMob(entity);
        
        return entity;
    }
    
    /**
     * 获取生物当前属性信息
     */
    public String getMobInfo(LivingEntity entity) {
        if (entity == null) {
            return "生物不存在";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("§6=== 生物信息 ===\n");
        info.append("§f类型: §e").append(entity.getType().name()).append("\n");
        info.append("§f名称: §e").append(entity.getCustomName() != null ? entity.getCustomName() : "无").append("\n");
        info.append("§f血量: §c").append(String.format("%.1f/%.1f", entity.getHealth(), entity.getAttribute(Attribute.MAX_HEALTH).getValue())).append("\n");
        info.append("§f速度: §a").append(String.format("%.2f", entity.getAttribute(Attribute.MOVEMENT_SPEED).getValue())).append("\n");
        info.append("§f护甲: §b").append(String.format("%.1f", entity.getAttribute(Attribute.ARMOR).getValue())).append("\n");
        
        if (entity.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            info.append("§f伤害: §c").append(String.format("%.1f", entity.getAttribute(Attribute.ATTACK_DAMAGE).getValue())).append("\n");
        }
        
        info.append("§f击退抗性: §d").append(String.format("%.2f", entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getValue())).append("\n");
        info.append("§f效果: ");
        
        boolean hasEffects = false;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            info.append("§e").append(effect.getType().getName()).append(" ").append(effect.getAmplifier() + 1).append("§f, ");
            hasEffects = true;
        }
        
        if (!hasEffects) {
            info.append("无");
        } else {
            info.setLength(info.length() - 2);  // 移除最后的逗号和空格
        }
        
        return info.toString();
    }
    
    /**
     * 应用升格特效
     */
    private void applyUpgradeEffects(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        // 播放升格音效（僵尸治愈音效）
        Location location = entity.getLocation();
        World world = location.getWorld();
        if (world != null) {
            world.playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.7f);
        }
        
        // 添加攻击特效（攻击时产生粒子）
        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 30, 0, true, false)); // 30秒发光效果
        
        // 添加攻击特效标记（用于在攻击时触发粒子）
        entity.setMetadata("UpMobs_Upgraded", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        
        // 播放升级粒子特效
        playUpgradeParticleEffect(entity);
        
        // 添加自定义名称前缀
        String currentName = entity.getCustomName();
        if (currentName == null || currentName.isEmpty()) {
            // 如果没有自定义名称，添加升格前缀
            String mobName = getMobDisplayName(entity);
            entity.setCustomName("§6§l[升格] §f" + mobName);
            entity.setCustomNameVisible(true);
        } else if (!currentName.contains("升格")) {
            // 如果有自定义名称但不包含升格前缀，添加前缀
            entity.setCustomName("§6§l[升格] §f" + currentName);
        }
        
        // 获取中文名称
        String mobName = getMobDisplayName(entity);
        plugin.getLogger().info("升格怪物特效已应用: " + mobName + " (" + entity.getType().name() + ")");
    }
    
    /**
     * 播放升级粒子特效
     */
    private void playUpgradeParticleEffect(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }
        
        new BukkitRunnable() {
            int ticks = 0;
            final int duration = 20; // 1秒特效
            
            @Override
            public void run() {
                if (entity.isDead() || !entity.isValid() || ticks >= duration) {
                    this.cancel();
                    return;
                }
                
                Location loc = entity.getLocation().clone().add(0, 1, 0);
                World world = entity.getWorld();
                
                // 金色升级粒子
                for (int i = 0; i < 10; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * 1.5;
                    double height = Math.random() * 2;
                    
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = loc.clone().add(x, height, z);
                    
                    // 使用金色粒子
                    world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * 获取怪物显示名称
     */
    private String getMobDisplayName(LivingEntity mob) {
        if (mob.getCustomName() != null && !mob.getCustomName().isEmpty()) {
            return mob.getCustomName();
        }
        
        // 将实体类型转换为中文名（简化版）
        String typeName = mob.getType().name().toLowerCase();
        typeName = typeName.replace("_", " ");
        
        // 简单的中文映射
        switch (mob.getType()) {
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
            default: return typeName;
        }
    }
    
    /**
     * 检查是否为升格怪物
     */
    public boolean isUpgradedMob(LivingEntity entity) {
        if (entity == null) return false;
        return entity.hasMetadata("UpMobs_Upgraded");
    }
}