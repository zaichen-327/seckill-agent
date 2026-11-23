package com.question.seckillagent.domain.order;

import com.question.seckillagent.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {

    @Test
    void pendingPaymentCanBecomePaid() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID));
        assertDoesNotThrow(() -> OrderStateMachine.requireTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID));
    }

    @Test
    void pendingPaymentCannotBecomeCompleted() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.COMPLETED));
        assertThrows(BusinessException.class,
                () -> OrderStateMachine.requireTransition(OrderStatus.PENDING_PAYMENT, OrderStatus.COMPLETED));
    }

    @Test
    void paidCanBecomeRefunded() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.PAID, OrderStatus.REFUNDED));
        assertDoesNotThrow(() -> OrderStateMachine.requireTransition(OrderStatus.PAID, OrderStatus.REFUNDED));
    }

    @Test
    void refundedCannotBecomePaid() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.REFUNDED, OrderStatus.PAID));
        assertThrows(BusinessException.class,
                () -> OrderStateMachine.requireTransition(OrderStatus.REFUNDED, OrderStatus.PAID));
    }
}
