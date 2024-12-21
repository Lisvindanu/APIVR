package com.virtualrealm.our.gameMarketPlaces.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory

@RestController
class RuntimeController {

    private val logger = LoggerFactory.getLogger(RuntimeController::class.java)

    @GetMapping("/runtime")
    fun getRuntime(): String {
        val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
        val uptime = runtimeMXBean.uptime
        logger.info("Endpoint /runtime accessed. Uptime: $uptime milliseconds")
        return "Application uptime: $uptime milliseconds"
    }
}
