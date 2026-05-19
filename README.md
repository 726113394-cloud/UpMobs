# 升格怪物Up!Mobs 🩸⚡

> **让Minecraft怪物拥有进化能力！怪物攻击玩家吸血进化，最高4阶段，每阶段属性增强+环绕粒子+随机装备+额外掉落物，击杀可获得经济奖励。**

---

## 📋 一句话介绍

怪物攻击玩家后触发**吸血进化**，从普通怪物 → 升格怪物 → 第一阶段 → 第二阶段 → 第三阶段 → 第四阶段，每阶段属性更强、外观更炫、击杀可获得经济奖励。同时支持**挑战塔**（18层逐层加强）和**通天层**（无尽挑战模式）。

---

## 🎯 核心功能一览

| 功能 | 说明 |
|------|------|
| **吸血进化** | 怪物攻击玩家积累能量，满足条件后进化到下一阶段 |
| **4阶段进化** | 升格→第一阶段→第二阶段→第三阶段→第四阶段，属性累乘增强 |
| **环绕粒子** | 不同阶段怪物身上有不同数量的环绕粒子圈（1/2/4/7圈） |
| **随机装备** | 高阶段怪物随机穿戴护甲和武器，第四阶段带附魔 |
| **自然刷新升格** | 野外怪物生成时有几率直接成为升格怪物 |
| **击杀奖励** | 击杀升格/进化怪物有几率获得游戏币（需Vault） |
| **额外掉落** | 不同阶段怪物掉落不同物资（铁锭→钻石→附魔书→下界合金碎片） |
| **挑战塔** | 18层挑战塔，逐层加强，支持多人组队排队 |
| **通天层** | 通关18层后解锁的无尽挑战模式 |
| **一键清除** | `/upmobs clearupgraded` 清除所有升格怪物 |

---

## 🩸 吸血进化机制

怪物攻击玩家时积累能量，满足条件后进化：

```
普通怪物 → 升格怪物（原版属性，有进化资格）
  ↓ 攻击玩家积累能量
第一阶段（1.5倍属性 + 1圈火焰粒子）
  ↓ 继续攻击
第二阶段（2.0倍属性 + 2圈粒子 + 随机皮革/金装备）
  ↓
第三阶段（2.5倍属性 + 4圈粒子 + 随机铁/链甲装备）
  ↓
第四阶段（3.0倍属性 + 7圈粒子 + 随机钻石附魔装备）
```

### 进化条件（可配置）
- 攻击玩家 **5次** 以上
- 造成累计伤害 **25点** 以上
- 血量低于 **50%**
- 冷却 **45秒**

---

## 🎨 阶段视觉区分

| 阶段 | 环绕粒子圈数 | 装备 | 附魔 | 额外掉落 |
|------|:-----------:|:----:|:----:|:--------:|
| 升格 | 无 | 无 | ❌ | 无 |
| 第一阶段 | 🔥 1圈火焰 | 无 | ❌ | 基础材料（40%） |
| 第二阶段 | 🔥💀 2圈 | 皮革/金套 | ❌ | 铁锭/金锭（50%） |
| 第三阶段 | 🔥💀🧙✨ 4圈 | 铁/链甲 | ❌ | 钻石/绿宝石（40%） |
| 第四阶段 | 🔥💀🧙✨🌀🐉🌟 7圈 | 钻石套 | ✅ 随机附魔 | 钻石+附魔书（30%）+下界合金碎片（10%） |

---

## 💰 经济奖励

击杀升格/进化怪物有几率获得游戏币（需安装Vault）：

```yaml
economy_rewards:
  enabled: true
  reward_chance: 0.3          # 30%几率获得奖励
  upgraded_amount: 15.0       # 升格怪物基础奖励
  base_amount: 10.0           # 第一阶段基础奖励
  stage_multiplier: 2.0       # 每阶段奖励倍率
  stage4_bonus: 50.0          # 第四阶段额外奖励
```

---

## 🏔️ 挑战塔 & 通天层

- **挑战塔**：18层，逐层加强，支持1-4人组队排队
- **通天层**：通关18层后解锁，无尽挑战模式，随时间增强

---

## ⌨️ 指令

