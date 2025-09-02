package com.walkyriasystems.pyme.facturacion.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "order_items",
    indexes = [
        Index(name = "idx_order_item_uuid", columnList = "uuid", unique = true),
        Index(name = "idx_order_item_order_id", columnList = "order_id"),
        Index(name = "idx_order_item_product_id", columnList = "product_id")
    ]
)
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val uuid: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,
    
    @Column(precision = 10, scale = 2)
    val discount: BigDecimal? = null,
    
    @Column(precision = 10, scale = 2)
    val totalPrice: BigDecimal
) {
    /**
     * Calculate the total price for this order item.
     * Total = (unitPrice * quantity) - discount
     */
    fun calculateTotalPrice(): BigDecimal {
        val subtotal = unitPrice.multiply(BigDecimal(quantity))
        return discount?.let { subtotal.subtract(it) } ?: subtotal
    }
}
