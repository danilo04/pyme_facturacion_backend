package com.walkyriasystems.pyme.facturacion.repository

import com.walkyriasystems.pyme.facturacion.entity.Order
import com.walkyriasystems.pyme.facturacion.entity.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    
    fun findByUuid(uuid: String): Order?
    
    fun findByCustomerName(customerName: String): List<Order>
    
    fun findByStatus(status: OrderStatus): List<Order>
    
    fun findByCustomerEmail(customerEmail: String): List<Order>
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    fun sumTotalAmountByStatus(status: OrderStatus): BigDecimal?
}
