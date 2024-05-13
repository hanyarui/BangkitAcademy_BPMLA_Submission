package com.dicoding.submission.machinelearning.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.submission.machinelearning.data.PredictionDb
import com.dicoding.submission.machinelearning.databinding.ActivityResultBinding
import com.dicoding.submission.machinelearning.helper.ClassifierModel
import com.dicoding.submission.machinelearning.local.data.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.FileOutputStream

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    companion object {
        const val IMAGE_URI = "img_uri"
        const val TAG = "imagePicker"
        const val RESULT_TEXT = "result_text"
        const val REQUEST_HISTORY_UPDATE = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageUriString = intent.getStringExtra(IMAGE_URI)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            showImage(imageUri)

            val classifierModel = ClassifierModel(
                contextValue = this,
                classifierListenerValue = object : ClassifierModel.ClassifierListener {
                    override fun onError(errorMessage: String) {
                        Log.d(TAG, "Error: $errorMessage")
                    }

                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        results?.let { showResults(it) }
                    }
                }
            )
            classifierModel.classifyImage(imageUri)
        } else {
            Log.e(TAG, "No image URI provided")
            finish()
        }

        binding.btnSave.setOnClickListener {
            val imageUriString = intent.getStringExtra(IMAGE_URI)
            val result = binding.tvResult.text.toString()

            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                showToast("Data saved")
                savePrediction(imageUri, result)
            } else {
                showToast("No image URI provided")
                finish()
            }
        }
    }

    private fun showImage(uri: Uri) {
        Log.d(TAG, "Displaying image: $uri")
        binding.ivResultImage.setImageURI(uri)
    }

    private fun showResults(results: List<Classifications>) {
        val topResult = results[0]
        val label = topResult.categories[0].label
        val score = topResult.categories[0].score

        fun Float.formatToString(): String {
            return String.format("%.2f%%", this * 100)
        }
        binding.tvResult.text = "$label ${score.formatToString()}"
    }

    private fun savePrediction(imageUri: Uri, result: String) {
        if (result.isNotEmpty()) {
            val fileName = "cropped_image_${System.currentTimeMillis()}.jpg"
            val destinationUri = Uri.fromFile(File(cacheDir, fileName))
            contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(File(cacheDir, fileName)).use { output ->
                    input.copyTo(output)
                }
            }
            val prediction = Prediction(imagePath = destinationUri.toString(), result = result)
            GlobalScope.launch(Dispatchers.IO) {
                val database = PredictionDb.getDatabase(applicationContext)
                try {
                    database.predictionHistoryDao().insertPrediction(prediction)
                    Log.d(TAG, "Prediction saved successfully: $prediction")
                    val predictions = database.predictionHistoryDao().getAllPredictions()
                    Log.d(TAG, "All predictions after save: $predictions")
                    moveToHistory(destinationUri, result)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save prediction: $prediction", e)
                }
            }
        } else {
            Log.e(TAG, "Result is empty, cannot save prediction to database.")
        }
    }

    private fun moveToHistory(imageUri: Uri, result: String) {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.putExtra(RESULT_TEXT, result)
        intent.putExtra(IMAGE_URI, imageUri.toString())
        setResult(RESULT_OK, intent)
        startActivity(intent)
        finish()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}