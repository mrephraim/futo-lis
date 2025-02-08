package com.example.logic.emr

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*

/*
For here we do few things:
-routes for dashboard
-logic to get total indexed patients
 */

fun Route.emrDashboard(){
    get("/emr/dashboard/") {
        val session = call.sessions.get<EmrLoginSessionData>()
        val userId = session?.userId // Assuming userId is part of your EmrLoginSessionData
        call.sessions.clear<UpdatePatientSession>()

        if (userId == null) {
            // Redirect to log in if userId is not present
            call.respondRedirect("/emr/login")
        } else {
            // Prepare the model with userName if userId exists
            val userName: Map<String, Any> = mapOf("userName" to (session.userName ?: ""))
            call.respond(ThymeleafContent("emr/index", userName))
        }
    }
    get("/emr/logout") {
        // Clear the session data
        call.sessions.clear<EmrLoginSessionData>()

        // Redirect to the login page or homepage after logging out
        call.respondRedirect("/emr/login")
    }


}