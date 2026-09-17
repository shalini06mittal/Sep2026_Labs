package com.neueda.mocking;

public class Order {

    private int orderId;
    private String productName;
    private int quantity;
    private double totalAmount;

    public Order(int orderId,
                 String productName,
                 int quantity,
                 double totalAmount) {

        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}