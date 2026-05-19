package io.Sriptirc_wp_1258.gost.managers;

import io.Sriptirc_wp_1258.gost.Gost;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final Gost plugin;
    private FileConfiguration config;
    
    public ConfigManager(Gost plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void ensureConfigLoaded() {
        if (config == null) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
        if (config != null) {
            return; // 已经加载
        }
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // 设置默认值
        config.addDefault("ScriptIrc-config-version", 20);
        config.addDefault("game.duration", 420); // 7分钟，单位秒
        config.addDefault("game.preparation-time", 20); // 准备时间，单位秒
        config.addDefault("game.queue-time", 60); // 队列等待时间，单位秒
        config.addDefault("game.min-players", 2); // 最小玩家数
        config.addDefault("game.max-players", 16); // 最大玩家数
        config.addDefault("game.max-games", 1); // 最大同时游戏数
        config.addDefault("game.match-queue-time", 30); // 匹配队列时间，队列满员后等待多久开始游戏
        
        // 经济设置
        config.addDefault("economy.entry-fee", 100.0); // 入场费
        config.addDefault("economy.server-bonus", 5000.0); // 服务器奖金
        
        // 道具设置
        config.addDefault("items.adrenaline.duration", 10); // 肾上腺素持续时间，单位秒
        config.addDefault("items.adrenaline.speed-level", 2); // 肾上腺素速度等级
        config.addDefault("items.frenzy.duration", 10); // 狂暴持续时间，单位秒
        config.addDefault("items.frenzy.speed-level", 2); // 狂暴速度等级
        config.addDefault("items.ice-ball.slow-duration", 4); // 凝冰球减速持续时间，单位秒
        config.addDefault("items.ice-ball.slow-level", 4); // 凝冰球减速等级
        config.addDefault("items.soul-control.freeze-duration", 6); // 控魂术冻结持续时间，单位秒
        config.addDefault("items.soul-control.cooldown", 18); // 控魂术冷却时间，单位秒
        config.addDefault("items.teleport-pearl.cooldown", 20); // 传送珍珠冷却时间，单位秒
        config.addDefault("items.stinky-steak.speed-duration", 14); // 臭牛排速度效果持续时间，单位秒
        config.addDefault("items.stinky-steak.speed-level", 1); // 臭牛排速度效果等级（1=速度II）
        config.addDefault("items.stinky-steak.glowing-duration", 10); // 臭牛排发光效果持续时间，单位秒
        config.addDefault("items.stinky-steak.cooldown", 30); // 臭牛排冷却时间，单位秒
        config.addDefault("items.soul-detector.duration", 25); // 灵魂探测器暴露持续时间，单位秒
        config.addDefault("items.soul-detector.cooldown", 35); // 灵魂探测器冷却时间，单位秒
        config.addDefault("items.second-chance.cooldown", 180); // 一次机会冷却时间，单位秒
        config.addDefault("items.second-chance.human-speed-duration", 10); // 人类玩家速度效果持续时间，单位秒
        config.addDefault("items.second-chance.human-speed-level", 2); // 人类玩家速度效果等级
        config.addDefault("items.second-chance.human-glowing-duration", 10); // 人类玩家高亮效果持续时间，单位秒
        config.addDefault("items.second-chance.ghost-slow-duration", 7); // 鬼玩家缓慢效果持续时间，单位秒
        config.addDefault("items.second-chance.ghost-slow-level", 1); // 鬼玩家缓慢效果等级
        
        // 效果设置
        config.addDefault("effects.mother-ghost-blindness-duration", 20); // 母体失明持续时间，单位秒
        config.addDefault("effects.ghost-immobilize-duration", 20); // 母体鬼固定时间，单位秒
        config.addDefault("effects.ghost-sense-duration", 5); // 幽灵感知高亮持续时间，单位秒
        config.addDefault("effects.infection-lightning", true); // 感染时是否显示闪电
        config.addDefault("effects.infection-sound", true); // 感染时是否播放音效
        config.addDefault("effects.minute-glowing.enabled", true); // 是否启用每分钟高亮效果
        config.addDefault("effects.minute-glowing.duration", 5); // 高亮持续时间，单位秒
        config.addDefault("effects.minute-glowing.interval", 60); // 触发间隔时间，单位秒
        
        // 游戏血量设置
        config.addDefault("health.max-health", 10.0); // 游戏期间玩家的最大生命值（默认10颗心，即20点生命值）
        
        // 鬼转人类功能设置
        config.addDefault("ghost-to-human.enabled", false); // 是否启用鬼转人类功能
        config.addDefault("ghost-to-human.remaining-time", 180); // 剩余多少秒时触发（默认3分钟）
        config.addDefault("ghost-to-human.count", 1); // 转换数量
        
        // 黑暗效果设置
        config.addDefault("dark-effect.enabled", true); // 是否启用黑暗效果（默认开启）
        config.addDefault("dark-effect.duration", 999999); // 黑暗效果持续时间（秒）
        config.addDefault("dark-effect.amplifier", 0); // 黑暗效果等级
        
        // 心跳声设置
        config.addDefault("heartbeat.enabled", true); // 是否启用心跳声
        config.addDefault("heartbeat.interval", 10); // 心跳声播放间隔（秒）
        
        // 转化功能设置
        config.addDefault("conversion.enabled", false); // 是否启用转化功能
        config.addDefault("conversion.activate-time", 120); // 转化激活时间（游戏剩余时间，秒）
        config.addDefault("conversion.cooldown", 30); // 转化冷却时间（秒）
        config.addDefault("conversion.cost", 15); // 转化消耗宝石数量
        
        // 道具刷新设置
        config.addDefault("item-spawn.enabled", true); // 是否启用道具刷新
        config.addDefault("item-spawn.interval", 60); // 刷新间隔（秒）
        config.addDefault("item-spawn.max-per-refresh", 3); // 每次刷新数量上限
        config.addDefault("item-spawn.max-per-player", 1); // 每位玩家最多获得数量
        config.addDefault("item-spawn.max-item-types-per-player", 6); // 玩家最多拥有的道具种类数量
        

        
        // 服务器模组设置（预留）
        
        // 区域选择设置
        config.addDefault("area.selection-tool", "MAGMA_CREAM"); // 选区工具物品
        config.addDefault("area.max-areas", 20); // 最大存档区域数量
        config.addDefault("area.auto-teleport", true); // 是否自动传送到区域
        
        // 语言设置
        config.addDefault("language.default", "zh_CN"); // 默认语言
        config.addDefault("language.auto-detect", true); // 是否自动检测玩家语言
        
        // 神圣守护系统设置（v2.2.2重构）
        config.addDefault("divine-guardian.enabled", true); // 是否启用神圣守护系统
        config.addDefault("divine-guardian.trigger-human-count", 2); // 触发神圣守护的人类玩家数量（最后N位人类）
        config.addDefault("divine-guardian.broadcast", true); // 是否广播神圣守护触发消息
        
        // 神圣守护效果设置
        config.addDefault("divine-guardian.holy-guardian.teleport-attacker", true); // 随机传送尝试感染的鬼
        config.addDefault("divine-guardian.holy-guardian.teleport-radius", 10.0); // 随机传送半径（方块）
        config.addDefault("divine-guardian.holy-guardian.effect-duration", 30); // 神圣守护效果持续时间（秒），0表示永久直到猎魔人阶段
        config.addDefault("divine-guardian.holy-guardian.defense-charges", 3); // 神圣守护可抵挡的攻击次数
        
        // 猎魔人阶段设置
        config.addDefault("divine-guardian.demon-hunter.phase-start-time", 90); // 猎魔人阶段开始时间（游戏剩余秒数）
        config.addDefault("divine-guardian.demon-hunter.max-uses", 2); // 神之救赎道具最大使用次数（保持不变）
        config.addDefault("divine-guardian.demon-hunter.holy-redemption-cooldown", 10); // 神之救赎冷却时间（秒）
        
        // 收割者道具设置
        config.addDefault("divine-guardian.demon-hunter.reaper-weapon.damage-per-hit", 1); // 每次攻击伤害
        config.addDefault("divine-guardian.demon-hunter.reaper-weapon.hits-to-kill", 2); // 击杀所需攻击次数
        config.addDefault("divine-guardian.demon-hunter.reaper-weapon.attack-cooldown", 2.0); // 攻击冷却时间（秒）
        config.addDefault("divine-guardian.demon-hunter.reaper-weapon.enchant-glow", true); // 是否显示附魔光效
        
        // 击杀奖励设置
        config.addDefault("divine-guardian.demon-hunter.kill-rewards.demon-hunter-kill-reward", 0.3); // 猎魔人击杀鬼获得的奖金比例（从人类奖池分配）
        config.addDefault("divine-guardian.demon-hunter.kill-rewards.mother-kill-demon-hunter-reward", 0.5); // 母体击杀猎魔人获得的奖金比例（高于感染奖励）
        
        // 复活机制设置
        config.addDefault("divine-guardian.demon-hunter.respawn.enabled", true); // 是否启用鬼玩家复活机制
        config.addDefault("divine-guardian.demon-hunter.respawn.respawn-time", 15); // 复活时间（秒）
        
        // 血量设置（猎魔人阶段）
        config.addDefault("divine-guardian.demon-hunter.health.ghost-normal", 2.0); // 普通鬼血量（猎魔人阶段）
        config.addDefault("divine-guardian.demon-hunter.health.ghost-mother", 3.0); // 母体鬼血量（猎魔人阶段）
        config.addDefault("divine-guardian.demon-hunter.health.demon-hunter", 2.0); // 猎魔人血量（猎魔人阶段）
        config.addDefault("divine-guardian.demon-hunter.health.no-healing", true); // 猎魔人阶段禁止回血
        
        // 母体新增设置
        config.addDefault("divine-guardian.demon-hunter.additional-mother.enabled", true); // 是否在猎魔人阶段新增母体
        config.addDefault("divine-guardian.demon-hunter.additional-mother.player-threshold", 8); // 触发新增母体的玩家总数阈值
        config.addDefault("divine-guardian.demon-hunter.additional-mother.only-in-demon-hunter-phase", true); // 是否只在猎魔人阶段新增
        
        // 鬼玩家粒子效果设置
        config.addDefault("ghost-particle.enabled", true); // 是否启用鬼玩家粒子效果
        config.addDefault("ghost-particle.type", "REDSTONE"); // 粒子类型：REDSTONE, FLAME, SOUL_FIRE_FLAME, DRAGON_BREATH, PORTAL, DUST_COLOR_TRANSITION, SPELL_MOB, SPELL_WITCH, ENCHANTMENT_TABLE, CRIT_MAGIC, FIREWORKS_SPARK, HEART, NOTE, VILLAGER_ANGRY, VILLAGER_HAPPY, TOTEM_OF_UNDYING, COMPOSTER, SQUID_INK, DRIPPING_OBSIDIAN_TEAR, FALLING_OBSIDIAN_TEAR, LANDING_OBSIDIAN_TEAR
        config.addDefault("ghost-particle.count", 5); // 每次生成粒子数量
        config.addDefault("ghost-particle.interval", 15); // 粒子生成间隔（刻，20刻=1秒）
        config.addDefault("ghost-particle.mother-color", "255,0,0"); // 母体鬼粒子颜色（RGB格式：红,绿,蓝）
        config.addDefault("ghost-particle.normal-color", "0,255,0"); // 普通鬼粒子颜色（RGB格式：红,绿,蓝）
        config.addDefault("ghost-particle.size", 1.0); // 粒子大小
        config.addDefault("ghost-particle.show-in-preparation", true); // 准备阶段是否显示粒子
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // 获取配置值的方法
    public int getConfigVersion() {
        return config.getInt("ScriptIrc-config-version", 2);
    }
    
    public int getGameDuration() {
        ensureConfigLoaded();
        return config.getInt("game.duration", 420);
    }
    
    public int getPreparationTime() {
        return config.getInt("game.preparation-time", 20);
    }
    
    public int getQueueTime() {
        return config.getInt("game.queue-time", 60);
    }
    
    public int getMinPlayers() {
        return config.getInt("game.min-players", 2);
    }
    
    public int getMaxPlayers() {
        return config.getInt("game.max-players", 16);
    }
    
    public int getMaxGames() {
        return config.getInt("game.max-games", 1);
    }
    
    public double getEntryFee() {
        ensureConfigLoaded();
        return config.getDouble("economy.entry-fee", 100.0);
    }
    
    public void setEntryFee(double entryFee) {
        ensureConfigLoaded();
        config.set("economy.entry-fee", entryFee);
        plugin.saveConfig();
    }
    
    public double getServerBonus() {
        ensureConfigLoaded();
        return config.getDouble("economy.server-bonus", 5000.0);
    }
    
    public void setServerBonus(double serverBonus) {
        ensureConfigLoaded();
        config.set("economy.server-bonus", serverBonus);
        plugin.saveConfig();
    }
    
    public int getAdrenalineDuration() {
        ensureConfigLoaded();
        return config.getInt("items.adrenaline.duration", 10);
    }
    
    public int getAdrenalineSpeedLevel() {
        ensureConfigLoaded();
        return config.getInt("items.adrenaline.speed-level", 2);
    }
    
    public int getFrenzyDuration() {
        ensureConfigLoaded();
        return config.getInt("items.frenzy.duration", 10);
    }
    
    public int getFrenzySpeedLevel() {
        ensureConfigLoaded();
        return config.getInt("items.frenzy.speed-level", 2);
    }
    
    public int getIceBallSlowDuration() {
        return config.getInt("items.ice-ball.slow-duration", 4);
    }
    
    public int getIceBallSlowLevel() {
        return config.getInt("items.ice-ball.slow-level", 4);
    }
    
    public int getSoulControlFreezeDuration() {
        return config.getInt("items.soul-control.freeze-duration", 6);
    }
    
    public int getSoulControlCooldown() {
        return config.getInt("items.soul-control.cooldown", 18);
    }
    
    public int getMotherGhostBlindnessDuration() {
        return config.getInt("effects.mother-ghost-blindness-duration", 20);
    }
    
    public int getGhostSenseDuration() {
        return config.getInt("effects.ghost-sense-duration", 5);
    }
    
    public boolean isInfectionLightningEnabled() {
        return config.getBoolean("effects.infection-lightning", true);
    }
    
    public boolean isInfectionSoundEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("effects.infection-sound", true);
    }
    
    public boolean isAutoTeleportEnabled() {
        return config.getBoolean("area.auto-teleport", true);
    }
    
    public String getSelectionTool() {
        ensureConfigLoaded();
        return config.getString("area.selection-tool", "MAGMA_CREAM");
    }
    
    public int getMaxAreas() {
        return config.getInt("area.max-areas", 20);
    }
    
    // 新功能配置方法
    
    public int getGhostImmobilizeDuration() {
        return config.getInt("effects.ghost-immobilize-duration", 20);
    }
    
    public boolean isConversionEnabled() {
        return config.getBoolean("conversion.enabled", false);
    }
    
    public int getConversionActivateTime() {
        return config.getInt("conversion.activate-time", 120);
    }
    
    public int getConversionCooldown() {
        return config.getInt("conversion.cooldown", 30);
    }
    
    public int getConversionCost() {
        return config.getInt("conversion.cost", 15);
    }
    
    public boolean isItemSpawnEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("item-spawn.enabled", true);
    }
    
    public int getItemSpawnInterval() {
        return config.getInt("item-spawn.interval", 60);
    }
    
    public int getItemSpawnMaxPerRefresh() {
        return config.getInt("item-spawn.max-per-refresh", 3);
    }
    
    public int getItemSpawnMaxPerPlayer() {
        return config.getInt("item-spawn.max-per-player", 1);
    }
    

    

    

    

    
    public int getMatchQueueTime() {
        return config.getInt("game.match-queue-time", 30);
    }
    
    public int getSoulDetectorDuration() {
        return config.getInt("items.soul-detector.duration", 25);
    }
    
    public int getSoulDetectorCooldown() {
        return config.getInt("items.soul-detector.cooldown", 30);
    }
    
    public int getTeleportPearlCooldown() {
        return config.getInt("items.teleport-pearl.cooldown", 10);
    }
    
    public int getStinkySteakSpeedDuration() {
        return config.getInt("items.stinky-steak.speed-duration", 14);
    }
    
    public int getStinkySteakSpeedLevel() {
        return config.getInt("items.stinky-steak.speed-level", 1);
    }
    
    public int getStinkySteakGlowingDuration() {
        return config.getInt("items.stinky-steak.glowing-duration", 10);
    }
    
    public int getStinkySteakCooldown() {
        return config.getInt("items.stinky-steak.cooldown", 30);
    }
    
    public int getSecondChanceCooldown() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.cooldown", 60);
    }
    
    public int getSecondChanceHumanSpeedDuration() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.human-speed-duration", 10);
    }
    
    public int getSecondChanceHumanSpeedLevel() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.human-speed-level", 2);
    }
    
    public int getSecondChanceHumanGlowingDuration() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.human-glowing-duration", 10);
    }
    
    public int getSecondChanceGhostSlowDuration() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.ghost-slow-duration", 7);
    }
    
    public int getSecondChanceGhostSlowLevel() {
        ensureConfigLoaded();
        return config.getInt("items.second-chance.ghost-slow-level", 1);
    }
    
    public int getMaxItemTypesPerPlayer() {
        return config.getInt("item-spawn.max-item-types-per-player", 6);
    }
    
    public boolean isMinuteGlowingEnabled() {
        return config.getBoolean("effects.minute-glowing.enabled", true);
    }
    
    public int getMinuteGlowingDuration() {
        return config.getInt("effects.minute-glowing.duration", 5);
    }
    
    public int getMinuteGlowingInterval() {
        return config.getInt("effects.minute-glowing.interval", 60);
    }
    
    // 鬼转人类功能配置
    public boolean isGhostToHumanEnabled() {
        return config.getBoolean("ghost-to-human.enabled", false);
    }
    
    public int getGhostToHumanRemainingTime() {
        return config.getInt("ghost-to-human.remaining-time", 180);
    }
    
    public int getGhostToHumanCount() {
        return config.getInt("ghost-to-human.count", 1);
    }
    
    // 黑暗效果配置
    public boolean isDarkEffectEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("dark-effect.enabled", true);
    }
    
    public int getDarkEffectDuration() {
        ensureConfigLoaded();
        return config.getInt("dark-effect.duration", 999999);
    }
    
    public int getDarkEffectAmplifier() {
        ensureConfigLoaded();
        return config.getInt("dark-effect.amplifier", 0);
    }
    
    // 设置黑暗效果开关
    public void setDarkEffectEnabled(boolean enabled) {
        config.set("dark-effect.enabled", enabled);
        plugin.saveConfig();
    }
    
    // 心跳声配置
    public boolean isHeartbeatEnabled() {
        return config.getBoolean("heartbeat.enabled", true);
    }
    
    public int getHeartbeatInterval() {
        return config.getInt("heartbeat.interval", 10);
    }
    
    // 设置心跳声开关
    public void setHeartbeatEnabled(boolean enabled) {
        config.set("heartbeat.enabled", enabled);
        plugin.saveConfig();
    }
    
    // 语言配置
    public String getDefaultLanguage() {
        return config.getString("language.default", "zh_CN");
    }
    
    public boolean isAutoDetectLanguage() {
        return config.getBoolean("language.auto-detect", true);
    }
    
    public void setDefaultLanguage(String language) {
        config.set("language.default", language);
        plugin.saveConfig();
    }
    
    public void setAutoDetectLanguage(boolean autoDetect) {
        config.set("language.auto-detect", autoDetect);
        plugin.saveConfig();
    }
    
    // 神圣守护配置
    public boolean isDivineGuardianEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.enabled", false);
    }
    
    public int getDivineGuardianMaxCharges() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.max-charges", 3);
    }
    
    public int getDivineGuardianCooldown() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.cooldown", 5);
    }
    
    public int getDivineGuardianInvisibilityDuration() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.invisibility-duration", 10);
    }
    
    public void setDivineGuardianEnabled(boolean enabled) {
        config.set("divine-guardian.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setDivineGuardianMaxCharges(int maxCharges) {
        config.set("divine-guardian.max-charges", maxCharges);
        plugin.saveConfig();
    }
    
    public void setDivineGuardianCooldown(int cooldown) {
        config.set("divine-guardian.cooldown", cooldown);
        plugin.saveConfig();
    }
    
    public void setDivineGuardianBroadcastEnabled(boolean enabled) {
        config.set("divine-guardian.broadcast", enabled);
        plugin.saveConfig();
    }
    
    public String getDivineGuardianMode() {
        ensureConfigLoaded();
        return config.getString("divine-guardian.mode", "1");
    }
    
    public void setDivineGuardianMode(String mode) {
        config.set("divine-guardian.mode", mode);
        plugin.saveConfig();
    }
    
    // 救赎者配置（模式2）
    public int getRedeemerMaxUses() {
        ensureConfigLoaded();
        return config.getInt("redeemer.max-uses", 2);
    }
    
    public int getRedeemerSpeedLevel() {
        return config.getInt("redeemer.speed-level", 1);
    }
    
    public int getHolyRedemptionCooldown() {
        return config.getInt("redeemer.holy-redemption-cooldown", 10);
    }
    
    public int getConversionInvincibilityTime() {
        return config.getInt("redeemer.conversion-invincibility-time", 5);
    }
    
    public boolean isRedeemerBroadcastEnabled() {
        return config.getBoolean("redeemer.broadcast", true);
    }
    
    public void setRedeemerMaxUses(int maxUses) {
        config.set("redeemer.max-uses", maxUses);
        plugin.saveConfig();
    }
    
    public void setRedeemerSpeedLevel(int speedLevel) {
        config.set("redeemer.speed-level", speedLevel);
        plugin.saveConfig();
    }
    
    public void setHolyRedemptionCooldown(int cooldown) {
        config.set("redeemer.holy-redemption-cooldown", cooldown);
        plugin.saveConfig();
    }
    
    public void setConversionInvincibilityTime(int time) {
        config.set("redeemer.conversion-invincibility-time", time);
        plugin.saveConfig();
    }
    
    public void setRedeemerBroadcastEnabled(boolean enabled) {
        config.set("redeemer.broadcast", enabled);
        plugin.saveConfig();
    }
    
    // 鬼玩家粒子效果配置
    public boolean isGhostParticleEnabled() {
        return config.getBoolean("ghost-particle.enabled", true);
    }
    
    public String getGhostParticleType() {
        return config.getString("ghost-particle.type", "REDSTONE");
    }
    
    public int getGhostParticleCount() {
        return config.getInt("ghost-particle.count", 5);
    }
    
    public int getGhostParticleInterval() {
        return config.getInt("ghost-particle.interval", 15);
    }
    
    public String getGhostParticleMotherColor() {
        return config.getString("ghost-particle.mother-color", "255,0,0");
    }
    
    public String getGhostParticleNormalColor() {
        return config.getString("ghost-particle.normal-color", "0,255,0");
    }
    
    public double getGhostParticleSize() {
        return config.getDouble("ghost-particle.size", 1.0);
    }
    
    public boolean isGhostParticleShowInPreparation() {
        return config.getBoolean("ghost-particle.show-in-preparation", true);
    }
    
    public void setGhostParticleEnabled(boolean enabled) {
        config.set("ghost-particle.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setGhostParticleType(String type) {
        config.set("ghost-particle.type", type);
        plugin.saveConfig();
    }
    
    public void setGhostParticleCount(int count) {
        config.set("ghost-particle.count", count);
        plugin.saveConfig();
    }
    
    public void setGhostParticleInterval(int interval) {
        config.set("ghost-particle.interval", interval);
        plugin.saveConfig();
    }
    
    public void setGhostParticleMotherColor(String color) {
        config.set("ghost-particle.mother-color", color);
        plugin.saveConfig();
    }
    
    public void setGhostParticleNormalColor(String color) {
        config.set("ghost-particle.normal-color", color);
        plugin.saveConfig();
    }
    
    public void setGhostParticleSize(double size) {
        config.set("ghost-particle.size", size);
        plugin.saveConfig();
    }
    
    public void setGhostParticleShowInPreparation(boolean show) {
        config.set("ghost-particle.show-in-preparation", show);
        plugin.saveConfig();
    }
    
    // 游戏血量配置
    public double getMaxHealth() {
        ensureConfigLoaded();
        return config.getDouble("health.max-health", 10.0);
    }
    
    public void setMaxHealth(double maxHealth) {
        config.set("health.max-health", maxHealth);
        plugin.saveConfig();
    }
    
    // ==============================================
    // 神圣守护系统 v2.2.2 新配置方法
    // ==============================================
    
    // 基础设置
    public boolean isDivineGuardianSystemEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.enabled", true);
    }
    
    public int getDivineGuardianTriggerHumanCount() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.trigger-human-count", 2);
    }
    
    public boolean isDivineGuardianBroadcastEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.broadcast", true);
    }
    
    // 神圣守护效果设置
    public boolean isHolyGuardianTeleportAttackerEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.holy-guardian.teleport-attacker", true);
    }
    
    public double getHolyGuardianTeleportRadius() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.holy-guardian.teleport-radius", 10.0);
    }
    
    public int getHolyGuardianEffectDuration() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.holy-guardian.effect-duration", 30);
    }
    
    public int getHolyGuardianDefenseCharges() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.holy-guardian.defense-charges", 3);
    }
    
    // 猎魔人阶段设置
    public int getDemonHunterPhaseStartTime() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.phase-start-time", 90);
    }
    
    public int getDemonHunterMaxUses() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.max-uses", 2);
    }
    
    public int getDemonHunterHolyRedemptionCooldown() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.holy-redemption-cooldown", 10);
    }
    
    // 收割者道具设置
    public int getReaperWeaponDamagePerHit() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.reaper-weapon.damage-per-hit", 1);
    }
    
    public int getReaperWeaponHitsToKill() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.reaper-weapon.hits-to-kill", 2);
    }
    
    public double getReaperWeaponAttackCooldown() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.reaper-weapon.attack-cooldown", 2.0);
    }
    
    public boolean isReaperWeaponEnchantGlowEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.demon-hunter.reaper-weapon.enchant-glow", true);
    }
    
    // 击杀奖励设置
    public double getDemonHunterKillRewardRatio() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.kill-rewards.demon-hunter-kill-reward", 0.3);
    }
    
    public double getMotherKillDemonHunterRewardRatio() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.kill-rewards.mother-kill-demon-hunter-reward", 0.5);
    }
    
    // 母体新增设置
    public boolean isAdditionalMotherEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.demon-hunter.additional-mother.enabled", true);
    }
    
    public int getAdditionalMotherPlayerThreshold() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.additional-mother.player-threshold", 8);
    }
    
    public boolean isAdditionalMotherOnlyInDemonHunterPhase() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.demon-hunter.additional-mother.only-in-demon-hunter-phase", true);
    }
    
    // 设置方法
    public void setDivineGuardianSystemEnabled(boolean enabled) {
        ensureConfigLoaded();
        config.set("divine-guardian.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setDivineGuardianTriggerHumanCount(int count) {
        ensureConfigLoaded();
        config.set("divine-guardian.trigger-human-count", count);
        plugin.saveConfig();
    }
    
    public void setDemonHunterPhaseStartTime(int seconds) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.phase-start-time", seconds);
        plugin.saveConfig();
    }
    
    public void setDemonHunterMaxUses(int uses) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.max-uses", uses);
        plugin.saveConfig();
    }
    
    public void setDemonHunterHolyRedemptionCooldown(int cooldown) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.holy-redemption-cooldown", cooldown);
        plugin.saveConfig();
    }
    
    public void setReaperWeaponHitsToKill(int hits) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.reaper-weapon.hits-to-kill", hits);
        plugin.saveConfig();
    }
    
    public void setReaperWeaponAttackCooldown(double cooldown) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.reaper-weapon.attack-cooldown", cooldown);
        plugin.saveConfig();
    }
    
    public void setDemonHunterKillRewardRatio(double ratio) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.kill-rewards.demon-hunter-kill-reward", ratio);
        plugin.saveConfig();
    }
    
    public void setMotherKillDemonHunterRewardRatio(double ratio) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.kill-rewards.mother-kill-demon-hunter-reward", ratio);
        plugin.saveConfig();
    }
    
    public void setAdditionalMotherPlayerThreshold(int threshold) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.additional-mother.player-threshold", threshold);
        plugin.saveConfig();
    }
    
    // 复活机制相关方法
    public boolean isRespawnEnabled() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.demon-hunter.respawn.enabled", true);
    }
    
    public int getRespawnTime() {
        ensureConfigLoaded();
        return config.getInt("divine-guardian.demon-hunter.respawn.respawn-time", 15);
    }
    
    // 血量设置相关方法
    public double getGhostNormalHealth() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.health.ghost-normal", 2.0);
    }
    
    public double getGhostMotherHealth() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.health.ghost-mother", 3.0);
    }
    
    public double getDemonHunterHealth() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.health.demon-hunter", 2.0);
    }
    
    public double getMotherAttackDamage() {
        ensureConfigLoaded();
        return config.getDouble("divine-guardian.demon-hunter.health.mother-attack-damage", 1.0);
    }
    
    public boolean isNoHealingInDemonHunterPhase() {
        ensureConfigLoaded();
        return config.getBoolean("divine-guardian.demon-hunter.health.no-healing", true);
    }
    
    public void setRespawnEnabled(boolean enabled) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.respawn.enabled", enabled);
        plugin.saveConfig();
    }
    
    public void setRespawnTime(int seconds) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.respawn.respawn-time", seconds);
        plugin.saveConfig();
    }
    
    public void setGhostNormalHealth(double health) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.health.ghost-normal", health);
        plugin.saveConfig();
    }
    
    public void setGhostMotherHealth(double health) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.health.ghost-mother", health);
        plugin.saveConfig();
    }
    
    public void setDemonHunterHealth(double health) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.health.demon-hunter", health);
        plugin.saveConfig();
    }
    
    public void setNoHealingInDemonHunterPhase(boolean noHealing) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.health.no-healing", noHealing);
        plugin.saveConfig();
    }
    
    public void setDemonHunterPhaseActivateTime(int seconds) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.phase-start-time", seconds);
        plugin.saveConfig();
    }
    
    public void setDemonHunterKillReward(double ratio) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.kill-rewards.demon-hunter-kill-reward", ratio);
        plugin.saveConfig();
    }
    
    public void setAdditionalMotherThreshold(int threshold) {
        ensureConfigLoaded();
        config.set("divine-guardian.demon-hunter.additional-mother.player-threshold", threshold);
        plugin.saveConfig();
    }
    
    /**
     * 检查Vault经济系统是否启用
     */
    public boolean isVaultEnabled() {
        ensureConfigLoaded();
        // 默认返回true，假设经济系统可用
        return true;
    }
}