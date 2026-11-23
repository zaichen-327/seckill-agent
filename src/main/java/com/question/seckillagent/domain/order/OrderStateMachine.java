package com.question.seckillagent.domain.order;

import com.question.seckillagent.exception.BusinessException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(OrderStatus.PENDING_PAYMENT, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PAID, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.REFUNDED));
        ALLOWED.put(OrderStatus.COMPLETED, EnumSet.of(OrderStatus.REFUNDED));
        ALLOWED.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    private OrderStateMachine() {
    }

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    public static void requireTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new BusinessException("非法订单状态迁移：" + from + " -> " + to);
        }
    }
}
