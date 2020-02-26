package com.heetch.technicaltest.features.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heetch.technicaltest.R
import com.heetch.technicaltest.data.local.DriverModel
import com.heetch.technicaltest.util.RxPicasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.driver_itemview.view.*
import java.util.concurrent.TimeUnit

class DriverAdapter(val context: Context)
    : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

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

        driversData.forEach {
            val newData = freshData.find { driver -> driver.id == it.id }

            val position = driversData.indexOf(it)
            val newPosition = freshData.indexOf(newData)

            if(position  != newPosition){
                //Change position and notify it
                driversData.removeAt(position)
                newData?.let { it1 -> driversData.add(position, it1) }

                notifyItemMoved(position, newPosition)

            }else{
                //Update distance and notify
                it.distance = newData?.distance ?: 0f
                notifyItemChanged(position)
            }
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val driverNameTextview = itemView.driver_name_tv
        val driverPicture = itemView.driver_picture
        val driverDistanceTextview = itemView.distance_tv
    }
}
