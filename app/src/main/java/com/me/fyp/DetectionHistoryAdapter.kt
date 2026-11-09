package com.me.fyp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.me.fyp.databinding.DetectionHistoryItemsBinding

class DetectionHistoryAdapter(
        var c:Context,
        var detectionList:ArrayList<DetectionHistoryData>
):RecyclerView.Adapter<DetectionHistoryAdapter.DetectionHistoryHolder>() {
    inner class DetectionHistoryHolder(var v:DetectionHistoryItemsBinding): RecyclerView.ViewHolder(v.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionHistoryHolder {
        val inflator = LayoutInflater.from(parent.context)
        val v = DataBindingUtil.inflate<DetectionHistoryItemsBinding>(
                inflator,R.layout.detection_history_items, parent, false)
        return DetectionHistoryHolder(v)
    }

    override fun getItemCount(): Int {
        return detectionList.size
    }

    override fun onBindViewHolder(holder: DetectionHistoryHolder, position: Int) {
        holder.v.isDetection = detectionList[position]
        var currentItem = detectionList[position]

        // Set onClickListener on the CardView or root layout
        holder.itemView.setOnClickListener {
            // Create an intent to navigate to the target activity
            val intent = Intent(c, DetailsActivity::class.java)

            // Pass the relevant data (imageUrl, label, etc.)
            intent.putExtra("imageUrl", currentItem.imageUrl)
            intent.putExtra("label", currentItem.label)
            intent.putExtra("confidence", currentItem.confidence)
            intent.putExtra("datetime", currentItem.datetime)

            // Start the new activity
            c.startActivity(intent)
        }
    }

}