package com.example.test

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import retrofit2.Response
import android.widget.Toast
import android.content.pm.PackageManager
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var photoFile: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the custom Toolbar as the ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Disable default title
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set custom title
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.text = "FOODSNAP"

        // UI Elements
        val buttonCapture = findViewById<Button>(R.id.buttonCapture)
//        val buttonSend = findViewById<Button>(R.id.buttonSend)
//        val buttonInfo = findViewById<Button>(R.id.buttonInfo)
//        val imagePreview = findViewById<ImageView>(R.id.imagePreview)
//        val textInfo = findViewById<TextView>(R.id.textInfo)
        Log.d("ButtonClick", "Send to Begin button clicked")
        // Request Camera Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        }

        // Initialize CameraX
        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Capture Image Button
        buttonCapture.setOnClickListener {
            Log.d("ButtonClick", "Capture button clicked")

            // Step 1: Capture Photo
            capturePhoto { file ->
                photoFile = file

                // Step 2: Show ProgressBar
                val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
                loadingOverlay.visibility = View.VISIBLE // Show the overlay

                if (::photoFile.isInitialized && photoFile.exists()) {
                    Log.d("PhotoFile", "Photo file is initialized: ${photoFile.absolutePath}")
                    sendImageToServer(photoFile) { response ->
                        loadingOverlay.visibility = View.GONE // Hide the overlay
                        // Hide the progress bar when response is received

                        if (!response.isNullOrEmpty()) {
                            // Navigate to DisplayInfoActivity with response
                            val intent = Intent(this, DisplayInfoActivity::class.java)
                            intent.putExtra("server_response", response) // Pass the server response
                            intent.putExtra("image_path", photoFile.absolutePath) // Pass the image file path
                            startActivity(intent)

                            // Apply the fade-in and fade-out animation
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        } else {
                            Toast.makeText(this, "Server returned no response.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    loadingOverlay.visibility = View.GONE // Hide the overlay
                    // Hide the progress bar if no photo is available
                    Log.e("PhotoFile", "Photo file is not initialized or does not exist.")
                    Toast.makeText(this, "No image captured to send!", Toast.LENGTH_SHORT).show()
                }
            }
        }





//        // Capture Image Button
//        buttonCapture.setOnClickListener {
//            Log.d("ButtonClick", "Send to Capture button clicked")
//            capturePhoto { file ->
//                photoFile = file
//                imagePreview.setImageURI(Uri.fromFile(photoFile))
//                textInfo.text = getString(R.string.image_preview_description)
//            }
//        }
//
//        // Send Image Button
//        buttonSend.setOnClickListener {
//            if (::photoFile.isInitialized && photoFile.exists()) {
//                Log.d("PhotoFile", "Photo file is initialized: ${photoFile.absolutePath}")
//                sendImageToServer(photoFile) { response ->
//                    Log.d("Upload", "Response from server: $response")
//                    textInfo.text = getString(R.string.image_sent, response)
//                }
//                Toast.makeText(this, "Food is SNAPPED !", Toast.LENGTH_SHORT).show()
//            } else {
//                Log.e("PhotoFile", "Photo file is not initialized or does not exist.")
//                textInfo.text = getString(R.string.no_image_to_send)
//
//                // Show a temporary message using Toast
//                Toast.makeText(this, "No image captured to send!", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//
//        // Display Info Button
//        buttonInfo.setOnClickListener {
//            if (serverInfo != null) {
//                // Navigate to DisplayInfoActivity and pass the stored serverInfo
//                val intent = Intent(this, DisplayInfoActivity::class.java)
//                intent.putExtra("server_response", serverInfo) // Pass the stored response
//                startActivity(intent) // Start the new activity
//            } else {
//                // No response available
//                textInfo.text = getString(R.string.no_info_received)
//
//                // Show a Toast message
//                Toast.makeText(this, "Please send the picture or check the server availability", Toast.LENGTH_SHORT).show()
//            }
//        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Configure Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<androidx.camera.view.PreviewView>(R.id.viewFinder).surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto(onPhotoCaptured: (File) -> Unit) {
        val outputDirectory = getOutputDirectory()
        val file = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "TestApp").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    interface ApiService {
        @Multipart
        @POST("/upload")
        fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>
    }

//    private fun sendImageToServer(file: File, onResponseReceived: (String) -> Unit) {
//        Log.d("SendToServer", "sendImageToServer called with file: ${file.name}")
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://192.168.1.19:5000/") // Replace with your Flask server's IP
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        val apiService = retrofit.create(ApiService::class.java)
//        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
//        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)
//
//        apiService.uploadImage(body).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body()?.string() ?: "Empty response"
//                    Log.d("Upload", "Response from server: $responseBody")
//                    onResponseReceived(responseBody)
//                } else {
//                    Log.e("Upload", "Upload failed with response code: ${response.code()}")
//                    onResponseReceived("Error: ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e("Upload", "Upload failed: ${t.message}")
//            }
//        })
//    }

    private var serverInfo: String? = null  // To store the server's response

    private fun sendImageToServer(file: File, onResponseReceived: (String) -> Unit) {
        Log.d("SendToServer", "sendImageToServer called with file: ${file.name}")

        val retrofit = Retrofit.Builder()
//            .baseUrl("http://192.168.1.16:5000/") // P1012
//            .baseUrl("http://192.168.1.108:5000/") // IC_Design_Lab_5Ghz
            .baseUrl("http://192.168.252.136:5000/") // Wifi Free
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestBody)

        apiService.uploadImage(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: "Empty response"
                    Log.d("Upload", "Response from server: $responseBody")

                    // Parse and save response data
                    serverInfo = responseBody
                    onResponseReceived(responseBody)
                } else {
                    Log.e("Upload", "Upload failed with response code: ${response.code()}")
                    onResponseReceived("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Upload", "Upload failed: ${t.message}")
                onResponseReceived("Upload failed: ${t.message}")
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
