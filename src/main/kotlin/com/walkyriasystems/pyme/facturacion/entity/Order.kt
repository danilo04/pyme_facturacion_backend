package com.walkyriasystems.pyme.facturacion.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val uuid: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING,
    
    @Column
    val expectedDeliveryDate: LocalDateTime? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false, length = 100)
    val customerName: String,
    
    @Column(length = 100)
    val customerEmail: String? = null,
    
    @Column(length = 20)
    val customerPhone: String? = null,
    
    @Column(length = 500)
    val notes: String? = null,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: List<OrderItem> = mutableListOf()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
