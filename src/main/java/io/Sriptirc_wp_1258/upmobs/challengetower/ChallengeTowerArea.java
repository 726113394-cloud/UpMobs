package io.Sriptirc_wp_1258.upmobs.challengetower;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * 挑战塔区域
 * 表示一个长方体区域，用于生成怪物和限制玩家活动
 */
public class ChallengeTowerArea {
    
    private final Location min;
    private final Location max;
    private final World world;
    private final String name;
    
    public ChallengeTowerArea(Location point1, Location point2) {
        if (point1.getWorld() == null || point2.getWorld() == null || 
            !point1.getWorld().equals(point2.getWorld())) {
            throw new IllegalArgumentException("两个点必须在同一个世界");
        }
        
        this.world = point1.getWorld();
        
        // 计算最小和最大坐标
        int minX = Math.min(point1.getBlockX(), point2.getBlockX());
        int minY = Math.min(point1.getBlockY(), point2.getBlockY());
        int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
        int maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        int maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        int maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());
        
        this.min = new Location(world, minX, minY, minZ);
        this.max = new Location(world, maxX, maxY, maxZ);
        this.name = "未命名区域";
    }
    
    public ChallengeTowerArea(Location point1, Location point2, String name) {
        if (point1.getWorld() == null || point2.getWorld() == null || 
            !point1.getWorld().equals(point2.getWorld())) {
            throw new IllegalArgumentException("两个点必须在同一个世界");
        }
        
        this.world = point1.getWorld();
        this.name = name;
        
        // 计算最小和最大坐标
        int minX = Math.min(point1.getBlockX(), point2.getBlockX());
        int minY = Math.min(point1.getBlockY(), point2.getBlockY());
        int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
        int maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        int maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        int maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());
        
        this.min = new Location(world, minX, minY, minZ);
        this.max = new Location(world, maxX, maxY, maxZ);
    }
    
    /**
     * 检查位置是否在区域内
     */
    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().equals(world)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= min.getBlockX() && x <= max.getBlockX() &&
               y >= min.getBlockY() && y <= max.getBlockY() &&
               z >= min.getBlockZ() && z <= max.getBlockZ();
    }
    
    /**
     * 检查玩家是否在区域内
     */
    public boolean contains(Player player) {
        return contains(player.getLocation());
    }
    
    /**
     * 获取区域内的随机位置（在区域内表面）
     */
    public Location getRandomLocation() {
        Random random = new Random();
        
        // 随机选择一个面
        int face = random.nextInt(6);
        int x, y, z;
        
        switch (face) {
            case 0: // 底面
                x = min.getBlockX() + random.nextInt(getWidth());
                y = min.getBlockY();
                z = min.getBlockZ() + random.nextInt(getDepth());
                break;
            case 1: // 顶面
                x = min.getBlockX() + random.nextInt(getWidth());
                y = max.getBlockY();
                z = min.getBlockZ() + random.nextInt(getDepth());
                break;
            case 2: // 北面
                x = min.getBlockX() + random.nextInt(getWidth());
                y = min.getBlockY() + random.nextInt(getHeight());
                z = min.getBlockZ();
                break;
            case 3: // 南面
                x = min.getBlockX() + random.nextInt(getWidth());
                y = min.getBlockY() + random.nextInt(getHeight());
                z = max.getBlockZ();
                break;
            case 4: // 西面
                x = min.getBlockX();
                y = min.getBlockY() + random.nextInt(getHeight());
                z = min.getBlockZ() + random.nextInt(getDepth());
                break;
            default: // 东面
                x = max.getBlockX();
                y = min.getBlockY() + random.nextInt(getHeight());
                z = min.getBlockZ() + random.nextInt(getDepth());
                break;
        }
        
        // 确保位置在地面上（Y+1）
        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        
        // 如果位置不是空气，向上寻找空气位置
        while (!world.getBlockAt(location).isPassable() && 
               location.getBlockY() < max.getBlockY()) {
            location.add(0, 1, 0);
        }
        
        return location;
    }
    
    /**
     * 获取区域内部地面上的随机位置（避免怪物生成在空中或区域外）
     */
    public Location getRandomGroundLocation() {
        Random random = new Random();
        
        // 在区域内部随机选择X和Z坐标
        int x = min.getBlockX() + random.nextInt(getWidth());
        int z = min.getBlockZ() + random.nextInt(getDepth());
        
        // 从区域底部开始，寻找地面位置
        int y = min.getBlockY();
        
        // 寻找地面（固体方块上的空气位置）
        while (y <= max.getBlockY()) {
            Location checkLoc = new Location(world, x, y, z);
            Location aboveLoc = new Location(world, x, y + 1, z);
            
            // 检查当前位置是否是固体方块，且上方是空气
            if (!world.getBlockAt(checkLoc).isPassable() && 
                world.getBlockAt(aboveLoc).isPassable()) {
                // 找到地面，返回上方的位置
                return new Location(world, x + 0.5, y + 1.5, z + 0.5);
            }
            y++;
        }
        
        // 如果找不到地面，使用安全生成位置
        return getSafeSpawnLocation();
    }
    
    /**
     * 获取区域内的安全位置（中心位置）
     */
    public Location getSafeSpawnLocation() {
        int centerX = (min.getBlockX() + max.getBlockX()) / 2;
        int centerY = (min.getBlockY() + max.getBlockY()) / 2;
        int centerZ = (min.getBlockZ() + max.getBlockZ()) / 2;
        
        Location location = new Location(world, centerX + 0.5, centerY + 1, centerZ + 0.5);
        
        // 向上寻找空气位置
        while (!world.getBlockAt(location).isPassable() && 
               location.getBlockY() < max.getBlockY()) {
            location.add(0, 1, 0);
        }
        
        return location;
    }
    
    /**
     * 获取区域中心
     */
    public Location getCenter() {
        double centerX = (min.getX() + max.getX()) / 2;
        double centerY = (min.getY() + max.getY()) / 2;
        double centerZ = (min.getZ() + max.getZ()) / 2;
        return new Location(world, centerX, centerY, centerZ);
    }
    
    /**
     * 获取区域体积
     */
    public int getVolume() {
        return getWidth() * getHeight() * getDepth();
    }
    
    /**
     * 获取区域宽度（X方向）
     */
    public int getWidth() {
        return max.getBlockX() - min.getBlockX() + 1;
    }
    
    /**
     * 获取区域高度（Y方向）
     */
    public int getHeight() {
        return max.getBlockY() - min.getBlockY() + 1;
    }
    
    /**
     * 获取区域深度（Z方向）
     */
    public int getDepth() {
        return max.getBlockZ() - min.getBlockZ() + 1;
    }
    
    /**
     * 检查区域是否足够大用于挑战塔
     */
    public boolean isValidForChallengeTower() {
        // 最小要求：10x10x5的区域
        return getWidth() >= 10 && getHeight() >= 5 && getDepth() >= 10;
    }
    
    /**
     * 获取区域边界的最小点
     */
    public Location getMin() {
        return min.clone();
    }
    
    /**
     * 获取区域边界的最大点
     */
    public Location getMax() {
        return max.clone();
    }
    
    /**
     * 获取区域所在世界
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * 获取区域名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取区域边界向量
     */
    public Vector getDimensions() {
        return new Vector(getWidth(), getHeight(), getDepth());
    }
    
    /**
     * 获取区域边界信息
     */
    public String getBoundaryInfo() {
        return String.format("X: %d-%d (%d格), Y: %d-%d (%d格), Z: %d-%d (%d格), 体积: %d",
            min.getBlockX(), max.getBlockX(), getWidth(),
            min.getBlockY(), max.getBlockY(), getHeight(),
            min.getBlockZ(), max.getBlockZ(), getDepth(),
            getVolume());
    }
    
    /**
     * 获取区域描述
     */
    @Override
    public String toString() {
        return String.format("挑战塔区域[%s] - %s (体积: %d)", 
            name, getBoundaryInfo(), getVolume());
    }
    
    /**
     * 检查区域是否与其他区域重叠
     */
    public boolean overlapsWith(ChallengeTowerArea other) {
        if (!world.equals(other.world)) {
            return false;
        }
        
        return !(max.getX() < other.min.getX() || min.getX() > other.max.getX() ||
                 max.getY() < other.min.getY() || min.getY() > other.max.getY() ||
                 max.getZ() < other.min.getZ() || min.getZ() > other.max.getZ());
    }
}