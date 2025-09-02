package com.walkyriasystems.pyme.facturacion.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO for sending product data in response
 */
data class ProductResponseDto(
    val uuid: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val stockQuantity: Int,
    val category: String? = null,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null
)
