package com.example.data.database


import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5433/futo-his",
        driver = "org.postgresql.Driver",
        user = "postgres",
        //store in environment
        password = "12345678"
    )
    //setup database tables
    createTableIfNotExists(EmrUsers)
    createTableIfNotExists(EmrDoctors)
    createTableIfNotExists(EmrPatients)
    createTableIfNotExists(EmrPatientsGuardians)
    createTableIfNotExists(EmrPatientsKins)
    createTableIfNotExists(EmrPatientMedicalHistory)
    createTableIfNotExists(EmrAppointments)
    createTableIfNotExists(LisUsers)
    createTableIfNotExists(LabCategories)
    createTableIfNotExists(LabParameters)
    createTableIfNotExists(LabParameterUnits)
    createTableIfNotExists(LabParameterComments)
    createTableIfNotExists(LabSampleTable)
    createTableIfNotExists(LabTests)
    createTableIfNotExists(LabAttendants)
    createTableIfNotExists(Requisitions)
    createTableIfNotExists(LabResults)
    createTableIfNotExists(BioHazardIncidents)
    createTableIfNotExists(LabOrders)



}
