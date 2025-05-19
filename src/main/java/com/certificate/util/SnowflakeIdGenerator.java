package com.certificate.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 */
@Component
public class SnowflakeIdGenerator {

    // 起始的时间戳
    private final static long START_STAMP = 1480166465631L;

    // 每一部分占用的位数
    private final static long SEQUENCE_BIT = 12; // 序列号占用的位数
    private final static long MACHINE_BIT = 5;   // 机器标识占用的位数
    private final static long DATACENTER_BIT = 5;// 数据中心占用的位数

    // 每一部分的最大值
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    // 每一部分向左的位移
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;  // 数据中心
    private long machineId;     // 机器标识

    // 修改为使用ThreadLocal确保线程安全
    private static final ThreadLocal<Long> LAST_STAMP_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> SEQUENCE_THREAD_LOCAL = new ThreadLocal<>();

    public SnowflakeIdGenerator() {
        this(1, 1);
    }

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     */
    public synchronized long nextId() {
        // 初始化ThreadLocal值
        if (LAST_STAMP_THREAD_LOCAL.get() == null) {
            LAST_STAMP_THREAD_LOCAL.set(-1L);
        }
        if (SEQUENCE_THREAD_LOCAL.get() == null) {
            SEQUENCE_THREAD_LOCAL.set(0L);
        }

        long currStamp = getNewStamp();
        long lastStamp = LAST_STAMP_THREAD_LOCAL.get();
        long sequence = SEQUENCE_THREAD_LOCAL.get();

        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        LAST_STAMP_THREAD_LOCAL.set(currStamp);
        SEQUENCE_THREAD_LOCAL.set(sequence);

        // 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= LAST_STAMP_THREAD_LOCAL.get()) {
            mill = getNewStamp();
        }
        return mill;
    }

    private long getNewStamp() {
        return System.currentTimeMillis();
    }
}