package com.ashome.core.servlet.controller

import com.ashome.tablet.gesture.model.AhGestureRecorder
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

internal open class InternalREST( url : String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {

    }

    @GetMapping("/showGesture")
    fun showGesture() {
        AhGestureRecorder.static.launch()
    }
}
