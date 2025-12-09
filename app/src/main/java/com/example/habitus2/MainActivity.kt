package com.example.habitus2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var etInput: TextInputEditText
    private lateinit var btnPredict: Button
    private lateinit var tvResult: TextView

    private var emotionAnalyzer: EmotionAnalyzer? = null
    private var emotionsReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etInput = findViewById(R.id.etInputText)
        btnPredict = findViewById(R.id.btnPredict)
        tvResult = findViewById(R.id.tvResult)

        btnPredict.isEnabled = false
        tvResult.text = "Cargando modelo de emociones..."

        // Cargar modelo en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val analyzer = EmotionAnalyzer(applicationContext)
                analyzer.loadModels()
                emotionAnalyzer = analyzer
                emotionsReady = true

                withContext(Dispatchers.Main) {
                    tvResult.text =
                        "Modelo cargado ✅\nEscribe cómo te sientes y toca \"ANALIZAR SENTIMIENTOS\"."
                    btnPredict.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvResult.text = "Error cargando modelo: ${e.message}"
                    btnPredict.isEnabled = false
                }
            }
        }

        btnPredict.setOnClickListener {
            val text = etInput.text?.toString().orEmpty().trim()

            if (text.isBlank()) {
                Toast.makeText(
                    this,
                    "Escribe algo para analizar 🙂",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!emotionsReady || emotionAnalyzer == null) {
                Toast.makeText(
                    this,
                    "El modelo aún se está cargando, espera un momento…",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnPredict.isEnabled = false
            tvResult.text = "Analizando tu texto..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val report = emotionAnalyzer!!.analyze(text)

                    withContext(Dispatchers.Main) {
                        val sb = StringBuilder()
                        sb.appendLine("--- ANÁLISIS DE EMOCIONES ---")
                        sb.appendLine("Texto: $text")
                        sb.appendLine()
                        sb.appendLine("Emoción dominante: ${report.dominantLabel}")
                        sb.appendLine(
                            "Confianza: " +
                                    "%.1f".format(report.dominantProb * 100f) + "%"
                        )
                        sb.appendLine()
                        sb.appendLine("Perfil detallado:")

                        report.scores.forEach { score ->
                            val pct = "%.1f".format(score.probability * 100f)
                            sb.appendLine("• ${score.label}: $pct%")
                        }

                        tvResult.text = sb.toString()
                        btnPredict.isEnabled = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        tvResult.text = "Error durante el análisis: ${e.message}"
                        btnPredict.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        emotionAnalyzer?.close()
        emotionAnalyzer = null
    }
}
