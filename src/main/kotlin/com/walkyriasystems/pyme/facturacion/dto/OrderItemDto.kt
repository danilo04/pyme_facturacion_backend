package com.walkyriasystems.pyme.facturacion.dto

import java.math.BigDecimal

/**
 * DTO for receiving order item data when creating a new order
 */
data class OrderItemDto(
    val productUuid: String,
    val quantity: Int,
    val unitPrice: BigDecimal? = null, // Optional - can be fetched from product if not provided
    val discount: BigDecimal? = null
)
