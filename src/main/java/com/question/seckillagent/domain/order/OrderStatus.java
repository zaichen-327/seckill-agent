package com.question.seckillagent.domain.order;

public enum OrderStatus {
    PENDING_PAYMENT("待支付"),
    PAID("已支付"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REFUNDED("已退款");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
