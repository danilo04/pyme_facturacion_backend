package com.walkyriasystems.pyme.facturacion.dto

import com.walkyriasystems.pyme.facturacion.entity.OrderStatus
import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * DTO for updating order data
 */
data class OrderUpdateDto(
    @field:NotBlank(message = "Customer name is required")
    @field:Size(max = 100, message = "Customer name must not exceed 100 characters")
    val customerName: String,
    
    @field:Email(message = "Invalid email format")
    @field:Size(max = 100, message = "Email must not exceed 100 characters")
    val customerEmail: String? = null,
    
    @field:Pattern(
        regexp = "^[+]?[\\d\\s\\-\\(\\)]+$",
        message = "Invalid phone number format"
    )
    @field:Size(max = 20, message = "Phone number must not exceed 20 characters")
    val customerPhone: String? = null,
    
    @field:Size(max = 500, message = "Notes must not exceed 500 characters")
    val notes: String? = null,
    
    @field:NotNull(message = "Status is required")
    val status: OrderStatus,
    
    val expectedDeliveryDate: LocalDateTime? = null
)
