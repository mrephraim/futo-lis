package com.example.application.routing

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        // Serve static files from the "static" folder
        staticResources("/static", "static")
        get("/"){
            val indexPage = application.environment.classLoader.getResource("static/index.html")
            if (indexPage != null) {
                call.respondFile(File(indexPage.toURI()))
            }
        }
    }
}
