package io.Sriptirc_wp_1258.upmobs;

import java.util.Random;

/**
 * 属性值计算器
 * 支持固定值、区间随机、百分比加成三种计算方式
 */
public class AttributeValue {
    
    public enum ValueType {
        FIXED,      // 固定值
        RANGE,      // 区间随机
        PERCENT     // 百分比加成
    }
    
    private ValueType type;
    private double value1;  // 固定值 / 最小值 / 百分比
    private double value2;  // 最大值 (仅RANGE类型使用)
    
    private static final Random random = new Random();
    
    /**
     * 创建固定值属性
     */
    public static AttributeValue fixed(double value) {
        return new AttributeValue(ValueType.FIXED, value, 0);
    }
    
    /**
     * 创建区间随机属性
     */
    public static AttributeValue range(double min, double max) {
        return new AttributeValue(ValueType.RANGE, min, max);
    }
    
    /**
     * 创建百分比加成属性
     */
    public static AttributeValue percent(double percentage) {
        return new AttributeValue(ValueType.PERCENT, percentage, 0);
    }
    
    private AttributeValue(ValueType type, double value1, double value2) {
        this.type = type;
        this.value1 = value1;
        this.value2 = value2;
    }
    
    /**
     * 计算最终属性值
     * @param baseValue 基础值（用于百分比计算）
     * @return 计算后的属性值
     */
    public double calculate(double baseValue) {
        switch (type) {
            case FIXED:
                return value1;
            case RANGE:
                return value1 + (value2 - value1) * random.nextDouble();
            case PERCENT:
                return baseValue * (value1 / 100.0);
            default:
                return baseValue;
        }
    }
    
    /**
     * 获取属性类型
     */
    public ValueType getType() {
        return type;
    }
    
    /**
     * 获取第一个值
     */
    public double getValue1() {
        return value1;
    }
    
    /**
     * 获取第二个值
     */
    public double getValue2() {
        return value2;
    }
    
    /**
     * 转换为字符串表示（用于配置文件）
     */
    @Override
    public String toString() {
        switch (type) {
            case FIXED:
                return String.format("%.2f", value1);
            case RANGE:
                return String.format("%.2f-%.2f", value1, value2);
            case PERCENT:
                return String.format("%.1f%%", value1);
            default:
                return "0";
        }
    }
    
    /**
     * 从字符串解析属性值
     */
    public static AttributeValue fromString(String str) {
        if (str == null || str.isEmpty()) {
            return fixed(0);
        }
        
        str = str.trim();
        
        // 检查是否为百分比
        if (str.endsWith("%")) {
            try {
                double percent = Double.parseDouble(str.substring(0, str.length() - 1));
                return percent(percent);
            } catch (NumberFormatException e) {
                return fixed(0);
            }
        }
        
        // 检查是否为区间
        if (str.contains("-")) {
            String[] parts = str.split("-");
            if (parts.length == 2) {
                try {
                    double min = Double.parseDouble(parts[0].trim());
                    double max = Double.parseDouble(parts[1].trim());
                    return range(min, max);
                } catch (NumberFormatException e) {
                    return fixed(0);
                }
            }
        }
        
        // 默认为固定值
        try {
            double value = Double.parseDouble(str);
            return fixed(value);
        } catch (NumberFormatException e) {
            return fixed(0);
        }
    }
}