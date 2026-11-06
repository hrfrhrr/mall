package com.baizhan.mall.common.core.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 轻量级 ID 生成器：时间戳(41bit) + 机器号(5bit) + 序列(12bit) 压缩为 19 位数字。
 * 适用于 bz_orders.id varchar(50) 这类非自增主键，避免依赖具体中间件。
 */
public final class IdUtils {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00
    private static final long MACHINE_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static volatile long lastTimestamp = -1L;
    private static final long MACHINE_ID = initMachineId();

    private IdUtils() {
    }

    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("系统时钟回拨，拒绝生成 ID");
        }
        if (timestamp == lastTimestamp) {
            long seq = SEQUENCE.incrementAndGet() & MAX_SEQUENCE;
            if (seq == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            SEQUENCE.set(0);
        }
        lastTimestamp = timestamp;
        long delta = timestamp - EPOCH;
        return (delta << (MACHINE_BITS + SEQUENCE_BITS))
                | (MACHINE_ID << SEQUENCE_BITS)
                | SEQUENCE.get();
    }

    public static String nextIdStr() {
        return String.valueOf(nextId());
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    private static long initMachineId() {
        try {
            return (ProcessHandle.current().pid() & 0x1F);
        } catch (Throwable ignored) {
            return 1L;
        }
    }
}
