package com.walkyriasystems.pyme.facturacion.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/health")
class HealthController {
    
    @GetMapping
    fun health(): ResponseEntity<Map<String, Any>> {
        val healthInfo = mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now(),
            "service" to "Pyme Facturacion API",
            "version" to "1.0.0"
        )
        return ResponseEntity.ok(healthInfo)
    }
}
