package com.me.fyp

import android.os.Bundle
import com.me.fyp.databinding.ActivityDetailsBinding
import com.squareup.picasso.Picasso

class DetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        allocatedActivityTitle("Details")

        // Retrieve the data passed from the previous activity
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val label = intent.getStringExtra("label") ?: "Unknown"
        val confidence = intent.getStringExtra("confidence") ?: "N/A"
        val datetime = intent.getStringExtra("datetime") ?: "Unknown"

        // Populate the views with the data
        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.imageView)
        }
        binding.labelTextView.text = label
        binding.confidenceTextView.text = confidence
        binding.datetimeTextView.text = datetime

    }
}
