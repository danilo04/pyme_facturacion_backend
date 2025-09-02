package com.walkyriasystems.pyme.facturacion.service

import com.walkyriasystems.pyme.facturacion.dto.OrderDto
import com.walkyriasystems.pyme.facturacion.dto.OrderItemResponseDto
import com.walkyriasystems.pyme.facturacion.dto.OrderResponseDto
import com.walkyriasystems.pyme.facturacion.dto.OrderUpdateDto
import com.walkyriasystems.pyme.facturacion.entity.Order
import com.walkyriasystems.pyme.facturacion.entity.OrderItem
import com.walkyriasystems.pyme.facturacion.entity.OrderStatus
import com.walkyriasystems.pyme.facturacion.repository.OrderRepository
import com.walkyriasystems.pyme.facturacion.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {
    fun createOrder(orderDto: OrderDto): OrderResponseDto {
        // Create order items with calculated prices
        val orderItems = mutableListOf<OrderItem>()
        var totalAmount = BigDecimal.ZERO
        
        // Create the order first (without items to avoid circular reference)
        val order = Order(
            uuid = UUID.randomUUID().toString(),
            status = orderDto.status,
            expectedDeliveryDate = orderDto.expectedDeliveryDate,
            totalAmount = BigDecimal.ZERO, // Will be updated after calculating items
            customerName = orderDto.customerName,
            customerEmail = orderDto.customerEmail,
            customerPhone = orderDto.customerPhone,
            notes = orderDto.notes
        )
        
        val savedOrder = orderRepository.save(order)
        
        // Create order items
        orderDto.items.forEach { itemDto ->
            val product = productRepository.findByUuid(itemDto.productUuid)
                ?: throw IllegalArgumentException("Product not found with UUID: ${itemDto.productUuid}")
            
            // Use provided unit price or get from product
            val unitPrice = itemDto.unitPrice ?: product.price
            
            val orderItem = OrderItem(
                uuid = UUID.randomUUID().toString(),
                order = savedOrder,
                product = product,
                quantity = itemDto.quantity,
                unitPrice = unitPrice,
                discount = itemDto.discount,
                totalPrice = calculateItemTotal(unitPrice, itemDto.quantity, itemDto.discount)
            )
            
            orderItems.add(orderItem)
            totalAmount = totalAmount.add(orderItem.totalPrice)
        }
        
        // Update order with correct total amount
        val updatedOrder = savedOrder.copy(totalAmount = totalAmount)
        val finalOrder = orderRepository.save(updatedOrder)
        
        // Save order items (this should be handled by cascade, but being explicit)
        // orderItemRepository.saveAll(orderItems)
        
        return mapToOrderResponseDto(finalOrder, orderItems)
    }
    
    /**
     * Get all orders with pagination
     */
    @Transactional(readOnly = true)
    fun getAllOrders(pageable: Pageable): Page<OrderResponseDto> {
        return orderRepository.findAll(pageable).map { order ->
            mapToOrderResponseDto(order, order.items)
        }
    }
    
    /**
     * Get order by UUID
     */
    @Transactional(readOnly = true)
    fun getOrderByUuid(uuid: String): OrderResponseDto {
        val order = orderRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Order not found with UUID: $uuid")
        return mapToOrderResponseDto(order, order.items)
    }
    
    /**
     * Get orders by customer name
     */
    @Transactional(readOnly = true)
    fun getOrdersByCustomerName(customerName: String): List<OrderResponseDto> {
        return orderRepository.findByCustomerName(customerName).map { order ->
            mapToOrderResponseDto(order, order.items)
        }
    }
    
    /**
     * Get orders by customer email
     */
    @Transactional(readOnly = true)
    fun getOrdersByCustomerEmail(customerEmail: String): List<OrderResponseDto> {
        return orderRepository.findByCustomerEmail(customerEmail).map { order ->
            mapToOrderResponseDto(order, order.items)
        }
    }
    
    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    fun getOrdersByStatus(status: OrderStatus): List<OrderResponseDto> {
        return orderRepository.findByStatus(status).map { order ->
            mapToOrderResponseDto(order, order.items)
        }
    }
    
    /**
     * Update order details (not items)
     */
    fun updateOrder(uuid: String, orderUpdateDto: OrderUpdateDto): OrderResponseDto {
        val existingOrder = orderRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Order not found with UUID: $uuid")
        
        // Prevent updating delivered or cancelled orders
        if (existingOrder.status == OrderStatus.DELIVERED || existingOrder.status == OrderStatus.CANCELLED) {
            throw IllegalStateException("Cannot update order with status: ${existingOrder.status}")
        }
        
        val updatedOrder = existingOrder.copy(
            customerName = orderUpdateDto.customerName,
            customerEmail = orderUpdateDto.customerEmail,
            customerPhone = orderUpdateDto.customerPhone,
            notes = orderUpdateDto.notes,
            status = orderUpdateDto.status,
            expectedDeliveryDate = orderUpdateDto.expectedDeliveryDate
        )
        
        val savedOrder = orderRepository.save(updatedOrder)
        return mapToOrderResponseDto(savedOrder, savedOrder.items)
    }
    
    /**
     * Update order status
     */
    fun updateOrderStatus(uuid: String, newStatus: OrderStatus): OrderResponseDto {
        val order = orderRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Order not found with UUID: $uuid")
        
        // Validate status transition
        validateStatusTransition(order.status, newStatus)
        
        val updatedOrder = order.copy(status = newStatus)
        val savedOrder = orderRepository.save(updatedOrder)
        return mapToOrderResponseDto(savedOrder, savedOrder.items)
    }
    
    /**
     * Cancel an order
     */
    fun cancelOrder(uuid: String): OrderResponseDto {
        val order = orderRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Order not found with UUID: $uuid")
        
        if (order.status == OrderStatus.DELIVERED) {
            throw IllegalStateException("Cannot cancel a delivered order")
        }
        
        if (order.status == OrderStatus.CANCELLED) {
            throw IllegalStateException("Order is already cancelled")
        }
        
        val cancelledOrder = order.copy(status = OrderStatus.CANCELLED)
        val savedOrder = orderRepository.save(cancelledOrder)
        return mapToOrderResponseDto(savedOrder, savedOrder.items)
    }
    
    /**
     * Delete an order (hard delete)
     */
    fun deleteOrder(uuid: String) {
        val order = orderRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Order not found with UUID: $uuid")
        
        // Only allow deletion of pending or cancelled orders
        if (order.status !in listOf(OrderStatus.PENDING, OrderStatus.CANCELLED)) {
            throw IllegalStateException("Can only delete pending or cancelled orders")
        }
        
        orderRepository.delete(order)
    }
    
    /**
     * Get total sales amount by status
     */
    @Transactional(readOnly = true)
    fun getTotalSalesByStatus(status: OrderStatus): BigDecimal {
        return orderRepository.sumTotalAmountByStatus(status) ?: BigDecimal.ZERO
    }
    
    /**
     * Check if order exists
     */
    @Transactional(readOnly = true)
    fun existsByUuid(uuid: String): Boolean {
        return orderRepository.findByUuid(uuid) != null
    }
    
    private fun validateStatusTransition(currentStatus: OrderStatus, newStatus: OrderStatus) {
        val validTransitions = mapOf(
            OrderStatus.PENDING to listOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED to listOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING to listOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED to listOf(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED to emptyList(),
            OrderStatus.CANCELLED to emptyList()
        )
        
        if (newStatus !in (validTransitions[currentStatus] ?: emptyList())) {
            throw IllegalArgumentException("Invalid status transition from $currentStatus to $newStatus")
        }
    }
    
    private fun calculateItemTotal(unitPrice: BigDecimal, quantity: Int, discount: BigDecimal?): BigDecimal {
        val subtotal = unitPrice.multiply(BigDecimal(quantity))
        return discount?.let { subtotal.subtract(it) } ?: subtotal
    }
    
    private fun mapToOrderResponseDto(order: Order, items: List<OrderItem>): OrderResponseDto {
        return OrderResponseDto(
            uuid = order.uuid,
            status = order.status,
            customerName = order.customerName,
            customerEmail = order.customerEmail,
            customerPhone = order.customerPhone,
            notes = order.notes,
            expectedDeliveryDate = order.expectedDeliveryDate,
            totalAmount = order.totalAmount,
            createdAt = order.createdAt,
            items = items.map { item ->
                OrderItemResponseDto(
                    uuid = item.uuid,
                    productUuid = item.product.uuid,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    discount = item.discount,
                    totalPrice = item.totalPrice
                )
            }
        )
    }
}