| 指令 | 说明 |
|------|------|
| `/upmobs help` | 查看帮助 |
| `/upmobs reload` | 重载配置 |
| `/upmobs clearupgraded` | 一键清除所有升格怪物 |
| `/upmobs tower join <ID>` | 加入挑战塔 |
| `/upmobs tower leave` | 离开挑战塔 |
| `/ct join <ID>` | 快捷加入挑战塔 |
| `/ct leave` | 快捷离开挑战塔 |

---

## ⚙️ 快速配置

```yaml
# 总开关
upgrade_enabled: true

# 进化条件
evolution:
  required_attacks: 5
  required_damage: 25.0
  health_threshold: 50.0
  cooldown: 45000
  max_stages: 4
  stage_equipment: true    # 高阶段怪物装备开关

# 自然刷新升格
natural_upgrade:
  enabled: true
  spawn_chance: 0.09       # 0.09%几率

# 经济奖励
economy_rewards:
  enabled: true
  reward_chance: 0.3       # 30%几率获得奖励
```

---

## 🔧 依赖

- **Vault**（必需）- 经济系统支持

---

## 📝 注意事项

- 升格怪物与原版属性一致，只是标记为有进化资格
- 进化后属性在当前基础上累乘增强
- 怪物死亡后环绕粒子自动停止
- 经济奖励需要服务器安装经济插件（如EssentialsX）

### 🏔️ 通天层无尽挑战系统

#### 🎯 通天层核心特性
- **无尽挑战**：通关挑战塔18层后解锁的无尽模式
- **固定怪物**：始终保持10只第四阶段升格怪物
- **死亡刷新**：怪物死亡后5秒自动刷新
- **时间限制**：10分钟挑战时间（可配置）
- **难度递增**：随时间逐渐增强，最大3倍难度
- **丰富奖励**：击杀奖励 + 里程碑奖励 + 排行榜

#### ⏱️ 时间管理系统
- **倒计时显示**：ActionBar实时显示剩余时间
- **警告系统**：最后60秒显示警告
- **自动结束**：时间到自动结束挑战
- **死亡结束**：玩家死亡立即结束挑战
- **手动结束**：支持手动结束挑战

#### 🎮 游戏体验
- **状态显示**：实时显示怪物数量、击杀数、奖励
- **音效反馈**：开始、里程碑、结束都有音效
- **视觉特效**：怪物生成、死亡都有粒子特效
- **排行榜系统**：记录玩家最佳成绩

#### ⚙️ 通天层配置
```yaml
sky_tower:
  enabled: true                      # 是否启用通天层
  unlock_requirement: complete_tower # 解锁要求：complete_tower=通关挑战塔
  
  # 怪物配置
  monster:
    base_count: 10                   # 基础怪物数量
    refresh_on_death: true           # 死亡后刷新怪物
    refresh_delay_seconds: 5         # 刷新延迟（秒）
    types:                           # 怪物类型（第四阶段怪物）
      - STAGE4_ZOMBIE
      - STAGE4_SKELETON
      - STAGE4_CREEPER
      - STAGE4_SPIDER
      - STAGE4_ENDERMAN
  
  # 时间限制
  time_limit:
    minutes: 10                      # 时间限制（分钟）
    show_timer: true                 # 显示倒计时
    warning_seconds: 60              # 警告时间（秒）
  
  # 难度配置
  difficulty:
    increase_per_minute: 0.1         # 每分钟难度增加
    max_multiplier: 3.0              # 最大难度倍率
  
  # 奖励配置
  rewards:
    base_per_kill: 50.0              # 基础击杀奖励
    multiplier_per_minute: 1.2       # 每分钟奖励倍率
    milestone_interval_minutes: 5    # 里程碑间隔（分钟）
    milestone_bonus: 100.0           # 里程碑额外奖励
  
  # 排行榜配置
  leaderboard:
    enabled: true                    # 启用排行榜
    size: 10                         # 排行榜大小
    save_stats: true                 # 保存玩家统计
  
  # 特殊效果
  effects:
    player_status_display: true      # 启用玩家状态显示
    status_update_seconds: 10        # 状态更新间隔
```

### 💰 通关奖金系统

#### 🎯 奖金设计理念
- **分层激励**：不同层数对应不同奖金，激励玩家挑战更高层
- **首次通关**：只有首次通关才发放奖金，避免重复刷奖励
- **丰厚回报**：高额奖金回报玩家的努力和技巧
- **经济平衡**：与服务器经济系统完美集成

