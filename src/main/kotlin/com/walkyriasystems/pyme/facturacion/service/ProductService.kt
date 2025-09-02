package com.walkyriasystems.pyme.facturacion.service

import com.walkyriasystems.pyme.facturacion.dto.ProductDto
import com.walkyriasystems.pyme.facturacion.dto.ProductResponseDto
import com.walkyriasystems.pyme.facturacion.entity.Product
import com.walkyriasystems.pyme.facturacion.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {
    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    fun getAllProducts(pageable: Pageable): Page<ProductResponseDto> {
        return productRepository.findAll(pageable).map { mapToProductResponseDto(it) }
    }
    
    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    fun getActiveProducts(): List<ProductResponseDto> {
        return productRepository.findByActiveTrue().map { mapToProductResponseDto(it) }
    }
    
    /**
     * Get product by UUID
     */
    @Transactional(readOnly = true)
    fun getProductByUuid(uuid: String): ProductResponseDto {
        val product = productRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Product not found with UUID: $uuid")
        return mapToProductResponseDto(product)
    }
    
    /**
     * Search products by name or description
     */
    @Transactional(readOnly = true)
    fun searchProducts(searchTerm: String): List<ProductResponseDto> {
        return productRepository.searchProducts(searchTerm).map { mapToProductResponseDto(it) }
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    fun getProductsByCategory(category: String): List<ProductResponseDto> {
        return productRepository.findByCategory(category).map { mapToProductResponseDto(it) }
    }
    
    /**
     * Get available products (active and in stock)
     */
    @Transactional(readOnly = true)
    fun getAvailableProducts(): List<ProductResponseDto> {
        return productRepository.findAvailableProducts().map { mapToProductResponseDto(it) }
    }
    
    /**
     * Create a new product
     */
    fun createProduct(productDto: ProductDto): ProductResponseDto {
        // Check if product name already exists
        productRepository.findByName(productDto.name)?.let {
            throw IllegalArgumentException("Product with name '${productDto.name}' already exists")
        }
        
        val product = Product(
            uuid = UUID.randomUUID().toString(),
            name = productDto.name,
            description = productDto.description,
            price = productDto.price,
            stockQuantity = productDto.stockQuantity,
            category = productDto.category,
            active = productDto.active,
            createdAt = LocalDateTime.now()
        )
        
        val savedProduct = productRepository.save(product)
        return mapToProductResponseDto(savedProduct)
    }
    
    /**
     * Update an existing product
     */
    fun updateProduct(uuid: String, productDto: ProductDto): ProductResponseDto {
        val existingProduct = productRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Product not found with UUID: $uuid")
        
        // Check if the new name conflicts with another product
        if (productDto.name != existingProduct.name) {
            productRepository.findByName(productDto.name)?.let {
                if (it.uuid != uuid) {
                    throw IllegalArgumentException("Product with name '${productDto.name}' already exists")
                }
            }
        }
        
        val updatedProduct = existingProduct.copy(
            name = productDto.name,
            description = productDto.description,
            price = productDto.price,
            stockQuantity = productDto.stockQuantity,
            category = productDto.category,
            active = productDto.active,
            updatedAt = LocalDateTime.now()
        )
        
        val savedProduct = productRepository.save(updatedProduct)
        return mapToProductResponseDto(savedProduct)
    }
    
    /**
     * Soft delete a product (set active to false)
     */
    fun deactivateProduct(uuid: String): ProductResponseDto {
        val product = productRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Product not found with UUID: $uuid")
        
        val deactivatedProduct = product.copy(
            active = false,
            updatedAt = LocalDateTime.now()
        )
        
        val savedProduct = productRepository.save(deactivatedProduct)
        return mapToProductResponseDto(savedProduct)
    }
    
    /**
     * Hard delete a product
     */
    fun deleteProduct(uuid: String) {
        val product = productRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Product not found with UUID: $uuid")
        
        productRepository.delete(product)
    }
    
    /**
     * Update product stock quantity
     */
    fun updateStock(uuid: String, newStock: Int): ProductResponseDto {
        if (newStock < 0) {
            throw IllegalArgumentException("Stock quantity cannot be negative")
        }
        
        val product = productRepository.findByUuid(uuid)
            ?: throw NoSuchElementException("Product not found with UUID: $uuid")
        
        val updatedProduct = product.copy(
            stockQuantity = newStock,
            updatedAt = LocalDateTime.now()
        )
        
        val savedProduct = productRepository.save(updatedProduct)
        return mapToProductResponseDto(savedProduct)
    }
    
    /**
     * Check if product exists
     */
    @Transactional(readOnly = true)
    fun existsByUuid(uuid: String): Boolean {
        return productRepository.findByUuid(uuid) != null
    }
    
    private fun mapToProductResponseDto(product: Product): ProductResponseDto {
        return ProductResponseDto(
            uuid = product.uuid,
            name = product.name,
            description = product.description,
            price = product.price,
            stockQuantity = product.stockQuantity,
            category = product.category,
            active = product.active,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        )
    }
}
