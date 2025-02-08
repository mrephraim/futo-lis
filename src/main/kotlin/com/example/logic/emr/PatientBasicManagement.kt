package com.example.logic.emr

import com.example.data.models.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UpdatePatientSession(
    val errors: MutableList<String> = mutableListOf(),
    val updateStatus: String = ""
)
@Serializable
data class AddPatientSessionData(
    val errors: MutableList<String> = mutableListOf(),
)

fun Route.patientBasicManagement(){
    get("/emr/add-patient") {
        val session = call.sessions.get<EmrLoginSessionData>()
        val userId = session?.userId // Assuming userId is part of your SessionData

        if (userId == null) {
            // Redirect to log in if userId is not present
            call.respondRedirect("/emr/login")
        } else {
            // Get errors from session if any
            val addPatientSessionData = call.sessions.get<AddPatientSessionData>()
            val errors = addPatientSessionData?.errors ?: emptyList()

            // Clear errors after retrieval
            call.sessions.set(AddPatientSessionData()) // Resetting the errors

            // Pass errors to the view
            call.respond(ThymeleafContent("emr/add-patient", mapOf("errors" to errors)))
        }
    }

    post("/emr/add-patient") {
        val parameters = call.receiveParameters()
        val errors = mutableListOf<String>()

        // Sanitize and validate inputs
        val regNo = parameters["regNo"]?.trim()?.takeIf {
            it.length == 11 && it.all { char -> char.isDigit() }
        } ?: run {
            errors.add("Invalid registration number, must be exactly 11 digits.")
            null
        }

        if (regNo != null && doesPatientExist(regNo)) {
            errors.add("Patient already exists, please use the edit-info feature on the dashboard instead.")
        }

        val firstName = parameters["firstName"]?.trim()?.takeIf { it.matches(Regex("[A-Za-z]+")) } ?: run {
            errors.add("Valid first name is required.")
            null
        }

        val surName = parameters["surName"]?.trim()?.takeIf { it.matches(Regex("[A-Za-z]+")) } ?: run {
            errors.add("Valid surname is required.")
            null
        }

        val middleName = parameters["middleName"]?.trim()?.takeIf { it.matches(Regex("[A-Za-z]*")) } // Optional field

        val school = parameters["schools"]?.takeIf { it.isNotBlank() } ?: run {
            errors.add("School selection is required.")
            null
        }

        val department = parameters["departments"]?.takeIf { it.isNotBlank() } ?: run {
            errors.add("Department selection is required.")
            null
        }
        val email = parameters["email"]?.takeIf { it.isNotBlank() } ?: run {
            errors.add("Email is required.")
            null
        }

        val phoneNo = parameters["phoneNo"]?.takeIf { it.matches(Regex("\\d{10,15}")) } ?: run {
            errors.add("A valid phone number is required.")
            null
        }

        // Date validation
        val dobDay = parameters["dob_day"]?.toIntOrNull()?.takeIf { it in 1..31 } ?: run {
            errors.add("Day of birth is required.")
            null
        }
        val dobMonth = parameters["dob_month"]?.toIntOrNull()?.takeIf { it in 1..12 } ?: run {
            errors.add("Month of birth is required.")
            null
        }
        val dobYear = parameters["dob_year"]?.toIntOrNull()?.takeIf { it in 1900..(LocalDate.now().year) } ?: run {
            errors.add("Year of birth is required.")
            null
        }
        val dob = if (dobDay != null && dobMonth != null && dobYear != null) {
            "$dobYear-$dobMonth-$dobDay"
        } else {
            null
        }

        val sex = parameters["sex"]?.takeIf { it in listOf("Male", "Female", "Other") } ?: run {
            errors.add("Sex selection is required.")
            null
        }

        val maritalStatus = parameters["maritalStatus"]?.takeIf { it in listOf("Single", "Married", "Divorced", "Widowed") } ?: run {
            errors.add("Marital status is required.")
            null
        }

        val hostelAddress = parameters["hostelAddress"]?.trim() // Optional field
        val homeTown = parameters["homeTown"]?.trim()?.takeIf { it.isNotBlank() } ?: run {
            errors.add("Home town is required.")
            null
        }

        val lga = parameters["lga"]?.trim()?.takeIf { it.isNotBlank() } ?: run {
            errors.add("Local Government Area is required.")
            null
        }

        val state = parameters["state"]?.trim()?.takeIf { it.isNotBlank() } ?: run {
            errors.add("State is required.")
            null
        }

        val country = parameters["country"]?.trim()?.takeIf { it.isNotBlank() } ?: run {
            errors.add("Country is required.")
            null
        }

        // If there are validation errors, save them to the session and redirect back to the form
        if (errors.isNotEmpty()) {
            call.sessions.set(AddPatientSessionData(errors = errors.toMutableList())) // Store errors in session
            call.respondRedirect("/emr/add-patient")
        } else {
            // Create the Patient instance with the correct types
            val patient = Patient(
                regNo = regNo ?: throw IllegalStateException("Registration number is required"), // This should not be null if validation passed
                firstName = firstName ?: throw IllegalStateException("First name is required"),
                surName = surName ?: throw IllegalStateException("Surname is required"),
                middleName = middleName,
                school = school ?: throw IllegalStateException("School is required"),
                department = department ?: throw IllegalStateException("Department is required"),
                phoneNo = phoneNo ?: throw IllegalStateException("Phone number is required"),
                email = email ?: throw IllegalStateException("Email is required"),
                dob = dob ?: throw IllegalStateException("Date of birth is required"),
                sex = sex ?: throw IllegalStateException("Sex is required"),
                maritalStatus = maritalStatus ?: throw IllegalStateException("Marital status is required"),
                hostelAddress = hostelAddress,
                homeTown = homeTown ?: throw IllegalStateException("Home town is required"),
                lga = lga ?: throw IllegalStateException("LGA is required"),
                state = state ?: throw IllegalStateException("State is required"),
                country = country ?: throw IllegalStateException("Country is required")
            )

            val success = addPatient(patient)
            if (success) {
                // Retrieve the existing session to clear errors
                val currentSessionData = call.sessions.get<AddPatientSessionData>()

                // If session data exists, clear the errors list
                if (currentSessionData != null) {
                    call.sessions.set(AddPatientSessionData(errors = mutableListOf())) // Clear errors by setting it to an empty list
                }

                call.respondRedirect("/emr/patient-added")
            } else {
                errors.add("Failed to add patient. Please try again.")
                call.sessions.set(AddPatientSessionData(errors = errors.toMutableList())) // Store errors in session
                call.respondRedirect("/emr/add-patient")
            }
        }
    }

    get("/emr/edit-patient/{regNo}") {
        val session = call.sessions.get<EmrLoginSessionData>()
        val userId = session?.userId // Assuming userId is part of your SessionData

        if (userId == null) {
            // Redirect to log in if userId is not present
            call.respondRedirect("/emr/login")
        } else {
            val regNo = call.parameters["regNo"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing regNo")

            // Clear any existing session data for PatientDetails
            call.sessions.clear("PatientDetails") // Clear specific session data

            // Fetch the complete patient data
            val completePatientData = fetchCompletePatientData(regNo)

            if (completePatientData != null) {
                // Store the fetched data in the session
                call.sessions.set("PatientDetails", completePatientData)
                call.respondRedirect("/emr/edit-patient") // Redirect to the edit page
            } else {
                call.respond(HttpStatusCode.NotFound, "Patient not found")
            }
        }
    }

    get("/emr/edit-patient") {
        // Retrieve the patient data and update session data from the session
        val sessionData = call.sessions.get<CompletePatientData>()
        val updateSession = call.sessions.get<UpdatePatientSession>() ?: UpdatePatientSession() // Use a default empty session if null

        // Log the session data to verify it is being passed correctly
        println("Patient data: $sessionData")
        println("Update session data: $updateSession")

        if (sessionData != null) {
            // Map both patientData and updateSession to the Thymeleaf template

            call.respond(
                ThymeleafContent(
                    "emr/edit-patient",
                    mapOf("patientData" to sessionData, "updateSession" to updateSession)
                )
            )
            call.sessions.clear<UpdatePatientSession>()


        } else {
            call.respond(HttpStatusCode.NotFound, "No patient data in session")
        }
    }

    post("/emr/edit-patient/{type}") {
        val type = call.parameters["type"]
        val session = call.sessions.get<UpdatePatientSession>() ?: UpdatePatientSession()

        // Get parameters from the form
        val params = call.receiveParameters()
        val errors = mutableListOf<String>()

        // Initialize the status message
        var updateStatus = ""

        when (type) {
            "patient" -> {
                val patient = Patient(
                    regNo = params["regNo"] ?: "",
                    surName = params["surName"] ?: "",
                    firstName = params["firstName"] ?: "",
                    middleName =  params["middleName"] ?: "",
                    phoneNo = params["phoneNo"] ?: "",
                    email = params["email"] ?: "",
                    dob = params["dob"] ?: "",
                    sex = params["sex"] ?: "",
                    maritalStatus = params["maritalStatus"] ?: "",
                    school = params["school"] ?: "",
                    department = params["department"] ?: "",
                    hostelAddress = params["hostelAddress"],
                    homeTown = params["homeTown"] ?: "",
                    lga = params["lga"] ?: "",
                    state = params["state"] ?: "",
                    country = params["country"] ?: ""
                )
                println("Received patient data: $patient")
                // Validate patient

                if (patient.surName.isEmpty()) {
                    errors.add("Surname cannot be empty.")
                }
                if (patient.firstName.isEmpty()) {
                    errors.add("First name cannot be empty.")
                }
                if (patient.phoneNo.isEmpty()) {
                    errors.add("Phone number cannot be empty.")
                }
                if (patient.email.isEmpty()) {
                    errors.add("Email cannot be empty.")
                }

                if (errors.isEmpty()) {
                    val updateResult = updatePatient(patient)
                    if (updateResult) {
                        updateStatus = "Patient ${patient.firstName} ${patient.surName} updated successfully."
                        println("Patient updated successfully")
                    } else {
                        errors.add("Error updating patient.")
                        updateStatus = "Error updating patient."
                        println("Validation failed, errors: $errors")
                    }
                } else {
                    updateStatus = "Validation failed. Please fix the errors."
                    println("Validation Error")
                }
            }
            "kin" -> {
                val kin = EmrPatientsKin(
                    regNo = params["regNo"] ?: "",
                    name = params["name"] ?: "",
                    phoneNo = params["phoneNo"] ?: "",
                    residentialAddress = params["residentialAddress"] ?: "",
                    occupation = params["occupation"] ?: "",
                    relationToStudent = params["relationToStudent"] ?: ""
                )

                // Validate kin
                if (kin.name.isEmpty()) {
                    errors.add("Kin name cannot be empty.")
                }
                if (kin.residentialAddress.isEmpty()) {
                    errors.add("Residential address cannot be empty.")
                }
                if (kin.occupation.isEmpty()) {
                    errors.add("Occupation cannot be empty.")
                }

                if (errors.isEmpty()) {
                    val updateResult = updateKin(kin)
                    if (updateResult) {
                        updateStatus = "Patient kin saved updated successfully."
                    } else {
                        errors.add("Error updating kin.")
                    }
                }
            }
            "guardian" -> {
                val guardian = EmrPatientsGuardian(
                    regNo = params["regNo"] ?: "",
                    name = params["name"] ?: "",
                    phoneNo = params["phoneNo"] ?: "",
                    officeAddress = params["officeAddress"] ?: "",
                    residentialAddress = params["residentialAddress"] ?: "",
                    occupation = params["occupation"] ?: ""
                )


                if (guardian.name.isEmpty()) {
                    errors.add("Guardian name cannot be empty.")
                }
                if (guardian.phoneNo.isEmpty()) {
                    errors.add("Phone number cannot be empty.")
                }

                if (errors.isEmpty()) {
                    val updateResult = updateGuardian(guardian)
                    if (updateResult) {
                        session.errors.add("Guardian ${guardian.name} updated successfully.")
                        updateStatus = "Patient guardian info saved successfully."
                    } else {
                        errors.add("Error updating guardian.")
                    }
                }
            }
            "medical-history" -> {
                val medicalHistory = EmrPatientMedicalHistoryData(
                    regNo = params["regNo"] ?: "",
                    familyHistory = params["familyHistory"] ?: "",
                    previousIllness = params["previousIllness"] ?: "",
                    height = params["height"]?.toDoubleOrNull() ?: 0.0,
                    weight = params["weight"]?.toDoubleOrNull() ?: 0.0,
                    visualAcuity = params["visualAcuity"] ?: "",
                    cardiovascularHealth = params["cardiovascularHealth"] ?: "",
                    hearingCondition = params["hearingCondition"] ?: "",
                    abdominalHealth = params["abdominalHealth"] ?: "",
                    respiratoryHealth = params["respiratoryHealth"] ?: "",
                    extremities = params["extremities"] ?: "",
                    psychiatricState = params["psychiatricState"] ?: "",
                    genotype = params["genotype"] ?: "",
                    bloodGroup = params["bloodGroup"] ?: "",
                    chestXray = params["chestXray"] ?: ""
                )

                // Validate medical history
                if (medicalHistory.height <= 0.0 || medicalHistory.weight <= 0.0) {
                    errors.add("Height and weight must be valid positive values.")
                }

                if (errors.isEmpty()) {
                    val updateResult = updateEmrPatientMedicalHistory(medicalHistory)
                    if (updateResult) {
                        session.errors.add("Medical history updated successfully.")
                        updateStatus = "Patient medical history saved successfully."
                    } else {
                        errors.add("Error updating medical history.")
                    }
                }
            }

            else -> {
                errors.add("Invalid update type.")
                updateStatus = "Invalid update type."
                println("Error: $updateStatus")
            }
        }

        // Set the session data
        val updatedSession = UpdatePatientSession(errors, updateStatus)
        call.sessions.set(updatedSession)  // Set the session without a name

        println("Debug: errors=${updatedSession.errors}, updateStatus=${updatedSession.updateStatus}")
        // Redirect to the Thymeleaf edit-patient page
        val patientRegNo = params["regNo"]
        call.respondRedirect("/emr/edit-patient/$patientRegNo")
    }




}


