package com.heetch.technicaltest

import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.heetch.technicaltest.features.DriversListActivity
import com.heetch.technicaltest.service.DriverService
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ServiceUnitTest {


    @Test
    fun getDriver() {
        DriverService().refreshDriver(mockLocation()).subscribe(
        { data ->
            assert( data.size != 0)
        })

    }

    fun mockLocation() : Location {
        val latitude = 37.422
        val longitude = -122.084

        val realLocation = Location("provider")
        realLocation.latitude = latitude
        realLocation.longitude = longitude

        return realLocation
    }
}
