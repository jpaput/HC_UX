package com.heetch.technicaltest.network

import com.heetch.technicaltest.data.remote.DriverRemoteModel
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.PUT

interface ApiInterface {

    @PUT("coordinates")
    fun getCoordinates(@Body coordinatesBody: CoordinatesBody): Observable<List<DriverRemoteModel>>

}