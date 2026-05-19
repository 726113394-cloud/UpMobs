package io.Sriptirc_wp_1258.gost.commands;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 神圣守护管理命令
 */
public class DivineGuardianCommand implements CommandExecutor, TabCompleter {
    
    private final Gost plugin;
    
    public DivineGuardianCommand(Gost plugin) {
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
                
            case "settriggercount":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian settriggercount <次数>");
                    return true;
                }
                handleSetTriggerCount(sender, args[1]);
                break;
                
            case "setreapercooldown":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian setreapercooldown <秒数>");
                    return true;
                }
                handleSetReaperCooldown(sender, args[1]);
                break;
                
            case "broadcast":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian broadcast <on|off>");
                    return true;
                }
                handleBroadcast(sender, args[1]);
                break;
                
            case "info":
                handleInfo(sender);
                break;
                
            case "force":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian force <玩家名>");
                    return true;
                }
                handleForce(sender, args[1]);
                break;
                
            case "clear":
                handleClear(sender);
                break;
                
            case "setphase":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian setphase <秒数>");
                    return true;
                }
                handleSetPhase(sender, args[1]);
                break;
                
            case "setreward":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian setreward <百分比>");
                    return true;
                }
                handleSetReward(sender, args[1]);
                break;
                
            case "setmotherthreshold":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "用法: /divineguardian setmotherthreshold <人数>");
                    return true;
                }
                handleSetMotherThreshold(sender, args[1]);
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
        sender.sendMessage(ChatColor.GOLD + "        ✨ 神圣守护管理 v2.2.2 ✨");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian status" + ChatColor.GRAY + " - 查看神圣守护状态");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian enable" + ChatColor.GRAY + " - 启用神圣守护系统");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian disable" + ChatColor.GRAY + " - 禁用神圣守护系统");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian reload" + ChatColor.GRAY + " - 重新加载配置");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian info" + ChatColor.GRAY + " - 查看当前神圣守护信息");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian settriggercount <人数>" + ChatColor.GRAY + " - 设置触发人数（最后N位人类）");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian setphase <秒数>" + ChatColor.GRAY + " - 设置猎魔人阶段开始时间");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian setbroadcast <on|off>" + ChatColor.GRAY + " - 设置广播开关");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian sethitstokill <次数>" + ChatColor.GRAY + " - 设置收割者击杀所需攻击次数");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian setcooldown <秒数>" + ChatColor.GRAY + " - 设置收割者攻击冷却时间");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian setreward <猎魔人比例> <母体比例>" + ChatColor.GRAY + " - 设置击杀奖励比例");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian setmotherthreshold <人数>" + ChatColor.GRAY + " - 设置新增母体玩家阈值");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian force <玩家名>" + ChatColor.GRAY + " - 强制为玩家激活神圣守护");
        sender.sendMessage(ChatColor.YELLOW + "/divineguardian clear" + ChatColor.GRAY + " - 清除神圣守护数据");
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    /**
     * 处理状态命令
     */
    private void handleStatus(CommandSender sender) {
        boolean enabled = plugin.getConfigManager().isDivineGuardianSystemEnabled();
        int triggerHumanCount = plugin.getConfigManager().getDivineGuardianTriggerHumanCount();
        boolean broadcast = plugin.getConfigManager().isDivineGuardianBroadcastEnabled();
        
        // 神圣守护效果配置
        boolean teleportAttacker = plugin.getConfigManager().isHolyGuardianTeleportAttackerEnabled();
        double teleportRadius = plugin.getConfigManager().getHolyGuardianTeleportRadius();
        int effectDuration = plugin.getConfigManager().getHolyGuardianEffectDuration();
        
        // 猎魔人阶段配置
        int demonHunterPhaseStartTime = plugin.getConfigManager().getDemonHunterPhaseStartTime();
        int demonHunterMaxUses = plugin.getConfigManager().getDemonHunterMaxUses();
        int holyRedemptionCooldown = plugin.getConfigManager().getDemonHunterHolyRedemptionCooldown();
        
        // 收割者道具配置
        int reaperDamagePerHit = plugin.getConfigManager().getReaperWeaponDamagePerHit();
        int reaperHitsToKill = plugin.getConfigManager().getReaperWeaponHitsToKill();
        double reaperAttackCooldown = plugin.getConfigManager().getReaperWeaponAttackCooldown();
        boolean reaperEnchantGlow = plugin.getConfigManager().isReaperWeaponEnchantGlowEnabled();
        
        // 击杀奖励配置
        double demonHunterKillReward = plugin.getConfigManager().getDemonHunterKillRewardRatio();
        double motherKillDemonHunterReward = plugin.getConfigManager().getMotherKillDemonHunterRewardRatio();
        
        // 母体新增配置
        boolean additionalMotherEnabled = plugin.getConfigManager().isAdditionalMotherEnabled();
        int additionalMotherThreshold = plugin.getConfigManager().getAdditionalMotherPlayerThreshold();
        boolean additionalMotherOnlyInPhase = plugin.getConfigManager().isAdditionalMotherOnlyInDemonHunterPhase();
        
        // 当前状态
        boolean isDemonHunterPhase = plugin.getDivineGuardianManager().isInDemonHunterPhase();
        int holyGuardianCount = 0; // 需要从管理器获取，暂时为0
        int demonHunterCount = 0;  // 需要从管理器获取，暂时为0
        
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "        ✨ 神圣守护状态 v2.2.2 ✨");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "系统状态: " + (enabled ? ChatColor.GREEN + "已启用" : ChatColor.RED + "已禁用"));
        sender.sendMessage(ChatColor.YELLOW + "猎魔人阶段: " + (isDemonHunterPhase ? ChatColor.GREEN + "进行中" : ChatColor.RED + "未开始"));
        sender.sendMessage(ChatColor.YELLOW + "神圣守护玩家数: " + ChatColor.GREEN + holyGuardianCount);
        sender.sendMessage(ChatColor.YELLOW + "猎魔人玩家数: " + ChatColor.GREEN + demonHunterCount);
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "基础配置:");
        sender.sendMessage(ChatColor.GRAY + "  • 触发人数: " + ChatColor.GREEN + triggerHumanCount + " 位最后人类");
        sender.sendMessage(ChatColor.GRAY + "  • 广播消息: " + (broadcast ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "神圣守护效果:");
        sender.sendMessage(ChatColor.GRAY + "  • 传送攻击者: " + (teleportAttacker ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));
        sender.sendMessage(ChatColor.GRAY + "  • 传送半径: " + ChatColor.GREEN + teleportRadius + " 方块");
        sender.sendMessage(ChatColor.GRAY + "  • 效果持续时间: " + ChatColor.GREEN + effectDuration + " 秒");
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "猎魔人阶段:");
        sender.sendMessage(ChatColor.GRAY + "  • 开始时间: " + ChatColor.GREEN + "游戏剩余 " + demonHunterPhaseStartTime + " 秒");
        sender.sendMessage(ChatColor.GRAY + "  • 神之救赎最大次数: " + ChatColor.GREEN + demonHunterMaxUses);
        sender.sendMessage(ChatColor.GRAY + "  • 神之救赎冷却: " + ChatColor.GREEN + holyRedemptionCooldown + " 秒");
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "收割者道具:");
        sender.sendMessage(ChatColor.GRAY + "  • 击杀所需攻击: " + ChatColor.GREEN + reaperHitsToKill + " 次");
        sender.sendMessage(ChatColor.GRAY + "  • 攻击冷却: " + ChatColor.GREEN + reaperAttackCooldown + " 秒");
        sender.sendMessage(ChatColor.GRAY + "  • 附魔光效: " + (reaperEnchantGlow ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "击杀奖励:");
        sender.sendMessage(ChatColor.GRAY + "  • 猎魔人击杀鬼: " + ChatColor.GREEN + (demonHunterKillReward * 100) + "% 人类奖池");
        sender.sendMessage(ChatColor.GRAY + "  • 母体击杀猎魔人: " + ChatColor.GREEN + (motherKillDemonHunterReward * 100) + "% 总奖池");
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "母体新增:");
        sender.sendMessage(ChatColor.GRAY + "  • 功能状态: " + (additionalMotherEnabled ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));
        sender.sendMessage(ChatColor.GRAY + "  • 玩家阈值: " + ChatColor.GREEN + additionalMotherThreshold + " 人");
        sender.sendMessage(ChatColor.GRAY + "  • 仅猎魔人阶段: " + (additionalMotherOnlyInPhase ? ChatColor.GREEN + "是" : ChatColor.RED + "否"));
        
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    /**
     * 处理启用命令
     */
    private void handleEnable(CommandSender sender) {
        plugin.getConfigManager().setDivineGuardianEnabled(true);
        plugin.getDivineGuardianManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护功能已启用！");
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理禁用命令
     */
    private void handleDisable(CommandSender sender) {
        plugin.getConfigManager().setDivineGuardianEnabled(false);
        plugin.getDivineGuardianManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护功能已禁用！");
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理重新加载命令
     */
    private void handleReload(CommandSender sender) {
        plugin.getDivineGuardianManager().reload();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护配置已重新加载！");
    }
    
    /**
     * 处理设置使用次数命令
     */
    private void handleSetCharges(CommandSender sender, String chargesStr) {
        try {
            int charges = Integer.parseInt(chargesStr);
            
            if (charges < 1 || charges > 10) {
                sender.sendMessage(ChatColor.RED + "❌ 使用次数必须在1-10之间！");
                return;
            }
            
            plugin.getConfigManager().setDivineGuardianMaxCharges(charges);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护最大使用次数已设置为: " + charges);
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    /**
     * 处理设置冷却时间命令
     */
    private void handleSetCooldown(CommandSender sender, String cooldownStr) {
        try {
            int cooldown = Integer.parseInt(cooldownStr);
            
            if (cooldown < 1 || cooldown > 60) {
                sender.sendMessage(ChatColor.RED + "❌ 冷却时间必须在1-60秒之间！");
                return;
            }
            
            plugin.getConfigManager().setDivineGuardianCooldown(cooldown);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护冷却时间已设置为: " + cooldown + "秒");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    /**
     * 处理设置广播命令
     */
    private void handleBroadcast(CommandSender sender, String value) {
        boolean enable = value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equals("1");
        
        plugin.getConfigManager().setDivineGuardianBroadcastEnabled(enable);
        
        sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护广播消息已" + (enable ? "开启" : "关闭"));
        sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
    }
    
    /**
     * 处理设置模式命令
     */
    private void handleSetMode(CommandSender sender, String modeStr) {
        if (!modeStr.equals("1") && !modeStr.equals("2") && 
            !modeStr.equalsIgnoreCase("mode1") && !modeStr.equalsIgnoreCase("mode2")) {
            sender.sendMessage(ChatColor.RED + "❌ 无效的模式！请使用 1 或 2");
            sender.sendMessage(ChatColor.GRAY + "模式1: 神圣守护（感染免疫+随机传送）");
            sender.sendMessage(ChatColor.GRAY + "模式2: 救赎者（转化鬼玩家回人类）");
            return;
        }
        
        // 设置配置
        plugin.getConfigManager().setDivineGuardianMode(modeStr);
        
        // 更新管理器中的模式
        boolean success = plugin.getDivineGuardianManager().setGuardianMode(modeStr);
        
        if (success) {
            String modeName = modeStr.equals("1") || modeStr.equalsIgnoreCase("mode1") ? 
                "神圣守护" : "救赎者";
            sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护模式已切换为: " + modeName);
            sender.sendMessage(ChatColor.YELLOW + "注意：如果游戏正在进行中，模式切换会立即生效");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "⚠ 已经是该模式，无需切换");
        }
    }
    
    /**
     * 处理信息命令
     */
    private void handleInfo(CommandSender sender) {
        UUID divineGuardian = plugin.getDivineGuardianManager().getDivineGuardianPlayer();
        
        if (divineGuardian == null) {
            sender.sendMessage(ChatColor.YELLOW + "⚠ 当前没有激活的神圣守护玩家");
            return;
        }
        
        Player player = Bukkit.getPlayer(divineGuardian);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "❌ 神圣守护玩家不在线！");
            return;
        }
        
        int remainingCharges = plugin.getDivineGuardianManager().getRemainingCharges(divineGuardian);
        
        String modeDisplay = plugin.getDivineGuardianManager().getModeDisplayName();
        
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
        sender.sendMessage(ChatColor.GOLD + "              ✨ 神圣守护信息 ✨");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "模式: " + ChatColor.GREEN + modeDisplay);
        sender.sendMessage(ChatColor.YELLOW + "玩家: " + ChatColor.GREEN + player.getName());
        
        if (modeDisplay.equals("神圣守护")) {
            sender.sendMessage(ChatColor.YELLOW + "剩余使用次数: " + ChatColor.GREEN + remainingCharges);
        } else if (modeDisplay.equals("救赎者")) {
            int remainingUses = plugin.getDivineGuardianManager().getRedeemerRemainingUses(divineGuardian);
            sender.sendMessage(ChatColor.YELLOW + "神之救赎剩余次数: " + ChatColor.GREEN + remainingUses);
        }
        sender.sendMessage(ChatColor.YELLOW + "位置: " + ChatColor.GREEN + 
            "X=" + (int)player.getLocation().getX() + 
            ", Y=" + (int)player.getLocation().getY() + 
            ", Z=" + (int)player.getLocation().getZ());
        sender.sendMessage(ChatColor.YELLOW + "世界: " + ChatColor.GREEN + player.getWorld().getName());
        sender.sendMessage(ChatColor.YELLOW + "血量: " + ChatColor.GREEN + player.getHealth() + "/" + player.getMaxHealth());
        sender.sendMessage(ChatColor.YELLOW + "饥饿值: " + ChatColor.GREEN + player.getFoodLevel() + "/20");
        sender.sendMessage(ChatColor.GOLD + "════════════════════════════════");
    }
    
    /**
     * 处理强制激活命令
     */
    private void handleForce(CommandSender sender, String playerName) {
        if (!plugin.getGameManager().isGameRunning()) {
            sender.sendMessage(ChatColor.RED + "❌ 游戏未开始，无法激活神圣守护！");
            return;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "❌ 玩家 " + playerName + " 不在线！");
            return;
        }
        
        if (!plugin.getPlayerManager().isHuman(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "❌ 玩家 " + playerName + " 不是人类，无法激活神圣守护！");
            return;
        }
        
        // 强制激活神圣守护
        List<UUID> humanPlayers = new ArrayList<>();
        humanPlayers.add(target.getUniqueId());
        plugin.getDivineGuardianManager().checkAndActivateDivineGuardian(humanPlayers);
        
        sender.sendMessage(ChatColor.GREEN + "✅ 已强制为玩家 " + target.getName() + " 激活神圣守护！");
        target.sendMessage(ChatColor.GOLD + "✨ 管理员为你激活了神圣守护！");
    }
    
    /**
     * 处理清除命令
     */
    private void handleClear(CommandSender sender) {
        plugin.getDivineGuardianManager().cleanup();
        
        sender.sendMessage(ChatColor.GREEN + "✅ 神圣守护数据已清除！");
    }
    
    private void handleSetPhase(CommandSender sender, String secondsStr) {
        try {
            int seconds = Integer.parseInt(secondsStr);
            
            if (seconds < 30 || seconds > 300) {
                sender.sendMessage(ChatColor.RED + "❌ 猎魔人阶段时间必须在30-300秒之间！");
                return;
            }
            
            plugin.getConfigManager().setDemonHunterPhaseActivateTime(seconds);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 猎魔人阶段开始时间已设置为: " + seconds + "秒");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    private void handleSetReward(CommandSender sender, String percentageStr) {
        try {
            double percentage = Double.parseDouble(percentageStr);
            
            if (percentage < 1 || percentage > 100) {
                sender.sendMessage(ChatColor.RED + "❌ 奖励百分比必须在1-100之间！");
                return;
            }
            
            double ratio = percentage / 100.0;
            plugin.getConfigManager().setDemonHunterKillReward(ratio);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 猎魔人击杀奖励比例已设置为: " + percentage + "%");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    private void handleSetMotherThreshold(CommandSender sender, String thresholdStr) {
        try {
            int threshold = Integer.parseInt(thresholdStr);
            
            if (threshold < 1 || threshold > 20) {
                sender.sendMessage(ChatColor.RED + "❌ 母体新增阈值必须在1-20之间！");
                return;
            }
            
            plugin.getConfigManager().setAdditionalMotherThreshold(threshold);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 母体新增玩家阈值已设置为: " + threshold + "人");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    private void handleSetTriggerCount(CommandSender sender, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            
            if (count < 1 || count > 10) {
                sender.sendMessage(ChatColor.RED + "❌ 触发神圣守护的人类玩家数量必须在1-10之间！");
                return;
            }
            
            plugin.getConfigManager().setDivineGuardianTriggerHumanCount(count);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 触发神圣守护的人类玩家数量已设置为: " + count + "人");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }
    
    private void handleSetReaperCooldown(CommandSender sender, String cooldownStr) {
        try {
            double cooldown = Double.parseDouble(cooldownStr);
            
            if (cooldown < 0.5 || cooldown > 30) {
                sender.sendMessage(ChatColor.RED + "❌ 收割者攻击冷却时间必须在0.5-30秒之间！");
                return;
            }
            
            plugin.getConfigManager().setReaperWeaponAttackCooldown(cooldown);
            
            sender.sendMessage(ChatColor.GREEN + "✅ 收割者攻击冷却时间已设置为: " + cooldown + "秒");
            sender.sendMessage(ChatColor.YELLOW + "注意：需要重新加载配置或重启游戏才能生效");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "❌ 请输入有效的数字！");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 主命令补全
            completions.addAll(Arrays.asList(
                "status", "enable", "disable", "reload", 
                "settriggercount", "setreapercooldown", "broadcast", 
                "info", "force", "clear", "setphase", "setreward", "setmotherthreshold"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "settriggercount":
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
                    break;
                    
                case "setreapercooldown":
                    completions.addAll(Arrays.asList("1", "2", "3", "5", "10", "15", "20", "30"));
                    break;
                    
                case "setphase":
                    completions.addAll(Arrays.asList("30", "60", "90", "120", "150", "180", "210", "240", "270", "300"));
                    break;
                    
                case "setreward":
                    completions.addAll(Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100"));
                    break;
                    
                case "setmotherthreshold":
                    completions.addAll(Arrays.asList("4", "6", "8", "10", "12", "14", "16", "18", "20"));
                    break;
                    
                case "broadcast":
                    completions.addAll(Arrays.asList("on", "off", "true", "false"));
                    break;
                    
                case "force":
                    // 在线玩家补全
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                    break;
            }
        }
        
        // 过滤匹配的补全
        String currentArg = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(currentArg));
        
        return completions;
    }
}