package io.Sriptirc_wp_1258.upmobs;

import io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 指令处理器
 * 处理所有插件指令
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final Upmobs plugin;
    private final ConfigManager configManager;
    private final MobManager mobManager;
    private final ChallengeTowerManager challengeTowerManager;
    
    public CommandHandler(Upmobs plugin, ConfigManager configManager, MobManager mobManager, ChallengeTowerManager challengeTowerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.mobManager = mobManager;
        this.challengeTowerManager = challengeTowerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 处理挑战塔快捷命令
        if (command.getName().equalsIgnoreCase("challengetower") || 
            command.getAliases().stream().anyMatch(a -> a.equalsIgnoreCase(label))) {
            return handleChallengeTowerShortcut(sender, args);
        }
        
        if (args.length == 0) {
            return showHelp(sender);
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                return showHelp(sender);
            case "reload":
                return reloadConfig(sender);
            case "set":
                return setAttribute(sender, args);
            case "get":
                return getAttribute(sender, args);
            case "preset":
                return managePreset(sender, args);
            case "custom":
                return manageCustomMob(sender, args);
            case "info":
                return getMobInfo(sender);
            case "spawn":
                return spawnCustomMob(sender, args);
            case "global":
                return setGlobalMultiplier(sender, args);
            case "tower":
                return manageChallengeTower(sender, args);
            case "clearupgraded":
                return clearUpgradedMobs(sender);
            default:
                sender.sendMessage(ChatColor.RED + "未知指令，使用 /upmobs help 查看帮助");
                return false;
        }
    }
    
    /**
     * 处理挑战塔快捷命令
     */
    private boolean handleChallengeTowerShortcut(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用挑战塔命令");
            return false;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== 挑战塔快捷命令 ===");
            player.sendMessage(ChatColor.YELLOW + "/ct join <ID>" + ChatColor.WHITE + " - 加入挑战塔队列");
            player.sendMessage(ChatColor.YELLOW + "/ct leave" + ChatColor.WHITE + " - 离开挑战塔队列");
            player.sendMessage(ChatColor.YELLOW + "/ct list" + ChatColor.WHITE + " - 列出所有挑战塔");
            player.sendMessage(ChatColor.YELLOW + "/ct info [ID]" + ChatColor.WHITE + " - 查看挑战塔信息");
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法: /ct join <挑战塔ID>");
                    return false;
                }
                return joinChallengeTower(player, args);
            case "leave":
                return leaveChallengeTower(player);
            case "list":
                return listChallengeTowers(player);
            case "info":
                return challengeTowerInfo(player, args);
            default:
                player.sendMessage(ChatColor.RED + "未知操作，可用: join, leave, list, info");
                return false;
        }
    }
    
    /**
     * 显示帮助信息
     */
    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 升格怪物Up!Mobs 帮助 ===");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs help" + ChatColor.WHITE + " - 显示此帮助");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs reload" + ChatColor.WHITE + " - 重载配置");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs set <生物> <属性> <值>" + ChatColor.WHITE + " - 设置生物属性");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs get <生物>" + ChatColor.WHITE + " - 查看生物配置");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs info" + ChatColor.WHITE + " - 查看准星所指生物信息");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs preset <save|load|list|delete> <名称>" + ChatColor.WHITE + " - 管理预设");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs custom <add|remove|list|spawn> <名称>" + ChatColor.WHITE + " - 管理自定义生物");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs global <倍率>" + ChatColor.WHITE + " - 设置全局增强倍率");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs spawn <自定义生物名称>" + ChatColor.WHITE + " - 生成自定义生物");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs tower <create|join|leave|list|info|tool|reset> [参数]" + ChatColor.WHITE + " - 挑战塔管理");
        sender.sendMessage(ChatColor.YELLOW + "/upmobs clearupgraded" + ChatColor.WHITE + " - 一键清除所有升格怪物 (管理员)");
        return true;
    }
    
    /**
     * 重载配置
     */
    private boolean reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("upmobs.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        configManager.loadConfig();
        sender.sendMessage(ChatColor.GREEN + "配置已重载");
        return true;
    }
    
    /**
     * 设置生物属性
     */
    private boolean setAttribute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upmobs.set")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs set <生物> <属性> <值>");
            sender.sendMessage(ChatColor.GRAY + "属性: health, speed, armor, damage, knockback, follow, attackspeed");
            sender.sendMessage(ChatColor.GRAY + "值格式: 固定值(100), 区间(50-150), 百分比(200%)");
            return false;
        }
        
        String mobName = args[1];
        String attribute = args[2].toLowerCase();
        String valueStr = args[3];
        
        // 解析生物类型
        EntityType entityType = null;
        try {
            entityType = EntityType.valueOf(mobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "未知的生物类型: " + mobName);
            sender.sendMessage(ChatColor.GRAY + "可用生物: " + getAvailableMobs());
            return false;
        }
        
        // 获取现有配置或创建新配置
        MobAttributes attributes = configManager.getMobAttributes(entityType);
        if (attributes == null) {
            attributes = new MobAttributes(entityType);
        }
        
        // 解析属性值
        AttributeValue attributeValue = AttributeValue.fromString(valueStr);
        
        // 设置属性
        switch (attribute) {
            case "health":
                attributes.setHealth(attributeValue);
                break;
            case "speed":
                attributes.setSpeed(attributeValue);
                break;
            case "armor":
                attributes.setArmor(attributeValue);
                break;
            case "damage":
                attributes.setDamage(attributeValue);
                break;
            case "knockback":
                attributes.setKnockbackResistance(attributeValue);
                break;
            case "follow":
                attributes.setFollowRange(attributeValue);
                break;
            case "attackspeed":
                attributes.setAttackSpeed(attributeValue);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "未知的属性: " + attribute);
                sender.sendMessage(ChatColor.GRAY + "可用属性: health, speed, armor, damage, knockback, follow, attackspeed");
                return false;
        }
        
        // 保存配置
        configManager.setMobAttributes(entityType, attributes);
        sender.sendMessage(ChatColor.GREEN + "已设置 " + mobName + " 的 " + attribute + " 为 " + valueStr);
        return true;
    }
    
    /**
     * 获取生物配置
     */
    private boolean getAttribute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upmobs.get")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs get <生物>");
            return false;
        }
        
        String mobName = args[1];
        
        // 检查是否为自定义生物
        MobAttributes attributes = configManager.getCustomMobAttributes(mobName);
        boolean isCustom = true;
        
        // 如果不是自定义生物，检查原版生物
        if (attributes == null) {
            try {
                EntityType entityType = EntityType.valueOf(mobName.toUpperCase());
                attributes = configManager.getMobAttributes(entityType);
                isCustom = false;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "未找到生物配置: " + mobName);
                return false;
            }
        }
        
        if (attributes == null) {
            sender.sendMessage(ChatColor.RED + "未找到生物配置: " + mobName);
            return false;
        }
        
        // 显示配置信息
        sender.sendMessage(ChatColor.GOLD + "=== " + mobName + " 配置 ===");
        sender.sendMessage(ChatColor.YELLOW + "类型: " + ChatColor.WHITE + (isCustom ? "自定义生物" : "原版生物"));
        sender.sendMessage(ChatColor.YELLOW + "血量: " + ChatColor.WHITE + attributes.getHealth().toString());
        sender.sendMessage(ChatColor.YELLOW + "速度: " + ChatColor.WHITE + attributes.getSpeed().toString());
        sender.sendMessage(ChatColor.YELLOW + "护甲: " + ChatColor.WHITE + attributes.getArmor().toString());
        sender.sendMessage(ChatColor.YELLOW + "伤害: " + ChatColor.WHITE + attributes.getDamage().toString());
        sender.sendMessage(ChatColor.YELLOW + "击退抗性: " + ChatColor.WHITE + attributes.getKnockbackResistance().toString());
        sender.sendMessage(ChatColor.YELLOW + "跟随范围: " + ChatColor.WHITE + attributes.getFollowRange().toString());
        sender.sendMessage(ChatColor.YELLOW + "攻击速度: " + ChatColor.WHITE + attributes.getAttackSpeed().toString());
        
        if (attributes.isFireResistant()) {
            sender.sendMessage(ChatColor.YELLOW + "火焰抗性: " + ChatColor.GREEN + "是");
        }
        if (attributes.isInvisible()) {
            sender.sendMessage(ChatColor.YELLOW + "隐身: " + ChatColor.GREEN + "是");
        }
        if (attributes.isGlowing()) {
            sender.sendMessage(ChatColor.YELLOW + "发光: " + ChatColor.GREEN + "是");
        }
        if (attributes.isSilent()) {
            sender.sendMessage(ChatColor.YELLOW + "静音: " + ChatColor.GREEN + "是");
        }
        
        return true;
    }
    
    /**
     * 管理预设
     */
    private boolean managePreset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upmobs.preset")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs preset <save|load|list|delete> [名称]");
            return false;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "save":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs preset save <名称>");
                    return false;
                }
                String saveName = args[2];
                configManager.savePreset(saveName, configManager.getAllMobAttributes());
                sender.sendMessage(ChatColor.GREEN + "预设已保存: " + saveName);
                break;
                
            case "load":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs preset load <名称>");
                    return false;
                }
                String loadName = args[2];
                Map<String, MobAttributes> preset = configManager.loadPreset(loadName);
                if (preset == null) {
                    sender.sendMessage(ChatColor.RED + "未找到预设: " + loadName);
                    return false;
                }
                // TODO: 应用预设到当前配置
                sender.sendMessage(ChatColor.GREEN + "预设已加载: " + loadName);
                break;
                
            case "list":
                Set<String> presetNames = configManager.getPresetNames();
                if (presetNames.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "暂无预设");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "可用预设:");
                    for (String name : presetNames) {
                        sender.sendMessage(ChatColor.YELLOW + "  - " + name);
                    }
                }
                break;
                
            case "delete":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs preset delete <名称>");
                    return false;
                }
                String deleteName = args[2];
                if (configManager.removePreset(deleteName)) {
                    sender.sendMessage(ChatColor.GREEN + "预设已删除: " + deleteName);
                } else {
                    sender.sendMessage(ChatColor.RED + "未找到预设: " + deleteName);
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "未知操作: " + action);
                return false;
        }
        
        return true;
    }
    
    /**
     * 管理自定义生物
     */
    private boolean manageCustomMob(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upmobs.custom")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs custom <add|remove|list|spawn> [名称]");
            return false;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs custom add <名称>");
                    return false;
                }
                String addName = args[2];
                MobAttributes attributes = new MobAttributes(addName);
                configManager.setCustomMob(addName, attributes);
                sender.sendMessage(ChatColor.GREEN + "自定义生物已添加: " + addName);
                sender.sendMessage(ChatColor.GRAY + "使用 /upmobs set " + addName + " ... 配置属性");
                break;
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs custom remove <名称>");
                    return false;
                }
                String removeName = args[2];
                if (configManager.removeCustomMob(removeName)) {
                    sender.sendMessage(ChatColor.GREEN + "自定义生物已删除: " + removeName);
                } else {
                    sender.sendMessage(ChatColor.RED + "未找到自定义生物: " + removeName);
                }
                break;
                
            case "list":
                Map<String, MobAttributes> customMobs = configManager.getAllCustomMobs();
                if (customMobs.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "暂无自定义生物");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "自定义生物列表:");
                    for (String name : customMobs.keySet()) {
                        sender.sendMessage(ChatColor.YELLOW + "  - " + name);
                    }
                }
                break;
                
            case "spawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "只有玩家可以生成生物");
                    return false;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /upmobs custom spawn <名称>");
                    return false;
                }
                String spawnName = args[2];
                Player player = (Player) sender;
                LivingEntity mob = mobManager.createCustomMob(player.getLocation(), spawnName);
                if (mob == null) {
                    sender.sendMessage(ChatColor.RED + "未找到自定义生物: " + spawnName);
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "已生成自定义生物: " + spawnName);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "未知操作: " + action);
                return false;
        }
        
        return true;
    }
    
    /**
     * 获取生物信息
     */
    private boolean getMobInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以查看生物信息");
            return false;
        }
        
        Player player = (Player) sender;
        LivingEntity target = getTargetEntity(player);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "请准星指向一个生物");
            return false;
        }
        
        String info = mobManager.getMobInfo(target);
        player.sendMessage(info);
        return true;
    }
    
    /**
     * 生成自定义生物
     */
    private boolean spawnCustomMob(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以生成生物");
            return false;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs spawn <自定义生物名称>");
            return false;
        }
        
        String mobName = args[1];
        Player player = (Player) sender;
        LivingEntity mob = mobManager.createCustomMob(player.getLocation(), mobName);
        
        if (mob == null) {
            sender.sendMessage(ChatColor.RED + "未找到自定义生物: " + mobName);
            return false;
        }
        
        sender.sendMessage(ChatColor.GREEN + "已生成自定义生物: " + mobName);
        return true;
    }
    
    /**
     * 设置全局倍率
     */
    private boolean setGlobalMultiplier(CommandSender sender, String[] args) {
        if (!sender.hasPermission("upmobs.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /upmobs global <倍率>");
            sender.sendMessage(ChatColor.GRAY + "例如: /upmobs global 3.0 (3倍增强)");
            return false;
        }
        
        try {
            double multiplier = Double.parseDouble(args[1]);
            if (multiplier <= 0) {
                sender.sendMessage(ChatColor.RED + "倍率必须大于0");
                return false;
            }
            
            configManager.setGlobalMultiplier(multiplier);
            sender.sendMessage(ChatColor.GREEN + "全局增强倍率已设置为: " + multiplier);
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "无效的倍率: " + args[1]);
            return false;
        }
    }
    
    /**
     * 获取玩家准星所指的生物
     */
    private LivingEntity getTargetEntity(Player player) {
        // 简单的射线检测
        List<org.bukkit.entity.Entity> nearby = player.getNearbyEntities(10, 10, 10);
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        org.bukkit.Location eyeLocation = player.getEyeLocation();
        
        for (org.bukkit.entity.Entity entity : nearby) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                if (eyeLocation.distance(entity.getLocation()) < 10) {
                    // 简单的方向检查
                    org.bukkit.util.Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector());
                    if (direction.angle(toEntity) < 0.5) {  // 30度内
                        return (LivingEntity) entity;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取可用生物列表
     */
    private String getAvailableMobs() {
        List<String> mobs = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (type.isAlive() && type.isSpawnable()) {
                mobs.add(type.name().toLowerCase());
            }
        }
        return String.join(", ", mobs.subList(0, Math.min(10, mobs.size()))) + "...";
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            completions.addAll(Arrays.asList("help", "reload", "set", "get", "preset", "custom", "info", "spawn", "global"));
        } else if (args.length == 2) {
            // 子命令参数补全
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "set":
                case "get":
                    // 生物类型补全
                    for (EntityType type : EntityType.values()) {
                        if (type.isAlive() && type.isSpawnable()) {
                            completions.add(type.name().toLowerCase());
                        }
                    }
                    // 自定义生物补全
                    completions.addAll(configManager.getAllCustomMobs().keySet());
                    break;
                    
                case "preset":
                    completions.addAll(Arrays.asList("save", "load", "list", "delete"));
                    break;
                    
                case "custom":
                    completions.addAll(Arrays.asList("add", "remove", "list", "spawn"));
                    break;
                    

                    
                case "tower":
                    completions.addAll(Arrays.asList("create", "join", "leave", "list", "info", "tool", "reset"));
                    break;
                    

            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            
            switch (subCommand) {
                case "set":
                    // 属性名补全
                    completions.addAll(Arrays.asList("health", "speed", "armor", "damage", "knockback", "follow", "attackspeed"));
                    break;
                    
                case "preset":
                    if (secondArg.equals("load") || secondArg.equals("delete")) {
                        // 预设名称补全
                        completions.addAll(configManager.getPresetNames());
                    }
                    break;
                    
                case "custom":
                    if (secondArg.equals("remove") || secondArg.equals("spawn")) {
                        // 自定义生物名称补全
                        completions.addAll(configManager.getAllCustomMobs().keySet());
                    }
                    break;
                    

                    
                case "tower":
                    // 挑战塔命令补全
                    if (secondArg.equals("join") || secondArg.equals("info")) {
                        // 挑战塔ID补全
                        completions.addAll(challengeTowerManager.getTowers().keySet());
                    } else if (secondArg.equals("reset")) {
                        // 玩家名称补全
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            completions.add(onlinePlayer.getName());
                        }
                    }
                    break;
            }
        }
        
        // 过滤匹配的补全项
        String currentArg = args[args.length - 1].toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(currentArg)) {
                filtered.add(completion);
            }
        }
        
        return filtered;
    }
    

    
    /**
     * 管理挑战塔
     */
    private boolean manageChallengeTower(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以管理挑战塔");
            return false;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower <create|join|leave|list|info|tool|reset|delete|sky> [参数]");
            player.sendMessage(ChatColor.GRAY + "create <ID> <名称> - 创建挑战塔 (需要选择区域)");
            player.sendMessage(ChatColor.GRAY + "join <ID> - 加入挑战塔队列");
            player.sendMessage(ChatColor.GRAY + "leave - 离开挑战塔队列");
            player.sendMessage(ChatColor.GRAY + "list - 列出所有挑战塔");
            player.sendMessage(ChatColor.GRAY + "info [ID] - 查看挑战塔信息");
            player.sendMessage(ChatColor.GRAY + "tool - 获取区域选择工具 (管理员)");
            player.sendMessage(ChatColor.GRAY + "reset [玩家] - 重置挑战塔进度 (管理员)");
            player.sendMessage(ChatColor.GRAY + "delete <ID> - 删除挑战塔区域 (管理员)");
            player.sendMessage(ChatColor.GRAY + "sky <start|status|end> - 通天层挑战 (通关18层后解锁)");
            return false;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "create":
                return createChallengeTower(player, args);
            case "join":
                return joinChallengeTower(player, args);
            case "leave":
                return leaveChallengeTower(player);
            case "list":
                return listChallengeTowers(player);
            case "info":
                return challengeTowerInfo(player, args);
            case "tool":
                return giveSelectionTool(player);
            case "reset":
                return resetChallengeTowerProgress(player, args);
            case "delete":
                return deleteChallengeTower(player, args);
            case "sky":
                return manageSkyTower(player, args);
            default:
                player.sendMessage(ChatColor.RED + "未知操作: " + action);
                player.sendMessage(ChatColor.GRAY + "可用操作: create, join, leave, list, info, tool, reset, delete, sky");
                return false;
        }
    }
    
    /**
     * 创建挑战塔
     */
    private boolean createChallengeTower(Player player, String[] args) {
        if (!player.hasPermission("upmobs.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限创建挑战塔");
            return false;
        }
        
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower create <ID> <名称>");
            player.sendMessage(ChatColor.GRAY + "ID: 挑战塔的唯一标识，使用英文字母和数字");
            player.sendMessage(ChatColor.GRAY + "名称: 挑战塔的显示名称");
            return false;
        }
        
        String towerId = args[2];
        String towerName = args[3];
        
        // 获取选择的区域
        io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerArea area = 
            challengeTowerManager.getSelectedArea(player);
        
        if (area == null) {
            player.sendMessage(ChatColor.RED + "请先使用区域选择工具选择区域");
            player.sendMessage(ChatColor.YELLOW + "使用: /upmobs tower tool 获取选择工具");
            return false;
        }
        
        // 创建挑战塔
        boolean success = challengeTowerManager.createTower(player, towerId, towerName, area);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "挑战塔创建成功！");
        }
        
        return success;
    }
    
    /**
     * 加入挑战塔队列
     */
    private boolean joinChallengeTower(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower join <挑战塔ID>");
            player.sendMessage(ChatColor.YELLOW + "使用 /upmobs tower list 查看可用挑战塔");
            return false;
        }
        
        String towerId = args[2];
        
        // 加入队列
        boolean success = challengeTowerManager.getQueue().joinQueue(player, towerId);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "已加入挑战塔队列");
        }
        
        return success;
    }
    
    /**
     * 离开挑战塔队列
     */
    private boolean leaveChallengeTower(Player player) {
        boolean success = challengeTowerManager.getQueue().leaveQueue(player);
        if (success) {
            player.sendMessage(ChatColor.YELLOW + "已离开挑战塔队列");
        }
        
        return success;
    }
    
    /**
     * 列出所有挑战塔
     */
    private boolean listChallengeTowers(Player player) {
        java.util.Collection<io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTower> towers = 
            challengeTowerManager.getAllTowers();
        
        if (towers.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "暂无挑战塔");
            return true;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== 挑战塔列表 ===");
        for (io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTower tower : towers) {
            player.sendMessage(ChatColor.YELLOW + "• " + tower.getShortInfo() + 
                ChatColor.GRAY + " (" + tower.getStatus() + ")");
        }
        
        // 显示队列信息
        java.util.List<io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerQueue.QueueInfo> queueInfos = 
            challengeTowerManager.getQueue().getAllQueueInfo();
        
        if (!queueInfos.isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "=== 当前队列 ===");
            for (io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerQueue.QueueInfo info : queueInfos) {
                player.sendMessage(ChatColor.YELLOW + "• " + info.getTowerId() + 
                    ChatColor.GRAY + " - " + info.getPlayerCount() + "人等待 (" + 
                    info.getStatus().getDisplayName() + ")");
            }
        }
        
        return true;
    }
    
    /**
     * 查看挑战塔信息
     */
    private boolean challengeTowerInfo(Player player, String[] args) {
        if (args.length < 3) {
            // 如果没有指定ID，显示玩家所在队列的信息
            String queueId = challengeTowerManager.getQueue().getPlayerQueue(player.getUniqueId());
            if (queueId != null) {
                io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTower tower = 
                    challengeTowerManager.getTower(queueId);
                if (tower != null) {
                    player.sendMessage(ChatColor.GOLD + "=== 你所在的挑战塔 ===");
                    player.sendMessage(ChatColor.YELLOW + tower.getInfo());
                    return true;
                }
            }
            
            // 显示所有挑战塔的简要信息
            return listChallengeTowers(player);
        }
        
        String towerId = args[2];
        io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTower tower = 
            challengeTowerManager.getTower(towerId);
        
        if (tower == null) {
            player.sendMessage(ChatColor.RED + "挑战塔不存在: " + towerId);
            player.sendMessage(ChatColor.YELLOW + "使用 /upmobs tower list 查看可用挑战塔");
            return false;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== 挑战塔信息 ===");
        player.sendMessage(ChatColor.YELLOW + tower.getInfo());
        
        // 显示队列信息
        io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerQueue.QueueInfo queueInfo = 
            challengeTowerManager.getQueue().getQueueInfo(towerId);
        
        if (queueInfo != null) {
            player.sendMessage(ChatColor.GOLD + "=== 队列信息 ===");
            player.sendMessage(ChatColor.YELLOW + "状态: " + ChatColor.WHITE + queueInfo.getStatus().getDisplayName());
            player.sendMessage(ChatColor.YELLOW + "等待人数: " + ChatColor.WHITE + queueInfo.getPlayerCount());
            if (!queueInfo.getPlayerNames().isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "队列玩家: " + 
                    ChatColor.WHITE + String.join(", ", queueInfo.getPlayerNames()));
            }
        }
        
        // 显示活动实例信息
        io.Sriptirc_wp_1258.upmobs.challengetower.ChallengeTowerInstance instance = 
            challengeTowerManager.getActiveInstance(towerId);
        
        if (instance != null && instance.isActive()) {
            player.sendMessage(ChatColor.GOLD + "=== 当前挑战 ===");
            player.sendMessage(ChatColor.YELLOW + instance.getStatusInfo());
        }
        
        return true;
    }
    
    /**
     * 给予区域选择工具
     */
    private boolean giveSelectionTool(Player player) {
        if (!player.hasPermission("upmobs.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用区域选择工具");
            return false;
        }
        
        challengeTowerManager.giveSelectionTool(player);
        return true;
    }
    
    /**
     * 重置挑战塔进度
     */
    private boolean resetChallengeTowerProgress(Player player, String[] args) {
        if (!player.hasPermission("upmobs.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限重置挑战塔进度");
            return false;
        }
        
        Player targetPlayer = player;
        
        if (args.length >= 3) {
            // 重置指定玩家的进度
            String targetName = args[2];
            targetPlayer = Bukkit.getPlayer(targetName);
            
            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "玩家不在线或不存在: " + targetName);
                return false;
            }
        }
        
        boolean success = challengeTowerManager.resetPlayerProgress(targetPlayer);
        if (success) {
            player.sendMessage(ChatColor.GREEN + "已重置 " + targetPlayer.getName() + " 的挑战塔进度");
        }
        
        return success;
    }
    
    /**
     * 管理通天层挑战
     */
    private boolean manageSkyTower(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower sky <start|status|end> [挑战塔ID]");
            player.sendMessage(ChatColor.GRAY + "start <挑战塔ID> - 开始通天层挑战 (需要通关18层)");
            player.sendMessage(ChatColor.GRAY + "status - 查看通天层挑战状态");
            player.sendMessage(ChatColor.GRAY + "end - 结束当前通天层挑战");
            return false;
        }
        
        String skyAction = args[2].toLowerCase();
        
        switch (skyAction) {
            case "start":
                return startSkyTowerChallenge(player, args);
            case "status":
                return showSkyTowerStatus(player);
            case "end":
                return endSkyTowerChallenge(player);
            default:
                player.sendMessage(ChatColor.RED + "未知操作: " + skyAction);
                player.sendMessage(ChatColor.GRAY + "可用操作: start, status, end");
                return false;
        }
    }
    
    /**
     * 开始通天层挑战
     */
    private boolean startSkyTowerChallenge(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower sky start <挑战塔ID>");
            return false;
        }
        
        String towerId = args[3];
        
        // 检查玩家是否已经在通天层中
        if (challengeTowerManager.isPlayerInSkyTower(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "§c你已经在通天层挑战中！");
            return false;
        }
        
        // 开始通天层挑战
        boolean started = challengeTowerManager.startSkyTowerChallenge(player, towerId);
        if (started) {
            player.sendMessage(ChatColor.GREEN + "§a通天层挑战已开始！祝你好运！");
        }
        
        return started;
    }
    
    /**
     * 显示通天层状态
     */
    private boolean showSkyTowerStatus(Player player) {
        String statusInfo = challengeTowerManager.getSkyTowerStatusInfo();
        player.sendMessage(statusInfo);
        return true;
    }
    
    /**
     * 结束通天层挑战
     */
    private boolean endSkyTowerChallenge(Player player) {
        // 检查玩家是否在通天层中
        if (!challengeTowerManager.isPlayerInSkyTower(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "§c你不在通天层挑战中！");
            return false;
        }
        
        // 结束通天层挑战
        challengeTowerManager.endSkyTowerChallenge(player.getUniqueId(), false);
        player.sendMessage(ChatColor.YELLOW + "§e已结束通天层挑战");
        return true;
    }
    
    /**
     * 删除挑战塔区域
     */
    private boolean deleteChallengeTower(Player player, String[] args) {
        // 检查权限
        if (!player.hasPermission("upmobs.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限删除挑战塔区域");
            return false;
        }
        
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "用法: /upmobs tower delete <挑战塔ID>");
            player.sendMessage(ChatColor.GRAY + "示例: /upmobs tower delete tower1");
            return false;
        }
        
        String towerId = args[2];
        
        // 检查挑战塔是否存在
        if (!challengeTowerManager.towerExists(towerId)) {
            player.sendMessage(ChatColor.RED + "挑战塔不存在: " + towerId);
            player.sendMessage(ChatColor.YELLOW + "使用 /upmobs tower list 查看所有挑战塔");
            return false;
        }
        
        // 检查挑战塔是否正在进行中
        if (challengeTowerManager.isTowerActive(towerId)) {
            player.sendMessage(ChatColor.RED + "挑战塔正在进行中，无法删除");
            player.sendMessage(ChatColor.YELLOW + "请等待挑战结束或强制结束挑战");
            return false;
        }
        
        // 确认删除
        player.sendMessage(ChatColor.YELLOW + "§l⚠ 警告：即将删除挑战塔区域");
        player.sendMessage(ChatColor.YELLOW + "挑战塔ID: " + ChatColor.WHITE + towerId);
        player.sendMessage(ChatColor.YELLOW + "此操作不可撤销！");
        player.sendMessage(ChatColor.YELLOW + "输入 " + ChatColor.GREEN + "/upmobs tower delete " + towerId + " confirm" + 
                          ChatColor.YELLOW + " 确认删除");
        
        // 如果提供了确认参数
        if (args.length >= 4 && args[3].equalsIgnoreCase("confirm")) {
            // 删除挑战塔
            boolean deleted = challengeTowerManager.deleteTower(towerId);
            if (deleted) {
                player.sendMessage(ChatColor.GREEN + "§a✓ 挑战塔区域已成功删除: " + towerId);
                player.sendMessage(ChatColor.YELLOW + "该挑战塔的所有配置和进度已被清除");
            } else {
                player.sendMessage(ChatColor.RED + "删除挑战塔失败，请检查控制台日志");
            }
            return deleted;
        }
        
        return true;
    }
    
    /**
     * 一键清除所有升格怪物（包括已进化的）
     */
    private boolean clearUpgradedMobs(CommandSender sender) {
        if (!sender.hasPermission("upmobs.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此指令");
            return false;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "正在扫描并清除所有升格怪物...");
        
        int removed = 0;
        
        // 遍历所有世界
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            // 遍历世界中的所有实体
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity)) continue;
                
                LivingEntity living = (LivingEntity) entity;
                
                // 检查是否有升格标记（upgraded_mob标签）或进化标记（进化阶段标签）
                boolean isUpgraded = false;
                
                for (String tag : living.getScoreboardTags()) {
                    if (tag.equals("upgraded_mob") || tag.startsWith("evolution_stage_")) {
                        isUpgraded = true;
                        break;
                    }
                }
                
                if (isUpgraded) {
                    // 清除该实体
                    entity.remove();
                    removed++;
                }
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "§a✓ 已清除 " + removed + " 个升格怪物");
        return true;
    }
}