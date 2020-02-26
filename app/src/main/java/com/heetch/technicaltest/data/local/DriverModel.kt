package com.heetch.technicaltest.data.local

data class DriverModel(val id: Int,
                       val completeName: String,
                       val image: String,
                       var distance: Float
): Comparable<DriverModel> {

    override fun compareTo(other: DriverModel): Int  = this.distance.compareTo(other.distance)

}
