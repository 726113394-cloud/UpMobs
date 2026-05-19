package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemManager {
    
    private final Gost plugin;
    private final Map<String, ItemStack> customItems = new HashMap<>();
    
    public ItemManager(Gost plugin) {
        this.plugin = plugin;
        initializeCustomItems();
    }
    
    private void initializeCustomItems() {
        createAdrenaline();
        createFrenzyPotion();
        createIceBall();
        createSoulControl();
        createSecondChance();
        // createBlinkPearl(); // 已废弃，使用传送珍珠
        
        plugin.getLogger().info("ItemManager初始化完成，创建了 " + customItems.size() + " 个自定义物品");
    }
    
    // 创建肾上腺素
    private void createAdrenaline() {
        ItemStack adrenaline = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) adrenaline.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "肾上腺素");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键使用获得速度效果",
                ChatColor.GRAY + "持续时间: " + plugin.getConfigManager().getAdrenalineDuration() + "秒",
                ChatColor.GRAY + "速度等级: " + plugin.getConfigManager().getAdrenalineSpeedLevel(),
                "",
                ChatColor.DARK_GRAY + "[消耗品]"
            ));
            
            // 设置药水颜色为绿色
            meta.setColor(Color.fromRGB(0, 255, 0));
            
            adrenaline.setItemMeta(meta);
        }
        
        customItems.put("adrenaline", adrenaline);
    }
    
    // 创建狂暴药水
    private void createFrenzyPotion() {
        ItemStack frenzy = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) frenzy.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "狂暴药水");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键使用获得速度效果",
                ChatColor.GRAY + "持续时间: " + plugin.getConfigManager().getFrenzyDuration() + "秒",
                ChatColor.GRAY + "速度等级: " + plugin.getConfigManager().getFrenzySpeedLevel(),
                "",
                ChatColor.DARK_GRAY + "[消耗品]"
            ));
            
            // 设置药水颜色为红色
            meta.setColor(Color.fromRGB(255, 0, 0));
            
            frenzy.setItemMeta(meta);
        }
        
        customItems.put("frenzy", frenzy);
    }
    
    // 创建凝冰球
    private void createIceBall() {
        ItemStack iceBall = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = iceBall.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "凝冰球");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键投掷对鬼造成减速效果",
                ChatColor.GRAY + "减速时间: " + plugin.getConfigManager().getIceBallSlowDuration() + "秒",
                ChatColor.GRAY + "减速等级: " + plugin.getConfigManager().getIceBallSlowLevel(),
                "",
                ChatColor.DARK_GRAY + "[消耗品]"
            ));
            
            // 添加发光效果
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            iceBall.setItemMeta(meta);
        }
        
        customItems.put("ice_ball", iceBall);
    }
    
    // 创建控魂术
    private void createSoulControl() {
        ItemStack soulControl = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = soulControl.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "控魂术");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键使用使所有鬼无法移动",
                ChatColor.GRAY + "冻结时间: " + plugin.getConfigManager().getSoulControlFreezeDuration() + "秒",
                ChatColor.GRAY + "冷却时间: " + plugin.getConfigManager().getSoulControlCooldown() + "秒",
                "",
                ChatColor.DARK_GRAY + "[消耗品]"
            ));
            
            // 添加发光效果
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            soulControl.setItemMeta(meta);
        }
        
        customItems.put("soul_control", soulControl);
    }
    
    // 创建闪现珍珠（已废弃，保留但不使用）
    private void createBlinkPearl() {
        // 这个方法已废弃，保留但不使用
        // 所有传送功能已合并到传送珍珠中
        plugin.getLogger().info("闪现珍珠创建方法已废弃，使用传送珍珠代替");
    }
    
    // 获取物品的方法
    public ItemStack getAdrenaline() {
        return customItems.get("adrenaline").clone();
    }
    
    public ItemStack getFrenzyPotion() {
        return customItems.get("frenzy").clone();
    }
    
    public ItemStack getIceBall() {
        return customItems.get("ice_ball").clone();
    }
    
    public ItemStack getSoulControl() {
        return customItems.get("soul_control").clone();
    }
    
    public ItemStack getBlinkPearl() {
        // 这个方法已废弃，返回空物品
        plugin.getLogger().warning("getBlinkPearl() 方法已废弃，请使用传送珍珠");
        return new ItemStack(Material.AIR);
    }
    
    // 应用物品效果
    public void applyAdrenalineEffect(Player player) {
        int duration = plugin.getConfigManager().getAdrenalineDuration() * 20;
        int level = plugin.getConfigManager().getAdrenalineSpeedLevel() - 1;
        
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            duration,
            level,
            true,
            true
        ));
        
        player.sendMessage(ChatColor.GREEN + "你使用了肾上腺素！速度提升！");
    }
    
    public void applyFrenzyEffect(Player player) {
        int duration = plugin.getConfigManager().getFrenzyDuration() * 20;
        int level = plugin.getConfigManager().getFrenzySpeedLevel() - 1;
        
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            duration,
            level,
            true,
            true
        ));
        
        player.sendMessage(ChatColor.RED + "你使用了狂暴药水！速度提升！");
    }
    
    public void applyIceBallEffect(Player target) {
        int duration = plugin.getConfigManager().getIceBallSlowDuration() * 20;
        int level = plugin.getConfigManager().getIceBallSlowLevel() - 1;
        
        plugin.getLogger().info("开始应用凝冰球效果给玩家: " + target.getName() + " (减速等级: " + level + ", 持续时间: " + duration + " ticks)");
        
        boolean success = target.addPotionEffect(new PotionEffect(
            PotionEffectType.SLOW,
            duration,
            level,
            true,
            true
        ));
        
        target.sendMessage(ChatColor.AQUA + "你被凝冰球击中了！移动速度降低！");
        plugin.getLogger().info("凝冰球效果应用给玩家: " + target.getName() + " (减速等级: " + level + ", 持续时间: " + duration + " ticks, 成功: " + success + ")");
    }
    
    public void applySoulControlEffect(List<Player> ghostPlayers) {
        int duration = plugin.getConfigManager().getSoulControlFreezeDuration() * 20;
        
        for (Player ghost : ghostPlayers) {
            ghost.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW,
                duration,
                255, // 最大减速效果
                true,
                true
            ));
            
            ghost.sendMessage(ChatColor.DARK_PURPLE + "你被控魂术影响了！无法移动！");
        }
    }
    
    // 应用幽灵感知效果（所有玩家高亮）
    public void applyGhostSenseEffect(List<Player> allPlayers) {
        int duration = plugin.getConfigManager().getGhostSenseDuration() * 20;
        
        // 直接使用GLOWING效果（1.20.x支持）
        for (Player player : allPlayers) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                duration,
                0,
                true,
                true
            ));
        }
        
        Bukkit.broadcastMessage(ChatColor.YELLOW + "幽灵感知激活！所有玩家高亮显示！");
    }
    
    // 随机分配道具
    public void distributeItems() {
        if (!plugin.getGameManager().isGameRunning()) {
            return;
        }
        
        List<UUID> allPlayers = plugin.getPlayerManager().getAllPlayers();
        List<UUID> humanPlayers = plugin.getPlayerManager().getHumanPlayers();
        List<UUID> ghostPlayers = plugin.getPlayerManager().getGhostPlayers();
        
        // 1. 所有人类获得肾上腺素
        for (UUID playerId : humanPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(getAdrenaline());
                player.sendMessage(ChatColor.GREEN + "你获得了肾上腺素！");
            }
        }
        
        // 2. 所有鬼获得狂暴药水
        for (UUID playerId : ghostPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(getFrenzyPotion());
                player.sendMessage(ChatColor.RED + "你获得了狂暴药水！");
            }
        }
        
        // 3. 随机6位玩家获得凝冰球
        List<UUID> iceBallRecipients = getRandomPlayers(allPlayers, Math.min(6, allPlayers.size()));
        for (UUID playerId : iceBallRecipients) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(getIceBall());
                player.sendMessage(ChatColor.AQUA + "你获得了凝冰球！");
            }
        }
        
        // 4. 随机3位人类获得控魂术
        if (!humanPlayers.isEmpty()) {
            int count = Math.min(3, humanPlayers.size());
            if (count == 0) count = 1; // 至少1位
            
            List<UUID> soulControlRecipients = getRandomPlayers(humanPlayers, count);
            for (UUID playerId : soulControlRecipients) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.getInventory().addItem(getSoulControl());
                    player.sendMessage(ChatColor.DARK_PURPLE + "你获得了控魂术！");
                }
            }
        }
        
        // 5. 随机6位玩家获得传送珍珠（替换闪现珍珠）
        List<UUID> teleportPearlRecipients = getRandomPlayers(allPlayers, Math.min(6, allPlayers.size()));
        for (UUID playerId : teleportPearlRecipients) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // 给予传送珍珠（从ItemSpawnManager获取）
                ItemStack teleportPearl = plugin.getItemSpawnManager().createTeleportPearl();
                player.getInventory().addItem(teleportPearl);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "你获得了传送珍珠！");
                plugin.getLogger().info("给予玩家 " + player.getName() + " 传送珍珠");
            }
        }
        
        Bukkit.broadcastMessage(ChatColor.YELLOW + "道具已发放！");
        plugin.getLogger().info("道具分发完成: 人类=" + humanPlayers.size() + ", 鬼=" + ghostPlayers.size() + 
            ", 凝冰球=" + iceBallRecipients.size() + ", 控魂术=" + (humanPlayers.isEmpty() ? 0 : Math.min(3, humanPlayers.size())) + 
            ", 传送珍珠=" + teleportPearlRecipients.size());
    }
    
    // 随机选择玩家
    private List<UUID> getRandomPlayers(List<UUID> players, int count) {
        if (players.size() <= count) {
            return new ArrayList<>(players);
        }
        
        List<UUID> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }
    
    // 游戏最后2分钟：随机将一名鬼变回人类
    public void randomGhostToHuman() {
        List<UUID> ghostPlayers = plugin.getPlayerManager().getGhostPlayers();
        if (ghostPlayers.isEmpty()) {
            return;
        }
        
        Collections.shuffle(ghostPlayers);
        UUID selectedGhost = ghostPlayers.get(0);
        
        plugin.getPlayerManager().setPlayerRole(selectedGhost, PlayerManager.PlayerRole.HUMAN);
        
        Player player = Bukkit.getPlayer(selectedGhost);
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "你在最后时刻被治愈了！变回了人类！");
            Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " 在最后时刻被治愈，变回了人类！");
        }
    }
    
    // 创建一次机会道具
    private void createSecondChance() {
        ItemStack secondChance = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = secondChance.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6一次机会"));
            meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7可抵挡一次鬼的感染"),
                ChatColor.translateAlternateColorCodes('&', "&6被动触发道具"),
                ChatColor.translateAlternateColorCodes('&', "&a仅限人类使用"),
                ChatColor.translateAlternateColorCodes('&', "&c[唯一道具]"),
                "",
                ChatColor.GRAY + "效果:",
                ChatColor.GRAY + "• 人类: 速度II " + plugin.getConfigManager().getSecondChanceHumanSpeedDuration() + "秒",
                ChatColor.GRAY + "• 人类: 高亮 " + plugin.getConfigManager().getSecondChanceHumanGlowingDuration() + "秒",
                ChatColor.GRAY + "• 鬼: 缓慢I " + plugin.getConfigManager().getSecondChanceGhostSlowDuration() + "秒",
                "",
                ChatColor.GRAY + "冷却时间: " + plugin.getConfigManager().getSecondChanceCooldown() + "秒",
                "",
                ChatColor.DARK_GRAY + "[被动触发]"
            ));
            
            // 添加发光效果
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            secondChance.setItemMeta(meta);
        }
        
        customItems.put("second_chance", secondChance);
        plugin.getLogger().info("已创建一次机会道具");
    }
    
    // 获取一次机会道具
    public ItemStack getSecondChance() {
        return customItems.get("second_chance").clone();
    }
    
    // 应用一次机会效果
    public void applySecondChanceEffect(Player humanPlayer, Player ghostPlayer) {
        plugin.getLogger().info("一次机会效果触发: 人类=" + humanPlayer.getName() + ", 鬼=" + ghostPlayer.getName());
        
        // 给人类玩家效果
        int humanSpeedDuration = plugin.getConfigManager().getSecondChanceHumanSpeedDuration() * 20;
        int humanSpeedLevel = plugin.getConfigManager().getSecondChanceHumanSpeedLevel() - 1;
        int humanGlowingDuration = plugin.getConfigManager().getSecondChanceHumanGlowingDuration() * 20;
        
        humanPlayer.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            humanSpeedDuration,
            humanSpeedLevel,
            true,
            true
        ));
        
        humanPlayer.addPotionEffect(new PotionEffect(
            PotionEffectType.GLOWING,
            humanGlowingDuration,
            0,
            true,
            true
        ));
        
        // 给鬼玩家效果
        int ghostSlowDuration = plugin.getConfigManager().getSecondChanceGhostSlowDuration() * 20;
        int ghostSlowLevel = plugin.getConfigManager().getSecondChanceGhostSlowLevel() - 1;
        
        ghostPlayer.addPotionEffect(new PotionEffect(
            PotionEffectType.SLOW,
            ghostSlowDuration,
            ghostSlowLevel,
            true,
            true
        ));
        
        // 发送消息
        humanPlayer.sendMessage(plugin.getLanguageManager().getMessage("item.one_chance_triggered"));
        humanPlayer.sendMessage(plugin.getLanguageManager().getMessage("item.random_teleport"));
        ghostPlayer.sendMessage(ChatColor.RED + "目标使用了一次机会！你被减速了！");
        
        // 广播消息
        Bukkit.broadcastMessage(ChatColor.YELLOW + humanPlayer.getName() + " 使用了一次机会抵挡了 " + ghostPlayer.getName() + " 的感染！");
        
        plugin.getLogger().info("一次机会效果应用完成");
    }
}