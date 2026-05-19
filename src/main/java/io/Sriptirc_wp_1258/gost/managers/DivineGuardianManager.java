package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DivineGuardianManager {
    
    private final Gost plugin;
    private final Random random = new Random();
    
    // 神圣守护玩家数据
    private final Set<UUID> holyGuardianPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> holyGuardianActivationTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> holyGuardianHitCount = new ConcurrentHashMap<>(); // 记录神圣守护被攻击次数
    
    // 猎魔人数据
    private final Set<UUID> demonHunterPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> demonHunterKillCount = new ConcurrentHashMap<>();
    private final Map<UUID, Long> reaperAttackCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> ghostHitCount = new ConcurrentHashMap<>(); // 记录鬼被攻击次数
    
    // 神之救赎道具数据
    private final Map<UUID, Integer> holyRedemptionUses = new ConcurrentHashMap<>();
    private final Map<UUID, Long> holyRedemptionCooldown = new ConcurrentHashMap<>();
    
    // 母体数据
    private final Set<UUID> additionalMothers = ConcurrentHashMap.newKeySet(); // 新增的母体
    
    // 状态标志
    private boolean isDemonHunterPhase = false;
    private boolean hasAdditionalMotherSpawned = false;
    
    // 旁观者数据
    private final Set<UUID> spectatorPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> spectatorOriginalLocations = new ConcurrentHashMap<>();
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> spectatorBoundaryTasks = new ConcurrentHashMap<>();
    
    // 复活机制数据
    private final Map<UUID, Long> respawnTimers = new ConcurrentHashMap<>(); // 玩家ID -> 复活时间戳
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> respawnTasks = new ConcurrentHashMap<>(); // 复活任务
    
    public DivineGuardianManager(Gost plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载配置
     */
    public void loadConfig() {
        plugin.getLogger().info("神圣守护管理器 v2.2.2 已加载");
    }
    
    /**
     * 检查并激活神圣守护
     * @param humanPlayers 当前人类玩家列表
     */
    public void checkAndActivateHolyGuardian(List<UUID> humanPlayers) {
        if (!plugin.getConfigManager().isDivineGuardianSystemEnabled()) {
            return;
        }
        
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        int triggerCount = plugin.getConfigManager().getDivineGuardianTriggerHumanCount();
        
        // 如果人类玩家数量等于或少于触发数量，激活神圣守护
        if (humanPlayers.size() <= triggerCount) {
            // 为所有剩余人类玩家激活神圣守护（排除被神之救赎转化的人类）
            for (UUID playerId : humanPlayers) {
                // 检查是否是神之救赎转化的玩家
                boolean isConvertedByRedemption = plugin.getPlayerManager().isConvertedByRedemption(playerId);
                if (!isConvertedByRedemption && !holyGuardianPlayers.contains(playerId)) {
                    activateHolyGuardian(playerId);
                }
            }
            
            // 移除不再是人类的神圣守护玩家和被神之救赎转化的玩家
            holyGuardianPlayers.removeIf(playerId -> {
                if (!humanPlayers.contains(playerId)) {
                    return true; // 不再是人类
                }
                // 检查是否被神之救赎转化
                return plugin.getPlayerManager().isConvertedByRedemption(playerId);
            });
        } else {
            // 如果人类数量超过触发数量，清除所有神圣守护
            holyGuardianPlayers.clear();
            holyGuardianActivationTime.clear();
        }
    }
    
    /**
     * 激活神圣守护
     */
    private void activateHolyGuardian(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }
        
        holyGuardianPlayers.add(playerId);
        holyGuardianActivationTime.put(playerId, System.currentTimeMillis());
        holyGuardianHitCount.put(playerId, 0); // 初始化攻击计数为0
        
        // 应用视觉效果
        applyHolyGuardianEffects(player);
        
        // 给予神之救赎道具
        giveHolyRedemptionItem(player);
        
        // 广播消息
        if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
            String message = String.format("§6§l[神圣守护] §e玩家 §a%s §e获得了神圣守护！", player.getName());
            Bukkit.broadcastMessage(message);
        }
        
        player.sendMessage("§6§l[神圣守护] §a你获得了神圣守护效果！");
        player.sendMessage("§e✨ 你被白色粒子特效环绕");
        player.sendMessage("§7• 鬼玩家尝试感染你时会被随机传送");
        player.sendMessage("§7• 游戏最后90秒你将变为§6猎魔人§7（金色粒子特效）");
        player.sendMessage("§7• 你获得了§6神之救赎§7道具（右键点击鬼玩家转化）");
        player.sendMessage("§7• 作为最后的人类，你有机会获得额外奖金");
    }
    
    /**
     * 应用神圣守护视觉效果
     */
    private void applyHolyGuardianEffects(Player player) {
        // 发光效果
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.GLOWING, 
            plugin.getConfigManager().getHolyGuardianEffectDuration() * 20, 
            0, 
            true, 
            true
        ));
        
        // 神圣守护白色粒子特效
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!holyGuardianPlayers.contains(player.getUniqueId()) || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                
                Location loc = player.getLocation();
                
                // 白色附魔台粒子（主要效果）
                player.getWorld().spawnParticle(
                    Particle.ENCHANTMENT_TABLE, 
                    loc.clone().add(0, 2.2, 0), 
                    12, 
                    0.5, 0.3, 0.5, 
                    0.05
                );
                
                // 白色灰烬粒子环绕
                player.getWorld().spawnParticle(
                    Particle.WHITE_ASH, 
                    loc.clone().add(0, 1.8, 0), 
                    8, 
                    0.7, 0.2, 0.7, 
                    0.02
                );
                
                // 雪花粒子（神圣感）
                player.getWorld().spawnParticle(
                    Particle.SNOWFLAKE, 
                    loc.clone().add(0, 2.5, 0), 
                    6, 
                    0.4, 0.1, 0.4, 
                    0.03
                );
                
                // 白色云朵粒子（柔和效果）
                player.getWorld().spawnParticle(
                    Particle.CLOUD, 
                    loc.clone().add(0, 1.5, 0), 
                    4, 
                    0.3, 0.1, 0.3, 
                    0.01
                );
                
                // 如果是猎魔人，添加金色粒子叠加
                if (demonHunterPlayers.contains(player.getUniqueId())) {
                    // 猎魔人金色粒子效果会在applyDemonHunterEffects中单独处理
                }
            }
        }.runTaskTimer(plugin, 0L, 15L); // 每0.75秒一次（15 ticks）
        
        // 额外：激活时的爆发效果
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!holyGuardianPlayers.contains(player.getUniqueId()) || !player.isOnline()) {
                    return;
                }
                
                Location loc = player.getLocation();
                
                // 激活时的白色爆发效果
                for (int i = 0; i < 2; i++) {
                    player.getWorld().spawnParticle(
                        Particle.FIREWORKS_SPARK, 
                        loc.clone().add(0, 1, 0), 
                        25, 
                        1.0, 0.5, 1.0, 
                        0.1
                    );
                }
            }
        }.runTaskLater(plugin, 5L); // 激活后5 ticks执行
    }
    
    /**
     * 给予神之救赎道具
     */
    private void giveHolyRedemptionItem(Player player) {
        int maxUses = plugin.getConfigManager().getDemonHunterMaxUses();
        holyRedemptionUses.put(player.getUniqueId(), maxUses);
        
        ItemStack holyRedemption = createHolyRedemptionItem(maxUses);
        
        // 尝试将神之救赎放在第二个物品栏（slot 1），如果被占用则寻找其他空位
        int preferredSlot = 1; // 第二个物品栏
        ItemStack currentItem = player.getInventory().getItem(preferredSlot);
        if (currentItem != null && currentItem.getType() != Material.AIR) {
            // 如果第二个物品栏已有物品，尝试寻找其他空位
            int emptySlot = player.getInventory().firstEmpty();
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, currentItem);
                player.getInventory().setItem(preferredSlot, holyRedemption);
                player.sendMessage("§6§l[神圣守护] §a神之救赎已放置在第二个物品栏，原有物品已移动到其他位置");
            } else {
                // 没有空位，直接替换
                player.getInventory().setItem(preferredSlot, holyRedemption);
                player.sendMessage("§6§l[神圣守护] §a神之救赎已放置在第二个物品栏，替换了原有物品");
            }
        } else {
            // 第二个物品栏为空，直接放置
            player.getInventory().setItem(preferredSlot, holyRedemption);
        }
        
        player.sendMessage("§6§l[神圣守护] §a你获得了神之救赎道具！");
        player.sendMessage("§e✨ 右键点击鬼玩家将其转化回人类");
        player.sendMessage("§7• 转化成功后你会被随机传送");
        player.sendMessage("§7• 每局最多使用: §e" + maxUses + "次");
        player.sendMessage("§7• 使用冷却: §e" + plugin.getConfigManager().getDemonHunterHolyRedemptionCooldown() + "秒");
        player.sendMessage("§7• 转化鬼玩家可获得额外奖金");
        player.sendMessage("§e提示: 将神之救赎拿在手中，右键点击鬼玩家使用");
    }
    
    /**
     * 创建神之救赎道具
     */
    private ItemStack createHolyRedemptionItem(int remainingUses) {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l神之救赎");
            List<String> lore = new ArrayList<>();
            lore.add("§7右键点击鬼玩家将其转化回人类");
            lore.add("§7使用后你会被随机传送");
            lore.add("§e剩余使用次数: " + remainingUses);
            lore.add("§8神圣守护专属道具");
            meta.setLore(lore);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 处理鬼玩家尝试感染神圣守护玩家
     * @param attacker 攻击者（鬼玩家）
     * @param target 目标（神圣守护玩家）
     * @return 是否阻止感染
     */
    public boolean handleGhostAttack(Player attacker, Player target) {
        UUID targetId = target.getUniqueId();
        
        // 获取攻击者角色（母体或普通鬼）
        boolean isMotherGhost = plugin.getPlayerManager().getPlayerRole(attacker.getUniqueId()) == 
            io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole.GHOST_MOTHER;
        
        // ========== 猎魔人阶段特殊规则 ==========
        if (isDemonHunterPhase) {
            // 如果目标不是猎魔人（普通人类）
            if (!demonHunterPlayers.contains(targetId)) {
                // 猎魔人阶段，普通鬼不能感染人类，只能躲避猎杀
                if (!isMotherGhost) {
                    attacker.sendMessage("§c§l[猎魔人阶段] §c猎魔人阶段普通鬼不能感染人类，只能躲避猎杀！");
                    return true; // 阻止感染
                }
                // 母体可以感染普通人类，继续后续检查
            }
            // 如果目标是猎魔人
            else {
                // 猎魔人阶段，只有母体可以攻击猎魔人（无论是否有神圣守护）
                if (!isMotherGhost) {
                    attacker.sendMessage("§c§l[猎魔人] §c只有母体可以攻击猎魔人！");
                    return true; // 阻止攻击
                }
                
                // 检查猎魔人是否有神圣守护
                if (!holyGuardianPlayers.contains(targetId)) {
                    // 无神圣守护的猎魔人，母体根据伤害值攻击
                    double damage = plugin.getConfigManager().getMotherAttackDamage();
                    double currentHealth = target.getHealth();
                    double newHealth = currentHealth - damage;
                    
                    if (newHealth <= 0) {
                        // 血量耗尽，击杀猎魔人
                        killDemonHunter(target, attacker);
                    } else {
                        // 应用伤害
                        target.setHealth(newHealth);
                        attacker.sendMessage(String.format("§6§l[母体] §e你对猎魔人造成了 §c%.1f §e点伤害！剩余血量: §c%.1f❤", 
                            damage, newHealth));
                        target.sendMessage(String.format("§c§l[猎魔人] §c你受到母体攻击！剩余血量: §c%.1f❤", newHealth));
                        
                        // 视觉效果
                        Location loc = target.getLocation();
                        loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 10, 0.5, 0.5, 0.5, 0.5);
                        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
                    }
                    return true; // 阻止后续处理
                }
                // 有神圣守护的猎魔人，母体需要攻击3次才能破除，继续后续处理
            }
        }
        
        // ========== 神圣守护检查 ==========
        // 检查目标是否拥有神圣守护（检查是否过期）
        if (!hasActiveHolyGuardian(targetId)) {
            return false; // 不阻止感染
        }
        
        // ========== 神圣守护抵挡逻辑 ==========
        // 无论是否是母体，神圣守护都可以抵挡3次攻击
        int maxCharges = plugin.getConfigManager().getHolyGuardianDefenseCharges();
        int currentHits = holyGuardianHitCount.getOrDefault(targetId, 0);
        
        // 增加攻击计数
        currentHits++;
        holyGuardianHitCount.put(targetId, currentHits);
        
        // 检查是否达到最大抵挡次数
        if (currentHits < maxCharges) {
            // 还有剩余次数，随机传送攻击者
            if (plugin.getConfigManager().isHolyGuardianTeleportAttackerEnabled()) {
                teleportAttackerRandomly(attacker);
                String messagePrefix = isDemonHunterPhase && demonHunterPlayers.contains(targetId) 
                    ? "§c§l[猎魔人]" : "§c§l[神圣守护]";
                attacker.sendMessage(String.format("%s §c目标受到神圣守护保护！剩余抵挡次数: §e%d§c/§e%d", 
                    messagePrefix, maxCharges - currentHits, maxCharges));
                target.sendMessage(String.format("§6§l[神圣守护] §a神圣守护保护了你！剩余抵挡次数: §e%d§a/§e%d", 
                    maxCharges - currentHits, maxCharges));
            }
            return true; // 阻止感染
        } else {
            // 达到最大抵挡次数，破除神圣守护
            if (breakHolyGuardian(target)) {
                String messagePrefix = isDemonHunterPhase && demonHunterPlayers.contains(targetId) 
                    ? "§6§l[猎魔人]" : "§6§l[神圣守护]";
                attacker.sendMessage(String.format("%s §e你成功破除了目标的神圣守护！", messagePrefix));
                target.sendMessage("§c§l[神圣守护] §c你的神圣守护已被破除！");
            }
            return true; // 阻止本次感染，但神圣守护已被破除
        }
    }
    
    /**
     * 随机传送攻击者
     */
    private void teleportAttackerRandomly(Player attacker) {
        double radius = plugin.getConfigManager().getHolyGuardianTeleportRadius();
        Location currentLoc = attacker.getLocation();
        
        // 在半径范围内随机生成新位置
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;
        
        double newX = currentLoc.getX() + Math.cos(angle) * distance;
        double newZ = currentLoc.getZ() + Math.sin(angle) * distance;
        
        // 获取安全高度
        World world = currentLoc.getWorld();
        if (world != null) {
            int newY = world.getHighestBlockYAt((int) newX, (int) newZ) + 1;
            Location newLoc = new Location(world, newX, newY, newZ);
            
            // 安全传送
            attacker.teleport(newLoc);
            
            // 视觉效果
            world.spawnParticle(Particle.PORTAL, currentLoc, 50, 0.5, 0.5, 0.5, 0.5);
            world.playSound(currentLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            world.spawnParticle(Particle.PORTAL, newLoc, 50, 0.5, 0.5, 0.5, 0.5);
            world.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }
    
    /**
     * 破除神圣守护
     */
    private boolean breakHolyGuardian(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 检查是否已经破除
        if (!holyGuardianPlayers.contains(playerId)) {
            return false; // 已经破除
        }
        
        // 移除神圣守护效果
        holyGuardianPlayers.remove(playerId);
        holyGuardianActivationTime.remove(playerId);
        holyGuardianHitCount.remove(playerId); // 清理攻击计数
        
        // 清除发光效果
        player.removePotionEffect(PotionEffectType.GLOWING);
        
        // 视觉效果
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 30, 0.5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_DAMAGE, 1.0f, 0.5f);
        
        return true;
    }
    
    /**
     * 检查并进入猎魔人阶段
     */
    public void checkAndEnterDemonHunterPhase(int remainingTime) {
        if (!plugin.getConfigManager().isDivineGuardianSystemEnabled()) {
            return;
        }
        
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        int phaseStartTime = plugin.getConfigManager().getDemonHunterPhaseStartTime();
        
        // 检查是否应该进入猎魔人阶段
        if (!isDemonHunterPhase && remainingTime <= phaseStartTime) {
            enterDemonHunterPhase();
        }
    }
    
    /**
     * 进入猎魔人阶段
     */
    private void enterDemonHunterPhase() {
        isDemonHunterPhase = true;
        
        // 清除所有的神之救赎道具
        clearAllHolyRedemptionItems();
        
        // 在猎魔人阶段，为所有剩余人类玩家激活神圣守护（忽略触发数量限制）
        List<UUID> humanPlayers = plugin.getPlayerManager().getHumanPlayers();
        
        // 清理：移除不再是人类或被神之救赎转化的玩家
        holyGuardianPlayers.removeIf(playerId -> {
            if (!humanPlayers.contains(playerId)) {
                return true; // 不再是人类
            }
            // 检查是否被神之救赎转化
            return plugin.getPlayerManager().isConvertedByRedemption(playerId);
        });
        
        // 为所有剩余人类玩家激活神圣守护（排除被神之救赎转化的人类）
        for (UUID playerId : humanPlayers) {
            // 检查是否是神之救赎转化的玩家
            boolean isConvertedByRedemption = plugin.getPlayerManager().isConvertedByRedemption(playerId);
            if (!isConvertedByRedemption && !holyGuardianPlayers.contains(playerId)) {
                activateHolyGuardian(playerId);
            }
        }
        
        // 检查是否有神圣守护玩家
        if (holyGuardianPlayers.isEmpty()) {
            // 没有玩家拥有神圣守护
            if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
                Bukkit.broadcastMessage("§6§l[猎魔人阶段] §c猎魔人阶段开始！");
                Bukkit.broadcastMessage("§7• 没有玩家拥有神圣守护，无法产生猎魔人");
                Bukkit.broadcastMessage("§7• 神之救赎道具已被清除");
                Bukkit.broadcastMessage("§7• 游戏继续，但无猎魔人参与");
            }
        } else {
            // 将所有拥有神圣守护的玩家变为猎魔人
            for (UUID playerId : holyGuardianPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    // 重置神圣守护攻击计数（猎魔人阶段重新计算）
                    holyGuardianHitCount.put(playerId, 0);
                    activateDemonHunter(player);
                }
            }
            
            // 广播消息
            if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
                Bukkit.broadcastMessage("§6§l[猎魔人阶段] §c猎魔人阶段开始！");
                Bukkit.broadcastMessage("§7• 拥有神圣守护的人类变为猎魔人");
                Bukkit.broadcastMessage("§7• 猎魔人可以使用收割者道具击杀鬼玩家");
                Bukkit.broadcastMessage("§7• 只有母体可以攻击猎魔人");
                Bukkit.broadcastMessage("§7• 神之救赎道具已被清除");
            }
        }
        
        // 调整所有鬼玩家的血量
        adjustGhostHealthInDemonHunterPhase();
        
        // 检查是否需要新增母体
        checkAndAddAdditionalMother();
    }
    
    /**
     * 激活猎魔人
     */
    private void activateDemonHunter(Player player) {
        UUID playerId = player.getUniqueId();
        
        demonHunterPlayers.add(playerId);
        demonHunterKillCount.put(playerId, 0);
        
        // 给予收割者道具
        giveReaperWeapon(player);
        
        // 应用猎魔人效果
        applyDemonHunterEffects(player);
        
        // 设置猎魔人血量
        double demonHunterHealth = plugin.getConfigManager().getDemonHunterHealth();
        player.setHealth(demonHunterHealth);
        
        player.sendMessage("§6§l[猎魔人] §a你成为了猎魔人！");
        player.sendMessage("§e✨ 你被金色粒子特效环绕");
        player.sendMessage(String.format("§7• 血量调整为: §c%.1f❤", demonHunterHealth));
        player.sendMessage("§7• 你获得了§4收割者§7道具（攻击鬼玩家两次击杀）");
        player.sendMessage("§7• 攻击冷却: §e" + plugin.getConfigManager().getReaperWeaponAttackCooldown() + "秒");
        player.sendMessage("§7• 只有§c母体§7可以攻击你");
        player.sendMessage("§7• 击杀鬼玩家可获得§e30%§7人类奖池奖金");
        player.sendMessage("§7• 被母体击杀后进入旁观模式");
        
        // 如果禁止回血，添加提示
        if (plugin.getConfigManager().isNoHealingInDemonHunterPhase()) {
            player.sendMessage("§c⚠ 猎魔人阶段禁止回血");
        }
    }
    
    /**
     * 给予收割者道具
     */
    private void giveReaperWeapon(Player player) {
        ItemStack reaperWeapon = createReaperWeapon();
        
        // 强制放在第一个物品栏（slot 0）
        ItemStack currentItem = player.getInventory().getItem(0);
        if (currentItem != null && currentItem.getType() != Material.AIR) {
            // 如果第一个物品栏已有物品，尝试寻找其他空位
            int emptySlot = player.getInventory().firstEmpty();
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, currentItem);
                player.getInventory().setItem(0, reaperWeapon);
                player.sendMessage("§6§l[猎魔人] §a收割者已放置在第一个物品栏，原有物品已移动到其他位置");
            } else {
                // 没有空位，直接替换
                player.getInventory().setItem(0, reaperWeapon);
                player.sendMessage("§6§l[猎魔人] §a收割者已放置在第一个物品栏，替换了原有物品");
            }
        } else {
            // 第一个物品栏为空，直接放置
            player.getInventory().setItem(0, reaperWeapon);
        }
        
        player.sendMessage("§6§l[猎魔人] §a你获得了收割者！");
        player.sendMessage("§e✨ 专属武器 - §4收割者");
        player.sendMessage("§7• 攻击鬼玩家造成伤害，血量归零即击杀");
        player.sendMessage("§7• 每次攻击伤害: §c" + plugin.getConfigManager().getReaperWeaponDamagePerHit() + "❤");
        player.sendMessage("§7• 攻击冷却: §e" + plugin.getConfigManager().getReaperWeaponAttackCooldown() + "秒");
        player.sendMessage("§7• 每击杀一名鬼玩家获得§e30%§7人类奖池奖金");
        player.sendMessage("§7• 只有§c母体§7可以对抗你");
        player.sendMessage("§e提示: 可以使用左键或右键攻击鬼玩家");
    }
    
    /**
     * 创建收割者道具
     */
    private ItemStack createReaperWeapon() {
        ItemStack item = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§4§l收割者");
            List<String> lore = new ArrayList<>();
            lore.add("§7猎魔人专属武器");
            lore.add("§c攻击鬼玩家两次即可击杀");
            lore.add("§e攻击冷却: " + plugin.getConfigManager().getReaperWeaponAttackCooldown() + "秒");
            lore.add("§8只有母体可以对抗猎魔人");
            meta.setLore(lore);
            
            // 附魔光效
            if (plugin.getConfigManager().isReaperWeaponEnchantGlowEnabled()) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
            }
            
            // 设置不可破坏
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 应用猎魔人效果
     */
    private void applyDemonHunterEffects(Player player) {
        // 速度效果
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED, 
            999999, 
            1, // 速度II
            true, 
            true
        ));
        
        // 更明显的发光效果
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.GLOWING, 
            999999, 
            0, 
            true, 
            true
        ));
        
        // 猎魔人金色粒子特效
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!demonHunterPlayers.contains(player.getUniqueId()) || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                
                Location loc = player.getLocation();
                
                // 金色火焰粒子（主要效果）
                player.getWorld().spawnParticle(
                    Particle.FLAME, 
                    loc.clone().add(0, 2.3, 0), 
                    15, 
                    0.6, 0.4, 0.6, 
                    0.05
                );
                
                // 金色火花粒子
                player.getWorld().spawnParticle(
                    Particle.FIREWORKS_SPARK, 
                    loc.clone().add(0, 2.0, 0), 
                    10, 
                    0.8, 0.3, 0.8, 
                    0.08
                );
                
                // 金色附魔粒子
                player.getWorld().spawnParticle(
                    Particle.ENCHANTMENT_TABLE, 
                    loc.clone().add(0, 2.5, 0), 
                    8, 
                    0.4, 0.2, 0.4, 
                    0.03
                );
                
                // 金色发光粒子
                player.getWorld().spawnParticle(
                    Particle.GLOW, 
                    loc.clone().add(0, 1.8, 0), 
                    12, 
                    0.5, 0.2, 0.5, 
                    0.04
                );
                
                // 金色烟雾粒子（底部）
                player.getWorld().spawnParticle(
                    Particle.CAMPFIRE_COSY_SMOKE, 
                    loc.clone().add(0, 0.5, 0), 
                    6, 
                    0.3, 0.1, 0.3, 
                    0.02
                );
            }
        }.runTaskTimer(plugin, 0L, 10L); // 每0.5秒一次（10 ticks），更频繁
        
        // 激活时的金色爆发效果
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!demonHunterPlayers.contains(player.getUniqueId()) || !player.isOnline()) {
                    return;
                }
                
                Location loc = player.getLocation();
                
                // 金色爆炸效果
                for (int i = 0; i < 3; i++) {
                    player.getWorld().spawnParticle(
                        Particle.EXPLOSION_LARGE, 
                        loc.clone().add(0, 1.5 + i * 0.5, 0), 
                        8, 
                        1.2, 0.3, 1.2, 
                        0.15
                    );
                }
                
                // 金色闪电效果
                player.getWorld().spawnParticle(
                    Particle.ELECTRIC_SPARK, 
                    loc.clone().add(0, 2.0, 0), 
                    20, 
                    1.0, 0.5, 1.0, 
                    0.1
                );
                
                player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
            }
        }.runTaskLater(plugin, 2L); // 激活后2 ticks执行
    }
    
    /**
     * 处理猎魔人攻击
     */
    public boolean handleDemonHunterAttack(Player demonHunter, Player ghost) {
        UUID demonHunterId = demonHunter.getUniqueId();
        UUID ghostId = ghost.getUniqueId();
        
        // 检查攻击者是否是猎魔人
        if (!demonHunterPlayers.contains(demonHunterId)) {
            return false;
        }
        
        // 检查冷却
        Long lastAttack = reaperAttackCooldown.get(demonHunterId);
        double cooldown = plugin.getConfigManager().getReaperWeaponAttackCooldown();
        if (lastAttack != null && System.currentTimeMillis() - lastAttack < cooldown * 1000) {
            demonHunter.sendMessage("§c§l[收割者] §c攻击冷却中！");
            return true;
        }
        
        // 更新冷却
        reaperAttackCooldown.put(demonHunterId, System.currentTimeMillis());
        
        // 获取伤害值
        int damage = plugin.getConfigManager().getReaperWeaponDamagePerHit();
        
        // 记录攻击次数
        int hitCount = ghostHitCount.getOrDefault(ghostId, 0) + 1;
        ghostHitCount.put(ghostId, hitCount);
        
        int hitsToKill = plugin.getConfigManager().getReaperWeaponHitsToKill();
        
        demonHunter.sendMessage("§6§l[收割者] §e命中！ (§c" + hitCount + "§e/§c" + hitsToKill + "§e)");
        demonHunter.sendMessage("§7造成伤害: §c" + damage + "❤");
        
        // 对鬼玩家造成伤害
        double currentHealth = ghost.getHealth();
        double newHealth = currentHealth - damage;
        
        if (newHealth <= 0) {
            // 玩家死亡
            killGhostWithReaper(ghost, demonHunter);
            ghostHitCount.remove(ghostId);
        } else {
            // 设置新血量
            ghost.setHealth(newHealth);
            
            // 击退效果
            Vector direction = ghost.getLocation().toVector().subtract(demonHunter.getLocation().toVector()).normalize();
            ghost.setVelocity(direction.multiply(0.5).setY(0.3));
            
            // 视觉效果
            ghost.getWorld().spawnParticle(Particle.CRIT, ghost.getLocation(), 10, 0.5, 0.5, 0.5, 0.5);
            ghost.playSound(ghost.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
            
            // 发送血量信息给鬼玩家
            ghost.sendMessage(String.format("§c§l[收割者] §c你被猎魔人攻击！剩余血量: §4%.1f❤", newHealth));
        }
        
        return true;
    }
    
    /**
     * 用收割者击杀鬼玩家
     */
    private void killGhostWithReaper(Player ghost, Player demonHunter) {
        UUID ghostId = ghost.getUniqueId();
        UUID demonHunterId = demonHunter.getUniqueId();
        
        // 检查是否启用复活机制
        if (plugin.getConfigManager().isRespawnEnabled() && isDemonHunterPhase) {
            // 启动复活计时器
            startRespawnTimer(ghost);
        } else {
            // 将鬼玩家变为旁观者（旧逻辑）
            setPlayerToSpectator(ghost);
        }
        
        // 更新击杀计数
        int kills = demonHunterKillCount.getOrDefault(demonHunterId, 0) + 1;
        demonHunterKillCount.put(demonHunterId, kills);
        
        // 奖励分配
        distributeDemonHunterKillReward(demonHunter, ghost);
        
        // 广播消息
        if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
            String message = String.format("§6§l[猎魔人] §e猎魔人 §a%s §e击杀了鬼玩家 §c%s§e！", 
                demonHunter.getName(), ghost.getName());
            Bukkit.broadcastMessage(message);
            
            // 如果启用复活，显示复活倒计时
            if (plugin.getConfigManager().isRespawnEnabled() && isDemonHunterPhase) {
                int respawnTime = plugin.getConfigManager().getRespawnTime();
                Bukkit.broadcastMessage(String.format("§7%s 将在 §e%d秒 §7后复活", ghost.getName(), respawnTime));
            }
        }
        
        // 视觉效果
        Location loc = ghost.getLocation();
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);
        
        demonHunter.sendMessage("§6§l[猎魔人] §a你击杀了一名鬼玩家！");
        demonHunter.sendMessage("§7累计击杀: §e" + kills);
        
        // 检查游戏是否结束
        checkGameEnd();
    }
    
    /**
     * 击杀猎魔人
     */
    private void killDemonHunter(Player demonHunter, Player motherGhost) {
        UUID demonHunterId = demonHunter.getUniqueId();
        
        // 将猎魔人变为旁观者
        setPlayerToSpectator(demonHunter);
        
        // 移除猎魔人状态
        demonHunterPlayers.remove(demonHunterId);
        holyGuardianPlayers.remove(demonHunterId);
        
        // 奖励分配
        distributeMotherKillDemonHunterReward(motherGhost, demonHunter);
        
        // 广播消息
        if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
            String message = String.format("§6§l[猎魔人] §c母体 §4%s §c击杀了猎魔人 §6%s§c！", 
                motherGhost.getName(), demonHunter.getName());
            Bukkit.broadcastMessage(message);
        }
        
        // 视觉效果
        Location loc = demonHunter.getLocation();
        loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 50, 0.5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
        
        motherGhost.sendMessage("§6§l[母体] §a你成功击杀了猎魔人！");
        
        // 检查游戏是否结束
        checkGameEnd();
    }
    
    /**
     * 将玩家设置为旁观者
     */
    private void setPlayerToSpectator(Player player) {
        UUID playerId = player.getUniqueId();
        
        spectatorPlayers.add(playerId);
        spectatorOriginalLocations.put(playerId, player.getLocation().clone());
        
        // 设置为旁观模式
        player.setGameMode(GameMode.SPECTATOR);
        
        // 清除所有效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // 防止离开游戏区域
        startSpectatorBoundaryCheck(player);
        
        player.sendMessage("§c§l[游戏结束] §c你已被淘汰，进入旁观模式");
        
        // 检查是否在猎魔人阶段且启用复活机制
        if (isDemonHunterPhase && plugin.getConfigManager().isRespawnEnabled() && 
            plugin.getPlayerManager().isGhost(playerId)) {
            // 启动复活计时器
            startRespawnTimer(player);
            int respawnTime = plugin.getConfigManager().getRespawnTime();
            player.sendMessage(String.format("§e你将在 §6%d秒 §e后复活", respawnTime));
        } else {
            player.sendMessage("§7请等待游戏结束");
        }
    }
    
    /**
     * 检查并新增母体
     */
    private void checkAndAddAdditionalMother() {
        if (!plugin.getConfigManager().isAdditionalMotherEnabled()) {
            return;
        }
        
        if (!isDemonHunterPhase) {
            return;
        }
        
        if (hasAdditionalMotherSpawned) {
            return;
        }
        
        // 获取所有玩家
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        int playerThreshold = plugin.getConfigManager().getAdditionalMotherPlayerThreshold();
        
        if (allPlayers.size() >= playerThreshold) {
            // 从普通鬼玩家中随机选择一位变为母体
            List<UUID> normalGhosts = new ArrayList<>();
            for (UUID playerId : allPlayers) {
                if (plugin.getPlayerManager().getPlayerRole(playerId) == 
                    io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole.GHOST_NORMAL) {
                    normalGhosts.add(playerId);
                }
            }
            
            if (!normalGhosts.isEmpty()) {
                UUID newMotherId = normalGhosts.get(random.nextInt(normalGhosts.size()));
                Player newMother = Bukkit.getPlayer(newMotherId);
                
                if (newMother != null && newMother.isOnline()) {
                    // 设置为母体
                    plugin.getPlayerManager().setPlayerRole(newMotherId, 
                        io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole.GHOST_MOTHER);
                    
                    additionalMothers.add(newMotherId);
                    hasAdditionalMotherSpawned = true;
                    

                    // 广播消息
                    if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
                        String message = String.format("§6§l[猎魔人阶段] §c玩家 §4%s §c被选为新增母体！", 
                            newMother.getName());
                        Bukkit.broadcastMessage(message);
                        Bukkit.broadcastMessage("§7• 只有母体可以攻击猎魔人");
                    }
                    
                    newMother.sendMessage("§6§l[母体] §a你被选为新增母体！");
                    newMother.sendMessage("§7• 你现在可以攻击猎魔人");
                    newMother.sendMessage("§7• 破除神圣守护后一击即可击杀猎魔人");
                }
            }
        }
    }
    
    /**
     * 分配猎魔人击杀奖励
     */
    private void distributeDemonHunterKillReward(Player demonHunter, Player ghost) {
        if (!plugin.getConfigManager().isVaultEnabled()) {
            return;
        }
        
        double rewardRatio = plugin.getConfigManager().getDemonHunterKillRewardRatio();
        double humanPool = plugin.getEconomyManager().getHumanPrizePool();
        double reward = humanPool * rewardRatio;
        
        if (reward > 0) {
            plugin.getEconomyManager().giveMoney(demonHunter, reward);
            demonHunter.sendMessage("§6════════════════════════════════");
            demonHunter.sendMessage("§e💰 猎魔人击杀奖励 💰");
            demonHunter.sendMessage(String.format("§a你获得了 §e§l%.2f §a金币奖励！", reward));
            demonHunter.sendMessage(String.format("§7（基于人类奖池的 §e%.0f%%§7 分配）", rewardRatio * 100));
            demonHunter.sendMessage("§6════════════════════════════════");
            demonHunter.playSound(demonHunter.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
    
    /**
     * 分配母体击杀猎魔人奖励
     */
    private void distributeMotherKillDemonHunterReward(Player mother, Player demonHunter) {
        if (!plugin.getConfigManager().isVaultEnabled()) {
            return;
        }
        
        double rewardRatio = plugin.getConfigManager().getMotherKillDemonHunterRewardRatio();
        double totalPool = plugin.getEconomyManager().getTotalPrizePool();
        double reward = totalPool * rewardRatio;
        
        if (reward > 0) {
            plugin.getEconomyManager().giveMoney(mother, reward);
            mother.sendMessage("§6════════════════════════════════");
            mother.sendMessage("§c💰 母体击杀猎魔人奖励 💰");
            mother.sendMessage(String.format("§a你获得了 §e§l%.2f §a金币奖励！", reward));
            mother.sendMessage(String.format("§7（基于总奖池的 §e%.0f%%§7 分配）", rewardRatio * 100));
            mother.sendMessage("§7（高于普通感染奖励）");
            mother.sendMessage("§6════════════════════════════════");
            mother.playSound(mother.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.9f);
            mother.playSound(mother.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
        }
    }
    
    /**
     * 检查游戏是否应该结束
     */
    private void checkGameEnd() {
        // 检查是否所有猎魔人都被淘汰
        boolean allDemonHuntersEliminated = true;
        for (UUID demonHunterId : demonHunterPlayers) {
            Player player = Bukkit.getPlayer(demonHunterId);
            if (player != null && player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                allDemonHuntersEliminated = false;
                break;
            }
        }
        
        // 检查是否所有鬼都被淘汰
        boolean allGhostsEliminated = true;
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        for (UUID playerId : allPlayers) {
            if (spectatorPlayers.contains(playerId)) {
                continue; // 旁观者不计入
            }
            
            io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole role = 
                plugin.getPlayerManager().getPlayerRole(playerId);
            if (role == io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole.GHOST_NORMAL ||
                role == io.Sriptirc_wp_1258.gost.managers.PlayerManager.PlayerRole.GHOST_MOTHER) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                    allGhostsEliminated = false;
                    break;
                }
            }
        }
        
        // 如果所有猎魔人或所有鬼都被淘汰，结束游戏
        if (allDemonHuntersEliminated || allGhostsEliminated) {
            plugin.getGameManager().endGame(true);
        }
    }
    
    /**
     * 开始旁观者边界检查
     */
    private void startSpectatorBoundaryCheck(Player spectator) {
        UUID playerId = spectator.getUniqueId();
        
        // 如果已有任务，先取消
        if (spectatorBoundaryTasks.containsKey(playerId)) {
            org.bukkit.scheduler.BukkitTask existingTask = spectatorBoundaryTasks.get(playerId);
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
        }
        
        org.bukkit.scheduler.BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!spectator.isOnline() || spectator.getGameMode() != GameMode.SPECTATOR) {
                    this.cancel();
                    spectatorBoundaryTasks.remove(playerId);
                    return;
                }
                
                // 检查是否离开游戏区域
                io.Sriptirc_wp_1258.gost.managers.AreaManager.GameArea area = 
                    plugin.getAreaManager().getSelectedArea();
                if (area != null) {
                    Location originalLoc = spectatorOriginalLocations.get(spectator.getUniqueId());
                    if (originalLoc != null) {
                        // 如果距离原始位置太远，传送回去
                        if (spectator.getLocation().distance(originalLoc) > 50) {
                            spectator.teleport(originalLoc);
                            spectator.sendMessage("§c§l[旁观] §c你不能离开游戏区域！");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒检查一次
        
        spectatorBoundaryTasks.put(playerId, task);
    }
    
    /**
     * 取消旁观者边界检查任务
     */
    private void cancelSpectatorBoundaryCheck(UUID playerId) {
        org.bukkit.scheduler.BukkitTask task = spectatorBoundaryTasks.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    
    /**
     * 清除所有玩家的神之救赎道具
     */
    private void clearAllHolyRedemptionItems() {
        // 遍历所有在线玩家，移除神之救赎道具
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline()) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null && meta.hasDisplayName()) {
                            String displayName = meta.getDisplayName();
                            // 检查是否是神之救赎道具
                            if (displayName.equals("§6§l神之救赎") || displayName.contains("神之救赎")) {
                                player.getInventory().remove(item);
                                player.sendMessage("§6§l[猎魔人阶段] §c神之救赎道具已被清除");
                            }
                        }
                    }
                }
            }
        }
        
        // 清除神之救赎使用次数
        holyRedemptionUses.clear();
        holyRedemptionCooldown.clear();
    }
    
    /**
     * 清理游戏数据
     */
    public void cleanup() {
        // 恢复所有旁观者为生存模式
        for (UUID playerId : spectatorPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("§a游戏结束，你已恢复为生存模式！");
            }
        }
        
        // 取消所有旁观者边界检查任务
        for (org.bukkit.scheduler.BukkitTask task : spectatorBoundaryTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        
        holyGuardianPlayers.clear();
        holyGuardianActivationTime.clear();
        holyGuardianHitCount.clear();
        demonHunterPlayers.clear();
        demonHunterKillCount.clear();
        reaperAttackCooldown.clear();
        ghostHitCount.clear();
        holyRedemptionUses.clear();
        holyRedemptionCooldown.clear();
        additionalMothers.clear();
        spectatorPlayers.clear();
        spectatorOriginalLocations.clear();
        spectatorBoundaryTasks.clear();
        
        // 清理复活数据
        cleanupRespawnData();
        
        isDemonHunterPhase = false;
        hasAdditionalMotherSpawned = false;
    }
    
    /**
     * 重置游戏数据
     */
    public void resetGame() {
        cleanup();
    }
    
    // ==================== 公共方法 ====================
    
    public boolean isHolyGuardian(UUID playerId) {
        return holyGuardianPlayers.contains(playerId);
    }
    
    public boolean isDemonHunter(UUID playerId) {
        return demonHunterPlayers.contains(playerId);
    }
    
    public boolean isInDemonHunterPhase() {
        return isDemonHunterPhase;
    }
    
    public int getDemonHunterKillCount(UUID playerId) {
        return demonHunterKillCount.getOrDefault(playerId, 0);
    }
    
    public int getHolyRedemptionRemainingUses(UUID playerId) {
        return holyRedemptionUses.getOrDefault(playerId, 0);
    }
    
    public boolean isSpectator(UUID playerId) {
        return spectatorPlayers.contains(playerId);
    }
    
    // ==================== 复活机制相关方法 ====================
    
    /**
     * 启动复活计时器
     */
    private void startRespawnTimer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // 取消现有的复活任务
        cancelRespawnTask(playerId);
        
        int respawnTime = plugin.getConfigManager().getRespawnTime();
        long respawnTimestamp = System.currentTimeMillis() + (respawnTime * 1000L);
        respawnTimers.put(playerId, respawnTimestamp);
        
        // 创建复活任务
        org.bukkit.scheduler.BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            respawnPlayer(playerId);
        }, respawnTime * 20L); // 转换为游戏刻
        
        respawnTasks.put(playerId, task);
        
        // 发送倒计时消息
        sendRespawnCountdown(player, respawnTime);
    }
    
    /**
     * 发送复活倒计时消息
     */
    private void sendRespawnCountdown(Player player, int totalSeconds) {
        for (int i = 1; i <= totalSeconds; i++) {
            final int remaining = totalSeconds - i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && respawnTimers.containsKey(player.getUniqueId())) {
                    if (remaining > 0) {
                        player.sendActionBar(String.format("§e复活倒计时: §6%d秒", remaining));
                    } else {
                        player.sendActionBar("§a正在复活...");
                    }
                }
            }, i * 20L);
        }
    }
    
    /**
     * 复活玩家
     */
    private void respawnPlayer(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            // 玩家不在线，清理数据
            respawnTimers.remove(playerId);
            cancelRespawnTask(playerId);
            return;
        }
        
        // 检查玩家是否还是旁观者
        if (!spectatorPlayers.contains(playerId)) {
            respawnTimers.remove(playerId);
            cancelRespawnTask(playerId);
            return;
        }
        
        // 恢复为生存模式
        player.setGameMode(GameMode.SURVIVAL);
        
        // 移除旁观者数据
        spectatorPlayers.remove(playerId);
        spectatorOriginalLocations.remove(playerId);
        
        // 取消边界检查任务
        cancelSpectatorBoundaryCheck(playerId);
        
        // 设置血量（根据配置）
        double health = plugin.getConfigManager().getGhostNormalHealth();
        if (plugin.getPlayerManager().isMotherGhost(playerId)) {
            health = plugin.getConfigManager().getGhostMotherHealth();
        }
        player.setHealth(health);
        
        // 清除复活数据
        respawnTimers.remove(playerId);
        cancelRespawnTask(playerId);
        
        // 发送消息
        player.sendMessage("§a§l[复活] §a你已复活！");
        player.sendMessage(String.format("§7当前血量: §c%.1f❤", health));
        
        // 广播消息
        if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
            Bukkit.broadcastMessage(String.format("§6§l[猎魔人] §e鬼玩家 §c%s §e已复活！", player.getName()));
        }
        
        // 视觉效果
        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 30, 0.5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
    }
    
    /**
     * 取消复活任务
     */
    private void cancelRespawnTask(UUID playerId) {
        org.bukkit.scheduler.BukkitTask task = respawnTasks.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    
    /**
     * 清理复活数据
     */
    private void cleanupRespawnData() {
        // 取消所有复活任务
        for (org.bukkit.scheduler.BukkitTask task : respawnTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        
        respawnTimers.clear();
        respawnTasks.clear();
    }
    
    /**
     * 检查玩家是否在复活倒计时中
     */
    public boolean isInRespawnTimer(UUID playerId) {
        return respawnTimers.containsKey(playerId);
    }
    
    /**
     * 获取剩余复活时间（秒）
     */
    public int getRemainingRespawnTime(UUID playerId) {
        Long timestamp = respawnTimers.get(playerId);
        if (timestamp == null) {
            return 0;
        }
        
        long remaining = timestamp - System.currentTimeMillis();
        return Math.max(0, (int) (remaining / 1000));
    }
    
    /**
     * 在猎魔人阶段调整所有鬼玩家的血量
     */
    private void adjustGhostHealthInDemonHunterPhase() {
        for (UUID playerId : plugin.getPlayerManager().getGhostPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline() && !spectatorPlayers.contains(playerId)) {
                double health;
                if (plugin.getPlayerManager().isMotherGhost(playerId)) {
                    health = plugin.getConfigManager().getGhostMotherHealth();
                } else {
                    health = plugin.getConfigManager().getGhostNormalHealth();
                }
                
                player.setHealth(health);
                player.sendMessage(String.format("§6§l[猎魔人阶段] §e你的血量已调整为 §c%.1f❤", health));
                
                // 如果禁止回血，添加提示
                if (plugin.getConfigManager().isNoHealingInDemonHunterPhase()) {
                    player.sendMessage("§c⚠ 猎魔人阶段禁止回血");
                }
            }
        }
    }
    
    // ==================== 兼容旧命令系统的方法 ====================
    
    /**
     * 重新加载配置
     */
    public void reload() {
        // 空实现，兼容旧命令
    }
    
    /**
     * 设置守护模式
     */
    public boolean setGuardianMode(String mode) {
        // 空实现，兼容旧命令
        return true;
    }
    
    /**
     * 获取神圣守护玩家
     */
    public UUID getDivineGuardianPlayer() {
        // 返回第一个神圣守护玩家或null
        if (!holyGuardianPlayers.isEmpty()) {
            return holyGuardianPlayers.iterator().next();
        }
        return null;
    }
    
    /**
     * 获取剩余次数
     */
    public int getRemainingCharges(UUID playerId) {
        // 返回0，兼容旧命令
        return 0;
    }
    
    /**
     * 获取模式显示名称
     */
    public String getModeDisplayName() {
        return "默认模式";
    }
    
    /**
     * 获取救赎者剩余使用次数
     */
    public int getRedeemerRemainingUses(UUID playerId) {
        return holyRedemptionUses.getOrDefault(playerId, 0);
    }
    
    /**
     * 检查并激活神圣守护
     * 兼容旧命令调用
     */
    public void checkAndActivateDivineGuardian(List<UUID> humanPlayers) {
        // 调用实际实现方法
        checkAndActivateHolyGuardian(humanPlayers);
    }
    
    /**
     * 检查玩家是否有活跃的神圣守护（未过期）
     */
    public boolean hasActiveHolyGuardian(UUID playerId) {
        if (!holyGuardianPlayers.contains(playerId)) {
            return false;
        }
        
        // 检查持续时间
        Long activationTime = holyGuardianActivationTime.get(playerId);
        if (activationTime == null) {
            holyGuardianPlayers.remove(playerId);
            return false;
        }
        
        int duration = plugin.getConfigManager().getHolyGuardianEffectDuration();
        long elapsed = System.currentTimeMillis() - activationTime;
        
        if (elapsed > duration * 1000L) {
            // 神圣守护已过期
            holyGuardianPlayers.remove(playerId);
            holyGuardianActivationTime.remove(playerId);
            
            // 清除发光效果
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.GLOWING);
            }
            
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查玩家是否是救赎者
     */
    public boolean isRedeemer(UUID playerId) {
        // 检查玩家是否有神之救赎使用次数
        return holyRedemptionUses.containsKey(playerId) && holyRedemptionUses.get(playerId) > 0;
    }

    
    /**
     * 使用神之救赎
     */
    public boolean useHolyRedemption(Player redeemer, Player target) {
        UUID redeemerId = redeemer.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // 检查是否是救赎者（有神之救赎使用次数）
        if (!holyRedemptionUses.containsKey(redeemerId) || holyRedemptionUses.get(redeemerId) <= 0) {
            redeemer.sendMessage("§c§l[神之救赎] §c你不是救赎者或已无使用次数！");
            return false;
        }
        
        // 检查冷却时间
        Long lastUse = holyRedemptionCooldown.get(redeemerId);
        int cooldown = plugin.getConfigManager().getDemonHunterHolyRedemptionCooldown();
        if (lastUse != null && System.currentTimeMillis() - lastUse < cooldown * 1000L) {
            long remaining = cooldown - (System.currentTimeMillis() - lastUse) / 1000L;
            redeemer.sendMessage(String.format("§c§l[神之救赎] §c道具冷却中，剩余 §e%d§c 秒", remaining));
            return false;
        }
        
        // 检查目标是否是鬼玩家
        if (!plugin.getPlayerManager().isGhost(targetId)) {
            redeemer.sendMessage("§c§l[神之救赎] §c只能对鬼玩家使用！");
            return false;
        }
        
        // 检查目标是否是母体（母体不能被转化）
        if (plugin.getPlayerManager().isMotherGhost(targetId)) {
            redeemer.sendMessage("§c§l[神之救赎] §c无法转化母体鬼！");
            return false;
        }
        
        // 执行转化
        boolean success = plugin.getPlayerManager().convertGhostToHuman(targetId);
        if (!success) {
            redeemer.sendMessage("§c§l[神之救赎] §c转化失败！");
            return false;
        }
        
        // 移除目标的神圣守护状态（如果有）
        holyGuardianPlayers.remove(targetId);
        holyGuardianActivationTime.remove(targetId);
        
        // 更新使用次数
        int remainingUses = holyRedemptionUses.get(redeemerId) - 1;
        holyRedemptionUses.put(redeemerId, remainingUses);
        
        // 设置冷却时间
        holyRedemptionCooldown.put(redeemerId, System.currentTimeMillis());
        
        // 移除道具（如果手中还有）
        ItemStack itemInHand = redeemer.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && meta.hasDisplayName() && 
                meta.getDisplayName().equals("§6§l神之救赎")) {
                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    redeemer.getInventory().setItemInMainHand(null);
                }
            }
        }
        
        // 发送消息
        redeemer.sendMessage("§6§l[神之救赎] §a你成功将 §e" + target.getName() + " §a转化回人类！");
        redeemer.sendMessage(String.format("§7剩余使用次数: §e%d", remainingUses));
        
        target.sendMessage("§6§l[神之救赎] §a你被 §e" + redeemer.getName() + " §a使用神之救赎转化回人类！");
        
        // 广播消息
        if (plugin.getConfigManager().isDivineGuardianBroadcastEnabled()) {
            Bukkit.broadcastMessage(String.format("§6§l[神之救赎] §e救赎者 §a%s §e使用神之救赎将 §a%s §e转化回人类！", 
                redeemer.getName(), target.getName()));
        }
        
        // 视觉效果
        Location loc = target.getLocation();
        loc.getWorld().spawnParticle(Particle.TOTEM, loc, 30, 0.5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        
        // 随机传送救赎者（防止被报复）
        teleportAttackerRandomly(redeemer);
        redeemer.sendMessage("§6§l[神之救赎] §e你被随机传送以保护安全！");
        
        // 更新神圣守护检查
        List<UUID> humanPlayers = plugin.getPlayerManager().getHumanPlayers();
        checkAndActivateHolyGuardian(humanPlayers);
        
        return true;
    }
}