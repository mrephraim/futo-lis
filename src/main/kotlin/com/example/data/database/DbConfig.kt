package com.example.data.database


import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://dpg-cv969opu0jms73efqbq0-a:5432/futo_his_db",
        driver = "org.postgresql.Driver",
        user = "futo_his_db_user",
        password = "DwnjZrxuZ8Eq2bnA3qOuRheSwvEzMLf0"
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
