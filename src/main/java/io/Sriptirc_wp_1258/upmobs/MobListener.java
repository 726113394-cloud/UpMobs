package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 生物事件监听器 - 吸血进化版本
 * 监听生物生成、攻击、死亡等事件，实现吸血进化机制
 */
public class MobListener implements Listener {
    
    private final Upmobs plugin;
    private final EvolutionManager evolutionManager;
    private final MobManager mobManager;
    private final EconomyRewardManager economyRewardManager;
    
    public MobListener(Upmobs plugin, EvolutionManager evolutionManager, MobManager mobManager, EconomyRewardManager economyRewardManager) {
        this.plugin = plugin;
        this.evolutionManager = evolutionManager;
        this.mobManager = mobManager;
        this.economyRewardManager = economyRewardManager;
    }
    
    /**
     * 生物生成事件
     * 30%几率自然刷新升格怪物
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        LivingEntity entity = event.getEntity();
        
        // 跳过玩家和非生物实体
        if (entity == null || entity instanceof Player) {
            return;
        }
        
        // 检查是否是怪物（敌对生物），排除动物
        if (!isMonster(entity)) {
            return;
        }
        
        // 自然刷新升格怪物（从配置读取几率）
        evolutionManager.spawnUpgradedMobNaturally(entity.getLocation(), entity.getType());
        
        plugin.getLogger().fine("生物生成: " + entity.getType() + " (自然刷新升格)");
    }
    
    /**
     * 实体攻击实体事件 - 核心进化触发
     * 当怪物攻击玩家时，记录攻击数据并检查进化条件
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 检查是否是怪物攻击玩家
        if (!(event.getDamager() instanceof LivingEntity) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        LivingEntity mob = (LivingEntity) event.getDamager();
        Player player = (Player) event.getEntity();
        double damage = event.getFinalDamage();
        
        // 跳过玩家和已死亡的生物
        if (mob instanceof Player || mob.isDead()) {
            return;
        }
        
        // 检查是否是怪物（敌对生物），排除动物
        if (!isMonster(mob)) {
            return;
        }
        
        // 记录攻击数据
        evolutionManager.recordAttack(mob, player, damage);
        
        plugin.getLogger().fine(String.format("怪物 %s 攻击玩家 %s 造成 %.1f 伤害", 
            mob.getType().name(), player.getName(), damage));
    }
    
    /**
     * 生物死亡事件
     * 清理进化数据，添加特殊掉落
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 清理进化数据
        evolutionManager.getTracker().removeMobData(entity);
        
        // 检查是否为进化过的生物，增加经验掉落
        EvolutionTracker.MobEvolutionData data = evolutionManager.getTracker().getMobData(entity);
        if (data != null && data.getEvolutionStage() > 0) {
            // 进化过的生物掉落更多经验
            int extraExp = (int) (event.getDroppedExp() * (0.5 * data.getEvolutionStage()));
            event.setDroppedExp(event.getDroppedExp() + extraExp);
            
            // 根据阶段添加额外掉落物
            addStageDrops(event, data.getEvolutionStage());
            
            // 添加特殊掉落提示
            if (entity.getKiller() != null) {
                String mobName = getMobDisplayName(entity);
                entity.getKiller().sendMessage("§6§l! §e你击败了一个进化" + data.getEvolutionStage() + "阶段的" + mobName + "！");
            }
        }
        
        // 自定义生物额外经验
        if (entity.getCustomName() != null && !entity.getCustomName().isEmpty()) {
            int extraExp = (int) (event.getDroppedExp() * 0.5);
            event.setDroppedExp(event.getDroppedExp() + extraExp);
        }
        
        // 检查是否为进化过的生物，给予经济奖励
        if (data != null && data.getEvolutionStage() > 0 && entity.getKiller() != null) {
            giveEconomyReward(entity.getKiller(), data.getEvolutionStage(), entity);
        }
        
        // 检查是否为第四阶段怪物，给予额外奖励
        if (entity.hasMetadata("upmobs_stage") && entity.getMetadata("upmobs_stage").get(0).asInt() == 4 && entity.getKiller() != null) {
            giveStage4EconomyReward(entity.getKiller(), entity);
        }
        
        // 检查是否为升格怪物（自然刷新），给予奖励
        if (entity.getScoreboardTags().contains("upgraded_mob") && entity.getKiller() != null) {
            giveUpgradedMobEconomyReward(entity.getKiller(), entity);
        }
    }
    
    /**
     * 生物转化事件
     * 例如：僵尸村民治愈、僵尸转化为溺尸等
     */
    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        // 当生物转化时，新生成的生物需要重新记录
        if (event.getTransformedEntity() instanceof LivingEntity) {
            LivingEntity newEntity = (LivingEntity) event.getTransformedEntity();
            
            // 延迟一 tick 处理，确保生物完全生成
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    // 新生物不继承进化状态，从零开始
                    plugin.getLogger().fine("生物转化: " + newEntity.getType() + " (重置进化状态)");
                },
                1L
            );
        }
    }
    
    /**
     * 定期清理任务（每5分钟）
     */
    public void scheduleCleanupTask() {
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            evolutionManager.cleanup();
            plugin.getLogger().fine("清理进化数据: " + evolutionManager.getStats());
        }, 6000L, 6000L); // 5分钟一次
    }
    
    /**
     * 给予进化怪物经济奖励
     */
    private void giveEconomyReward(Player player, int evolutionStage, LivingEntity mob) {
        if (!economyRewardManager.isEconomyEnabled()) {
            return;
        }
        
        // 检查是否启用奖励
        if (!plugin.getConfig().getBoolean("economy_rewards.enabled", true)) {
            return;
        }
        
        // 从配置读取基础奖励
        double baseReward = plugin.getConfig().getDouble("economy_rewards.base_amount", 10.0);
        
        // 阶段奖励倍率
        double stageMultiplier = plugin.getConfig().getDouble("economy_rewards.stage_multiplier", 2.0);
        
        // 计算基础奖励
        double reward = baseReward * Math.pow(stageMultiplier, evolutionStage - 1);
        
        // 添加随机性（±30%）
        double randomFactor = 0.7 + (Math.random() * 0.6); // 0.7 到 1.3
        reward *= randomFactor;
        
        // 确保最小奖励
        double minReward = plugin.getConfig().getDouble("economy_rewards.min_amount", 5.0);
        reward = Math.max(reward, minReward);
        
        // 检查最大奖励限制
        double maxReward = plugin.getConfig().getDouble("economy_rewards.max_reward_per_kill", 200.0);
        if (reward > maxReward) {
            reward = maxReward;
        }
        
        // 四舍五入到2位小数
        reward = Math.round(reward * 100.0) / 100.0;
        
        String mobName = getMobDisplayName(mob);
        String reason = "击败第" + evolutionStage + "阶段" + mobName;
        
        economyRewardManager.rewardPlayer(player, reward, reason);
        
        plugin.getLogger().fine(String.format("奖励玩家 %s %.2f 游戏币 (第%d阶段%s)", 
            player.getName(), reward, evolutionStage, mobName));
    }
    
    /**
     * 给予第四阶段怪物额外经济奖励
     */
    private void giveStage4EconomyReward(Player player, LivingEntity mob) {
        if (!economyRewardManager.isEconomyEnabled()) {
            return;
        }
        
        // 检查是否启用奖励
        if (!plugin.getConfig().getBoolean("economy_rewards.enabled", true)) {
            return;
        }
        
        // 几率判定（默认30%）
        double rewardChance = plugin.getConfig().getDouble("economy_rewards.reward_chance", 0.3);
        if (Math.random() > rewardChance) {
            return;
        }
        
        // 第四阶段额外奖励
        double stage4Bonus = plugin.getConfig().getDouble("economy_rewards.stage4_bonus", 50.0);
        
        // 添加随机性（±20%）
        double randomFactor = 0.8 + (Math.random() * 0.4); // 0.8 到 1.2
        stage4Bonus *= randomFactor;
        
        // 检查最大奖励限制
        double maxReward = plugin.getConfig().getDouble("economy_rewards.max_reward_per_kill", 200.0);
        if (stage4Bonus > maxReward) {
            stage4Bonus = maxReward;
        }
        
        // 四舍五入到2位小数
        stage4Bonus = Math.round(stage4Bonus * 100.0) / 100.0;
        
        String mobName = getMobDisplayName(mob);
        String reason = "击败第四阶段精英" + mobName;
        
        economyRewardManager.rewardPlayer(player, stage4Bonus, reason);
        
        plugin.getLogger().fine(String.format("奖励玩家 %s %.2f 游戏币 (第四阶段精英%s)", 
            player.getName(), stage4Bonus, mobName));
    }
    
    /**
     * 给予升格怪物经济奖励
     */
    private void giveUpgradedMobEconomyReward(Player player, LivingEntity mob) {
        if (!economyRewardManager.isEconomyEnabled()) {
            return;
        }
        
        // 检查是否启用奖励
        if (!plugin.getConfig().getBoolean("economy_rewards.enabled", true)) {
            return;
        }
        
        // 几率判定（默认30%）
        double rewardChance = plugin.getConfig().getDouble("economy_rewards.reward_chance", 0.3);
        if (Math.random() > rewardChance) {
            return; // 没中奖，不给钱
        }
        
        // 升格怪物基础奖励
        double upgradedReward = plugin.getConfig().getDouble("economy_rewards.upgraded_amount", 15.0);
        
        // 添加随机性（±25%）
        double randomFactor = 0.75 + (Math.random() * 0.5); // 0.75 到 1.25
        upgradedReward *= randomFactor;
        
        // 检查最大奖励限制
        double maxReward = plugin.getConfig().getDouble("economy_rewards.max_reward_per_kill", 200.0);
        if (upgradedReward > maxReward) {
            upgradedReward = maxReward;
        }
        
        // 四舍五入到2位小数
        upgradedReward = Math.round(upgradedReward * 100.0) / 100.0;
        
        String mobName = getMobDisplayName(mob);
        String reason = "击败升格" + mobName;
        
        economyRewardManager.rewardPlayer(player, upgradedReward, reason);
        
        plugin.getLogger().fine(String.format("奖励玩家 %s %.2f 游戏币 (升格%s)", 
            player.getName(), upgradedReward, mobName));
    }
    
    /**
     * 判断是否是怪物（敌对生物）
     */
    private boolean isMonster(LivingEntity mob) {
        org.bukkit.entity.EntityType type = mob.getType();
        
        // 定义怪物类型（敌对生物）
        switch (type) {
            // 常见怪物
            case ZOMBIE:
            case SKELETON:
            case CREEPER:
            case SPIDER:
            case ENDERMAN:
            case WITCH:
            case BLAZE:
            case WITHER_SKELETON:
            case GUARDIAN:
            case SHULKER:
            case RAVAGER:
            case PIGLIN:
            case HOGLIN:
            case PHANTOM:
            case DROWNED:
            case HUSK:
            case STRAY:
            case VEX:
            case EVOKER:
            case VINDICATOR:
            case PILLAGER:
            case ILLUSIONER:
            case WITHER:
            case ENDER_DRAGON:
            case ZOMBIFIED_PIGLIN:
            case GHAST:
            case MAGMA_CUBE:
            case SLIME:
            case CAVE_SPIDER:
            case SILVERFISH:
            case ENDERMITE:
            case GIANT:
            case ZOMBIE_VILLAGER:
                return true;
            
            // 中立生物（不算怪物）
            case VILLAGER:
            case IRON_GOLEM:
            case SNOW_GOLEM:
            case WOLF:
            case CAT:
            case OCELOT:
            case FOX:
            case PANDA:
            case POLAR_BEAR:
            case DOLPHIN:
            case TURTLE:
            case COD:
            case SALMON:
            case PUFFERFISH:
            case TROPICAL_FISH:
            case SQUID:
            case GLOW_SQUID:
            case AXOLOTL:
            case GOAT:
            case STRIDER:
            case BEE:
            case PARROT:
            case BAT:
            case CHICKEN:
            case COW:
            case MOOSHROOM:
            case PIG:
            case SHEEP:
            case HORSE:
            case DONKEY:
            case MULE:
            case LLAMA:
            case TRADER_LLAMA:
            case CAMEL:
            case RABBIT:
            default:
                return false;
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
        
        // 中文映射（与其他类保持一致）
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
     * 根据进化阶段添加额外掉落物
     */
    private void addStageDrops(EntityDeathEvent event, int stage) {
        if (stage < 1 || stage > 4) return;
        
        java.util.Random rand = new java.util.Random();
        java.util.List<ItemStack> drops = event.getDrops();
        
        switch (stage) {
            case 1:
                // 第一阶段：骨头/线/火药等基础材料 1-2个
                if (rand.nextDouble() < 0.4) {
                    drops.add(new ItemStack(getRandomBasicMaterial(rand), 1 + rand.nextInt(2)));
                }
                break;
                
            case 2:
                // 第二阶段：铁锭/金锭 1-3个 + 基础材料
                if (rand.nextDouble() < 0.5) {
                    drops.add(new ItemStack(rand.nextBoolean() ? Material.IRON_INGOT : Material.GOLD_INGOT, 1 + rand.nextInt(3)));
                }
                if (rand.nextDouble() < 0.3) {
                    drops.add(new ItemStack(getRandomBasicMaterial(rand), 1 + rand.nextInt(2)));
                }
                break;
                
            case 3:
                // 第三阶段：钻石/绿宝石 1-2个 + 铁锭/金锭
                if (rand.nextDouble() < 0.4) {
                    drops.add(new ItemStack(rand.nextBoolean() ? Material.DIAMOND : Material.EMERALD, 1 + rand.nextInt(2)));
                }
                if (rand.nextDouble() < 0.5) {
                    drops.add(new ItemStack(rand.nextBoolean() ? Material.IRON_INGOT : Material.GOLD_INGOT, 1 + rand.nextInt(3)));
                }
                break;
                
            case 4:
                // 第四阶段：钻石 1-3个 + 高等级附魔书/下界合金碎片
                drops.add(new ItemStack(Material.DIAMOND, 1 + rand.nextInt(3)));
                if (rand.nextDouble() < 0.3) {
                    drops.add(createOverleveledEnchantedBook(rand));
                }
                if (rand.nextDouble() < 0.1) {
                    drops.add(new ItemStack(Material.NETHERITE_SCRAP, 1));
                }
                break;
        }
    }
    
    /**
     * 随机基础材料
     */
    private Material getRandomBasicMaterial(java.util.Random rand) {
        Material[] materials = {
            Material.BONE, Material.STRING, Material.GUNPOWDER, 
            Material.SPIDER_EYE, Material.ROTTEN_FLESH, Material.ARROW
        };
        return materials[rand.nextInt(materials.length)];
    }
    
    /**
     * 生成高一级的附魔书（比原版最高等级高1级）
     */
    private ItemStack createOverleveledEnchantedBook(java.util.Random rand) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        
        // 可用的附魔列表（每个附魔的原版最高等级）
        // 格式：附魔, 原版最高等级, 超等级（原版+1）
        Object[][] enchantments = {
            {Enchantment.SHARPNESS, 5, 6},           // 锋利 VI
            {Enchantment.PROTECTION, 4, 5},           // 保护 V
            {Enchantment.FIRE_ASPECT, 2, 3},          // 火焰附加 III
            {Enchantment.KNOCKBACK, 2, 3},            // 击退 III
            {Enchantment.POWER, 5, 6},                // 力量 VI
            {Enchantment.FEATHER_FALLING, 4, 5},      // 摔落保护 V
            {Enchantment.THORNS, 3, 4},               // 荆棘 IV
            {Enchantment.UNBREAKING, 3, 4},           // 耐久 IV
            {Enchantment.EFFICIENCY, 5, 6},           // 效率 VI
            {Enchantment.FORTUNE, 3, 4},              // 时运 IV
            {Enchantment.LOOTING, 3, 4},              // 抢夺 IV
            {Enchantment.SWEEPING_EDGE, 3, 4},        // 横扫之刃 IV
        };
        
        // 随机选择1-2个附魔
        int enchantCount = 1 + rand.nextInt(2);
        for (int i = 0; i < enchantCount; i++) {
            Object[] enchData = enchantments[rand.nextInt(enchantments.length)];
            Enchantment ench = (Enchantment) enchData[0];
            int maxLevel = (int) enchData[1];
            int overLevel = (int) enchData[2];
            
            // 70%概率出超等级附魔，30%概率出原版最高等级
            int level = rand.nextDouble() < 0.7 ? overLevel : maxLevel;
            
            try {
                meta.addStoredEnchant(ench, level, true);
            } catch (Exception e) {
                // 忽略冲突
            }
        }
        
        book.setItemMeta(meta);
        return book;
    }
}