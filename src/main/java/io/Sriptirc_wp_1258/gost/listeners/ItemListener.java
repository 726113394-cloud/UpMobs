package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemListener implements Listener {
    
    private final Gost plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Long> lastSoulDetectorUse = new HashMap<>(); // 防止快速连续点击
    
    public ItemListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        if (!plugin.getPlayerManager().getAllPlayers().contains(player.getUniqueId())) {
            return;
        }
        
        // 检查是否是左键或右键操作（支持左右键使用道具）
        Action action = event.getAction();
        boolean isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        
        if (!isRightClick && !isLeftClick) {
            return;
        }
        
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }
        
        String displayName = meta.getDisplayName();
        event.setCancelled(true);
        
        // 处理不同物品
        if (displayName.contains("肾上腺素")) {
            handleAdrenaline(player, item);
        } else if (displayName.contains("狂暴药水")) {
            handleFrenzyPotion(player, item);
        } else if (displayName.contains("凝冰球")) {
            handleIceBall(player, event);
        } else if (displayName.contains("控魂术")) {
            handleSoulControl(player, item);
        } else if (displayName.contains("臭牛排")) {
            handleStinkySteak(player, item);
        } else if (displayName.contains("传送珍珠")) {
            handleTeleportPearl(player, item, event);
        } else if (displayName.contains("灵魂探测器")) {
            handleSoulDetector(player, item);
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // 检查是否是雪球（凝冰球）
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }
        
        Snowball snowball = (Snowball) event.getEntity();
        if (!(snowball.getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) snowball.getShooter();
        
        // 检查射击者是否在游戏中
        if (!plugin.getPlayerManager().getAllPlayers().contains(shooter.getUniqueId())) {
            return;
        }
        
        // 检查被击中的实体是否是玩家
        if (event.getHitEntity() instanceof Player) {
            Player target = (Player) event.getHitEntity();
            
            // 检查目标是否是鬼
            boolean isGhost = plugin.getPlayerManager().isGhost(target.getUniqueId());
            plugin.getLogger().info("凝冰球击中玩家: " + target.getName() + " (射击者: " + shooter.getName() + ", 是鬼: " + isGhost + ")");
            
            // 凝冰球是通用道具，对所有玩家都应用减速效果
            // 应用凝冰球效果
            plugin.getItemManager().applyIceBallEffect(target);
            shooter.sendMessage(ChatColor.AQUA + "你成功击中了 " + target.getName() + "！");
            plugin.getLogger().info("凝冰球击中玩家: " + target.getName() + " (射击者: " + shooter.getName() + ")，应用减速效果");
        }
    }
    
    private void handleAdrenaline(Player player, ItemStack item) {
        // 检查玩家是否是人类
        if (!plugin.getPlayerManager().isHuman(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "只有人类可以使用肾上腺素！");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendAdrenalineHint(player);
        
        // 应用肾上腺素效果
        plugin.getItemManager().applyAdrenalineEffect(player);
        
        // 消耗物品
        consumeItem(player, item);
    }
    
    private void handleFrenzyPotion(Player player, ItemStack item) {
        // 检查玩家是否是鬼
        if (!plugin.getPlayerManager().isGhost(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "只有鬼可以使用狂暴药水！");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendFrenzyPotionHint(player);
        
        // 应用狂暴药水效果
        plugin.getItemManager().applyFrenzyEffect(player);
        
        // 消耗物品
        consumeItem(player, item);
    }
    
    private void handleIceBall(Player player, PlayerInteractEvent event) {
        // 发送ActionBar提示
        plugin.getActionBarManager().sendIceBallHint(player);
        
        // 允许投掷雪球（不消耗物品，由事件处理）
        // 这里不消耗物品，让雪球正常投掷
        event.setCancelled(false);
    }
    
    private void handleSoulControl(Player player, ItemStack item) {
        // 检查冷却时间
        if (isOnCooldown(player, "soul-control")) {
            int remaining = getRemainingCooldown(player, "soul-control");
            player.sendMessage(ChatColor.RED + "控魂术冷却中，剩余 " + remaining + " 秒");
            return;
        }
        
        // 检查玩家是否是人类
        if (!plugin.getPlayerManager().isHuman(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "只有人类可以使用控魂术！");
            return;
        }
        
        // 获取所有鬼玩家
        List<Player> ghostPlayers = new ArrayList<>();
        io.Sriptirc_wp_1258.gost.managers.AreaManager.GameArea selectedArea = plugin.getAreaManager().getSelectedArea();
        if (selectedArea != null) {
            ghostPlayers = plugin.getAreaManager().getPlayersInArea(selectedArea);
            ghostPlayers.removeIf(p -> !plugin.getPlayerManager().isGhost(p.getUniqueId()));
        }
        
        if (ghostPlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "附近没有鬼玩家！");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendSoulControlHint(player);
        
        // 应用控魂术效果
        plugin.getItemManager().applySoulControlEffect(ghostPlayers);
        
        // 设置冷却时间
        int cooldown = plugin.getConfigManager().getSoulControlCooldown();
        setCooldown(player, "soul-control", cooldown);
        
        // 消耗物品
        consumeItem(player, item);
    }
    
    private void handleBlinkPearl(Player player, ItemStack item, PlayerInteractEvent event) {
        // 这个方法已废弃，保留但不使用
        // 所有传送功能已合并到handleTeleportPearl方法中
        player.sendMessage(ChatColor.RED + "闪现珍珠已废弃，请使用传送珍珠！");
        event.setCancelled(true);
    }
    
    private void consumeItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
        player.updateInventory();
    }
    
    // 冷却时间管理方法
    private boolean isOnCooldown(Player player, String itemType) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(itemType)) {
            return false;
        }
        
        long cooldownEnd = playerCooldowns.get(itemType);
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    private int getRemainingCooldown(Player player, String itemType) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(itemType)) {
            return 0;
        }
        
        long cooldownEnd = playerCooldowns.get(itemType);
        long remaining = cooldownEnd - System.currentTimeMillis();
        return (int) Math.ceil(remaining / 1000.0);
    }
    
    private void setCooldown(Player player, String itemType, int seconds) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
        playerCooldowns.put(itemType, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    // 清理玩家的冷却时间
    public void clearPlayerCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }
    
    // 清理所有冷却时间
    public void clearAllCooldowns() {
        cooldowns.clear();
    }
    
    private void handleStinkySteak(Player player, ItemStack item) {
        // 检查冷却时间
        if (isOnCooldown(player, "stinky-steak")) {
            int remaining = getRemainingCooldown(player, "stinky-steak");
            player.sendMessage(ChatColor.RED + "臭牛排冷却中，剩余 " + remaining + " 秒");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendStinkySteakHint(player);
        
        // 保存当前饱食度
        float savedFoodLevel = player.getFoodLevel();
        float savedSaturation = player.getSaturation();
        
        // 临时设置饱食度为20，确保可以食用
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // 从配置获取效果参数
        int speedDuration = plugin.getConfigManager().getStinkySteakSpeedDuration();
        int speedLevel = plugin.getConfigManager().getStinkySteakSpeedLevel();
        int glowingDuration = plugin.getConfigManager().getStinkySteakGlowingDuration();
        int cooldown = plugin.getConfigManager().getStinkySteakCooldown();
        
        // 应用臭牛排效果 - 速度效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, speedLevel));
        
        // 应用发光效果
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowingDuration * 20, 0));
        
        // 设置冷却时间
        setCooldown(player, "stinky-steak", cooldown);
        
        // 消耗物品
        consumeItem(player, item);
        
        // 恢复原来的饱食度
        player.setFoodLevel((int) savedFoodLevel);
        player.setSaturation(savedSaturation);
        
        // 发送使用提示
        player.sendMessage(ChatColor.GREEN + "你食用了臭牛排，获得了速度" + (speedLevel + 1) + "效果和发光效果！");
    }
    
    private void handleTeleportPearl(Player player, ItemStack item, PlayerInteractEvent event) {
        // 检查冷却时间
        if (isOnCooldown(player, "teleport-pearl")) {
            int remaining = getRemainingCooldown(player, "teleport-pearl");
            player.sendMessage(ChatColor.RED + "传送珍珠冷却中，剩余 " + remaining + " 秒");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendTeleportPearlHint(player);
        
        // 允许使用末影珍珠（不取消事件）
        event.setCancelled(false);
        
        // 设置冷却时间
        int cooldown = plugin.getConfigManager().getTeleportPearlCooldown();
        setCooldown(player, "teleport-pearl", cooldown);
        
        // 发送使用提示
        player.sendMessage(ChatColor.GREEN + "你使用了传送珍珠，冷却时间 " + cooldown + " 秒");
    }
    
    private void handleSoulDetector(Player player, ItemStack item) {
        // 防止快速连续点击（500毫秒内）
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (lastSoulDetectorUse.containsKey(playerId)) {
            long lastUse = lastSoulDetectorUse.get(playerId);
            if (currentTime - lastUse < 500) {
                plugin.getLogger().warning("玩家 " + player.getName() + " 快速连续点击灵魂探测器，已阻止");
                return;
            }
        }
        lastSoulDetectorUse.put(playerId, currentTime);
        
        // 检查冷却时间
        if (isOnCooldown(player, "soul-detector")) {
            int remaining = getRemainingCooldown(player, "soul-detector");
            player.sendMessage(ChatColor.RED + "灵魂探测器冷却中，剩余 " + remaining + " 秒");
            plugin.getLogger().warning("玩家 " + player.getName() + " 尝试在冷却期间使用灵魂探测器，剩余 " + remaining + " 秒");
            return;
        }
        
        plugin.getLogger().info("玩家 " + player.getName() + " 尝试使用灵魂探测器");
        
        // 检查玩家是否是鬼
        boolean isGhost = plugin.getPlayerManager().isGhost(player.getUniqueId());
        plugin.getLogger().info("玩家 " + player.getName() + " 是鬼: " + isGhost);
        
        if (!isGhost) {
            player.sendMessage(ChatColor.RED + "只有鬼可以使用灵魂探测器！");
            plugin.getLogger().info("玩家 " + player.getName() + " 不是鬼，无法使用灵魂探测器");
            return;
        }
        
        // 发送ActionBar提示
        plugin.getActionBarManager().sendSoulDetectorHint(player);
        
        // 获取所有游戏中的玩家
        List<UUID> allPlayerIds = plugin.getPlayerManager().getAllPlayers();
        plugin.getLogger().info("游戏中玩家数量: " + allPlayerIds.size());
        
        List<Player> allPlayers = new ArrayList<>();
        for (UUID uuid : allPlayerIds) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                allPlayers.add(p);
                plugin.getLogger().info("在线玩家: " + p.getName());
            }
        }
        
        plugin.getLogger().info("在线玩家数量: " + allPlayers.size());
        
        if (allPlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "没有找到其他玩家！");
            plugin.getLogger().warning("灵魂探测器: 没有找到在线玩家");
            return;
        }
        
        // 应用发光效果给所有玩家
        plugin.getLogger().info("开始应用发光效果给 " + allPlayers.size() + " 名玩家");
        for (Player target : allPlayers) {
            boolean success = target.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                500, // 25秒 * 20 = 500 ticks
                0,
                true,
                true
            ));
            plugin.getLogger().info("给玩家 " + target.getName() + " 应用发光效果: " + (success ? "成功" : "失败"));
        }
        
        // 消耗物品
        consumeItem(player, item);
        
        // 设置冷却时间
        int cooldown = plugin.getConfigManager().getSoulDetectorCooldown();
        setCooldown(player, "soul-detector", cooldown);
        
        // 发送消息
        player.sendMessage(ChatColor.GREEN + "你使用了灵魂探测器！所有玩家发光25秒！");
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " 使用了灵魂探测器，所有玩家发光25秒！");
        
        plugin.getLogger().info("玩家 " + player.getName() + " 使用了灵魂探测器，所有玩家发光25秒，共影响 " + allPlayers.size() + " 名玩家");
        
        // 记录玩家当前位置，用于检测是否发生传送
        Location originalLocation = player.getLocation();
        plugin.getLogger().info("玩家 " + player.getName() + " 使用灵魂探测器时的位置: " + 
            "世界=" + originalLocation.getWorld().getName() + 
            ", X=" + originalLocation.getX() + 
            ", Y=" + originalLocation.getY() + 
            ", Z=" + originalLocation.getZ());
        
        // 延迟检查玩家是否被传送（1 tick后）
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location currentLocation = player.getLocation();
            if (!currentLocation.getWorld().getName().equals(originalLocation.getWorld().getName()) ||
                currentLocation.distance(originalLocation) > 1.0) {
                // 玩家被传送了，传送回原位置
                plugin.getLogger().warning("玩家 " + player.getName() + " 在使用灵魂探测器后被传送！从 " + 
                    originalLocation + " 到 " + currentLocation + "，正在传送回原位置");
                player.teleport(originalLocation);
                player.sendMessage(ChatColor.RED + "错误：灵魂探测器不应导致传送！已恢复原位置。");
            }
        }, 1L);
    }
}