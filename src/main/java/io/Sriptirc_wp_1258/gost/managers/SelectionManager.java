package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 选区管理器
 * 管理玩家的选区操作，类似WorldEdit的选区功能
 */
public class SelectionManager {
    
    private final Gost plugin;
    private final Map<UUID, PlayerSelection> playerSelections = new HashMap<>();
    
    // 选区工具物品
    private ItemStack selectionTool;
    
    public SelectionManager(Gost plugin) {
        this.plugin = plugin;
        // 延迟初始化选区工具，避免在插件完全加载前访问配置
    }
    
    /**
     * 初始化选区工具
     */
    private void initializeSelectionTool() {
        if (selectionTool != null) {
            return; // 已经初始化
        }
        
        String toolMaterialName = plugin.getConfigManager().getSelectionTool();
        Material toolMaterial;
        
        try {
            toolMaterial = Material.valueOf(toolMaterialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("配置的选区工具物品 " + toolMaterialName + " 无效，使用默认值 MAGMA_CREAM");
            toolMaterial = Material.MAGMA_CREAM;
        }
        
        selectionTool = new ItemStack(toolMaterial);
        ItemMeta meta = selectionTool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "区域选择工具");
            meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "左键点击方块设置第一个点",
                ChatColor.GRAY + "右键点击方块设置第二个点",
                ChatColor.GRAY + "使用 /gostadmin save <名称> 保存区域"
            ));
            selectionTool.setItemMeta(meta);
        }
    }
    
    /**
     * 获取选区工具物品
     */
    public ItemStack getSelectionTool() {
        initializeSelectionTool();
        return selectionTool.clone();
    }
    
    /**
     * 给玩家选区工具
     */
    public void giveSelectionTool(Player player) {
        player.getInventory().addItem(getSelectionTool());
        player.sendMessage(ChatColor.GREEN + "已获得区域选择工具！");
        player.sendMessage(ChatColor.YELLOW + "使用说明：");
        player.sendMessage(ChatColor.GRAY + "  • 左键点击方块设置第一个点");
        player.sendMessage(ChatColor.GRAY + "  • 右键点击方块设置第二个点");
        player.sendMessage(ChatColor.GRAY + "  • 使用 /gostadmin save <名称> 保存区域");
    }
    
    /**
     * 设置第一个点
     */
    public boolean setPos1(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        PlayerSelection selection = playerSelections.computeIfAbsent(playerId, k -> new PlayerSelection());
        
        selection.setPos1(location);
        player.sendMessage(ChatColor.GREEN + "第一个点已设置: " + formatLocation(location));
        
        // 如果两个点都已设置，显示区域信息
        if (selection.isComplete()) {
            showSelectionInfo(player, selection);
        }
        
        return true;
    }
    
    /**
     * 设置第二个点
     */
    public boolean setPos2(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        PlayerSelection selection = playerSelections.computeIfAbsent(playerId, k -> new PlayerSelection());
        
        selection.setPos2(location);
        player.sendMessage(ChatColor.GREEN + "第二个点已设置: " + formatLocation(location));
        
        // 如果两个点都已设置，显示区域信息
        if (selection.isComplete()) {
            showSelectionInfo(player, selection);
        }
        
        return true;
    }
    
    /**
     * 清除选区
     */
    public boolean clearSelection(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerSelections.containsKey(playerId)) {
            playerSelections.remove(playerId);
            player.sendMessage(ChatColor.YELLOW + "选区已清除");
            return true;
        }
        return false;
    }
    
    /**
     * 获取玩家的选区
     */
    public PlayerSelection getSelection(Player player) {
        return playerSelections.get(player.getUniqueId());
    }
    
    /**
     * 检查选区是否完整
     */
    public boolean isSelectionComplete(Player player) {
        PlayerSelection selection = getSelection(player);
        return selection != null && selection.isComplete();
    }
    
    /**
     * 显示选区信息
     */
    private void showSelectionInfo(Player player, PlayerSelection selection) {
        if (!selection.isComplete()) {
            return;
        }
        
        Location pos1 = selection.getPos1();
        Location pos2 = selection.getPos2();
        
        // 检查是否在同一世界
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(ChatColor.RED + "错误：两个点必须在同一世界！");
            return;
        }
        
        // 计算区域信息
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        int width = (int) Math.abs(maxX - minX) + 1;
        int height = (int) Math.abs(maxY - minY) + 1;
        int length = (int) Math.abs(maxZ - minZ) + 1;
        int volume = width * height * length;
        
        player.sendMessage(ChatColor.GOLD + "========== 选区信息 ==========");
        player.sendMessage(ChatColor.YELLOW + "世界: " + ChatColor.WHITE + pos1.getWorld().getName());
        player.sendMessage(ChatColor.YELLOW + "点1: " + ChatColor.WHITE + formatLocation(pos1));
        player.sendMessage(ChatColor.YELLOW + "点2: " + ChatColor.WHITE + formatLocation(pos2));
        player.sendMessage(ChatColor.YELLOW + "尺寸: " + ChatColor.WHITE + width + "×" + height + "×" + length);
        player.sendMessage(ChatColor.YELLOW + "体积: " + ChatColor.WHITE + volume + " 方块");
        player.sendMessage(ChatColor.GOLD + "==============================");
        player.sendMessage(ChatColor.GREEN + "使用 /gostadmin save <名称> 保存此区域");
    }
    
    /**
     * 格式化位置信息
     */
    private String formatLocation(Location location) {
        if (location == null) return "未设置";
        return String.format("(%d, %d, %d)", 
            (int) location.getX(), 
            (int) location.getY(), 
            (int) location.getZ()
        );
    }
    
    /**
     * 玩家选区类
     */
    public static class PlayerSelection {
        private Location pos1;
        private Location pos2;
        
        public Location getPos1() { return pos1; }
        public Location getPos2() { return pos2; }
        
        public void setPos1(Location pos1) { this.pos1 = pos1; }
        public void setPos2(Location pos2) { this.pos2 = pos2; }
        
        public boolean isComplete() {
            return pos1 != null && pos2 != null;
        }
        
        public boolean isValid() {
            if (!isComplete()) return false;
            return pos1.getWorld().equals(pos2.getWorld());
        }
        
        /**
         * 获取选区中心
         */
        public Location getCenter() {
            if (!isComplete()) return null;
            
            double x = (pos1.getX() + pos2.getX()) / 2;
            double y = (pos1.getY() + pos2.getY()) / 2;
            double z = (pos1.getZ() + pos2.getZ()) / 2;
            
            return new Location(pos1.getWorld(), x, y, z);
        }
        
        /**
         * 获取最小点
         */
        public Location getMinPoint() {
            if (!isComplete()) return null;
            
            double minX = Math.min(pos1.getX(), pos2.getX());
            double minY = Math.min(pos1.getY(), pos2.getY());
            double minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            return new Location(pos1.getWorld(), minX, minY, minZ);
        }
        
        /**
         * 获取最大点
         */
        public Location getMaxPoint() {
            if (!isComplete()) return null;
            
            double maxX = Math.max(pos1.getX(), pos2.getX());
            double maxY = Math.max(pos1.getY(), pos2.getY());
            double maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            return new Location(pos1.getWorld(), maxX, maxY, maxZ);
        }
        
        /**
         * 获取尺寸
         */
        public int[] getDimensions() {
            if (!isComplete()) return new int[]{0, 0, 0};
            
            Location min = getMinPoint();
            Location max = getMaxPoint();
            
            int width = (int) Math.abs(max.getX() - min.getX()) + 1;
            int height = (int) Math.abs(max.getY() - min.getY()) + 1;
            int length = (int) Math.abs(max.getZ() - min.getZ()) + 1;
            
            return new int[]{width, height, length};
        }
        
        /**
         * 获取体积
         */
        public int getVolume() {
            int[] dims = getDimensions();
            return dims[0] * dims[1] * dims[2];
        }
    }
}