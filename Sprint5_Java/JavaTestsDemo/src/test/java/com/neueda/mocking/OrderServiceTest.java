package com.neueda.mocking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

//    @Mock
//    OrderRepository repository;

    @Test
    void shouldSaveOrder() {

        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        OrderService orderService = new OrderService(repository);
        Product product = new Product("Laptop", 1000);

        // Act
        orderService.placeOrder(101, product, 3);

        // Assert
        //I expect the save() method to have been called on the repository mock with an Order object.
        //It doesn't care whether the order contained the correct total.
        verify(repository).save(any(Order.class));
    }
    @Test
    void shouldCreateCorrectOrder() {

        // Arrange
        OrderRepository repository = mock(OrderRepository.class);
        OrderService orderService = new OrderService(repository);
        Product product = new Product("Laptop", 1000);

        // Act
        orderService.placeOrder(101, product, 3);

        // Capture the argument
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        // 1. Did repository.save() get called?
        // 2. While verifying that call, capture the actual Order object that was passed.
        verify(repository).save(captor.capture());
        // Get the actual Order passed to save()
        Order capturedOrder =
                captor.getValue();
        // Assert its contents
        assertEquals(101, capturedOrder.getOrderId());

        assertEquals("Laptop", capturedOrder.getProductName());

        assertEquals(3, capturedOrder.getQuantity());

        assertEquals(3000, capturedOrder.getTotalAmount());
    }
}