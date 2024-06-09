package com.cwb.storyapp.ui.addStory

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cwb.storyapp.R
import com.cwb.storyapp.api.AddStoryResponse
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.data.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

class AddStoryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivPhoto: ImageView
    private lateinit var edDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var switchIncludeLocation: Switch
    private var photoFile: File? = null
    private var currentLocation: Location? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                ivPhoto.setImageBitmap(bitmap)
                photoFile = uriToFile(it)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 10000)
        .setMinUpdateIntervalMillis(5000)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation = locationResult.lastLocation
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        sessionManager = SessionManager(this)
        ivPhoto = findViewById(R.id.iv_add_photo)
        edDescription = findViewById(R.id.ed_add_description)
        btnAdd = findViewById(R.id.button_add)
        progressBar = findViewById(R.id.progress_bar)
        switchIncludeLocation = findViewById(R.id.switch_include_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ivPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        btnAdd.setOnClickListener {
            uploadStory()
        }

        switchIncludeLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermission()
            } else {
                currentLocation = null
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkLocationPermission()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                switchIncludeLocation.isChecked = false
            }
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun uriToFile(uri: Uri): File? {
        val file = File(cacheDir, contentResolver.getFileName(uri))
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }

    private fun uploadStory() {
        val description = edDescription.text.toString().trim()
        if (description.isEmpty() || photoFile == null) {
            Toast.makeText(this, "Description and photo are required", Toast.LENGTH_SHORT).show()
            return
        }

        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        val descriptionPart = MultipartBody.Part.createFormData("description", description)
        val photoPart = photoFile?.let {
            val requestFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", it.name, requestFile)
        }

        val latPart = currentLocation?.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonPart = currentLocation?.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        progressBar.visibility = ProgressBar.VISIBLE

        val client = ApiConfig.getApiService(token).addStory(descriptionPart, photoPart, latPart, lonPart)
        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                progressBar.visibility = ProgressBar.GONE
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@AddStoryActivity, "Story added successfully", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)  // Set result to OK
                    finish()
                } else {
                    Log.e("AddStoryActivity", "Failed to add story: ${response.errorBody()?.string()}")
                    Toast.makeText(this@AddStoryActivity, "Failed to add story", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Log.e("AddStoryActivity", "Error: ${t.message}")
                Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

fun ContentResolver.getFileName(uri: Uri): String {
    var name = ""
    val returnCursor = query(uri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}



