package com.walkyriasystems.pyme.facturacion.controller

import com.walkyriasystems.pyme.facturacion.dto.OrderDto
import com.walkyriasystems.pyme.facturacion.dto.OrderResponseDto
import com.walkyriasystems.pyme.facturacion.dto.OrderUpdateDto
import com.walkyriasystems.pyme.facturacion.entity.OrderStatus
import com.walkyriasystems.pyme.facturacion.service.OrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = ["*"])
@Validated
class OrderController(
    private val orderService: OrderService
) {

    /**
     * Get all orders with pagination and sorting
     * GET /api/orders?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    fun getAllOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sort: String,
        @RequestParam(defaultValue = "desc") direction: String
    ): ResponseEntity<Page<OrderResponseDto>> {
        val sortDirection = if (direction.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))
        
        val orders = orderService.getAllOrders(pageable)
        return ResponseEntity.ok(orders)
    }

    /**
     * Get order by UUID
     * GET /api/orders/{uuid}
     */
    @GetMapping("/{uuid}")
    fun getOrderByUuid(@PathVariable uuid: String): ResponseEntity<OrderResponseDto> {
        return try {
            val order = orderService.getOrderByUuid(uuid)
            ResponseEntity.ok(order)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get orders by customer name
     * GET /api/orders/customer/{customerName}
     */
    @GetMapping("/customer/{customerName}")
    fun getOrdersByCustomerName(@PathVariable customerName: String): ResponseEntity<List<OrderResponseDto>> {
        val orders = orderService.getOrdersByCustomerName(customerName)
        return ResponseEntity.ok(orders)
    }

    /**
     * Get orders by customer email
     * GET /api/orders/email/{customerEmail}
     */
    @GetMapping("/email/{customerEmail}")
    fun getOrdersByCustomerEmail(@PathVariable customerEmail: String): ResponseEntity<List<OrderResponseDto>> {
        val orders = orderService.getOrdersByCustomerEmail(customerEmail)
        return ResponseEntity.ok(orders)
    }

    /**
     * Get orders by status
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    fun getOrdersByStatus(@PathVariable status: OrderStatus): ResponseEntity<List<OrderResponseDto>> {
        val orders = orderService.getOrdersByStatus(status)
        return ResponseEntity.ok(orders)
    }

    /**
     * Get total sales by status
     * GET /api/orders/sales/total/{status}
     */
    @GetMapping("/sales/total/{status}")
    fun getTotalSalesByStatus(@PathVariable status: OrderStatus): ResponseEntity<Map<String, Any>> {
        val totalSales = orderService.getTotalSalesByStatus(status)
        val response = mapOf(
            "status" to status,
            "totalSales" to totalSales
        )
        return ResponseEntity.ok(response)
    }

    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    fun createOrder(@Valid @RequestBody orderDto: OrderDto): ResponseEntity<OrderResponseDto> {
        return try {
            val createdOrder = orderService.createOrder(orderDto)
            ResponseEntity.status(HttpStatus.CREATED).body(createdOrder)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Update an existing order
     * PUT /api/orders/{uuid}
     */
    @PutMapping("/{uuid}")
    fun updateOrder(
        @PathVariable uuid: String,
        @Valid @RequestBody orderUpdateDto: OrderUpdateDto
    ): ResponseEntity<OrderResponseDto> {
        return try {
            val updatedOrder = orderService.updateOrder(uuid, orderUpdateDto)
            ResponseEntity.ok(updatedOrder)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Update order status
     * PATCH /api/orders/{uuid}/status
     */
    @PatchMapping("/{uuid}/status")
    fun updateOrderStatus(
        @PathVariable uuid: String,
        @RequestParam status: OrderStatus
    ): ResponseEntity<OrderResponseDto> {
        return try {
            val updatedOrder = orderService.updateOrderStatus(uuid, status)
            ResponseEntity.ok(updatedOrder)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Cancel an order
     * PATCH /api/orders/{uuid}/cancel
     */
    @PatchMapping("/{uuid}/cancel")
    fun cancelOrder(@PathVariable uuid: String): ResponseEntity<OrderResponseDto> {
        return try {
            val cancelledOrder = orderService.cancelOrder(uuid)
            ResponseEntity.ok(cancelledOrder)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Delete an order (hard delete)
     * DELETE /api/orders/{uuid}
     */
    @DeleteMapping("/{uuid}")
    fun deleteOrder(@PathVariable uuid: String): ResponseEntity<Void> {
        return try {
            orderService.deleteOrder(uuid)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Check if order exists
     * HEAD /api/orders/{uuid}
     */
    @RequestMapping(value = ["/{uuid}"], method = [RequestMethod.HEAD])
    fun checkOrderExists(@PathVariable uuid: String): ResponseEntity<Void> {
        return if (orderService.existsByUuid(uuid)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}