#### 📊 奖金分配
- **基础层（1-5层）**：每层8000游戏币，总计40000游戏币
- **进阶层（6-10层）**：每层9000游戏币，总计45000游戏币
- **精英层（11-15层）**：每层10000游戏币，总计50000游戏币
- **大师层（16-18层）**：每层11000游戏币，总计33000游戏币
- **总计奖金**：通关所有18层可获得168000游戏币

#### 🎮 奖金发放机制
1. **首次通关检测**：系统检测玩家是否首次达到该层
2. **实时发放**：通关后立即发放奖金
3. **个人化奖励**：每个玩家独立计算首次通关
4. **消息通知**：清晰的奖金发放消息
5. **音效反馈**：发放奖金时播放庆祝音效

#### ⚙️ 通关奖金配置
```yaml
challenge_tower:
  # 通关奖金配置（首次通关每层时发放）
  completion_rewards:
    enabled: true                    # 启用通关奖金
    base_reward: 8000.0              # 基础层奖金（1-5层）
    advanced_reward: 9000.0          # 进阶层奖金（6-10层）
    elite_reward: 10000.0            # 精英层奖金（11-15层）
    master_reward: 11000.0           # 大师层奖金（16-18层）
```

#### 💡 使用场景
- **新手激励**：基础层奖金帮助新手快速积累财富
- **进阶挑战**：进阶层奖金激励玩家提升技术
- **高手追求**：精英层和大师层奖金吸引高手挑战
- **团队合作**：多人挑战时所有队员都能获得奖金
- **服务器经济**：合理的经济注入，促进服务器经济循环

## ⚙️ 兼容性设计

### 与其他模组兼容
- **延迟进化**：不在生成时立即升级，避免与其他修改怪物的模组冲突
- **攻击触发**：只在攻击玩家后进化，确保兼容性
- **渐进变化**：属性逐步变化，避免瞬间突变导致的兼容问题

### 配置灵活性
```yaml
evolution:
  required_attacks: 3      # 可调节攻击次数
  required_damage: 10.0    # 可调节伤害阈值
  health_threshold: 70.0   # 可调节血量阈值
  max_stages: 3           # 可调节最大阶段
  duration: 60            # 可调节进化时长
```

## 🎮 指令系统

### 进化相关指令
- `/upmobs evolution info` - 查看进化统计数据
- `/upmobs evolution stats` - 查看详细进化记录
- `/upmobs evolution reset <玩家>` - 重置玩家相关进化数据

### 第四阶段怪物指令
- `/upmobs info` - 查看准星所指怪物信息（包括第四阶段标记）
- `/upmobs get <生物>` - 显示生物配置（包括第四阶段属性）
- `/upmobs set stage4.spawn_chance <几率>` - 设置第四阶段生成几率

### 原有指令增强
- `/upmobs info` - 现在显示进化阶段
- `/upmobs get <生物>` - 显示进化配置
- `/upmobs set` - 可设置进化相关属性

## 🔧 配置文件

### 进化专属配置
```yaml
# 吸血进化设置
evolution:
  enabled: true
  required_attacks: 3
  required_damage: 10.0
  health_threshold: 70.0
  cooldown: 30000
  max_stages: 3
  duration: 60
  
  # 多语言支持
  start_message: "§c§l{怪物} §f吸食了你的血液，开始进化！"
  complete_message: "§6§l{怪物} §e已完成第{阶段}阶段进化！"
  
  # 效果开关
  particles: true
  sounds: true
  screen_shake: true
  glowing: true
```

### 生物配置（支持进化）
```yaml
zombie:
  type: ZOMBIE
  health: 500%      # 基础属性（进化时以此为基础）
  speed: 150%
  armor: 300%
  damage: 400%
  # ... 其他属性
```

### 经济奖励系统配置
```yaml
# 经济奖励配置
economy_rewards:
  enabled: true                      # 是否启用经济奖励
  
  # 基础奖励设置
  base_amount: 10.0                  # 第一阶段基础奖励
  stage_multiplier: 2.0              # 每阶段奖励倍率（第二阶段=20，第三阶段=40，第四阶段=80）
  min_amount: 5.0                    # 最小奖励金额
  
  # 特殊怪物奖励
  stage4_bonus: 50.0                 # 第四阶段怪物额外奖励
  upgraded_amount: 15.0              # 升格怪物基础奖励
  
  # 奖励消息
  message_enabled: true              # 是否显示奖励消息
  message_format: "§a§l+ §f{amount} 游戏币 §7({reason})"  # 奖励消息格式
  
  # 奖励限制
  max_reward_per_kill: 200.0         # 单次击杀最大奖励
  require_permission: false          # 是否需要权限才能获得奖励
  permission_node: "upmobs.rewards"  # 奖励权限节点
```

