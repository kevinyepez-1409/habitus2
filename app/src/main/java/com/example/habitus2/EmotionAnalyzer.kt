package com.example.habitus2

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp

data class EmotionScore(
    val label: String,
    val probability: Float
)

data class EmotionReport(
    val dominantLabel: String,
    val dominantProb: Float,
    val scores: List<EmotionScore>   // sorted high → low
)

/**
 * Emotion analysis with BERT GoEmotions (28 clases)
 * agrupadas en 7 emociones de Ekman.
 *
 * Archivos esperados en assets:
 *  - bert_28.onnx
 *  - vocab_bert      (un token por línea, estilo BERT)
 */
class EmotionAnalyzer(private val context: Context) {

    private var env: OrtEnvironment? = null
    private var bertSession: OrtSession? = null

    private lateinit var bertVocab: Map<String, Int>
    private lateinit var tokenizer: WordPieceTokenizer

    // Longitud de secuencia usada al exportar el modelo
    private val maxLen: Int = 64

    // IDs especiales (se corrigen con el vocab)
    private var clsId: Int = 101
    private var sepId: Int = 102
    private var padId: Int = 0
    private var unkId: Int = 100

    /**
     * Cargar modelo BERT y vocabulario.
     * Llamar desde un hilo de IO / background.
     */
    fun loadModels() {
        if (env != null && bertSession != null && ::tokenizer.isInitialized) {
            return
        }

        val assetManager = context.assets

        // 1. Entorno ONNX
        val environment = OrtEnvironment.getEnvironment()
        env = environment

        // 2. Copiar modelo a memoria interna y crear sesión
        val bertPath = assetFilePath(context, "bert_28.onnx")
        bertSession = environment.createSession(bertPath)

        // 3. Cargar vocabulario (nombre exacto: "vocab_bert")
        bertVocab = assetManager.open("vocab_bert.txt").bufferedReader().useLines { lines ->
            lines.mapIndexed { index, line ->
                line.trim() to index
            }.toMap()
        }

        // 4. IDs especiales
        clsId = bertVocab["[CLS]"] ?: clsId
        sepId = bertVocab["[SEP]"] ?: sepId
        padId = bertVocab["[PAD]"] ?: padId
        unkId = bertVocab["[UNK]"] ?: unkId

        // 5. Tokenizer WordPiece
        tokenizer = WordPieceTokenizer(bertVocab, unkToken = "[UNK]")
    }

    /**
     * Analizar texto → reporte de emociones (7 Ekman).
     * Llamar desde hilo en segundo plano.
     */
    fun analyze(text: String): EmotionReport {
        val environment = env ?: throw IllegalStateException("ONNX environment not initialized.")
        val bert = bertSession ?: throw IllegalStateException("BERT session not initialized.")

        if (!::tokenizer.isInitialized) {
            throw IllegalStateException("Emotion tokenizer is not initialized.")
        }

        // 1) Tokenizar
        val cleanText = text.trim().lowercase()
        val tokenIdsInt: IntArray = tokenizer.tokenizeToIds(
            text = cleanText,
            maxLen = maxLen,
            clsId = clsId,
            sepId = sepId,
            padId = padId
        )

        val tokenIds = LongArray(maxLen) { idx -> tokenIdsInt[idx].toLong() }
        val attentionMask = LongArray(maxLen) { idx ->
            if (tokenIds[idx] != padId.toLong()) 1L else 0L
        }
        val tokenTypeIds = LongArray(maxLen) { 0L } // una sola oración

        // 2) Tensores [1, maxLen]
        val inputIdsTensor = OnnxTensor.createTensor(environment, arrayOf(tokenIds))
        val attMaskTensor = OnnxTensor.createTensor(environment, arrayOf(attentionMask))
        val tokenTypeTensor = OnnxTensor.createTensor(environment, arrayOf(tokenTypeIds))

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attMaskTensor,
            "token_type_ids" to tokenTypeTensor
        )

        try {
            // 3) Ejecutar BERT → 28 logits
            val bertResult = bert.run(inputs)
            val bertLogits = (bertResult[0].value as Array<FloatArray>)[0]
            bertResult.close()

            // 4) Sigmoid multi-etiqueta
            val bertProbs = sigmoid(bertLogits)

            // 5) Agrupar a 7 emociones Ekman
            // (usa los índices que ya tenías; ajusta si tu modelo usa otro orden)
            val anger    = bertProbs.getOrElse(2)  { 0f }  // Anger
            val disgust  = bertProbs.getOrElse(11) { 0f }  // Disgust
            val fear     = bertProbs.getOrElse(14) { 0f }  // Fear
            val joy      = bertProbs.getOrElse(17) { 0f }  // Joy
            val neutral  = bertProbs.getOrElse(27) { 0f }  // Neutral
            val sadness  = bertProbs.getOrElse(25) { 0f }  // Sadness
            val surprise = bertProbs.getOrElse(26) { 0f }  // Surprise

            val labels = arrayOf(
                "Anger 😡",
                "Disgust 🤢",
                "Fear 😱",
                "Joy 😂",
                "Neutral 😐",
                "Sadness 😢",
                "Surprise 😲"
            )

            val ekmanProbs = floatArrayOf(
                anger, disgust, fear, joy, neutral, sadness, surprise
            )

            // 6) Ordenar de mayor a menor probabilidad
            val scores = labels.indices.map { i ->
                EmotionScore(
                    label = labels[i],
                    probability = ekmanProbs[i].coerceIn(0f, 1f)
                )
            }.sortedByDescending { it.probability }

            val dominant = scores.first()

            return EmotionReport(
                dominantLabel = dominant.label,
                dominantProb = dominant.probability,
                scores = scores
            )
        } finally {
            inputIdsTensor.close()
            attMaskTensor.close()
            tokenTypeTensor.close()
        }
    }

    fun close() {
        bertSession?.close()
        env?.close()
        bertSession = null
        env = null
    }

    // --- Helpers --- //

    private fun sigmoid(logits: FloatArray): FloatArray {
        return logits.map { x ->
            val ex = exp(x)
            ex / (1f + ex)
        }.toFloatArray()
    }

    /**
     * Copia un archivo de assets a memoria interna y devuelve su path.
     * (Solo copia la primera vez).
     */
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return file.absolutePath
    }
}
