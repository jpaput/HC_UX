package com.heetch.technicaltest.features.adapter

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heetch.technicaltest.R
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.util.RxPicasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.driver_itemview.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class DriverAdapter(val context: Context)
    : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    val locationManager = LocationManager(context)

    var driversData: MutableList<DriverModel> = arrayListOf();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.driver_itemview, parent, false))
    }

    override fun getItemCount(): Int {
        return driversData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = driversData.get(position)

        holder.driverNameTextview.text = driver.completeName
        holder.driverDistanceTextview.text = context.resources.getString(R.string.driver_away_from_you, driver.distance)

        Observable.just(
            locationManager.retrieveSnapshot(driver.coordinates.latitude, driver.coordinates.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(5, TimeUnit.SECONDS)
                .subscribe(
                    { data ->
                    holder.mapViewImage.setImageBitmap(data)
                    },
                    { error ->
                        println("Error: $error")
                    }
                )
        )

        RxPicasso()
            .loadImage("http://hiring.heetch.com/" + driver.image)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(5, TimeUnit.SECONDS)
            .subscribe(
                { data ->
                    holder.driverPicture.setImageBitmap(data)
                },
                { error ->
                    println("Error: $error")
                }
            )
    }

    public fun updateData(freshData : List<DriverModel>) {

        //Add if any item from fresh Data exist in list
        freshData.forEach {
            if (!driversData.any { driver -> driver.id == it.id }) {
                driversData.add(it)
                val position = driversData.indexOf(it)
                notifyItemInserted(position)
            }
        }

        //delete if item doesn't exist in fresh data anymore
        driversData.forEach {
            if (!freshData.any { driver -> driver.id == it.id }) {
                val position = driversData.indexOf(it)
                driversData.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        freshData.forEach {
            val oldData = driversData.find { driver -> driver.id == it.id }

            val oldposition = driversData.indexOf(oldData)
            val newPosition = freshData.indexOf(it)

            if(oldposition  != newPosition){
                //Change position and notify it
                driversData.removeAt(oldposition)
                driversData.add(newPosition, it)

                notifyItemMoved(oldposition, newPosition)
            }else{
                //Update distance and notify
                driversData.get(newPosition).distance = it.distance
                notifyItemChanged(oldposition)
            }
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val driverNameTextview = itemView.driver_name_tv
        val driverPicture = itemView.driver_picture
        val mapViewImage = itemView.driver_map
        val driverDistanceTextview = itemView.distance_tv
    }
}
