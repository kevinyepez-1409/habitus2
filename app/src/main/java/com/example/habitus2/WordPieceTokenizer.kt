package com.example.habitus2

/**
 * Minimal WordPiece tokenizer for BERT vocab.
 * No hace manejo avanzado de puntuación, pero sirve para probar en móvil.
 */
class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    private val unkToken: String = "[UNK]"
) {

    private val unkId: Int = vocab[unkToken] ?: 100

    fun tokenizeToIds(
        text: String,
        maxLen: Int,
        clsId: Int,
        sepId: Int,
        padId: Int
    ): IntArray {
        val tokens = mutableListOf<Int>()

        // [CLS]
        tokens.add(clsId)

        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }

        for (word in words) {
            if (tokens.size >= maxLen - 1) break
            tokens.addAll(wordToPieces(word))
            if (tokens.size >= maxLen - 1) break
        }

        // [SEP]
        if (tokens.size < maxLen) {
            tokens.add(sepId)
        }

        // Padding
        val result = IntArray(maxLen) { padId }
        for (i in tokens.indices) {
            result[i] = tokens[i]
        }
        return result
    }

    private fun wordToPieces(word: String): List<Int> {
        val pieces = mutableListOf<Int>()
        var current = word

        // Si la palabra completa existe en el vocab, úsala
        if (vocab.containsKey(current)) {
            pieces.add(vocab[current]!!)
            return pieces
        }

        // descomposición MUY simple: si no está, usa [UNK]
        pieces.add(unkId)
        return pieces
    }
}
