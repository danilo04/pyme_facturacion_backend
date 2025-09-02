package com.walkyriasystems.pyme.facturacion.repository

import com.walkyriasystems.pyme.facturacion.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    
    fun findByUuid(uuid: String): Product?
    
    fun findByName(name: String): Product?
    
    fun findByCategory(category: String): List<Product>
    
    fun findByActiveTrue(): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity > 0")
    fun findAvailableProducts(): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    fun searchProducts(searchTerm: String): List<Product>
}
