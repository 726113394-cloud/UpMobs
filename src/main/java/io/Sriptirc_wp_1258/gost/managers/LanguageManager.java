package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final Gost plugin;
    private final Map<String, String> messages = new HashMap<>();
    
    public LanguageManager(Gost plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    /**
     * 加载语言文件 - 简化版本，只加载默认中文消息
     */
    private void loadMessages() {
        // 直接加载默认中文消息，不依赖外部文件
        loadDefaultMessages();
        plugin.getLogger().info("已加载 " + messages.size() + " 条中文默认消息");
    }
    
    /**
     * 递归加载配置节（简化版本，不需要）
     */
    private void loadSection(String prefix, Object config) {
        // 简化版本：不需要加载外部文件
    }
    
    /**
     * 递归加载配置节内部实现（简化版本，不需要）
     */
    private void loadSectionInternal(String prefix, Object config) {
        // 简化版本：不需要加载外部文件
    }
    
    /**
     * 加载默认消息（当文件加载失败时）
     */
    private void loadDefaultMessages() {
        // 游戏相关消息
        messages.put("game.starting", "§e§l游戏开始倒计时");
        messages.put("game.started-human", "§a§l坚持到最后！");
        messages.put("game.started-ghost", "§c§l狩猎开始！");
        messages.put("game.ending", "§e§l游戏结束");
        messages.put("game.ended", "§a§l游戏已结束");
        
        // 角色相关消息
        messages.put("role.infected", "§c§l你已被感染！");
        messages.put("role.converted", "§a§l幸运星！这次重新做人！");
        messages.put("role.ghost-disabled-prep", "§c你是母体鬼，前20秒无法移动！");
        
        // 广播消息
        messages.put("broadcast.mother-ghost-selected", "§4{0} 被选为母体鬼！");
        messages.put("broadcast.player-infected", "§c{0} 被 {1} 感染了！");
        
        // 队列相关消息
        messages.put("queue.spectator-confirm", "§e游戏已在进行，再次点击确认观战");
        messages.put("queue.spectator-joined", "§a你已进入观战模式");
        
        // 道具相关消息
        messages.put("item.one_chance_triggered", "§a一次机会已触发！你已被传送至安全位置");
        messages.put("item.random_teleport", "§a你已被随机传送！");
        messages.put("item.second_chance_teleport_broadcast", "§a一次机会触发！玩家已被传送");
        messages.put("item.teleport_coordinates", "§a传送坐标: X={0}, Y={1}, Z={2}");
        messages.put("item.teleport_failed", "§c传送失败！找不到安全位置");
        
        // 道具刷新消息
        messages.put("item-spawn.refresh", "§e随机道具已刷新！");
        messages.put("item-spawn.received", "§a你获得了随机道具: {0}");
        
        // 时间消息
        messages.put("time.preparation", "§e准备阶段: {0}秒");
        
        // 添加更多默认消息，防止显示"Missing message"
        messages.put("game.starting-title", "§e§l游戏开始");
        messages.put("game.ending-title", "§e§l游戏结束");
        messages.put("role.human", "§a人类");
        messages.put("role.ghost", "§c鬼");
        messages.put("role.mother-ghost", "§4母体鬼");
        
        // 经济相关消息
        messages.put("economy.entry-fee-paid", "§a已支付入场费: {0} 金币");
        messages.put("economy.refund-received", "§e入场费已退还: {0} 金币");
        messages.put("economy.reward-received", "§a💰 你获得了 {0} 金币奖励！");
        
        // 神圣守护相关消息
        messages.put("divine-guardian.activated", "§e你已被选为神圣守护者！");
        messages.put("divine-guardian.triggered", "§a神圣守护触发！免疫感染并被传送");
        messages.put("divine-guardian.cooldown", "§c神圣守护冷却中，剩余 {0} 秒");
        
        // 救赎者相关消息
        messages.put("redeemer.activated", "§e你已被选为救赎者！");
        messages.put("redeemer.converted", "§a成功将 {0} 转化回人类！");
        messages.put("redeemer.cooldown", "§c神之救赎道具冷却中，剩余 {0} 秒");
        
        // 鬼粒子效果消息
        messages.put("ghost-particle.enabled", "§a鬼玩家粒子效果已启用");
        messages.put("ghost-particle.disabled", "§c鬼玩家粒子效果已禁用");
        
        // 黑暗效果消息
        messages.put("dark-effect.enabled", "§c黑暗效果已启用！所有玩家获得失明效果");
        messages.put("dark-effect.disabled", "§a黑暗效果已禁用！失明效果已移除");
        
        // 心跳声效果消息
        messages.put("heartbeat.enabled", "§c心跳声效果已启用");
        messages.put("heartbeat.disabled", "§a心跳声效果已禁用");
        
        // 区域相关消息
        messages.put("area.teleported", "§a你已被传送到游戏区域！");
        messages.put("area.not-exist", "§c区域不存在！");
        messages.put("area.saved", "§a区域 '{0}' 已保存！");
        messages.put("area.deleted", "§a区域 '{0}' 已删除！");
        messages.put("area.loaded", "§a已选择区域: {0}");
        
        // 通用错误消息
        messages.put("error.no-permission", "§c你没有权限使用此命令！");
        messages.put("error.player-only", "§c只有玩家可以使用此命令！");
        messages.put("error.not-in-game", "§c你不在游戏中！");
        messages.put("error.already-in-game", "§c你已经在游戏中！");
        messages.put("error.queue-full", "§c队列已满！");
        messages.put("error.insufficient-funds", "§c金币不足！需要 {0} 金币");
        messages.put("error.game-in-progress", "§c当前有游戏正在进行，请等待游戏结束！");
        messages.put("error.cannot-start", "§c当前不允许开始新游戏！");
        
        // 物品使用消息
        messages.put("item.adrenaline-used", "§a你使用了肾上腺素！速度提升！");
        messages.put("item.berserk-potion-used", "§c你使用了狂暴药水！速度提升！");
        messages.put("item.freeze-ball-used", "§b你被凝冰球击中了！移动速度降低！");
        messages.put("item.soul-control-used", "§5你被控魂术影响了！无法移动！");
        messages.put("item.stinky-steak-used", "§a你食用了臭牛排，获得了速度效果和发光效果！");
        messages.put("item.teleport-pearl-used", "§a你使用了传送珍珠，冷却时间 {0} 秒");
        messages.put("item.soul-detector-used", "§a你使用了灵魂探测器！所有玩家发光25秒！");
        
        // 物品获得消息
        messages.put("item.adrenaline-received", "§a你获得了肾上腺素！");
        messages.put("item.berserk-potion-received", "§c你获得了狂暴药水！");
        messages.put("item.freeze-ball-received", "§b你获得了凝冰球！");
        messages.put("item.soul-control-received", "§5你获得了控魂术！");
        messages.put("item.teleport-pearl-received", "§d你获得了传送珍珠！");
        messages.put("item.one-chance-received", "§6你获得了一次机会道具！");
        
        plugin.getLogger().info("已加载 " + messages.size() + " 条默认消息");
    }
    
    /**
     * 获取消息
     * @param key 消息键
     * @return 格式化后的消息
     */
    public String getMessage(String key) {
        String message = messages.get(key);
        if (message != null) {
            return message;
        }
        
        // 记录警告，但给玩家返回友好的默认消息
        plugin.getLogger().warning("找不到语言消息: " + key);
        
        // 根据key的类型返回不同的友好消息
        if (key.startsWith("game.")) {
            return "§e游戏提示";
        } else if (key.startsWith("role.")) {
            return "§c角色状态";
        } else if (key.startsWith("item.")) {
            return "§a道具效果";
        } else if (key.startsWith("broadcast.")) {
            return "§6广播消息";
        } else if (key.startsWith("queue.")) {
            return "§b队列信息";
        } else if (key.startsWith("economy.")) {
            return "§e经济提示";
        } else if (key.startsWith("error.")) {
            return "§c操作失败";
        } else if (key.startsWith("area.")) {
            return "§a区域信息";
        }
        
        // 通用默认消息
        return "§7系统提示";
    }
    
    /**
     * 获取消息并替换参数
     * @param key 消息键
     * @param args 参数
     * @return 格式化后的消息
     */
    public String getMessage(String key, Object... args) {
        String message = getMessage(key);
        try {
            return MessageFormat.format(message, args);
        } catch (Exception e) {
            plugin.getLogger().warning("格式化消息失败: " + key + " - " + e.getMessage());
            return message;
        }
    }
    
    /**
     * 向玩家发送消息
     * @param player 玩家
     * @param key 消息键
     * @param args 参数
     */
    public void sendMessage(Player player, String key, Object... args) {
        player.sendMessage(getMessage(key, args));
    }
    
    /**
     * 向玩家发送标题消息
     * @param player 玩家
     * @param titleKey 标题消息键
     * @param subtitleKey 副标题消息键
     * @param titleArgs 标题参数
     * @param subtitleArgs 副标题参数
     */
    public void sendTitle(Player player, String titleKey, String subtitleKey, 
                          Object[] titleArgs, Object[] subtitleArgs) {
        String title = titleKey != null ? getMessage(titleKey, titleArgs) : "";
        String subtitle = subtitleKey != null ? getMessage(subtitleKey, subtitleArgs) : "";
        
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    /**
     * 向玩家发送标题消息（简版）
     * @param player 玩家
     * @param titleKey 标题消息键
     * @param subtitleKey 副标题消息键
     */
    public void sendTitle(Player player, String titleKey, String subtitleKey) {
        sendTitle(player, titleKey, subtitleKey, new Object[0], new Object[0]);
    }
    
    /**
     * 向玩家发送行动栏消息
     * @param player 玩家
     * @param key 消息键
     * @param args 参数
     */
    public void sendActionBar(Player player, String key, Object... args) {
        String message = getMessage(key, args);
        // 简化版本：直接发送消息到聊天栏，避免依赖ActionBarManager
        player.sendMessage(message);
    }
    
    /**
     * 加载语言（兼容性方法）
     */
    public void loadLanguage() {
        loadMessages();
    }
    
    /**
     * 重新加载语言文件
     */
    public void reload() {
        loadMessages();
    }
    
    /**
     * 保存语言文件（简化版本，不需要保存）
     */
    public void save() {
        // 简化版本：不需要保存，所有消息都在代码中
    }
}