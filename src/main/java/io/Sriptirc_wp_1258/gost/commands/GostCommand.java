package io.Sriptirc_wp_1258.gost.commands;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GostCommand implements CommandExecutor, TabCompleter {
    
    private final Gost plugin;
    
    public GostCommand(Gost plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (!player.hasPermission("gost.player")) {
                    player.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
                    return true;
                }
                handleJoin(player);
                break;
            case "leave":
                if (!player.hasPermission("gost.player")) {
                    player.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
                    return true;
                }
                handleLeave(player);
                break;
            case "info":
                if (!player.hasPermission("gost.player")) {
                    player.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
                    return true;
                }
                handleInfo(player);
                break;
            case "help":
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleJoin(Player player) {
        // 检查权限
        if (!player.hasPermission("gost.join")) {
            player.sendMessage(ChatColor.RED + "你没有权限加入游戏！");
            return;
        }
        
        // 检查是否有游戏在进行
        if (plugin.getGameManager().isAnyGameRunning()) {
            player.sendMessage(ChatColor.RED + "当前有游戏正在进行，请等待游戏结束！");
            return;
        }
        
        // 检查最大游戏数
        if (plugin.getConfigManager().getMaxGames() == 0) {
            player.sendMessage(ChatColor.RED + "当前不允许开始新游戏！");
            return;
        }
        
        // 加入队列
        if (plugin.getGameManager().joinQueue(player)) {
            player.sendMessage(ChatColor.GREEN + "你已加入游戏队列！");
        }
    }
    
    private void handleLeave(Player player) {
        // 检查权限
        if (!player.hasPermission("gost.leave")) {
            player.sendMessage(ChatColor.RED + "你没有权限离开游戏！");
            return;
        }
        
        // 尝试离开游戏
        if (plugin.getPlayerManager().leaveGame(player)) {
            player.sendMessage(ChatColor.YELLOW + "你已离开游戏！");
            return;
        }
        
        // 尝试离开队列
        if (plugin.getGameManager().leaveQueue(player)) {
            player.sendMessage(ChatColor.YELLOW + "你已离开游戏队列！");
            return;
        }
        
        player.sendMessage(ChatColor.RED + "你不在游戏或队列中！");
    }
    
    private void handleInfo(Player player) {
        // 检查游戏状态
        if (plugin.getGameManager().isGameRunning()) {
            int humanCount = plugin.getPlayerManager().getHumanPlayers().size();
            int ghostCount = plugin.getPlayerManager().getGhostPlayers().size();
            
            player.sendMessage(ChatColor.GREEN + "=== 游戏信息 ===");
            player.sendMessage(ChatColor.YELLOW + "游戏状态: " + ChatColor.GREEN + "进行中");
            player.sendMessage(ChatColor.YELLOW + "人类数量: " + ChatColor.GREEN + humanCount);
            player.sendMessage(ChatColor.YELLOW + "鬼数量: " + ChatColor.RED + ghostCount);
            
            // 显示奖池信息
            if (plugin.getEconomyManager().isEconomyEnabled()) {
                double prizePool = plugin.getEconomyManager().getPrizePool();
                player.sendMessage(ChatColor.YELLOW + "当前奖池: " + ChatColor.GOLD + prizePool + " 金币");
            }
        } else if (plugin.getGameManager().getWaitingPlayersCount() > 0) {
            int waitingPlayers = plugin.getGameManager().getWaitingPlayersCount();
            int minPlayers = plugin.getConfigManager().getMinPlayers();
            
            player.sendMessage(ChatColor.GREEN + "=== 队列信息 ===");
            player.sendMessage(ChatColor.YELLOW + "游戏状态: " + ChatColor.GOLD + "等待玩家");
            player.sendMessage(ChatColor.YELLOW + "等待玩家: " + ChatColor.GOLD + waitingPlayers + "/" + minPlayers);
        } else {
            player.sendMessage(ChatColor.GREEN + "=== 游戏信息 ===");
            player.sendMessage(ChatColor.YELLOW + "游戏状态: " + ChatColor.GRAY + "未开始");
            player.sendMessage(ChatColor.YELLOW + "使用 " + ChatColor.GREEN + "/gost join" + ChatColor.YELLOW + " 加入游戏");
        }
        
        // 显示区域信息
        io.Sriptirc_wp_1258.gost.managers.AreaManager.GameArea selectedArea = plugin.getAreaManager().getSelectedArea();
        if (selectedArea != null) {
            player.sendMessage(ChatColor.YELLOW + "当前区域: " + ChatColor.GREEN + selectedArea.getName());
            int[] dims = selectedArea.getDimensions();
            player.sendMessage(ChatColor.YELLOW + "区域尺寸: " + ChatColor.WHITE + dims[0] + "×" + dims[1] + "×" + dims[2]);
        } else {
            player.sendMessage(ChatColor.YELLOW + "当前区域: " + ChatColor.RED + "未选择");
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== Gost 生化模式 ===");
        player.sendMessage(ChatColor.YELLOW + "/gost join" + ChatColor.GRAY + " - 加入游戏队列");
        player.sendMessage(ChatColor.YELLOW + "/gost leave" + ChatColor.GRAY + " - 离开游戏/队列");
        player.sendMessage(ChatColor.YELLOW + "/gost info" + ChatColor.GRAY + " - 查看游戏信息");
        player.sendMessage(ChatColor.YELLOW + "/gost help" + ChatColor.GRAY + " - 显示此帮助");
        
        if (player.hasPermission("gost.admin")) {
            player.sendMessage(ChatColor.RED + "管理员命令: /gostadmin");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("join");
            completions.add("leave");
            completions.add("info");
            completions.add("help");
        }
        
        return completions;
    }
}