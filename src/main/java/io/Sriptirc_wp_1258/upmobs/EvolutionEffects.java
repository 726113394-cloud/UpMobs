package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * 进化效果管理器
 * 处理进化时的视觉、音效和文本效果
 */
public class EvolutionEffects {
    
    private final Upmobs plugin;
    private final Random random = new Random();
    
    public EvolutionEffects(Upmobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 播放进化开始效果
     */
    public void playEvolutionStartEffects(LivingEntity mob, Player targetPlayer, int stage) {
        Location location = mob.getLocation();
        World world = location.getWorld();
        
        if (world == null) return;
        
        // 1. 文本提示
        sendEvolutionMessage(mob, targetPlayer, stage, true);
        
        // 2. 音效
        playEvolutionSound(location, stage);
        
        // 3. 粒子效果
        spawnEvolutionParticles(location, stage);
        
        // 4. 屏幕震动（对目标玩家）
        if (targetPlayer != null) {
            shakeScreen(targetPlayer, stage);
        }
        
        // 5. 怪物发光效果
        mob.setGlowing(true);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, true, false));
    }
    
    /**
     * 播放进化完成效果
     */
    public void playEvolutionCompleteEffects(LivingEntity mob, Player targetPlayer, int stage) {
        Location location = mob.getLocation();
        World world = location.getWorld();
        
        if (world == null) return;
        
        // 1. 文本提示
        sendEvolutionMessage(mob, targetPlayer, stage, false);
        
        // 2. 音效
        world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        
        // 3. 爆炸粒子效果
        spawnExplosionParticles(location);
        
        // 4. 停止发光
        mob.setGlowing(false);
        mob.removePotionEffect(PotionEffectType.GLOWING);
    }
    
    /**
     * 播放进化过程效果（渐进式进化）
     */
    public void playEvolutionProgressEffects(LivingEntity mob, int stage, float progress) {
        Location location = mob.getLocation();
        World world = location.getWorld();
        
        if (world == null) return;
        
        // 根据进度播放不同的粒子效果
        if (progress < 0.33f) {
            // 第一阶段：红色粒子
            spawnProgressParticles(location, progress);
        } else if (progress < 0.66f) {
            // 第二阶段：橙色粒子
            spawnProgressParticles(location, progress);
        } else {
            // 第三阶段：金色粒子
            spawnProgressParticles(location, progress);
        }
        
        // 根据进度调整发光强度
        int amplifier = (int) (progress * 3);
        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, amplifier, true, false));
    }
    
    /**
     * 发送进化消息
     */
    private void sendEvolutionMessage(LivingEntity mob, Player targetPlayer, int stage, boolean isStart) {
        String mobName = getMobDisplayName(mob);
        String message;
        
        if (isStart) {
            message = plugin.getConfig().getString("evolution.start_message", 
                "§c§l{怪物} §f吸食了你的血液，开始进化！");
        } else {
            message = plugin.getConfig().getString("evolution.complete_message", 
                "§6§l{怪物} §e已完成第{阶段}阶段进化！");
            message = message.replace("{阶段}", String.valueOf(stage));
        }
        
        message = message.replace("{怪物}", mobName);
        
        // 发送给目标玩家
        if (targetPlayer != null) {
            targetPlayer.sendMessage(message);
        }
        
        // 发送给附近玩家
        for (Player nearby : mob.getWorld().getPlayers()) {
            if (nearby != targetPlayer && nearby.getLocation().distance(mob.getLocation()) <= 20) {
                nearby.sendMessage(message);
            }
        }
    }
    
    /**
     * 播放进化音效
     */
    private void playEvolutionSound(Location location, int stage) {
        World world = location.getWorld();
        if (world == null) return;
        
        Sound sound;
        float pitch;
        
        switch (stage) {
            case 1:
                sound = Sound.ENTITY_ZOMBIE_VILLAGER_CURE;
                pitch = 0.7f;
                break;
            case 2:
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                pitch = 0.5f;
                break;
            case 3:
                sound = Sound.ENTITY_WITHER_SPAWN;
                pitch = 0.8f;
                break;
            default:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                pitch = 0.6f;
        }
        
        world.playSound(location, sound, 1.0f, pitch);
    }
    
    /**
     * 生成进化粒子
     */
    private void spawnEvolutionParticles(Location location, int stage) {
        World world = location.getWorld();
        if (world == null) return;
        
        Particle particle;
        int count;
        
        switch (stage) {
            case 1:
                particle = Particle.DRAGON_BREATH;
                count = 30;
                break;
            case 2:
                particle = Particle.WITCH;
                count = 50;
                break;
            case 3:
                particle = Particle.SOUL_FIRE_FLAME;
                count = 80;
                break;
            default:
                particle = Particle.HEART;
                count = 20;
        }
        
        for (int i = 0; i < count; i++) {
            double offsetX = random.nextDouble() * 2 - 1;
            double offsetY = random.nextDouble() * 2;
            double offsetZ = random.nextDouble() * 2 - 1;
            
            world.spawnParticle(particle, 
                location.clone().add(offsetX, offsetY, offsetZ), 
                1, 0, 0, 0, 0);
        }
    }
    
    /**
     * 生成进度粒子
     */
    private void spawnProgressParticles(Location location, float progress) {
        World world = location.getWorld();
        if (world == null) return;
        
        int particleCount = (int) (progress * 50) + 10;
        
        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 1.5;
            double height = random.nextDouble() * 2;
            
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = location.clone().add(x, height, z);
            
            // 使用火焰粒子
            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * 生成爆炸粒子
     */
    private void spawnExplosionParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        // 爆炸粒子
        world.spawnParticle(Particle.EXPLOSION, location, 3, 1, 1, 1, 0);
        
        // 火焰粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = random.nextDouble() * 3 - 1.5;
            double offsetY = random.nextDouble() * 3;
            double offsetZ = random.nextDouble() * 3 - 1.5;
            
            world.spawnParticle(Particle.FLAME, 
                location.clone().add(offsetX, offsetY, offsetZ), 
                1, 0, 0, 0, 0);
        }
    }
    
    /**
     * 屏幕震动效果
     */
    private void shakeScreen(Player player, int stage) {
        float intensity = stage * 0.3f;
        
        // 使用击退效果模拟屏幕震动
        Vector direction = player.getLocation().getDirection().multiply(-intensity * 0.1);
        player.setVelocity(player.getVelocity().add(direction));
        
        // 发送标题消息
        player.sendTitle("", "§c§l!!!", 5, 10, 5);
    }
    
    /**
     * 渐进式进化动画
     */
    public void playGradualEvolutionAnimation(LivingEntity mob, Player targetPlayer, int stage, 
                                             MobAttributes baseAttributes, MobAttributes targetAttributes,
                                             int durationTicks) {
        
        new BukkitRunnable() {
            int tick = 0;
            final int totalTicks = durationTicks;
            
            @Override
            public void run() {
                if (mob.isDead() || !mob.isValid()) {
                    this.cancel();
                    return;
                }
                
                float progress = (float) tick / totalTicks;
                
                // 播放进度效果
                playEvolutionProgressEffects(mob, stage, progress);
                
                // 渐进式应用属性（每10tick应用一次）
                if (tick % 10 == 0) {
                    applyGradualAttributes(mob, baseAttributes, targetAttributes, progress);
                }
                
                tick++;
                
                if (tick >= totalTicks) {
                    // 进化完成
                    playEvolutionCompleteEffects(mob, targetPlayer, stage);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * 渐进式应用属性
     */
    private void applyGradualAttributes(LivingEntity mob, MobAttributes baseAttributes, 
                                       MobAttributes targetAttributes, float progress) {
        // 这里简化处理，实际应该根据progress计算中间值
        // 在实际实现中，应该计算每个属性的中间值并应用
        
        // 临时实现：直接应用目标属性的百分比
        if (progress >= 1.0f) {
            // 进化完成，应用完整属性
            // 这里需要调用MobManager的applyAttributes方法
        }
    }
    
    /**
     * 应用进化效果到怪物
     */
    public void applyEvolutionEffects(LivingEntity mob, int stage) {
        if (mob == null || stage < 1) return;
        
        // 确保有升格标记（用于奖励识别）
        mob.addScoreboardTag("upgraded_mob");
        
        // 应用进化属性增强
        applyEvolutionAttributes(mob, stage);
        
        // 播放进化效果
        playEvolutionStartEffects(mob, null, stage);
        
        // 启动持续环绕粒子效果（根据阶段数量不同）
        startAmbientParticles(mob, stage);
        
        // 根据阶段添加随机护甲和武器（受配置控制）
        if (plugin.getConfig().getBoolean("evolution.stage_equipment", true)) {
            applyStageEquipment(mob, stage);
        }
        
        plugin.getLogger().fine("应用进化效果阶段 " + stage + " 到怪物: " + getMobDisplayName(mob));
    }
    
    /**
     * 启动持续环绕粒子效果
     * 第一阶段：1个环绕圈
     * 第二阶段：2个环绕圈
     * 第三阶段：4个环绕圈
     * 第四阶段：7个环绕圈
     */
    private void startAmbientParticles(LivingEntity mob, int stage) {
        if (mob == null || mob.isDead()) return;
        
        // 根据阶段确定环绕圈数量
        int ringCount;
        switch (stage) {
            case 1: ringCount = 1; break;
            case 2: ringCount = 2; break;
            case 3: ringCount = 4; break;
            case 4: ringCount = 7; break;
            default: ringCount = 1;
        }
        
        // 每个环绕圈的粒子类型
        Particle[] ringParticles = {
            Particle.FLAME,        // 火焰
            Particle.SOUL_FIRE_FLAME, // 灵魂火
            Particle.WITCH,        // 女巫
            Particle.ENCHANT,      // 附魔
            Particle.PORTAL,       // 传送门
            Particle.DRAGON_BREATH, // 龙息
            Particle.GLOW          // 发光
        };
        
        // 每个环绕圈的半径
        double[] ringRadii = {1.0, 1.5, 2.0, 2.5, 1.2, 1.8, 2.2};
        
        // 每个环绕圈的高度偏移
        double[] ringHeights = {0.5, 1.0, 1.5, 0.0, 2.0, 0.8, 1.2};
        
        // 启动持续任务
        new BukkitRunnable() {
            int tick = 0;
            
            @Override
            public void run() {
                // 如果怪物已死亡或无效，停止任务
                if (mob.isDead() || !mob.isValid()) {
                    this.cancel();
                    return;
                }
                
                Location loc = mob.getLocation().add(0, 0.5, 0);
                
                // 生成每个环绕圈的粒子
                for (int r = 0; r < ringCount; r++) {
                    double radius = ringRadii[r % ringRadii.length];
                    double height = ringHeights[r % ringHeights.length];
                    Particle particle = ringParticles[r % ringParticles.length];
                    
                    // 每个环绕圈生成4个粒子（均匀分布）
                    for (int i = 0; i < 4; i++) {
                        double angle = (tick * 0.05) + (i * Math.PI / 2) + (r * Math.PI / 4);
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        
                        Location particleLoc = loc.clone().add(x, height, z);
                        mob.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                tick++;
                
                // 每5分钟检查一次，防止无限运行
                if (tick > 6000) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // 每2tick（0.1秒）更新一次
    }
    
    /**
     * 应用进化属性增强
     * 每个阶段在当前属性基础上累乘增强
     */
    private void applyEvolutionAttributes(LivingEntity mob, int stage) {
        // 每阶段增强倍率（累乘）
        double healthMultiplier = 1.0 + (stage * 0.5); // 每阶段增加50%生命值
        double damageMultiplier = 1.0 + (stage * 0.3); // 每阶段增加30%伤害
        double speedMultiplier = 1.0 + (stage * 0.0143);  // 每阶段增加1.43%速度（降低7倍）
        
        // 应用生命值增强
        double maxHealth = mob.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        mob.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth * healthMultiplier);
        mob.setHealth(mob.getMaxHealth());
        
        // 应用伤害增强
        if (mob.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            mob.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(
                mob.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue() * damageMultiplier
            );
        }
        
        // 应用速度增强
        mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(
            mob.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * speedMultiplier
        );
        
        // 根据阶段添加特殊效果
        if (stage >= 2) {
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
        }
        if (stage >= 3) {
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
        }
    }
    
    /**
     * 根据阶段给怪物添加随机护甲和武器
     * 第一阶段：无装备
     * 第二阶段：随机皮革/金装备
     * 第三阶段：随机铁/链甲装备
     * 第四阶段：随机钻石装备 + 附魔
     */
    private void applyStageEquipment(LivingEntity mob, int stage) {
        if (stage < 2) return; // 第一阶段不给装备
        
        EntityEquipment equipment = mob.getEquipment();
        if (equipment == null) return;
        
        Random rand = new Random();
        
        // 护甲材料（按阶段）
        Material[][] armorSets;
        Material[] weaponMaterials;
        boolean applyEnchantments = false;
        
        switch (stage) {
            case 2:
                armorSets = new Material[][] {
                    {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET},
                    {Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET}
                };
                weaponMaterials = new Material[]{Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD};
                break;
            case 3:
                armorSets = new Material[][] {
                    {Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET},
                    {Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET}
                };
                weaponMaterials = new Material[]{Material.IRON_SWORD, Material.IRON_AXE};
                break;
            case 4:
                armorSets = new Material[][] {
                    {Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET}
                };
                weaponMaterials = new Material[]{Material.DIAMOND_SWORD, Material.DIAMOND_AXE};
                applyEnchantments = true;
                break;
            default:
                return;
        }
        
        // 随机选择一套护甲
        Material[] selectedArmor = armorSets[rand.nextInt(armorSets.length)];
        
        // 设置护甲（有概率不穿满）
        if (rand.nextDouble() < 0.8) { // 80%概率穿靴子
            ItemStack boots = new ItemStack(selectedArmor[0]);
            if (applyEnchantments) applyRandomEnchantments(boots, rand);
            equipment.setBoots(boots);
            equipment.setBootsDropChance(0.0f);
        }
        if (rand.nextDouble() < 0.7) { // 70%概率穿护腿
            ItemStack leggings = new ItemStack(selectedArmor[1]);
            if (applyEnchantments) applyRandomEnchantments(leggings, rand);
            equipment.setLeggings(leggings);
            equipment.setLeggingsDropChance(0.0f);
        }
        if (rand.nextDouble() < 0.6) { // 60%概率穿胸甲
            ItemStack chestplate = new ItemStack(selectedArmor[2]);
            if (applyEnchantments) applyRandomEnchantments(chestplate, rand);
            equipment.setChestplate(chestplate);
            equipment.setChestplateDropChance(0.0f);
        }
        if (rand.nextDouble() < 0.5) { // 50%概率戴头盔
            ItemStack helmet = new ItemStack(selectedArmor[3]);
            if (applyEnchantments) applyRandomEnchantments(helmet, rand);
            equipment.setHelmet(helmet);
            equipment.setHelmetDropChance(0.0f);
        }
        
        // 随机选择武器
        if (rand.nextDouble() < 0.6) { // 60%概率拿武器
            Material weaponMat = weaponMaterials[rand.nextInt(weaponMaterials.length)];
            ItemStack weapon = new ItemStack(weaponMat);
            if (applyEnchantments) applyRandomEnchantments(weapon, rand);
            equipment.setItemInMainHand(weapon);
            equipment.setItemInMainHandDropChance(0.0f);
        }
        
        // 第四阶段额外：副手也可能拿东西
        if (stage >= 4 && rand.nextDouble() < 0.3) {
            ItemStack offHand = new ItemStack(Material.SHIELD);
            if (rand.nextDouble() < 0.5) {
                offHand = new ItemStack(Material.TOTEM_OF_UNDYING);
            }
            equipment.setItemInOffHand(offHand);
            equipment.setItemInOffHandDropChance(0.0f);
        }
    }
    
    /**
     * 给物品随机附魔
     */
    private void applyRandomEnchantments(ItemStack item, Random rand) {
        if (item == null || item.getType() == Material.AIR) return;
        
        // 可用的附魔列表
        Enchantment[] weaponEnchants = {Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS, Enchantment.FIRE_ASPECT, Enchantment.KNOCKBACK};
        Enchantment[] armorEnchants = {Enchantment.PROTECTION, Enchantment.PROJECTILE_PROTECTION, Enchantment.BLAST_PROTECTION, Enchantment.FIRE_PROTECTION, Enchantment.THORNS};
        Enchantment[] bootEnchants = {Enchantment.PROTECTION, Enchantment.FEATHER_FALLING, Enchantment.DEPTH_STRIDER};
        
        String typeName = item.getType().name();
        boolean isWeapon = typeName.contains("SWORD") || typeName.contains("AXE");
        boolean isBoots = typeName.contains("BOOTS");
        
        Enchantment[] pool = isWeapon ? weaponEnchants : (isBoots ? bootEnchants : armorEnchants);
        
        // 随机附魔数量（1-3个）
        int enchantCount = 1 + rand.nextInt(3);
        for (int i = 0; i < enchantCount; i++) {
            Enchantment ench = pool[rand.nextInt(pool.length)];
            // 随机等级（1-3级）
            int level = 1 + rand.nextInt(3);
            try {
                item.addEnchantment(ench, level);
            } catch (Exception e) {
                // 忽略冲突附魔
            }
        }
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
}