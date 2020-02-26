package com.heetch.technicaltest.data.local

import com.heetch.technicaltest.data.remote.DriverRemoteModel

data class DriverModel(val id: Int,
                       val completeName: String,
                       val image: String,
                       val coordinates: DriverRemoteModel.Coordinates,
                       var distance: Float
): Comparable<DriverModel> {

    override fun compareTo(other: DriverModel): Int  = this.distance.compareTo(other.distance)

}
