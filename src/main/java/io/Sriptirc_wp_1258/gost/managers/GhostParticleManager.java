package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 鬼玩家粒子效果管理器
 * 为鬼玩家添加持续环绕粒子效果，区分母体鬼和普通鬼
 */
public class GhostParticleManager {
    
    private final Gost plugin;
    private BukkitTask particleTask;
    private final Map<UUID, ParticleData> particleDataMap = new HashMap<>();
    
    // 粒子效果数据类
    private static class ParticleData {
        final PlayerRole role;
        final long lastParticleTime;
        
        ParticleData(PlayerRole role) {
            this.role = role;
            this.lastParticleTime = System.currentTimeMillis();
        }
    }
    
    // 玩家角色枚举（与PlayerManager保持一致）
    public enum PlayerRole {
        HUMAN,
        GHOST_MOTHER,
        GHOST_NORMAL
    }
    
    public GhostParticleManager(Gost plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载配置
     */
    public void loadConfig() {
        boolean enabled = plugin.getConfigManager().isGhostParticleEnabled();
        plugin.getLogger().info("鬼玩家粒子效果: " + (enabled ? "已启用" : "已禁用"));
        
        if (enabled) {
            startParticleTask();
        } else {
            stopParticleTask();
        }
    }
    
    /**
     * 开始粒子效果任务
     */
    private void startParticleTask() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
        }
        
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getGameManager().isGameRunning()) {
                    return;
                }
                
                // 检查是否在准备阶段
                boolean isPreparationPhase = plugin.getGameManager().isPreparationPhase();
                boolean showInPreparation = plugin.getConfigManager().isGhostParticleShowInPreparation();
                
                if (isPreparationPhase && !showInPreparation) {
                    return;
                }
                
                // 为所有鬼玩家生成粒子
                generateParticlesForAllGhosts();
            }
        }.runTaskTimer(plugin, 0L, plugin.getConfigManager().getGhostParticleInterval());
    }
    
    /**
     * 停止粒子效果任务
     */
    private void stopParticleTask() {
        if (particleTask != null && !particleTask.isCancelled()) {
            particleTask.cancel();
            particleTask = null;
        }
        
        // 清理粒子数据
        particleDataMap.clear();
    }
    
    /**
     * 为所有鬼玩家生成粒子
     */
    private void generateParticlesForAllGhosts() {
        PlayerManager playerManager = plugin.getPlayerManager();
        
        for (UUID playerId : playerManager.getAllPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // 检查玩家是否是鬼
            if (playerManager.isGhost(playerId)) {
                // 获取玩家角色
                PlayerRole role = getPlayerRole(playerId);
                
                // 生成粒子效果
                generateParticlesForPlayer(player, role);
            }
        }
    }
    
    /**
     * 获取玩家角色
     */
    private PlayerRole getPlayerRole(UUID playerId) {
        PlayerManager playerManager = plugin.getPlayerManager();
        
        if (playerManager.isHuman(playerId)) {
            return PlayerRole.HUMAN;
        } else if (playerManager.isGhostMother(playerId)) {
            return PlayerRole.GHOST_MOTHER;
        } else if (playerManager.isGhost(playerId)) {
            return PlayerRole.GHOST_NORMAL;
        }
        
        return PlayerRole.HUMAN;
    }
    
    /**
     * 为单个玩家生成粒子
     */
    private void generateParticlesForPlayer(Player player, PlayerRole role) {
        if (role == PlayerRole.HUMAN) {
            return; // 人类玩家不显示粒子
        }
        
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) return;
        
        // 获取配置
        String particleType = plugin.getConfigManager().getGhostParticleType();
        int particleCount = plugin.getConfigManager().getGhostParticleCount();
        float particleSize = (float) plugin.getConfigManager().getGhostParticleSize();
        
        // 根据角色获取颜色
        Color color = getColorForRole(role);
        
        try {
            // 解析粒子类型
            Particle particle = Particle.valueOf(particleType);
            
            // 生成环绕粒子效果
            generateOrbitingParticles(world, location, particle, color, particleCount, particleSize, role);
            
        } catch (IllegalArgumentException e) {
            // 如果粒子类型无效，使用默认的REDSTONE
            plugin.getLogger().warning("无效的粒子类型: " + particleType + "，使用默认REDSTONE");
            generateOrbitingParticles(world, location, Particle.REDSTONE, color, particleCount, particleSize, role);
        }
    }
    
    /**
     * 根据角色获取颜色
     */
    private Color getColorForRole(PlayerRole role) {
        if (role == PlayerRole.GHOST_MOTHER) {
            // 母体鬼颜色
            String[] rgb = plugin.getConfigManager().getGhostParticleMotherColor().split(",");
            if (rgb.length == 3) {
                try {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    return Color.fromRGB(r, g, b);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("无效的母体鬼颜色格式，使用默认红色");
                }
            }
            return Color.RED;
        } else if (role == PlayerRole.GHOST_NORMAL) {
            // 普通鬼颜色
            String[] rgb = plugin.getConfigManager().getGhostParticleNormalColor().split(",");
            if (rgb.length == 3) {
                try {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    return Color.fromRGB(r, g, b);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("无效的普通鬼颜色格式，使用默认绿色");
                }
            }
            return Color.GREEN;
        }
        
        return Color.WHITE;
    }
    
    /**
     * 生成环绕粒子效果
     */
    private void generateOrbitingParticles(World world, Location center, Particle particle, Color color, 
                                          int count, float size, PlayerRole role) {
        double radius = 0.8; // 环绕半径
        double height = 1.2; // 粒子高度（相对于玩家脚部）
        
        // 根据角色调整效果
        if (role == PlayerRole.GHOST_MOTHER) {
            radius = 1.0; // 母体鬼有更大的环绕半径
            height = 1.5; // 更高的粒子
        }
        
        // 生成环绕粒子
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = center.clone().add(x, height, z);
            
            // 根据粒子类型生成不同的效果
            if (particle == Particle.REDSTONE) {
                // 红色/绿色粒子
                world.spawnParticle(
                    particle,
                    particleLoc,
                    1, // 数量
                    0, 0, 0, // 偏移
                    0, // 速度
                    new Particle.DustOptions(color, size)
                );
            } else if (particle == Particle.DUST_COLOR_TRANSITION) {
                // 颜色过渡灰尘粒子（1.20.x中的DUST）
                world.spawnParticle(
                    particle,
                    particleLoc,
                    1,
                    0, 0, 0,
                    0,
                    new Particle.DustTransition(color, color, size)
                );
            } else {
                // 其他类型的粒子
                world.spawnParticle(
                    particle,
                    particleLoc,
                    1,
                    0.1, 0.1, 0.1, // 小范围随机偏移
                    0
                );
            }
        }
        
        // 为母体鬼添加额外的头顶粒子
        if (role == PlayerRole.GHOST_MOTHER) {
            Location headLoc = center.clone().add(0, 2.2, 0);
            if (particle == Particle.REDSTONE || particle == Particle.DUST_COLOR_TRANSITION) {
                world.spawnParticle(
                    particle,
                    headLoc,
                    3,
                    0.2, 0, 0.2,
                    0,
                    new Particle.DustOptions(color, size * 1.5f)
                );
            } else {
                world.spawnParticle(
                    particle,
                    headLoc,
                    3,
                    0.2, 0.2, 0.2,
                    0
                );
            }
        }
    }
    
    /**
     * 更新玩家粒子数据
     */
    public void updatePlayerParticleData(UUID playerId, PlayerRole role) {
        if (role == PlayerRole.GHOST_MOTHER || role == PlayerRole.GHOST_NORMAL) {
            particleDataMap.put(playerId, new ParticleData(role));
        } else {
            particleDataMap.remove(playerId);
        }
    }
    
    /**
     * 移除玩家粒子数据
     */
    public void removePlayerParticleData(UUID playerId) {
        particleDataMap.remove(playerId);
    }
    
    /**
     * 清理所有粒子数据
     */
    public void cleanup() {
        stopParticleTask();
        particleDataMap.clear();
        plugin.getLogger().info("鬼玩家粒子效果数据已清理");
    }
    
    /**
     * 重新加载配置
     */
    public void reload() {
        loadConfig();
        cleanup();
    }
}