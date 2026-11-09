package com.me.fyp

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.me.fyp.databinding.ActivityDetectionHistoryBinding

class DetectionActivityHistory:BaseActivity() {
    private lateinit var activityDetectionHistoryBinding: ActivityDetectionHistoryBinding
    lateinit var firestore: FirebaseFirestore
    private lateinit var detectionList: ArrayList<DetectionHistoryData>
    private lateinit var mAdapter: DetectionHistoryAdapter

    private var authProfile = FirebaseAuth.getInstance()
    private var firebaseUser = authProfile.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityDetectionHistoryBinding = ActivityDetectionHistoryBinding.inflate(layoutInflater)
        setContentView(activityDetectionHistoryBinding.root)
        allocatedActivityTitle("Detection History")

        detectionList = ArrayList()
        mAdapter = DetectionHistoryAdapter(this, detectionList)
        activityDetectionHistoryBinding.detectionRecyclerView.layoutManager = LinearLayoutManager(this)
        activityDetectionHistoryBinding.detectionRecyclerView.setHasFixedSize(true)
        activityDetectionHistoryBinding.detectionRecyclerView.adapter = mAdapter

        val userUID = firebaseUser?.uid
        if (userUID != null) {
            getDetectionData(userUID)
        }
    }

    private fun getDetectionData(uid: String) {
        firestore = FirebaseFirestore.getInstance()

        val storageRef = firestore.collection("Captured")
                .document(uid)
                .collection("Detection")
                .orderBy("datetime", com.google.firebase.firestore.Query.Direction.DESCENDING)

        storageRef.get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Clear the current list
                        detectionList.clear()

                        // Iterate through each image document in the 'Detection' collection
                        for (document in querySnapshot.documents) {
                            // Retrieve the image data fields
                            val imageURL = document.getString("imageUrl")
                            val label = document.getString("label")
                            val confidence = document.getString("confidence")
                            val datetime = document.getString("datetime")

                            // Create a data model (assuming you have an ImageData class)
                            val detectionHistoryData = DetectionHistoryData(imageURL, label, confidence, datetime)

                            // Add the image data to the list
                            detectionList.add(detectionHistoryData)
                        }

                        // Notify the adapter to update the RecyclerView
                        activityDetectionHistoryBinding.detectionRecyclerView.adapter = mAdapter
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this@DetectionActivityHistory, error.message, Toast.LENGTH_SHORT).show()
                }
    }
}