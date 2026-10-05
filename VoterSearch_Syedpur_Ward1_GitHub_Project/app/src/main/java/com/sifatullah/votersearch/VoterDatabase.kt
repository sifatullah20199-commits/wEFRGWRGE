
package com.sifatullah.votersearch

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.util.Locale

data class Voter(
    val serial: String,
    val name: String,
    val voterNo: String,
    val father: String,
    val mother: String,
    val address: String,
    val ward: String,
    val gender: String
)

class VoterDatabase(context: Context) :
    SQLiteOpenHelper(context, "voters.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE voters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serial TEXT NOT NULL,
                name TEXT NOT NULL,
                voter_no TEXT NOT NULL,
                father TEXT NOT NULL,
                mother TEXT NOT NULL,
                address TEXT NOT NULL,
                ward TEXT NOT NULL,
                gender TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_name ON voters(name)")
        db.execSQL("CREATE INDEX idx_father ON voters(father)")
        db.execSQL("CREATE INDEX idx_mother ON voters(mother)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun count(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM voters", null).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    fun importAsset(assetName: String) {
        context.assets.open(assetName).use { replaceWithCsv(it) }
    }

    fun replaceWithCsv(input: InputStream) {
        val database = writableDatabase
        database.beginTransaction()
        try {
            database.delete("voters", null, null)
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                val rows = CsvParser(reader).rows()
                if (rows.isEmpty()) error("CSV ফাইলটি খালি।")

                val header = rows.first().map { normalizeHeader(it) }
                val index = mapHeaders(header)

                val required = listOf("name", "father", "mother", "address")
                val missing = required.filter { index[it] == null }
                if (missing.isNotEmpty()) {
                    error("প্রয়োজনীয় column পাওয়া যায়নি: ${missing.joinToString(", ")}")
                }

                for (row in rows.drop(1)) {
                    if (row.all { it.isBlank() }) continue
                    val values = ContentValues().apply {
                        put("serial", get(row, index["serial"]))
                        put("name", get(row, index["name"]))
                        put("voter_no", get(row, index["voter_no"]))
                        put("father", get(row, index["father"]))
                        put("mother", get(row, index["mother"]))
                        put("address", get(row, index["address"]))
                        put("ward", get(row, index["ward"]))
                        put("gender", get(row, index["gender"]))
                    }
                    if (values.getAsString("name").isNotBlank()) {
                        database.insertOrThrow("voters", null, values)
                    }
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun get(row: List<String>, idx: Int?): String =
        if (idx != null && idx >= 0 && idx < row.size) row[idx].trim() else ""

    private fun mapHeaders(h: List<String>): Map<String, Int?> = mapOf(
        "serial" to find(h, "serial", "সিরিয়াল", "ক্রমিক"),
        "name" to find(h, "name", "নাম"),
        "voter_no" to find(h, "voter no", "voterno", "voter number", "ভোটার নং", "ভোটার নম্বর"),
        "father" to find(h, "father's name", "fathers name", "father name", "বাবার নাম", "পিতার নাম"),
        "mother" to find(h, "mother's name", "mothers name", "mother name", "মায়ের নাম", "মাতার নাম"),
        "address" to find(h, "address", "ঠিকানা"),
        "ward" to find(h, "ward", "ওয়ার্ড", "ওয়ার্ড"),
        "gender" to find(h, "gender", "লিঙ্গ")
    )

    private fun find(h: List<String>, vararg candidates: String): Int? {
        candidates.forEach { c ->
            val idx = h.indexOf(normalizeHeader(c))
            if (idx >= 0) return idx
        }
        return null
    }

    private fun normalizeHeader(s: String): String =
        s.trim().lowercase(Locale.ROOT)
            .replace("\uFEFF", "")
            .replace("’", "'")
            .replace(Regex("\\s+"), " ")

    fun search(field: MainActivity.SearchField, query: String): List<Voter> {
        val column = when (field) {
            MainActivity.SearchField.NAME -> "name"
            MainActivity.SearchField.FATHER -> "father"
            MainActivity.SearchField.MOTHER -> "mother"
        }
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        // Lowercase comparison keeps the search forgiving for Latin text.
        // SQLite LIKE remains Unicode-safe for Bengali text.
        val sql = "SELECT serial,name,voter_no,father,mother,address,ward,gender FROM voters " +
                "WHERE $column LIKE ? COLLATE NOCASE ORDER BY CAST(serial AS INTEGER) LIMIT 500"
        val args = arrayOf("%$q%")
        return readableDatabase.rawQuery(sql, args).use { c ->
            val out = ArrayList<Voter>()
            while (c.moveToNext()) {
                out.add(
                    Voter(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5),
                        c.getString(6), c.getString(7)
                    )
                )
            }
            out
        }
    }
}

/** Small RFC-4180-style CSV parser that handles quoted commas and escaped quotes. */
class CsvParser(private val reader: BufferedReader) {
    fun rows(): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val current = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var ch: Int

        while (true) {
            ch = reader.read()
            if (ch == -1) break
            val c = ch.toChar()
            when {
                c == '"' -> {
                    if (inQuotes && reader.markSupported()) {
                        reader.mark(1)
                        val next = reader.read()
                        if (next == '"'.code) {
                            field.append('"')
                        } else {
                            if (next != -1) reader.reset()
                            inQuotes = false
                        }
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    current.add(field.toString())
                    field.setLength(0)
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r') {
                        reader.mark(1)
                        val next = reader.read()
                        if (next != '\n'.code && next != -1) reader.reset()
                    }
                    current.add(field.toString())
                    field.setLength(0)
                    if (current.any { it.isNotEmpty() }) rows.add(current.toList())
                    current.clear()
                }
                else -> field.append(c)
            }
        }
        if (field.isNotEmpty() || current.isNotEmpty()) {
            current.add(field.toString())
            rows.add(current.toList())
        }
        return rows
    }
}
