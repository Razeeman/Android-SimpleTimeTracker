package com.example.util.simpletimetracker.data_local.file

import java.io.IOException
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CsvCodecTest {

    @Test
    fun `writer and reader round-trip special characters`() {
        val fields = listOf(
            "ordinary",
            "contains, comma",
            "contains \"quotes\"",
            "line one\nline two",
            "Windows\r\nnewlines",
            "old Mac\rnewlines",
            "",
            "Привет 😀 你好",
        )

        val reader = CsvReader(StringReader(CsvWriter.writeRow(fields)))

        assertEquals(fields, reader.readRow())
        assertNull(reader.readRow())
    }

    @Test
    fun `reader separates logical records instead of physical lines`() {
        val first = listOf("Activity \"A\", west", "first line\nsecond line")
        val second = listOf("Activity B", "single line")
        val csv = CsvWriter.writeRow(first) + CsvWriter.writeRow(second)
        val reader = CsvReader(StringReader(csv))

        assertEquals(first, reader.readRow())
        assertEquals(second, reader.readRow())
        assertNull(reader.readRow())
    }

    @Test
    fun `reader accepts UTF-8 BOM and common record separators`() {
        val csv = "\uFEFFone,two\nthree,four\rfive,six\r\n"
        val reader = CsvReader(StringReader(csv))

        assertEquals(listOf("one", "two"), reader.readRow())
        assertEquals(listOf("three", "four"), reader.readRow())
        assertEquals(listOf("five", "six"), reader.readRow())
        assertNull(reader.readRow())
    }

    @Test(expected = IOException::class)
    fun `reader rejects unclosed quoted field`() {
        CsvReader(StringReader("one,\"two")).readRow()
    }
}