### 挑战塔逐层加强配置
```yaml
# 挑战塔逐层加强配置
challenge_tower:
  # 基础配置
  max_players: 4                     # 最大玩家数
  max_levels: 18                     # 最大层数
  waves_per_level: 3                 # 每层波次数
  base_monster_count: 10             # 基础怪物数量
  increment_per_level: 6             # 每层增加怪物数量
  monster_per_player: 3              # 每多1人增加的怪物数量
  
  # 逐层加强配置
  level_scaling:
    # 基础属性增强
    base_health_multiplier: 1.0      # 基础血量倍率
    health_multiplier_per_level: 0.5 # 每层血量倍率增加
    max_health_multiplier: 10.0      # 最大血量倍率
    
    base_damage_multiplier: 1.0      # 基础伤害倍率
    damage_multiplier_per_level: 0.4 # 每层伤害倍率增加
    max_damage_multiplier: 8.0       # 最大伤害倍率
    
    # 特殊效果配置
    min_level_for_effects: 5         # 开始出现特殊效果的最小层数
    effect_chance_per_level: 0.03    # 每层特殊效果几率增加
    max_effect_chance: 0.5           # 最大特殊效果几率
    
    # 进化系统配置
    min_level_for_evolution: 6       # 开始出现进化怪物的最小层数
    evolution_chance_per_level: 0.017 # 每层进化几率增加
    max_evolution_chance: 0.3        # 最大进化几率
    
    # 第四阶段怪物配置
    min_level_for_stage4: 11         # 开始出现第四阶段怪物的最小层数
    stage4_chance_per_level: 0.008   # 每层第四阶段怪物几率增加
    max_stage4_chance: 0.15          # 最大第四阶段怪物几率
    
    # 特殊效果类型
    special_effects:
      - FIRE_RESISTANCE              # 火焰抗性
      - KNOCKBACK_RESISTANCE         # 击退抗性
      - REGENERATION                 # 生命恢复
      - STRENGTH                     # 力量效果
```

## 🚀 快速开始

### 1. 安装插件
将编译后的插件放入 `plugins` 目录，重启服务器。

### 2. 体验进化
1. 让怪物攻击你3次以上
2. 观察怪物开始进化（粒子效果+提示）
3. 享受3秒的渐进强化过程
4. 与进化后的强大怪物战斗！

### 3. 基础指令
```bash
# 查看帮助
/upmobs help

# 查看进化统计
/upmobs evolution stats

# 调整进化难度
/upmobs set evolution.required_attacks 5
/upmobs set evolution.required_damage 15.0

# 第四阶段怪物设置
/upmobs set stage4.spawn_chance 10.0    # 设置第四阶段生成几率为10%
/upmobs set stage4.health_multiplier 5.0 # 设置第四阶段血量倍率为5倍

# 经济奖励设置
/upmobs set economy_rewards.enabled true          # 启用经济奖励
/upmobs set economy_rewards.base_amount 15.0      # 设置第一阶段基础奖励为15
/upmobs set economy_rewards.stage_multiplier 2.5  # 设置阶段奖励倍率为2.5倍

# 挑战塔逐层加强设置
/upmobs set challenge_tower.level_scaling.health_multiplier_per_level 0.3  # 设置血量增强速率
/upmobs set challenge_tower.level_scaling.damage_multiplier_per_level 0.25 # 设置伤害增强速率
/upmobs set challenge_tower.level_scaling.evolution_chance_per_level 0.02  # 设置进化几率增加
/upmobs set challenge_tower.level_scaling.stage4_chance_per_level 0.01     # 设置第四阶段怪物几率增加

# 通天层设置
/upmobs set sky_tower.monster.base_count 12                    # 设置怪物数量为12只
/upmobs set sky_tower.time_limit.minutes 15                    # 设置时间限制为15分钟
/upmobs set sky_tower.rewards.base_per_kill 75.0               # 设置基础击杀奖励为75
/upmobs set sky_tower.difficulty.increase_per_minute 0.15      # 设置每分钟难度增加15%
```

