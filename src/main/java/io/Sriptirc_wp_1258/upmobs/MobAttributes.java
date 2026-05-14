package io.Sriptirc_wp_1258.upmobs;

import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;

/**
 * 生物属性配置
 * 包含血量、速度、护甲、伤害等属性
 */
public class MobAttributes {
    
    private EntityType entityType;
    private String customName;  // 自定义生物名称（如果为null则表示原版生物）
    
    // 属性配置
    private AttributeValue health;
    private AttributeValue speed;
    private AttributeValue armor;
    private AttributeValue damage;
    private AttributeValue knockbackResistance;
    private AttributeValue followRange;
    private AttributeValue attackSpeed;
    
    // 额外效果
    private boolean fireResistant;
    private boolean invisible;
    private boolean glowing;
    private boolean silent;
    
    // 生成权重（用于随机个体升级）
    private double spawnWeight = 1.0;
    
    public MobAttributes(EntityType entityType) {
        this.entityType = entityType;
        this.customName = null;
        
        // 默认值（中等增强）
        this.health = AttributeValue.percent(200);  // 2倍血量
        this.speed = AttributeValue.percent(150);   // 1.5倍速度
        this.armor = AttributeValue.percent(200);   // 2倍护甲
        this.damage = AttributeValue.percent(200);  // 2倍伤害
        this.knockbackResistance = AttributeValue.percent(150);
        this.followRange = AttributeValue.percent(150);
        this.attackSpeed = AttributeValue.percent(150);
    }
    
    public MobAttributes(String customName) {
        this.entityType = null;
        this.customName = customName;
        
        // 默认值
        this.health = AttributeValue.percent(200);
        this.speed = AttributeValue.percent(150);
        this.armor = AttributeValue.percent(200);
        this.damage = AttributeValue.percent(200);
        this.knockbackResistance = AttributeValue.percent(150);
        this.followRange = AttributeValue.percent(150);
        this.attackSpeed = AttributeValue.percent(150);
    }
    
    // Getter 和 Setter 方法
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public String getCustomName() {
        return customName;
    }
    
    public boolean isCustomMob() {
        return customName != null;
    }
    
    public AttributeValue getHealth() {
        return health;
    }
    
    public void setHealth(AttributeValue health) {
        this.health = health;
    }
    
    public AttributeValue getSpeed() {
        return speed;
    }
    
    public void setSpeed(AttributeValue speed) {
        this.speed = speed;
    }
    
    public AttributeValue getArmor() {
        return armor;
    }
    
    public void setArmor(AttributeValue armor) {
        this.armor = armor;
    }
    
    public AttributeValue getDamage() {
        return damage;
    }
    
    public void setDamage(AttributeValue damage) {
        this.damage = damage;
    }
    
    public AttributeValue getKnockbackResistance() {
        return knockbackResistance;
    }
    
    public void setKnockbackResistance(AttributeValue knockbackResistance) {
        this.knockbackResistance = knockbackResistance;
    }
    
    public AttributeValue getFollowRange() {
        return followRange;
    }
    
    public void setFollowRange(AttributeValue followRange) {
        this.followRange = followRange;
    }
    
    public AttributeValue getAttackSpeed() {
        return attackSpeed;
    }
    
    public void setAttackSpeed(AttributeValue attackSpeed) {
        this.attackSpeed = attackSpeed;
    }
    
    public boolean isFireResistant() {
        return fireResistant;
    }
    
    public void setFireResistant(boolean fireResistant) {
        this.fireResistant = fireResistant;
    }
    
    public boolean isInvisible() {
        return invisible;
    }
    
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
    
    public boolean isGlowing() {
        return glowing;
    }
    
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }
    
    public boolean isSilent() {
        return silent;
    }
    
    public void setSilent(boolean silent) {
        this.silent = silent;
    }
    
    public double getSpawnWeight() {
        return spawnWeight;
    }
    
    public void setSpawnWeight(double spawnWeight) {
        this.spawnWeight = spawnWeight;
    }
    
    /**
     * 转换为Map（用于配置文件）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        if (isCustomMob()) {
            map.put("type", "CUSTOM");
            map.put("name", customName);
        } else {
            map.put("type", entityType.name());
        }
        
        map.put("health", health.toString());
        map.put("speed", speed.toString());
        map.put("armor", armor.toString());
        map.put("damage", damage.toString());
        map.put("knockback_resistance", knockbackResistance.toString());
        map.put("follow_range", followRange.toString());
        map.put("attack_speed", attackSpeed.toString());
        
        map.put("fire_resistant", fireResistant);
        map.put("invisible", invisible);
        map.put("glowing", glowing);
        map.put("silent", silent);
        map.put("spawn_weight", spawnWeight);
        
        return map;
    }
    
    /**
     * 从Map解析（从配置文件加载）
     */
    public static MobAttributes fromMap(Map<String, Object> map) {
        String typeStr = (String) map.get("type");
        MobAttributes attributes;
        
        if ("CUSTOM".equals(typeStr)) {
            String name = (String) map.get("name");
            attributes = new MobAttributes(name);
        } else {
            EntityType entityType = EntityType.valueOf(typeStr);
            attributes = new MobAttributes(entityType);
        }
        
        // 解析属性值
        attributes.health = AttributeValue.fromString((String) map.get("health"));
        attributes.speed = AttributeValue.fromString((String) map.get("speed"));
        attributes.armor = AttributeValue.fromString((String) map.get("armor"));
        attributes.damage = AttributeValue.fromString((String) map.get("damage"));
        attributes.knockbackResistance = AttributeValue.fromString((String) map.get("knockback_resistance"));
        attributes.followRange = AttributeValue.fromString((String) map.get("follow_range"));
        attributes.attackSpeed = AttributeValue.fromString((String) map.get("attack_speed"));
        
        // 解析额外效果
        attributes.fireResistant = (Boolean) map.getOrDefault("fire_resistant", false);
        attributes.invisible = (Boolean) map.getOrDefault("invisible", false);
        attributes.glowing = (Boolean) map.getOrDefault("glowing", false);
        attributes.silent = (Boolean) map.getOrDefault("silent", false);
        attributes.spawnWeight = ((Number) map.getOrDefault("spawn_weight", 1.0)).doubleValue();
        
        return attributes;
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        if (isCustomMob()) {
            return customName;
        } else {
            return entityType.name().toLowerCase().replace("_", " ");
        }
    }
}