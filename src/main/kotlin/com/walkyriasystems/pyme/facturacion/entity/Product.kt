package com.walkyriasystems.pyme.facturacion.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val uuid: String,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(length = 500)
    val description: String? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,
    
    @Column(nullable = false)
    val stockQuantity: Int = 0,
    
    @Column(length = 50)
    val category: String? = null,
    
    @Column(nullable = false)
    val active: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val updatedAt: LocalDateTime? = null
)
