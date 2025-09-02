package com.walkyriasystems.pyme.facturacion.dto

import com.walkyriasystems.pyme.facturacion.entity.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO for order response data
 */
data class OrderResponseDto(
    val uuid: String,
    val status: OrderStatus,
    val customerName: String,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val notes: String? = null,
    val expectedDeliveryDate: LocalDateTime? = null,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime,
    val items: List<OrderItemResponseDto>
)

/**
 * DTO for order item response data
 */
data class OrderItemResponseDto(
    val uuid: String,
    val productUuid: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal? = null,
    val totalPrice: BigDecimal
)
