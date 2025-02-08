package com.example.logic.lis

import com.example.data.database.LabOrders
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class LabOrderCount(val count: Int)
fun Route.lisDashboard(){
    get("/lis") {
        val session = call.sessions.get<LisLoginSessionData>()
        val userId = session?.userId // Assuming userId is part of your EmrLoginSessionData

        if (userId == null) {
            // Redirect to log in if userId is not present
            call.respondRedirect("/lis/login")
        } else {
            // Prepare the model with userName if userId exists
            val userName: Map<String, Any> = mapOf("userName" to (session.userName ?: ""))
            call.respond(ThymeleafContent("lis/index", userName))
        }
    }
    get("/lis/logout") {
        // Clear the session data
        call.sessions.clear<LisLoginSessionData>()

        // Redirect to the login page or homepage after logging out
        call.respondRedirect("/lis/login")
    }

    get("/lis/laborder-count") {
        val count = transaction { LabOrders
                .selectAll().where { LabOrders.status eq "pending" }
                .count()
        }
        call.respond(count)
    }


}