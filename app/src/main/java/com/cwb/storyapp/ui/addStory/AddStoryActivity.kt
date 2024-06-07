package com.cwb.storyapp.ui.addStory

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cwb.storyapp.R
import com.cwb.storyapp.api.AddStoryResponse
import com.cwb.storyapp.api.ApiConfig
import com.cwb.storyapp.data.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivPhoto: ImageView
    private lateinit var edDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var progressBar: ProgressBar
    private var photoFile: File? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        sessionManager = SessionManager(this)
        ivPhoto = findViewById(R.id.iv_add_photo)
        edDescription = findViewById(R.id.ed_add_description)
        btnAdd = findViewById(R.id.button_add)
        progressBar = findViewById(R.id.progress_bar)

        ivPhoto.setOnClickListener {
            pickImageFromGallery()
        }

        btnAdd.setOnClickListener {
            uploadStory()
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

        progressBar.visibility = ProgressBar.VISIBLE

        val client = ApiConfig.getApiService(token).addStory(descriptionPart, photoPart)
        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                progressBar.visibility = ProgressBar.GONE
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@AddStoryActivity, "Story added successfully", Toast.LENGTH_SHORT).show()
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


