package com.me.fyp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.me.fyp.databinding.ClassificationHistoryItemsBinding


class ClassificationHistoryAdapter(
        var c:Context,
        var classifyList:ArrayList<ClassifyHistoryData>
):RecyclerView.Adapter<ClassificationHistoryAdapter.ClassificationHistoryHolder>() {
    inner class ClassificationHistoryHolder(var v:ClassificationHistoryItemsBinding): RecyclerView.ViewHolder(v.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassificationHistoryHolder {
        val inflator = LayoutInflater.from(parent.context)
        val v = DataBindingUtil.inflate<ClassificationHistoryItemsBinding>(
                inflator,R.layout.classification_history_items, parent, false)
        return ClassificationHistoryHolder(v)
    }

    override fun getItemCount(): Int {
        return classifyList.size
    }

    override fun onBindViewHolder(holder: ClassificationHistoryHolder, position: Int) {
        val currentItem = classifyList[position]
        holder.v.isClassification = currentItem

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