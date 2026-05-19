package io.Sriptirc_wp_1258.gost.listeners;

import io.Sriptirc_wp_1258.gost.Gost;
import io.Sriptirc_wp_1258.gost.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecondChanceListener implements Listener {
    
    private final Gost plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public SecondChanceListener(Gost plugin) {
        this.plugin = plugin;
    }
    
    /*
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 检查是否是人类玩家被鬼玩家攻击
        if (!(event.getEntity() instanceof Player humanPlayer)) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player ghostPlayer)) {
            return;
        }
        
        // 检查玩家角色
        if (!plugin.getPlayerManager().isHuman(humanPlayer.getUniqueId()) || 
            !plugin.getPlayerManager().isGhost(ghostPlayer.getUniqueId())) {
            return;
        }
        
        // 检查人类玩家是否拥有"一次机会"道具
        ItemStack secondChanceItem = findSecondChanceItem(humanPlayer);
        if (secondChanceItem == null) {
            return;
        }
        
        // 检查冷却时间
        UUID humanId = humanPlayer.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getConfigManager().getSecondChanceCooldown() * 1000L;
        
        if (cooldowns.containsKey(humanId)) {
            long lastUseTime = cooldowns.get(humanId);
            long timeLeft = (lastUseTime + cooldownTime) - currentTime;
            
            if (timeLeft > 0) {
                // 还在冷却中
                humanPlayer.sendMessage(ChatColor.RED + "一次机会还在冷却中！剩余时间: " + (timeLeft / 1000) + "秒");
                return;
            }
        }
        
        // 触发一次机会效果
        triggerSecondChance(humanPlayer, ghostPlayer, secondChanceItem);
        
        // 设置冷却时间
        cooldowns.put(humanId, currentTime);
        
        // 取消伤害事件（抵挡感染）
        event.setCancelled(true);
        
        // 发送屏幕居中字幕
        sendTitleMessages(humanPlayer, ghostPlayer);
    }
    */
    
    private ItemStack findSecondChanceItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    meta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&6一次机会"))) {
                    return item;
                }
            }
        }
        return null;
    }
    
    private void triggerSecondChance(Player humanPlayer, Player ghostPlayer, ItemStack secondChanceItem) {
        // 应用效果
        plugin.getItemManager().applySecondChanceEffect(humanPlayer, ghostPlayer);
        
        // 随机传送持有者
        teleportPlayerRandomly(humanPlayer);
        
        // 移除道具（因为是一次性道具）
        removeSecondChanceItem(humanPlayer, secondChanceItem);
        
        // 发送对话框消息
        sendDialogMessages(humanPlayer, ghostPlayer);
        
        // 发送公告
        Bukkit.broadcastMessage(ChatColor.YELLOW + "════════════════════════════════");
        Bukkit.broadcastMessage(ChatColor.GOLD + "✨ " + humanPlayer.getName() + " 使用了一次机会！");
        Bukkit.broadcastMessage(ChatColor.GOLD + "✨ 成功抵挡了 " + ghostPlayer.getName() + " 的感染！");
        Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("item.second_chance_teleport_broadcast"));
        Bukkit.broadcastMessage(ChatColor.YELLOW + "════════════════════════════════");
    }
    
    private void removeSecondChanceItem(Player player, ItemStack itemToRemove) {
        // 找到并移除道具
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.isSimilar(itemToRemove)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                break;
            }
        }
    }
    
    private void sendTitleMessages(Player humanPlayer, Player ghostPlayer) {
        // 给人类玩家发送标题
        humanPlayer.sendTitle(
            ChatColor.GOLD + "✨ 一次机会触发！",
            ChatColor.GREEN + "成功抵挡感染！获得速度和高亮效果",
            10, 40, 10
        );
        
        // 给鬼玩家发送标题
        ghostPlayer.sendTitle(
            ChatColor.RED + "⚠ 感染被抵挡！",
            ChatColor.YELLOW + "目标使用了一次机会！你被减速了",
            10, 40, 10
        );
    }
    
    private void sendDialogMessages(Player humanPlayer, Player ghostPlayer) {
        // 给人类玩家的对话框消息
        humanPlayer.sendMessage(ChatColor.YELLOW + "════════════════════════════════");
        humanPlayer.sendMessage(ChatColor.GOLD + "             一次机会触发！");
        humanPlayer.sendMessage("");
        humanPlayer.sendMessage(ChatColor.GREEN + "✓ 成功抵挡了 " + ghostPlayer.getName() + " 的感染");
        humanPlayer.sendMessage(ChatColor.GREEN + "✓ 获得速度II效果 " + plugin.getConfigManager().getSecondChanceHumanSpeedDuration() + "秒");
        humanPlayer.sendMessage(ChatColor.GREEN + "✓ 获得高亮效果 " + plugin.getConfigManager().getSecondChanceHumanGlowingDuration() + "秒");
        humanPlayer.sendMessage(ChatColor.YELLOW + "════════════════════════════════");
        
        // 给鬼玩家的对话框消息
        ghostPlayer.sendMessage(ChatColor.YELLOW + "════════════════════════════════");
        ghostPlayer.sendMessage(ChatColor.RED + "             感染被抵挡！");
        ghostPlayer.sendMessage("");
        ghostPlayer.sendMessage(ChatColor.RED + "✗ 对 " + humanPlayer.getName() + " 的感染被抵挡");
        ghostPlayer.sendMessage(ChatColor.RED + "✗ 获得缓慢I效果 " + plugin.getConfigManager().getSecondChanceGhostSlowDuration() + "秒");
        ghostPlayer.sendMessage(ChatColor.YELLOW + "════════════════════════════════");
    }
    
    // 清理冷却时间
    public void clearCooldowns() {
        cooldowns.clear();
    }
    
    /**
     * 检查并触发一次机会道具
     * @param humanPlayer 人类玩家
     * @param ghostPlayer 鬼玩家
     * @param event 伤害事件
     * @return 是否触发了一次机会
     */
    public boolean checkAndTriggerSecondChance(Player humanPlayer, Player ghostPlayer, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        // 检查人类玩家是否拥有"一次机会"道具
        ItemStack secondChanceItem = findSecondChanceItem(humanPlayer);
        if (secondChanceItem == null) {
            return false;
        }
        
        // 检查冷却时间
        UUID humanId = humanPlayer.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getConfigManager().getSecondChanceCooldown() * 1000L;
        
        if (cooldowns.containsKey(humanId)) {
            long lastUseTime = cooldowns.get(humanId);
            long timeLeft = (lastUseTime + cooldownTime) - currentTime;
            
            if (timeLeft > 0) {
                // 还在冷却中
                humanPlayer.sendMessage(ChatColor.RED + "一次机会还在冷却中！剩余时间: " + (timeLeft / 1000) + "秒");
                return false;
            }
        }
        
        // 触发一次机会效果
        triggerSecondChance(humanPlayer, ghostPlayer, secondChanceItem);
        
        // 设置冷却时间
        cooldowns.put(humanId, currentTime);
        
        // 发送屏幕居中字幕
        sendTitleMessages(humanPlayer, ghostPlayer);
        
        return true;
    }
    
    // 获取剩余冷却时间
    public long getRemainingCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long lastUseTime = cooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownTime = plugin.getConfigManager().getSecondChanceCooldown() * 1000L;
        long timeLeft = (lastUseTime + cooldownTime) - currentTime;
        
        return Math.max(0, timeLeft);
    }
    
    /**
     * 随机传送玩家到游戏区域内的随机位置
     */
    private void teleportPlayerRandomly(Player player) {
        try {
            // 获取游戏区域
            Location areaMin = plugin.getAreaManager().getAreaMin();
            Location areaMax = plugin.getAreaManager().getAreaMax();
            
            if (areaMin == null || areaMax == null) {
                plugin.getLogger().warning("无法随机传送玩家: 游戏区域未设置");
                return;
            }
            
            World world = areaMin.getWorld();
            if (world == null) {
                plugin.getLogger().warning("无法随机传送玩家: 世界未加载");
                return;
            }
            
            // 计算区域边界
            int minX = (int) Math.min(areaMin.getX(), areaMax.getX());
            int maxX = (int) Math.max(areaMin.getX(), areaMax.getX());
            int minZ = (int) Math.min(areaMin.getZ(), areaMax.getZ());
            int maxZ = (int) Math.max(areaMin.getZ(), areaMax.getZ());
            
            Random random = new Random();
            
            // 尝试寻找安全位置
            Location safeLocation = null;
            int attempts = 0;
            int maxAttempts = 20;
            
            while (safeLocation == null && attempts < maxAttempts) {
                attempts++;
                
                // 生成随机坐标
                int randomX = minX + random.nextInt(maxX - minX + 1);
                int randomZ = minZ + random.nextInt(maxZ - minZ + 1);
                
                // 获取该位置的地面Y坐标
                int groundY = findGroundY(world, randomX, randomZ);
                
                if (groundY > world.getMinHeight() && groundY < world.getMaxHeight()) {
                    // 创建安全位置
                    safeLocation = new Location(world, randomX + 0.5, groundY + 1, randomZ + 0.5);
                    
                    // 检查位置是否安全（没有液体，不是虚空）
                    if (isLocationSafe(safeLocation)) {
                        break;
                    } else {
                        safeLocation = null;
                    }
                }
            }
            
            if (safeLocation != null) {
                // 执行传送
                player.teleport(safeLocation);
                
                // 发送传送消息
                player.sendMessage(plugin.getLanguageManager().getMessage("item.random_teleport"));
                player.sendMessage(plugin.getLanguageManager().getMessage("item.teleport_coordinates",
                    (int)safeLocation.getX(),
                    (int)safeLocation.getY(),
                    (int)safeLocation.getZ()));
                
                plugin.getLogger().info("玩家 " + player.getName() + " 被随机传送到: " + safeLocation);
            } else {
                plugin.getLogger().warning("无法为玩家 " + player.getName() + " 找到安全传送位置");
                player.sendMessage(plugin.getLanguageManager().getMessage("item.teleport_failed"));
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("随机传送玩家时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 查找地面Y坐标
     */
    private int findGroundY(World world, int x, int z) {
        // 从最高处开始向下查找
        int maxY = world.getMaxHeight() - 1;
        
        for (int y = maxY; y > world.getMinHeight(); y--) {
            Location loc = new Location(world, x, y, z);
            
            // 检查当前位置是否是固体方块
            if (!loc.getBlock().getType().isAir() && 
                !loc.getBlock().isLiquid() && 
                loc.getBlock().getType().isSolid()) {
                
                // 检查上方是否有足够空间
                if (y + 2 < world.getMaxHeight()) {
                    Location above1 = new Location(world, x, y + 1, z);
                    Location above2 = new Location(world, x, y + 2, z);
                    
                    if (above1.getBlock().getType().isAir() && 
                        above2.getBlock().getType().isAir()) {
                        return y;
                    }
                }
            }
        }
        
        return world.getMinHeight();
    }
    
    /**
     * 检查位置是否安全
     */
    private boolean isLocationSafe(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 检查当前位置是否是空气
        if (!world.getBlockAt(x, y, z).getType().isAir()) {
            return false;
        }
        
        // 检查脚下是否是固体方块
        if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) {
            return false;
        }
        
        // 检查上方是否有足够空间
        if (!world.getBlockAt(x, y + 1, z).getType().isAir()) {
            return false;
        }
        
        // 检查是否在液体中
        if (world.getBlockAt(x, y, z).isLiquid() || 
            world.getBlockAt(x, y - 1, z).isLiquid()) {
            return false;
        }
        
        // 检查是否在危险方块上（岩浆等）
        Material floorMaterial = world.getBlockAt(x, y - 1, z).getType();
        if (floorMaterial == Material.LAVA || 
            floorMaterial == Material.FIRE || 
            floorMaterial == Material.CAMPFIRE || 
            floorMaterial == Material.SOUL_CAMPFIRE) {
            return false;
        }
        
        return true;
    }
}