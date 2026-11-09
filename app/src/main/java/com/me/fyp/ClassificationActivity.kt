package com.me.fyp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.me.fyp.Constants.CLASS_LABELS_PATH
import com.me.fyp.Constants.CLASS_MODEL_PATH
import com.me.fyp.databinding.ActivityClassificationBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ClassificationActivity : BaseActivity(), Detector.DetectorListener {
    //TODO add progress dialog and check whether base activity successfully implemented
    private lateinit var binding: ActivityClassificationBinding
    private val isFrontCamera = false

    private lateinit var imageCapture: ImageCapture

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector

    private lateinit var cameraExecutor: ExecutorService

    private var authProfile = FirebaseAuth.getInstance()
    private var firebaseUser = authProfile.currentUser
    private var storageRef = FirebaseStorage.getInstance().reference

    val date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        allocatedActivityTitle("Classification")

        detector = Detector(baseContext, CLASS_MODEL_PATH, CLASS_LABELS_PATH, this)
        detector.setup()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ), ClassificationActivity.REQUEST_CODE_PERMISSIONS)
        }

        binding.classifyScreenshotButton.setOnClickListener{
            val userUID = firebaseUser?.uid
            val overlayView = binding.overlay
            val label = overlayView?.let { ov -> ov.results.maxByOrNull { it.cnf }?.clsName } ?: "Unknown"
            val confidenceScore = overlayView?.let { ov -> ov.results.maxByOrNull { it.cnf }?.percentage.toString() } ?: "0"

            // Capture the image
            val photoFile = File(externalMediaDirs.firstOrNull(), "camera_capture_${System.currentTimeMillis()}.jpg")

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            // Get the captured image as a bitmap
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                            // Call takeScreenshot and pass the captured bitmap
                            val finalScreenshot = takeScreenshot(bitmap)

                            // Now you can save the finalScreenshot or upload it
                            if (userUID != null) {
                                captureAndUploadScreenshot(finalScreenshot, userUID, label, confidenceScore)
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                        }
                    })
        }
    }

    private fun captureAndUploadScreenshot(screenshotBitmap: Bitmap, userUID: String, label: String, confidence: String) {
        // Save image to device gallery
        saveImageToGallery(screenshotBitmap)

        // Save image to Firebase and Firestore
        val file = saveBitmapToFile(screenshotBitmap, this)
        val format = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val datetime = format.format(date)

        uploadFileToFirebaseStorage(file, userUID) { downloadUrl ->
            if (downloadUrl != null) {
                saveImageToFirestore(userUID, downloadUrl, label, confidence, datetime, this)
                Toast.makeText(this, "Screenshot uploaded and metadata saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.png"
        val fos: OutputStream?

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 and above
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp") // Specify the folder inside the Pictures directory
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            fos = imageUri?.let { resolver.openOutputStream(it) }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            fos?.flush()
            fos?.close()

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            imageUri?.let { resolver.update(it, contentValues, null, null) }

            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()

        } else {
            // For devices with Android version < Q, save to external storage directory manually
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MyApp"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdirs() // Create directory if it does not exist
            }

            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos?.flush()
            fos?.close()

            // Refresh the media gallery for the new image
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(image)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)

            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeScreenshot(cameraBitmap: Bitmap): Bitmap {

        // Define the desired width and height for resizing
        val targetWidth = binding.viewFinder.width
        val targetHeight = binding.viewFinder.height

        // Rotate first
        val rotateCameraBitmap = rotateImage(cameraBitmap, 90f)

        // Resize the camera bitmap
        val resizedCameraBitmap = Bitmap.createScaledBitmap(rotateCameraBitmap, targetWidth, targetHeight, true)

        // Create a bitmap for the overlay
        val overlayBitmap = Bitmap.createBitmap(binding.overlay.width, binding.overlay.height, Bitmap.Config.ARGB_8888)

        // Draw the overlay on the canvas
        val overlayCanvas = Canvas(overlayBitmap)
        binding.overlay.draw(overlayCanvas)

        // Combine both resized camera preview and overlay bitmaps into a new canvas
        val finalBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(finalBitmap)

        // Draw the resized camera bitmap first
        finalCanvas.drawBitmap(resizedCameraBitmap, 0f, 0f, null)

        // Then draw the overlay on top
        finalCanvas.drawBitmap(overlayBitmap, 0f, 0f, null)

        return finalBitmap
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }



    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): File{

        val formatFileDate = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val fileDatetime = formatFileDate.format(date)
        val file = File(context.cacheDir, "classification_${fileDatetime}.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        return file
    }

    private fun uploadFileToFirebaseStorage(file:File, userUID:String, onComplete: (String?)-> Unit){
        val fileRef = storageRef.child("classification/$userUID/${file.name}")
        val uploadTask = fileRef.putFile(Uri.fromFile(file))

        uploadTask.continueWithTask{task->
            if(!task.isSuccessful){
                task.exception?.let { throw it }
            }

            fileRef.downloadUrl
        }.addOnCompleteListener{task->
            if(task.isSuccessful){
                val downloadUrl = task.result
                onComplete(downloadUrl.toString())
            }else{
                onComplete(null)
            }
        }
    }

    private fun saveImageToFirestore(userUID: String, imageUrl: String, label: String, confidence: String, datetime: String, context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        val classifyCollection = firestore.collection("Captured").document(userUID).collection("Classification")

        val imageData = hashMapOf(
                "imageUrl" to imageUrl,
                "label" to label,
                "confidence" to confidence,
                "datetime" to datetime
        )

        // Create a unique document ID using the format "image_datetime"
        val documentId = "image_$datetime"

        classifyCollection.document(documentId).set(imageData)
                .addOnSuccessListener {
                    // Successfully saved metadata
                    Toast.makeText(context, "Metadata successfully saved for image: $documentId", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(context, "Error saving metadata: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error saving metadata: ", e)
                }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

        // ImageCapture use case
        imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build()

        preview =  Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build()

        imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                    Bitmap.createBitmap(
                            imageProxy.width,
                            imageProxy.height,
                            Bitmap.Config.ARGB_8888
                    )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

                if (isFrontCamera) {
                    postScale(
                            -1f,
                            1f,
                            imageProxy.width.toFloat(),
                            imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                    bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                    matrix, true
            )

            detector.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer,
                    imageCapture
            )

            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
                Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                defineResults(boundingBoxes)
                invalidate()
            }
        }
    }
}