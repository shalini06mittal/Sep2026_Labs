package com.neueda.mocking;

public class OrderService {

    private OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void placeOrder(int orderId,
                           Product product,
                           int quantity) {

        double totalAmount =
                product.getPrice() * quantity;

        Order order = new Order(
                orderId,
                product.getName(),
                quantity,
                totalAmount
        );

        orderRepository.save(order);
    }

//    public void placeOrder(int orderId,
//                           Product product,
//                           int quantity) {
//
//        double totalAmount =
//                product.getPrice() + quantity; // BUG!
//
//        Order order = new Order(
//                orderId,
//                product.getName(),
//                quantity,
//                totalAmount
//        );
//
//        orderRepository.save(order);
//    }
}
