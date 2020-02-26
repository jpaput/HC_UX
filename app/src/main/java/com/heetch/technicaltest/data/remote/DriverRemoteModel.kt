package com.heetch.technicaltest.data.remote

data class DriverRemoteModel(val id: Int,
                             val firstname: String,
                             val lastname: String,
                             val image: String,
                             val coordinates: Coordinates
)  {

    public fun getFullName() : String {
        return firstname + " " + lastname.get(0).toUpperCase() + "."
    }

    data class Coordinates(val latitude: Double,
                           val longitude: Double)

}