package com.me.fyp

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.me.fyp.databinding.ActivityClassificationHistoryBinding

class ClassificationActivityHistory:BaseActivity() {
    private lateinit var activityClassificationHistoryBinding: ActivityClassificationHistoryBinding
    lateinit var firestore: FirebaseFirestore
    private lateinit var classifyList: ArrayList<ClassifyHistoryData>
    private lateinit var mAdapter: ClassificationHistoryAdapter

    private var authProfile = FirebaseAuth.getInstance()
    private var firebaseUser = authProfile.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityClassificationHistoryBinding = ActivityClassificationHistoryBinding.inflate(layoutInflater)
        setContentView(activityClassificationHistoryBinding.root)
        allocatedActivityTitle("Classification History")

        classifyList = ArrayList()
        mAdapter = ClassificationHistoryAdapter(this, classifyList)
        activityClassificationHistoryBinding.classificationRecyclerView.layoutManager = LinearLayoutManager(this)
        activityClassificationHistoryBinding.classificationRecyclerView.setHasFixedSize(true)
        activityClassificationHistoryBinding.classificationRecyclerView.adapter = mAdapter

        val userUID = firebaseUser?.uid
        if (userUID != null) {
            getClassifyData(userUID)
        }
    }

    private fun getClassifyData(uid: String) {
        firestore = FirebaseFirestore.getInstance()

        val storageRef = firestore.collection("Captured")
                .document(uid)
                .collection("Classification")
                .orderBy("datetime", com.google.firebase.firestore.Query.Direction.DESCENDING)

        storageRef.get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Clear the current list
                        classifyList.clear()

                        // Iterate through each image document in the 'Classification' collection
                        for (document in querySnapshot.documents) {
                            // Retrieve the image data fields
                            val imageURL = document.getString("imageUrl")
                            val label = document.getString("label")
                            val confidence = document.getString("confidence")
                            val datetime = document.getString("datetime")

                            // Create a data model (assuming you have an ImageData class)
                            val classifyHistoryData = ClassifyHistoryData(imageURL, label, confidence, datetime)

                            // Add the image data to the list
                            classifyList.add(classifyHistoryData)
                        }

                        // Notify the adapter to update the RecyclerView
                        activityClassificationHistoryBinding.classificationRecyclerView.adapter = mAdapter
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this@ClassificationActivityHistory, error.message, Toast.LENGTH_SHORT).show()
                }
    }
}