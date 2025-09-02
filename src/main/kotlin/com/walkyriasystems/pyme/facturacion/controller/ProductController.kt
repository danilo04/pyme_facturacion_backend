package com.walkyriasystems.pyme.facturacion.controller

import com.walkyriasystems.pyme.facturacion.dto.ProductDto
import com.walkyriasystems.pyme.facturacion.dto.ProductResponseDto
import com.walkyriasystems.pyme.facturacion.service.ProductService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@Validated
class ProductController(
    private val productService: ProductService
) {
    
    /**
     * Get all products with pagination and sorting
     * GET /api/products?page=0&size=10&sort=name,asc
     */
    @GetMapping
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "name") sort: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<Page<ProductResponseDto>> {
        val sortDirection = if (direction.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))
        
        val products = productService.getAllProducts(pageable)
        return ResponseEntity.ok(products)
    }
    
    /**
     * Get all active products
     * GET /api/products/active
     */
    @GetMapping("/active")
    fun getActiveProducts(): ResponseEntity<List<ProductResponseDto>> {
        val products = productService.getActiveProducts()
        return ResponseEntity.ok(products)
    }
    
    /**
     * Get available products (active and in stock)
     * GET /api/products/available
     */
    @GetMapping("/available")
    fun getAvailableProducts(): ResponseEntity<List<ProductResponseDto>> {
        val products = productService.getAvailableProducts()
        return ResponseEntity.ok(products)
    }
    
    /**
     * Get product by UUID
     * GET /api/products/{uuid}
     */
    @GetMapping("/{uuid}")
    fun getProductByUuid(@PathVariable uuid: String): ResponseEntity<ProductResponseDto> {
        return try {
            val product = productService.getProductByUuid(uuid)
            ResponseEntity.ok(product)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Search products by name or description
     * GET /api/products/search?term=searchTerm
     */
    @GetMapping("/search")
    fun searchProducts(@RequestParam term: String): ResponseEntity<List<ProductResponseDto>> {
        val products = productService.searchProducts(term)
        return ResponseEntity.ok(products)
    }
    
    /**
     * Get products by category
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    fun getProductsByCategory(@PathVariable category: String): ResponseEntity<List<ProductResponseDto>> {
        val products = productService.getProductsByCategory(category)
        return ResponseEntity.ok(products)
    }
    
    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    fun createProduct(@Valid @RequestBody productDto: ProductDto): ResponseEntity<ProductResponseDto> {
        return try {
            val createdProduct = productService.createProduct(productDto)
            ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
    
    /**
     * Update an existing product
     * PUT /api/products/{uuid}
     */
    @PutMapping("/{uuid}")
    fun updateProduct(
        @PathVariable uuid: String,
        @Valid @RequestBody productDto: ProductDto
    ): ResponseEntity<ProductResponseDto> {
        return try {
            val updatedProduct = productService.updateProduct(uuid, productDto)
            ResponseEntity.ok(updatedProduct)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
    
    /**
     * Partially update a product (PATCH)
     * PATCH /api/products/{uuid}
     */
    @PatchMapping("/{uuid}")
    fun partialUpdateProduct(
        @PathVariable uuid: String,
        @RequestBody updates: Map<String, Any>
    ): ResponseEntity<ProductResponseDto> {
        return try {
            val existingProduct = productService.getProductByUuid(uuid)
            
            // Create a new ProductDto with updated fields
            val updatedDto = ProductDto(
                name = updates["name"] as? String ?: existingProduct.name,
                description = updates["description"] as? String ?: existingProduct.description,
                price = updates["price"]?.let { 
                    when (it) {
                        is Number -> it.toString().toBigDecimal()
                        is String -> it.toBigDecimal()
                        else -> existingProduct.price
                    }
                } ?: existingProduct.price,
                stockQuantity = updates["stockQuantity"] as? Int ?: existingProduct.stockQuantity,
                category = updates["category"] as? String ?: existingProduct.category,
                active = updates["active"] as? Boolean ?: existingProduct.active
            )
            
            val updatedProduct = productService.updateProduct(uuid, updatedDto)
            ResponseEntity.ok(updatedProduct)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
    
    /**
     * Update product stock quantity
     * PATCH /api/products/{uuid}/stock
     */
    @PatchMapping("/{uuid}/stock")
    fun updateStock(
        @PathVariable uuid: String,
        @RequestParam @Min(0, message = "Stock quantity cannot be negative") stock: Int
    ): ResponseEntity<ProductResponseDto> {
        return try {
            val updatedProduct = productService.updateStock(uuid, stock)
            ResponseEntity.ok(updatedProduct)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
    
    /**
     * Deactivate a product (soft delete)
     * PATCH /api/products/{uuid}/deactivate
     */
    @PatchMapping("/{uuid}/deactivate")
    fun deactivateProduct(@PathVariable uuid: String): ResponseEntity<ProductResponseDto> {
        return try {
            val deactivatedProduct = productService.deactivateProduct(uuid)
            ResponseEntity.ok(deactivatedProduct)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Permanently delete a product (hard delete)
     * DELETE /api/products/{uuid}
     */
    @DeleteMapping("/{uuid}")
    fun deleteProduct(@PathVariable uuid: String): ResponseEntity<Void> {
        return try {
            productService.deleteProduct(uuid)
            ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Check if product exists
     * HEAD /api/products/{uuid}
     */
    @RequestMapping(value = ["/{uuid}"], method = [RequestMethod.HEAD])
    fun checkProductExists(@PathVariable uuid: String): ResponseEntity<Void> {
        return if (productService.existsByUuid(uuid)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}