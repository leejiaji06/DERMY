package com.me.fyp

import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun getProgressDrawable(c:Context):CircularProgressDrawable{
    return CircularProgressDrawable(c).apply {
        strokeWidth = 6f
        centerRadius = 40f
        start()
    }
}

fun ImageView.loadImages(uri:String?, progressDrawable: CircularProgressDrawable){
    val option = RequestOptions()
            .placeholder(progressDrawable)
            .error(R.mipmap.ic_launcher)

    Glide.with(context)
            .setDefaultRequestOptions(option)
            .load(uri)
            .into(this)
}

@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView,url:String){
    view.loadImages(url, getProgressDrawable(view.context))
}