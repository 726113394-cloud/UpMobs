package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 游戏区域管理器
 * 负责管理多个游戏区域，支持保存、加载、删除等操作
 */
public class AreaManager {
    
    private final Gost plugin;
    private final Map<String, GameArea> areas = new HashMap<>();
    private final File areasFile;
    private FileConfiguration areasConfig;
    
    // 当前选中的区域（用于快速开始游戏）
    private String selectedAreaName = null;
    
    // 启用的区域列表（管理员通过指令加载的区域）
    private final Set<String> enabledAreas = new HashSet<>();
    
    // 启用区域配置文件
    private final File enabledAreasFile;
    private FileConfiguration enabledAreasConfig;
    
    public AreaManager(Gost plugin) {
        this.plugin = plugin;
        this.areasFile = new File(plugin.getDataFolder(), "areas.yml");
        this.enabledAreasFile = new File(plugin.getDataFolder(), "enabled_areas.yml");
        loadAreas();
        loadEnabledAreas();
    }
    
    /**
     * 游戏区域类
     */
    public static class GameArea {
        private final String name;
        private final Location pos1;
        private final Location pos2;
        private final Date createdDate;
        private Date lastUsedDate;
        private int usageCount;
        
        public GameArea(String name, Location pos1, Location pos2) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.createdDate = new Date();
            this.lastUsedDate = new Date();
            this.usageCount = 0;
        }
        
