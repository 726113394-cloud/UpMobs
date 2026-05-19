package io.Sriptirc_wp_1258.gost.commands;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 鬼玩家粒子效果管理命令
 */
public class GhostParticleCommand implements CommandExecutor, TabCompleter {
    
    private final Gost plugin;
    
    public GhostParticleCommand(Gost plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                handleStatus(sender);
                break;
                
            case "enable":
                handleEnable(sender);
                break;
                
            case "disable":
                handleDisable(sender);
                break;
                
            case "reload":
                handleReload(sender);
                break;
                
            case "settype":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle settype <粒子类型>");
                    return true;
                }
                handleSetType(sender, args[1]);
                break;
                
            case "setcount":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setcount <数量>");
                    return true;
                }
                handleSetCount(sender, args[1]);
                break;
                
            case "setinterval":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setinterval <间隔>");
                    return true;
                }
                handleSetInterval(sender, args[1]);
                break;
                
            case "setmothercolor":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setmothercolor <红,绿,蓝>");
                    return true;
                }
                handleSetMotherColor(sender, args[1]);
                break;
                
            case "setnormalcolor":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setnormalcolor <红,绿,蓝>");
                    return true;
                }
                handleSetNormalColor(sender, args[1]);
                break;
                
            case "setsize":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setsize <大小>");
                    return true;
                }
                handleSetSize(sender, args[1]);
                break;
                
            case "setpreparation":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /ghostparticle setpreparation <on|off>");
                    return true;
                }
                handleSetPreparation(sender, args[1]);
                break;
                
            case "test":
                handleTest(sender);
                break;
                
            case "listtypes":
                handleListTypes(sender);
                break;
                
            default:
                sendUsage(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * 发送命令用法
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "              👻 鬼玩家粒子效果管理 👻");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle status" + ChatColor.GRAY + " - 查看粒子效果状态");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle enable" + ChatColor.GRAY + " - 启用粒子效果");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle disable" + ChatColor.GRAY + " - 禁用粒子效果");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle reload" + ChatColor.GRAY + " - 重新加载配置");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle test" + ChatColor.GRAY + " - 测试粒子效果");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle listtypes" + ChatColor.GRAY + " - 列出可用粒子类型");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle settype <类型>" + ChatColor.GRAY + " - 设置粒子类型");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setcount <数量>" + ChatColor.GRAY + " - 设置粒子数量");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setinterval <间隔>" + ChatColor.GRAY + " - 设置生成间隔（刻）");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setmothercolor <红,绿,蓝>" + ChatColor.GRAY + " - 设置母体鬼颜色");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setnormalcolor <红,绿,蓝>" + ChatColor.GRAY + " - 设置普通鬼颜色");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setsize <大小>" + ChatColor.GRAY + " - 设置粒子大小");
        sender.sendMessage(ChatColor.YELLOW + "/ghostparticle setpreparation <on|off>" + ChatColor.GRAY + " - 设置准备阶段显示");
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    /**
     * 处理状态命令
     */
    private void handleStatus(CommandSender sender) {
        boolean enabled = plugin.getConfigManager().isGhostParticleEnabled();
        String type = plugin.getConfigManager().getGhostParticleType();
        int count = plugin.getConfigManager().getGhostParticleCount();
        int interval = plugin.getConfigManager().getGhostParticleInterval();
        String motherColor = plugin.getConfigManager().getGhostParticleMotherColor();
        String normalColor = plugin.getConfigManager().getGhostParticleNormalColor();
        double size = plugin.getConfigManager().getGhostParticleSize();
        boolean showInPreparation = plugin.getConfigManager().isGhostParticleShowInPreparation();
        
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "              👻 鬼玩家粒子效果状态 👻");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "状态: " + (enabled ? ChatColor.GREEN + "已启用" : ChatColor.RED + "已禁用"));
        sender.sendMessage(ChatColor.YELLOW + "粒子类型: " + ChatColor.GREEN + type);
        sender.sendMessage(ChatColor.YELLOW + "粒子数量: " + ChatColor.GREEN + count);
        sender.sendMessage(ChatColor.YELLOW + "生成间隔: " + ChatColor.GREEN + interval + "刻 (" + (interval / 20.0) + "秒)");
        sender.sendMessage(ChatColor.YELLOW + "母体鬼颜色: " + ChatColor.GREEN + motherColor + ChatColor.GRAY + " (RGB)");
        sender.sendMessage(ChatColor.YELLOW + "普通鬼颜色: " + ChatColor.GREEN + normalColor + ChatColor.GRAY + " (RGB)");
        sender.sendMessage(ChatColor.YELLOW + "粒子大小: " + ChatColor.GREEN + size);
        sender.sendMessage(ChatColor.YELLOW + "准备阶段显示: " + (showInPreparation ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    /**
     * 处理启用命令
     */
    private void handleEnable(CommandSender sender) {
        plugin.getConfigManager().setGhostParticleEnabled(true);
        plugin.getGhostParticleManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子效果已启用！");
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理禁用命令
     */
    private void handleDisable(CommandSender sender) {
        plugin.getConfigManager().setGhostParticleEnabled(false);
        plugin.getGhostParticleManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子效果已禁用！");
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理重新加载命令
     */
    private void handleReload(CommandSender sender) {
        plugin.getGhostParticleManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子效果配置已重新加载！");
    }
    
    /**
     * 处理设置粒子类型命令
     */
    private void handleSetType(CommandSender sender, String type) {
        try {
            // 验证粒子类型是否有效
            Particle particle = Particle.valueOf(type.toUpperCase());
            plugin.getConfigManager().setGhostParticleType(type.toUpperCase());
            
            sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子类型已设置为: " + type.toUpperCase());
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "❌ 无效的粒子类型！");
            sender.sendMessage(ChatColor.YELLOW + "使用 /ghostparticle listtypes 查看可用类型");
        }
    }
    
    /**
     * 处理设置粒子数量命令
     */
    private void handleSetCount(CommandSender sender, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            
            if (count < 1 || count > 20) {
                sender.sendMessage(ChatColor.RED + "❌ 粒子数量必须在1-20之间！");
                return;
            }
            
            plugin.getConfigManager().setGhostParticleCount(count);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子数量已设置为: " + count);
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    /**
     * 处理设置生成间隔命令
     */
    private void handleSetInterval(CommandSender sender, String intervalStr) {
        try {
            int interval = Integer.parseInt(intervalStr);
            
            if (interval < 1 || interval > 100) {
                sender.sendMessage(ChatColor.RED + "❌ 生成间隔必须在1-100刻之间！");
                return;
            }
            
            plugin.getConfigManager().setGhostParticleInterval(interval);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子生成间隔已设置为: " + interval + "刻 (" + (interval / 20.0) + "秒)");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    /**
     * 处理设置母体鬼颜色命令
     */
    private void handleSetMotherColor(CommandSender sender, String colorStr) {
        if (isValidColorFormat(colorStr)) {
            plugin.getConfigManager().setGhostParticleMotherColor(colorStr);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 母体鬼粒子颜色已设置为: " + colorStr);
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
        } else {
            sender.sendMessage(ChatColor.RED + "❌ 无效的颜色格式！");
            sender.sendMessage(ChatColor.YELLOW + "正确格式: 红,绿,蓝 (例如: 255,0,0)");
            sender.sendMessage(ChatColor.YELLOW + "每个值范围: 0-255");
        }
    }
    
    /**
     * 处理设置普通鬼颜色命令
     */
    private void handleSetNormalColor(CommandSender sender, String colorStr) {
        if (isValidColorFormat(colorStr)) {
            plugin.getConfigManager().setGhostParticleNormalColor(colorStr);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 普通鬼粒子颜色已设置为: " + colorStr);
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
        } else {
            sender.sendMessage(ChatColor.RED + "❌ 无效的颜色格式！");
            sender.sendMessage(ChatColor.YELLOW + "正确格式: 红,绿,蓝 (例如: 0,255,0)");
            sender.sendMessage(ChatColor.YELLOW + "每个值范围: 0-255");
        }
    }
    
    /**
     * 检查颜色格式是否有效
     */
    private boolean isValidColorFormat(String colorStr) {
        String[] rgb = colorStr.split(",");
        if (rgb.length != 3) return false;
        
        try {
            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());
            
            return r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 处理设置粒子大小命令
     */
    private void handleSetSize(CommandSender sender, String sizeStr) {
        try {
            double size = Double.parseDouble(sizeStr);
            
            if (size < 0.1 || size > 5.0) {
                sender.sendMessage(ChatColor.RED + "❌ 粒子大小必须在0.1-5.0之间！");
                return;
            }
            
            plugin.getConfigManager().setGhostParticleSize(size);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子大小已设置为: " + size);
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    /**
     * 处理设置准备阶段显示命令
     */
    private void handleSetPreparation(CommandSender sender, String value) {
        boolean enable = value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equals("1");
        
        plugin.getConfigManager().setGhostParticleShowInPreparation(enable);
        
        sender.sendMessage(ChatColor.GREEN + "✅ 准备阶段粒子显示已" + (enable ? "开启" : "关闭"));
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理测试命令
     */
    private void handleTest(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "✅ 鬼玩家粒子效果测试命令已执行");
        sender.sendMessage(ChatColor.YELLOW + "注意：需要在游戏中进行测试");
        sender.sendMessage(ChatColor.YELLOW + "1. 开始游戏");
        sender.sendMessage(ChatColor.YELLOW + "2. 观察鬼玩家身上的粒子效果");
        sender.sendMessage(ChatColor.YELLOW + "3. 母体鬼为红色，普通鬼为绿色");
    }
    
    /**
     * 处理列出粒子类型命令
     */
    private void handleListTypes(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "              📋 可用粒子类型 📋");
        sender.sendMessage("");
        
        // 常用粒子类型
        String[] commonTypes = {
            "REDSTONE", "FLAME", "SOUL_FIRE_FLAME", "DRAGON_BREATH", "PORTAL",
            "DUST_COLOR_TRANSITION", "SPELL_MOB", "SPELL_WITCH", "ENCHANTMENT_TABLE", "CRIT_MAGIC",
            "FIREWORKS_SPARK", "HEART", "NOTE", "VILLAGER_ANGRY", "VILLAGER_HAPPY",
            "TOTEM_OF_UNDYING", "COMPOSTER", "SQUID_INK", "DRIPPING_OBSIDIAN_TEAR",
            "FALLING_OBSIDIAN_TEAR", "LANDING_OBSIDIAN_TEAR"
        };
        
        for (int i = 0; i < commonTypes.length; i++) {
            if (i % 3 == 0) {
                sender.sendMessage("");
            }
            sender.sendMessage(ChatColor.YELLOW + "• " + ChatColor.GREEN + commonTypes[i] + ChatColor.GRAY + 
                (i % 3 == 2 ? "" : "   "));
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "推荐使用: " + ChatColor.GREEN + "REDSTONE" + ChatColor.GRAY + " (支持颜色)");
        sender.sendMessage(ChatColor.YELLOW + "火焰效果: " + ChatColor.GREEN + "FLAME, SOUL_FIRE_FLAME");
        sender.sendMessage(ChatColor.YELLOW + "魔法效果: " + ChatColor.GREEN + "SPELL_MOB, SPELL_WITCH");
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            completions.addAll(Arrays.asList(
                "status", "enable", "disable", "reload", 
                "settype", "setcount", "setinterval", 
                "setmothercolor", "setnormalcolor", "setsize",
                "setpreparation", "test", "listtypes"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "settype":
                    // 常用粒子类型补全
                    completions.addAll(Arrays.asList(
                        "REDSTONE", "FLAME", "SOUL_FIRE_FLAME", "DRAGON_BREATH", 
                        "PORTAL", "DUST", "SPELL_MOB", "SPELL_WITCH"
                    ));
                    break;
                    
                case "setcount":
                    completions.addAll(Arrays.asList("1", "3", "5", "8", "10", "15", "20"));
                    break;
                    
                case "setinterval":
                    completions.addAll(Arrays.asList("5", "10", "15", "20", "30", "40", "60"));
                    break;
                    
                case "setmothercolor":
                    completions.addAll(Arrays.asList("255,0,0", "255,100,100", "200,0,0", "255,50,50"));
                    break;
                    
                case "setnormalcolor":
                    completions.addAll(Arrays.asList("0,255,0", "100,255,100", "0,200,0", "50,255,50"));
                    break;
                    
                case "setsize":
                    completions.addAll(Arrays.asList("0.5", "1.0", "1.5", "2.0", "2.5", "3.0"));
                    break;
                    
                case "setpreparation":
                    completions.addAll(Arrays.asList("on", "off", "true", "false"));
                    break;
            }
        }
        
        // 过滤匹配的补全
        String currentArg = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(currentArg));
        
        return completions;
    }
}