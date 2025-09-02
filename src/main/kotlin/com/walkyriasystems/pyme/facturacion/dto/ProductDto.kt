package com.walkyriasystems.pyme.facturacion.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

/**
 * DTO for receiving product data when creating or updating a product
 */
data class ProductDto(
    @field:NotBlank(message = "Product name is required")
    @field:Size(max = 100, message = "Product name must not exceed 100 characters")
    val name: String,
    
    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,
    
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @field:Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    val price: BigDecimal,
    
    @field:Min(value = 0, message = "Stock quantity cannot be negative")
    val stockQuantity: Int = 0,
    
    @field:Size(max = 50, message = "Category must not exceed 50 characters")
    val category: String? = null,
    
    val active: Boolean = true
)
