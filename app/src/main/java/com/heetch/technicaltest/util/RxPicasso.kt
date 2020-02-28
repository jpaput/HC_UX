package com.heetch.technicaltest.util

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import io.reactivex.Observable

class RxPicasso : ImageDownloader {

    fun loadAvatarImage(url: String): Observable<Bitmap> {
        return Observable.fromCallable { Picasso.get().load(url).transform(CircleTransform()).get() }
    }

    override fun loadImage(url: String): Observable<Bitmap> {
        return Observable.fromCallable { Picasso.get().load(url).get() }
    }

}