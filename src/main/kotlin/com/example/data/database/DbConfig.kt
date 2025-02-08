package com.example.data.database


import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://ephraim:9V9xYm64wWmOVu2XTkdF4e5kZmGdzMz8@dpg-cujqk03tq21c73e1apgg-a:5432/futohis",
        driver = "org.postgresql.Driver",
        user = "ephraim",
        //store in environment
        password = "9V9xYm64wWmOVu2XTkdF4e5kZmGdzMz8"
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