## 📈 性能优化

### 智能数据管理
- **自动清理**：每5分钟清理死亡怪物的数据
- **内存优化**：使用UUID索引，高效查找
- **事件优化**：只监听必要事件，性能影响小

### 可调节性能
```yaml
# 性能相关配置
performance:
  cleanup_interval: 6000    # 清理间隔（tick）
  max_tracked_mobs: 1000    # 最大追踪怪物数
  debug_logging: false      # 调试日志
```

## 🎯 使用场景

### PvE服务器
- 增加怪物挑战性
- 创造动态难度
- 提升战斗刺激感

### RPG服务器  
- 作为Boss进化机制
- 创建精英怪物系统
- 实现怪物成长线

### 生存服务器
- 增加生存挑战
- 创造意外事件
- 提升游戏趣味性

## 🔄 更新日志

### v7.0.0 - 通关奖金系统版
- **分层奖金系统**：每层首次通关获得丰厚奖金
- **基础层奖金**：1-5层，每层8000游戏币
- **进阶层奖金**：6-10层，每层9000游戏币
- **精英层奖金**：11-15层，每层10000游戏币
- **大师层奖金**：16-18层，每层11000游戏币
- **首次通关**：只有首次通关才发放奖金
- **经济集成**：与Vault经济系统完美集成

### v6.0.0 - 通天层无尽挑战版
- **通天层系统**：通关18层后解锁的无尽挑战模式
- **无尽挑战**：固定10只第四阶段升格怪物，死亡后刷新
- **时间限制**：10分钟挑战时间，可配置
- **难度递增**：随时间逐渐增强，最大3倍难度
- **奖励系统**：击杀奖励+里程碑奖励+排行榜
- **状态显示**：实时显示怪物数量、击杀数、奖励

### v5.0.0 - 挑战塔逐层加强版
- **逐层加强系统**：挑战塔怪物根据层数获得多种增强
- **属性增强**：血量、伤害、速度、护甲逐层提升
- **特殊效果**：火焰抗性、击退抗性、生命恢复、力量效果
- **进化系统**：高层出现进化怪物和第四阶段怪物
- **配置系统**：完整的逐层加强配置选项
- **信息显示**：每层显示增强信息

### v4.0.0 - 经济奖励版
- **经济奖励系统**：击杀进化怪物获得游戏币奖励
- **Vault集成**：支持主流经济插件（EssentialsX、CMI等）
- **阶段奖励**：不同进化阶段获得不同金额奖励
- **随机性系统**：奖励金额带有随机浮动
- **配置系统**：完整的经济奖励配置选项
- **权限系统**：可配置奖励权限要求

### v3.0.0 - 第四阶段怪物版
- **第四阶段怪物**：8%几率自然生成第四阶段怪物
- **属性大幅增强**：相比第三阶段有大的跨度
- **永久特效**：多种永久性增益效果
- **配置系统**：完整的第四阶段配置选项
- **粒子特效**：龙息粒子 + 凋灵生成音效
- **标签系统**：添加第四阶段和精英标签

### v2.0.0 - 吸血进化版
- **全新机制**：吸血进化系统
- **渐进进化**：3秒属性渐变过程
- **华丽特效**：粒子、音效、屏幕震动
- **多阶段系统**：最多3阶段进化
- **兼容优化**：专为模组服务器设计
- **数据统计**：完整的进化记录系统

### v1.0.0 - 基础版
- 原版生物全面升级
- 属性调节系统
- 自定义生物支持
- 预设配置管理

## ⚠️ 注意事项

1. **模组兼容**：专为模组服务器设计，延迟进化避免冲突
2. **性能监控**：大量进化怪物可能增加性能负担
3. **难度平衡**：根据服务器情况调节进化条件
4. **经济系统**：经济奖励需要Vault插件和经济系统支持
5. **奖励平衡**：根据服务器经济情况调节奖励金额
6. **备份配置**：修改前备份config.yml

## 🐛 问题反馈

如遇到问题，请提供：
1. 服务器版本和模组列表
2. 复现步骤
3. 相关日志
4. 期望效果

## 📄 许可证

MIT License - 详见LICENSE文件

---

**让每一场战斗都充满变数，让每一个怪物都有进化的可能！** 🩸⚡