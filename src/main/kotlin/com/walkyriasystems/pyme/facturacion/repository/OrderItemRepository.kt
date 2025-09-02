package com.walkyriasystems.pyme.facturacion.repository

import com.walkyriasystems.pyme.facturacion.entity.Order
import com.walkyriasystems.pyme.facturacion.entity.OrderItem
import com.walkyriasystems.pyme.facturacion.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    
    fun findByUuid(uuid: String): OrderItem?
    
    fun findByOrder(order: Order): List<OrderItem>
    
    fun findByProduct(product: Product): List<OrderItem>
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.uuid = :orderUuid")
    fun findByOrderUuid(orderUuid: String): List<OrderItem>
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.uuid = :productUuid")
    fun findByProductUuid(productUuid: String): List<OrderItem>
}