        public GameArea(String name, Location pos1, Location pos2, Date createdDate, Date lastUsedDate, int usageCount) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.createdDate = createdDate;
            this.lastUsedDate = lastUsedDate;
            this.usageCount = usageCount;
        }
        
        public String getName() { return name; }
        public Location getPos1() { return pos1; }
        public Location getPos2() { return pos2; }
        public Date getCreatedDate() { return createdDate; }
        public Date getLastUsedDate() { return lastUsedDate; }
        public int getUsageCount() { return usageCount; }
        
        public void incrementUsage() {
            this.usageCount++;
            this.lastUsedDate = new Date();
        }
        
        /**
         * 获取区域中心点
         */
        public Location getCenter() {
            if (pos1 == null || pos2 == null) return null;
            
            double x = (pos1.getX() + pos2.getX()) / 2;
            double y = (pos1.getY() + pos2.getY()) / 2;
            double z = (pos1.getZ() + pos2.getZ()) / 2;
            
            return new Location(pos1.getWorld(), x, y, z);
        }
        
        /**
         * 获取区域最小点（用于计算边界）
         */
        public Location getMinPoint() {
            if (pos1 == null || pos2 == null) return null;
            
            double minX = Math.min(pos1.getX(), pos2.getX());
            double minY = Math.min(pos1.getY(), pos2.getY());
            double minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            return new Location(pos1.getWorld(), minX, minY, minZ);
        }
        
        /**
         * 获取区域最大点（用于计算边界）
         */
        public Location getMaxPoint() {
            if (pos1 == null || pos2 == null) return null;
            
            double maxX = Math.max(pos1.getX(), pos2.getX());
            double maxY = Math.max(pos1.getY(), pos2.getY());
            double maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            return new Location(pos1.getWorld(), maxX, maxY, maxZ);
        }
        
        /**
         * 检查位置是否在区域内
         */
        public boolean contains(Location location) {
            if (pos1 == null || pos2 == null || location == null) return false;
            if (!pos1.getWorld().equals(location.getWorld())) return false;
            
            Location min = getMinPoint();
            Location max = getMaxPoint();
            
            // 检查X、Y、Z三个轴
            return location.getX() >= min.getX() && location.getX() <= max.getX() &&
                   location.getY() >= min.getY() && location.getY() <= max.getY() &&
                   location.getZ() >= min.getZ() && location.getZ() <= max.getZ();
        }
        
        /**
         * 获取区域尺寸
         */
        public int[] getDimensions() {
            if (pos1 == null || pos2 == null) return new int[]{0, 0, 0};
            
            Location min = getMinPoint();
            Location max = getMaxPoint();
            
            int width = (int) Math.abs(max.getX() - min.getX()) + 1;
            int height = (int) Math.abs(max.getY() - min.getY()) + 1;
            int length = (int) Math.abs(max.getZ() - min.getZ()) + 1;
            
            return new int[]{width, height, length};
        }
        
        /**
         * 获取区域体积（方块数）
         */
        public int getVolume() {
            int[] dims = getDimensions();
            return dims[0] * dims[1] * dims[2];
        }
        
        /**
         * 转换为Map用于保存
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("world", pos1.getWorld().getName());
            map.put("pos1_x", pos1.getX());
            map.put("pos1_y", pos1.getY());
            map.put("pos1_z", pos1.getZ());
            map.put("pos2_x", pos2.getX());
            map.put("pos2_y", pos2.getY());
            map.put("pos2_z", pos2.getZ());
            map.put("created", createdDate.getTime());
            map.put("last_used", lastUsedDate.getTime());
            map.put("usage_count", usageCount);
            return map;
        }
        
        /**
         * 从Map创建GameArea
         */
        public static GameArea fromMap(Map<String, Object> map) {
            String name = (String) map.get("name");
            String worldName = (String) map.get("world");
            World world = Bukkit.getWorld(worldName);
            
            if (world == null) {
                return null;
            }
            
            Location pos1 = new Location(
                world,
                ((Number) map.get("pos1_x")).doubleValue(),
                ((Number) map.get("pos1_y")).doubleValue(),
                ((Number) map.get("pos1_z")).doubleValue()
            );
            
            Location pos2 = new Location(
                world,
                ((Number) map.get("pos2_x")).doubleValue(),
                ((Number) map.get("pos2_y")).doubleValue(),
                ((Number) map.get("pos2_z")).doubleValue()
            );
            
            Date createdDate = new Date(((Number) map.get("created")).longValue());
            Date lastUsedDate = new Date(((Number) map.get("last_used")).longValue());
            int usageCount = ((Number) map.get("usage_count")).intValue();
            
            return new GameArea(name, pos1, pos2, createdDate, lastUsedDate, usageCount);
        }
    }
    
    /**
     * 加载所有区域
     */
    private void loadAreas() {
        areas.clear();
        
        if (!areasFile.exists()) {
            plugin.saveResource("areas.yml", false);
        }
        
        areasConfig = YamlConfiguration.loadConfiguration(areasFile);
        ConfigurationSection areasSection = areasConfig.getConfigurationSection("areas");
        
        if (areasSection != null) {
            for (String key : areasSection.getKeys(false)) {
                ConfigurationSection areaSection = areasSection.getConfigurationSection(key);
                if (areaSection != null) {
                    Map<String, Object> areaMap = areaSection.getValues(false);
                    GameArea area = GameArea.fromMap(areaMap);
                    if (area != null) {
                        areas.put(area.getName(), area);
                    }
                }
            }
        }
        
        plugin.getLogger().info("已加载 " + areas.size() + " 个游戏区域");
    }
    
    /**
     * 加载启用的区域
     */
    private void loadEnabledAreas() {
        enabledAreas.clear();
        
        if (!enabledAreasFile.exists()) {
            // 创建空的配置文件
            try {
                enabledAreasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("创建启用区域文件失败: " + e.getMessage());
                return;
            }
        }
        
        enabledAreasConfig = YamlConfiguration.loadConfiguration(enabledAreasFile);
        
        // 加载启用的区域列表
        List<String> enabledList = enabledAreasConfig.getStringList("enabled_areas");
        enabledAreas.addAll(enabledList);
        
        plugin.getLogger().info("已加载 " + enabledAreas.size() + " 个启用的区域: " + String.join(", ", enabledAreas));
    }
    
    /**
     * 保存启用的区域
     */
    private void saveEnabledAreas() {
        enabledAreasConfig.set("enabled_areas", new ArrayList<>(enabledAreas));
        
        try {
            enabledAreasConfig.save(enabledAreasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存启用区域文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存所有区域
     */
    private void saveAreas() {
        areasConfig.set("areas", null); // 清空现有数据
        
        for (GameArea area : areas.values()) {
            String path = "areas." + area.getName();
            areasConfig.set(path + ".name", area.getName());
            areasConfig.set(path + ".world", area.getPos1().getWorld().getName());
            areasConfig.set(path + ".pos1_x", area.getPos1().getX());
            areasConfig.set(path + ".pos1_y", area.getPos1().getY());
            areasConfig.set(path + ".pos1_z", area.getPos1().getZ());
            areasConfig.set(path + ".pos2_x", area.getPos2().getX());
            areasConfig.set(path + ".pos2_y", area.getPos2().getY());
            areasConfig.set(path + ".pos2_z", area.getPos2().getZ());
            areasConfig.set(path + ".created", area.getCreatedDate().getTime());
            areasConfig.set(path + ".last_used", area.getLastUsedDate().getTime());
            areasConfig.set(path + ".usage_count", area.getUsageCount());
        }
        
        try {
            areasConfig.save(areasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存区域文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存区域
     */
    public boolean saveArea(String name, Location pos1, Location pos2) {
        // 检查名称是否已存在
        if (areas.containsKey(name)) {
            return false;
        }
        
        // 检查是否达到最大数量
        if (areas.size() >= plugin.getConfigManager().getMaxAreas()) {
            return false;
        }
        
        // 检查区域是否重叠
        if (isAreaOverlapping(pos1, pos2, name)) {
            return false;
        }
        
        GameArea area = new GameArea(name, pos1, pos2);
        areas.put(name, area);
        saveAreas();
        return true;
    }
    
    /**
     * 删除区域
     */
    public boolean deleteArea(String name) {
        if (!areas.containsKey(name)) {
            return false;
        }
        
        areas.remove(name);
        
        // 如果删除的是当前选中的区域，清空选中
        if (name.equals(selectedAreaName)) {
            selectedAreaName = null;
        }
        
        saveAreas();
        return true;
    }
    
    /**
     * 获取区域
     */
    public GameArea getArea(String name) {
        return areas.get(name);
    }
    
    /**
     * 获取所有区域名称
     */
    public List<String> getAreaNames() {
        return new ArrayList<>(areas.keySet());
    }
    
    /**
     * 获取所有启用的区域名称
     */
    public List<String> getEnabledAreaNames() {
        return new ArrayList<>(enabledAreas);
    }
    
    /**
     * 获取所有启用的区域
     */
    public List<GameArea> getEnabledAreas() {
        List<GameArea> enabled = new ArrayList<>();
        for (String areaName : enabledAreas) {
            GameArea area = areas.get(areaName);
            if (area != null) {
                enabled.add(area);
            }
        }
        return enabled;
    }
    
    /**
     * 获取所有区域
     */
    public List<GameArea> getAllAreas() {
        return new ArrayList<>(areas.values());
    }
    
    /**
     * 获取区域列表（按使用次数排序）
     */
    public List<GameArea> getAreasSortedByUsage() {
        return areas.values().stream()
            .sorted((a1, a2) -> Integer.compare(a2.getUsageCount(), a1.getUsageCount()))
            .collect(Collectors.toList());
    }
    
    /**
     * 设置选中的区域
     */
    public boolean selectArea(String name) {
        if (!areas.containsKey(name)) {
            return false;
        }
        
        selectedAreaName = name;
        areas.get(name).incrementUsage();
        saveAreas();
        return true;
    }
    
    /**
     * 获取当前选中的区域
     */
    public GameArea getSelectedArea() {
        if (selectedAreaName == null) {
            return null;
        }
        return areas.get(selectedAreaName);
    }
    
    /**
     * 获取当前选中的区域名称
     */
    public String getSelectedAreaName() {
        return selectedAreaName;
    }
    
    /**
     * 启用区域（管理员指令）
     */
    public boolean enableArea(String areaName) {
        if (!areas.containsKey(areaName)) {
            return false;
        }
        
        if (enabledAreas.contains(areaName)) {
            return false; // 已经启用
        }
        
        enabledAreas.add(areaName);
        saveEnabledAreas();
        plugin.getLogger().info("区域 " + areaName + " 已启用");
        return true;
    }
    
    /**
     * 禁用区域（管理员指令）
     */
    public boolean disableArea(String areaName) {
        if (!enabledAreas.contains(areaName)) {
            return false; // 未启用
        }
        
        enabledAreas.remove(areaName);
        saveEnabledAreas();
        
        // 如果禁用的是当前选中的区域，清空选中
        if (areaName.equals(selectedAreaName)) {
            selectedAreaName = null;
        }
        
        plugin.getLogger().info("区域 " + areaName + " 已禁用");
        return true;
    }
    
    /**
     * 检查区域是否已启用
     */
    public boolean isAreaEnabled(String areaName) {
        return enabledAreas.contains(areaName);
    }
    
    /**
     * 随机选择一个启用的区域
     */
    public GameArea selectRandomEnabledArea() {
        List<GameArea> enabledAreasList = getEnabledAreas();
        if (enabledAreasList.isEmpty()) {
            plugin.getLogger().warning("没有启用的区域可供选择");
            return null;
        }
        
        Random random = new Random();
        GameArea selectedArea = enabledAreasList.get(random.nextInt(enabledAreasList.size()));
        selectedAreaName = selectedArea.getName();
        selectedArea.incrementUsage();
        saveAreas();
        
        plugin.getLogger().info("随机选择了区域: " + selectedArea.getName());
        return selectedArea;
    }
    
    /**
     * 自动选择区域（如果有启用的区域则随机选择，否则返回null）
     */
    public GameArea autoSelectArea() {
        List<GameArea> enabledAreasList = getEnabledAreas();
        if (enabledAreasList.isEmpty()) {
            plugin.getLogger().warning("没有启用的区域，无法自动选择");
            return null;
        }
        
        return selectRandomEnabledArea();
    }
    
    /**
     * 检查区域是否重叠
     */
    public boolean isAreaOverlapping(Location pos1, Location pos2, String excludeName) {
        Location min1 = new Location(
            pos1.getWorld(),
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
        
        Location max1 = new Location(
            pos1.getWorld(),
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
        
        for (GameArea area : areas.values()) {
            // 排除自身
            if (area.getName().equals(excludeName)) {
                continue;
            }
            
            Location min2 = area.getMinPoint();
            Location max2 = area.getMaxPoint();
            
            // 检查是否在同一世界
            if (!min1.getWorld().equals(min2.getWorld())) {
                continue;
            }
            
            // 检查是否重叠
            boolean overlapX = max1.getX() >= min2.getX() && min1.getX() <= max2.getX();
            boolean overlapY = max1.getY() >= min2.getY() && min1.getY() <= max2.getY();
            boolean overlapZ = max1.getZ() >= min2.getZ() && min1.getZ() <= max2.getZ();
            
            if (overlapX && overlapY && overlapZ) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取区域内的所有玩家
     */
    public List<Player> getPlayersInArea(GameArea area) {
        List<Player> players = new ArrayList<>();
        
        if (area == null || area.getPos1() == null || area.getPos2() == null) {
            return players;
        }
        
        World world = area.getPos1().getWorld();
        for (Player player : world.getPlayers()) {
            if (area.contains(player.getLocation())) {
                players.add(player);
            }
        }
        
        return players;
    }
    
    /**
     * 将玩家传送到区域中心
     */
    public boolean teleportPlayerToArea(Player player, GameArea area) {
        if (area == null) {
            player.sendMessage("§c区域不存在！");
            return false;
        }
        
        // 获取随机安全位置（忽略Y轴限制）
        Location safeLocation = getRandomSafeLocationInArea(area);
        if (safeLocation == null) {
            // 如果找不到安全位置，尝试使用中心点
            safeLocation = area.getCenter();
            if (safeLocation == null) {
                player.sendMessage("§c无法获取传送位置！");
                return false;
            }
        }
        
        player.teleport(safeLocation);
        player.sendMessage("§a你已被传送到游戏区域！");
        return true;
    }
    
    /**
     * 获取区域内随机安全位置
     * 忽略Y轴限制，在地面上寻找安全位置
     */
    public Location getRandomSafeLocationInArea(GameArea area) {
        if (area == null) return null;
        
        Location min = area.getMinPoint();
        Location max = area.getMaxPoint();
        if (min == null || max == null) return null;
        
        World world = min.getWorld();
        Random random = new Random();
        
        // 尝试最多50次寻找安全位置
        for (int i = 0; i < 50; i++) {
            // 在X和Z坐标范围内随机选择
            double x = min.getX() + random.nextDouble() * (max.getX() - min.getX());
            double z = min.getZ() + random.nextDouble() * (max.getZ() - min.getZ());
            
            // 获取世界最高Y坐标（考虑建筑）
            int worldHeight = world.getMaxHeight();
            int groundY = world.getHighestBlockYAt((int) x, (int) z);
            
            // 确保Y坐标在地面之上
            double y = groundY + 1.0; // 站在地面上
            
            // 检查位置是否安全（不是液体，不是危险方块）
            Location testLoc = new Location(world, x, y, z);
            if (isLocationSafe(testLoc)) {
                return testLoc;
            }
        }
        
        // 如果找不到安全位置，返回中心点
        return area.getCenter();
    }
    
    /**
     * 检查位置是否安全
     */
    private boolean isLocationSafe(Location location) {
        // 检查脚下方块是否安全（不是液体，不是岩浆等）
        org.bukkit.block.Block block = location.getBlock();
        org.bukkit.block.Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        
        // 不允许的方块类型
        Set<org.bukkit.Material> unsafeMaterials = new HashSet<>(Arrays.asList(
            org.bukkit.Material.LAVA,
            org.bukkit.Material.WATER,
            org.bukkit.Material.CACTUS,
            org.bukkit.Material.MAGMA_BLOCK,
            org.bukkit.Material.CAMPFIRE,
            org.bukkit.Material.SOUL_CAMPFIRE,
            org.bukkit.Material.FIRE
        ));
        
        // 检查脚下方块是否安全
        if (unsafeMaterials.contains(blockBelow.getType())) {
            return false;
        }
        
        // 检查当前位置方块是否可通行（不是固体方块）
        if (block.getType().isSolid()) {
            return false;
        }
        
        // 检查头顶上方方块是否可通行（至少2格空间）
        org.bukkit.block.Block blockAbove = location.clone().add(0, 1, 0).getBlock();
        if (blockAbove.getType().isSolid()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 重新加载区域
     */
    public void reload() {
        loadAreas();
    }
    
    /**
     * 获取当前选中区域的最小点
     * @return 区域最小点，如果没有选中区域则返回null
     */
    public Location getAreaMin() {
        GameArea selectedArea = getSelectedArea();
        if (selectedArea != null) {
            return selectedArea.getMinPoint();
        }
        return null;
    }
    
    /**
     * 获取当前选中区域的最大点
     * @return 区域最大点，如果没有选中区域则返回null
     */
    public Location getAreaMax() {
        GameArea selectedArea = getSelectedArea();
        if (selectedArea != null) {
            return selectedArea.getMaxPoint();
        }
        return null;
    }
}