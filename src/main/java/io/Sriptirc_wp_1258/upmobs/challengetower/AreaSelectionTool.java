package io.Sriptirc_wp_1258.upmobs.challengetower;

import io.Sriptirc_wp_1258.upmobs.Upmobs;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 区域选择工具 - 使用烈焰棒选择挑战塔区域
 */
public class AreaSelectionTool implements Listener {
    
    private final Upmobs plugin;
    private final Map<UUID, Location> firstSelection = new HashMap<>();
    private final Map<UUID, Location> secondSelection = new HashMap<>();
    
    public AreaSelectionTool(Upmobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 给予玩家选择工具
     */
    public void giveSelectionTool(Player player) {
        ItemStack tool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tool.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "§l挑战塔区域选择工具");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "左键点击选择第一个点",
                ChatColor.GRAY + "右键点击选择第二个点",
                ChatColor.GRAY + "两个点确定一个长方体区域",
                ChatColor.YELLOW + "使用 /upmobs tower create <名称> 创建挑战塔"
            ));
            tool.setItemMeta(meta);
        }
        
        player.getInventory().addItem(tool);
        player.sendMessage(ChatColor.GREEN + "已给予挑战塔区域选择工具");
        player.sendMessage(ChatColor.YELLOW + "使用烈焰棒左键和右键选择两个对角点");
    }
    
    /**
     * 处理玩家交互事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || 
            !meta.getDisplayName().equals(ChatColor.GOLD + "§l挑战塔区域选择工具")) {
            return;
        }
        
        // 只有管理员可以使用
        if (!player.hasPermission("upmobs.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用区域选择工具");
            return;
        }
        
        event.setCancelled(true);
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        Location selectedLocation = clickedBlock.getLocation();
        UUID playerId = player.getUniqueId();
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // 左键选择第一个点
            firstSelection.put(playerId, selectedLocation);
            player.sendMessage(ChatColor.GREEN + "§l✓ 已选择第一个点: " + 
                ChatColor.YELLOW + formatLocation(selectedLocation));
            playSelectionEffect(selectedLocation, Color.GREEN);
            
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // 右键选择第二个点
            secondSelection.put(playerId, selectedLocation);
            player.sendMessage(ChatColor.GREEN + "§l✓ 已选择第二个点: " + 
                ChatColor.YELLOW + formatLocation(selectedLocation));
            playSelectionEffect(selectedLocation, Color.RED);
            
            // 如果两个点都已选择，显示区域信息
            if (firstSelection.containsKey(playerId)) {
                Location first = firstSelection.get(playerId);
                Location second = secondSelection.get(playerId);
                
                showAreaInfo(player, first, second);
            }
        }
    }
    
    /**
     * 播放选择效果
     */
    private void playSelectionEffect(Location location, Color color) {
        World world = location.getWorld();
        if (world == null) return;
        
        // 方块边框效果
        for (int i = 0; i < 5; i++) {
            double offset = i * 0.2;
            world.spawnParticle(Particle.DUST, 
                location.clone().add(0.5, offset, 0.5), 
                10, 0.2, 0.2, 0.2, 0,
                new Particle.DustOptions(color, 1.0f));
        }
        
        // 音效
        world.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }
    
    /**
     * 显示区域信息
     */
    private void showAreaInfo(Player player, Location first, Location second) {
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        
        int widthX = maxX - minX + 1;
        int heightY = maxY - minY + 1;
        int depthZ = maxZ - minZ + 1;
        int volume = widthX * heightY * depthZ;
        
        player.sendMessage(ChatColor.GOLD + "§l=== 区域信息 ===");
        player.sendMessage(ChatColor.YELLOW + "X范围: " + ChatColor.WHITE + minX + " ~ " + maxX + " (" + widthX + "格)");
        player.sendMessage(ChatColor.YELLOW + "Y范围: " + ChatColor.WHITE + minY + " ~ " + maxY + " (" + heightY + "格)");
        player.sendMessage(ChatColor.YELLOW + "Z范围: " + ChatColor.WHITE + minZ + " ~ " + maxZ + " (" + depthZ + "格)");
        player.sendMessage(ChatColor.YELLOW + "总体积: " + ChatColor.WHITE + volume + " 方块");
        player.sendMessage(ChatColor.YELLOW + "区域中心: " + ChatColor.WHITE + 
            ((minX + maxX) / 2) + ", " + ((minY + maxY) / 2) + ", " + ((minZ + maxZ) / 2));
        
        // 显示创建指令提示
        player.sendMessage(ChatColor.GREEN + "使用指令创建挑战塔:");
        player.sendMessage(ChatColor.YELLOW + "/upmobs tower create <挑战塔名称>");
    }
    
    /**
     * 格式化位置信息
     */
    private String formatLocation(Location location) {
        return location.getWorld().getName() + " (" + 
               location.getBlockX() + ", " + 
               location.getBlockY() + ", " + 
               location.getBlockZ() + ")";
    }
    
    /**
     * 获取玩家选择的区域
     */
    public ChallengeTowerArea getSelectedArea(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!firstSelection.containsKey(playerId) || !secondSelection.containsKey(playerId)) {
            return null;
        }
        
        Location first = firstSelection.get(playerId);
        Location second = secondSelection.get(playerId);
        
        return new ChallengeTowerArea(first, second);
    }
    
    /**
     * 清除玩家的选择
     */
    public void clearSelection(Player player) {
        UUID playerId = player.getUniqueId();
        firstSelection.remove(playerId);
        secondSelection.remove(playerId);
        player.sendMessage(ChatColor.YELLOW + "已清除区域选择");
    }
    
    /**
     * 可视化显示区域边界
     */
    public void visualizeArea(Player player, ChallengeTowerArea area) {
        if (area == null) {
            player.sendMessage(ChatColor.RED + "没有可显示的区域");
            return;
        }
        
        Location min = area.getMin();
        Location max = area.getMax();
        World world = min.getWorld();
        
        if (world == null) return;
        
        // 显示区域边界粒子
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    // 只在边界上显示粒子
                    if (x == min.getBlockX() || x == max.getBlockX() || 
                        y == min.getBlockY() || y == max.getBlockY() || 
                        z == min.getBlockZ() || z == max.getBlockZ()) {
                        
                        world.spawnParticle(Particle.HAPPY_VILLAGER, 
                            new Location(world, x + 0.5, y + 0.5, z + 0.5), 
                            1, 0, 0, 0, 0);
                    }
                }
            }
        }
        
        player.sendMessage(ChatColor.GREEN + "已显示挑战塔区域边界");
    }
}