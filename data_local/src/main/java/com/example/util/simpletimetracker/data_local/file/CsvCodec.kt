package com.example.util.simpletimetracker.data_local.file

import java.io.BufferedReader
import java.io.IOException
import java.io.PushbackReader
import java.io.Reader

internal object CsvWriter {

    fun writeRow(fields: List<String>): String {
        return fields.joinToString(separator = ",", postfix = "\r\n", transform = ::escape)
    }

    private fun escape(value: String): String {
        val requiresQuotes = value.any { char ->
            char == DELIMITER || char == QUOTE || char == '\r' || char == '\n'
        }
        if (!requiresQuotes) return value

        return buildString {
            append(QUOTE)
            value.forEach { char ->
                if (char == QUOTE) append(QUOTE)
                append(char)
            }
            append(QUOTE)
        }
    }

    private const val DELIMITER = ','
    private const val QUOTE = '"'
}

internal class CsvReader(reader: Reader) {

    private val input = PushbackReader(
        BufferedReader(reader),
        PUSHBACK_BUFFER_SIZE,
    ).apply {
        val firstCharacter = read()
        if (firstCharacter != END_OF_INPUT && firstCharacter != UTF8_BOM_CHARACTER) {
            // Optionally removes the UTF-8 BOM from the beginning of the CSV.
            unread(firstCharacter)
        }
    }

    fun readRow(): List<String>? {
        val result = mutableListOf<String>()
        // Currently read field from comma separated fields.
        val field = StringBuilder()
        // Distinguishes an empty row from reaching EOF before the row starts.
        var hasInput = false
        // Indicates that characters are being read from a quoted field.
        var insideQuotes = false
        // Allows only a delimiter or row ending after a quoted field closes.
        var afterClosingQuote = false

        while (true) {
            val value = input.read()
            if (value == END_OF_INPUT) {
                if (!hasInput && result.isEmpty() && field.isEmpty()) return null
                if (insideQuotes) throw IOException("Unclosed quoted CSV field")

                result += field.toString()
                return result
            }

            hasInput = true
            val char = value.toChar()

            // A quote here is either escaped by a second quote or closes the field;
            // all other characters are part of the field value.
            if (insideQuotes) {
                if (char == QUOTE) {
                    val next = input.read()
                    if (next == QUOTE.code) {
                        field.append(QUOTE)
                    } else {
                        insideQuotes = false
                        afterClosingQuote = true
                        if (next != END_OF_INPUT) input.unread(next)
                    }
                } else {
                    field.append(char)
                }
                continue
            }

            // After a quoted field closes, only a delimiter or row ending is valid.
            if (afterClosingQuote) {
                when (char) {
                    DELIMITER -> {
                        result += field.toString()
                        field.clear()
                        afterClosingQuote = false
                    }

                    '\n' -> {
                        result += field.toString()
                        return result
                    }

                    '\r' -> {
                        consumeOptionalLineFeed()
                        result += field.toString()
                        return result
                    }

                    else -> throw IOException(
                        "Unexpected character after closing CSV quote: $char",
                    )
                }
                continue
            }

            when (char) {
                QUOTE -> {
                    if (field.isNotEmpty()) {
                        throw IOException("Quote inside an unquoted CSV field")
                    }
                    insideQuotes = true
                }

                DELIMITER -> {
                    result += field.toString()
                    field.clear()
                }

                '\n' -> {
                    result += field.toString()
                    return result
                }

                '\r' -> {
                    consumeOptionalLineFeed()
                    result += field.toString()
                    return result
                }

                else -> field.append(char)
            }
        }
    }

    // Consumes the LF in a CRLF row ending, preserving any other character.
    private fun consumeOptionalLineFeed() {
        val next = input.read()
        if (next != END_OF_INPUT && next != '\n'.code) {
            input.unread(next)
        }
    }

    private companion object {
        const val PUSHBACK_BUFFER_SIZE = 1
        const val END_OF_INPUT = -1
        const val UTF8_BOM_CHARACTER = 0xFEFF
        const val DELIMITER = ','
        const val QUOTE = '"'
    }
}
