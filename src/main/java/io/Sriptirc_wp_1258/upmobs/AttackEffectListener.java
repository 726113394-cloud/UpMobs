package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * 攻击特效监听器
 * 处理升格怪物攻击时的特效
 */
public class AttackEffectListener implements Listener {
    
    private final Upmobs plugin;
    private final MobManager mobManager;
    private final Random random;
    
    public AttackEffectListener(Upmobs plugin, MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
        this.random = new Random();
    }
    
    /**
     * 实体攻击实体事件 - 攻击特效
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 检查是否是升格怪物攻击
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity attacker = (LivingEntity) event.getDamager();
        
        // 检查是否是怪物（敌对生物），排除动物
        if (!isMonster(attacker)) {
            return;
        }
        
        // 检查是否为升格怪物
        if (!mobManager.isUpgradedMob(attacker)) {
            return;
        }
        
        // 播放攻击音效
        playAttackSound(attacker.getLocation());
        
        // 播放攻击粒子特效
        playAttackParticleEffect(attacker, event.getEntity().getLocation());
        
        // 如果攻击目标是玩家，显示攻击提示
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            String mobName = getMobDisplayName(attacker);
            target.sendMessage("§c§l! §6" + mobName + "对你造成了额外伤害！");
        }
    }
    
    /**
     * 播放攻击音效
     */
    private void playAttackSound(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        // 随机选择攻击音效
        Sound[] attackSounds = {
            Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,
            Sound.ENTITY_PLAYER_ATTACK_CRIT,
            Sound.ENTITY_PLAYER_ATTACK_STRONG
        };
        
        Sound sound = attackSounds[random.nextInt(attackSounds.length)];
        world.playSound(location, sound, 0.8f, 0.8f + random.nextFloat() * 0.4f);
    }
    
    /**
     * 播放攻击粒子特效
     */
    private void playAttackParticleEffect(LivingEntity attacker, Location targetLocation) {
        World world = attacker.getWorld();
        if (world == null) return;
        
        // 从攻击者到目标的连线
        Location attackerLoc = attacker.getLocation().clone().add(0, 1, 0);
        Location targetLoc = targetLocation.clone().add(0, 1, 0);
        
        // 计算方向向量
        double dx = targetLoc.getX() - attackerLoc.getX();
        double dy = targetLoc.getY() - attackerLoc.getY();
        double dz = targetLoc.getZ() - attackerLoc.getZ();
        
        // 播放攻击轨迹粒子
        for (int i = 0; i < 5; i++) {
            double progress = i / 4.0;
            double x = attackerLoc.getX() + dx * progress;
            double y = attackerLoc.getY() + dy * progress;
            double z = attackerLoc.getZ() + dz * progress;
            
            Location particleLoc = new Location(world, x, y, z);
            
            // 使用红色粒子表示攻击轨迹
            world.spawnParticle(Particle.FLAME, particleLoc, 2, 0.1, 0.1, 0.1, 0);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1.0f));
        }
        
        // 在目标位置播放命中特效
        world.spawnParticle(Particle.CRIT, targetLoc, 10, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.SWEEP_ATTACK, targetLoc, 5, 0.3, 0.3, 0.3, 0);
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
    private String getMobDisplayName(LivingEntity mob) {
        if (mob == null) return "未知生物";
        
        // 优先使用自定义名称
        if (mob.getCustomName() != null && !mob.getCustomName().isEmpty()) {
            return mob.getCustomName();
        }
        
        // 将实体类型转换为中文名
        EntityType type = mob.getType();
        
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
}