package com.dicoding.submission.machinelearning.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.submission.machinelearning.R
import com.dicoding.submission.machinelearning.adapter.PredictionAdapter
import com.dicoding.submission.machinelearning.data.PredictionDb
import com.dicoding.submission.machinelearning.local.data.Prediction
import com.dicoding.submission.machinelearning.ui.ResultActivity.Companion.REQUEST_HISTORY_UPDATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity(), PredictionAdapter.OnDeleteClickListener {

    private lateinit var predictionRecyclerView: RecyclerView
    private lateinit var predictionAdapter: PredictionAdapter
    private var predictionList: MutableList<Prediction> = mutableListOf()
    private lateinit var tvNotFound: TextView

    companion object{
        const val TAG = "history data"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        predictionRecyclerView = findViewById(R.id.rvHistory)
        tvNotFound = findViewById(R.id.tvEmpty)

        predictionRecyclerView = findViewById(R.id.rvHistory)
        tvNotFound = findViewById(R.id.tvEmpty)

        predictionAdapter = PredictionAdapter(predictionList)
        predictionAdapter.setOnDeleteClickListener(this)
        predictionRecyclerView.adapter = predictionAdapter
        predictionRecyclerView.layoutManager = LinearLayoutManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        GlobalScope.launch(Dispatchers.Main) {
            loadHistory()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_HISTORY_UPDATE && resultCode == RESULT_OK) {
            GlobalScope.launch(Dispatchers.Main) {
                loadHistory()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadHistory() {
        GlobalScope.launch(Dispatchers.Main) {
            val predictions = PredictionDb.getDatabase(this@HistoryActivity).predictionHistoryDao().getAllPredictions()
            predictionList.clear()
            predictionList.addAll(predictions)
            predictionAdapter.notifyDataSetChanged()
            showHistoryText()
        }
    }

    private fun showHistoryText() {
        if (predictionList.isEmpty()) {
            tvNotFound.visibility = View.VISIBLE
            predictionRecyclerView.visibility = View.GONE
        } else {
            tvNotFound.visibility = View.GONE
            predictionRecyclerView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDeleteClick(position: Int) {
        val prediction = predictionList[position]
        if (prediction.result.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                PredictionDb.getDatabase(this@HistoryActivity).predictionHistoryDao().deletePrediction(prediction)
            }
            predictionList.removeAt(position)
            predictionAdapter.notifyDataSetChanged()
            showHistoryText()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}