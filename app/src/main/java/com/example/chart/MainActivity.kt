package com.example.chart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private lateinit var chartView: ChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chartView = findViewById(R.id.charView)
        val btn = findViewById<Button>(R.id.btn)
        chartView.visibility = View.INVISIBLE
        btn.setOnClickListener {
            chartView.visibility = View.VISIBLE
            setupRandomData()
        }
    }

    override fun onResume() {
        super.onResume()
        setupRandomData()
    }

    private fun setupRandomData(){
        val data = Array(5){Random.nextInt(0,20)}
        chartView.setData(data)
    }
}