package com.alexandro.safeguardk3.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import com.alexandro.safeguardk3.data.model.InspectionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Helper database SQLite lokal untuk menyimpan riwayat inspeksi K3 secara persisten.
 * Menyimpan metadata inspeksi dan path gambar permanen di memori internal aplikasi.
 */
class InspectionDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "safeguard_k3.db"
        const val DATABASE_VERSION = 2

        const val TABLE_INSPECTIONS = "inspections"
        const val COLUMN_ID = "id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_FORMATTED_TIME = "formatted_time"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_CLASSIFICATION = "classification"
        const val COLUMN_CLASSIFICATION_LABEL = "classification_label"
        const val COLUMN_REASON = "reason"
        const val COLUMN_PERSON_COUNT = "person_count"
        const val COLUMN_HARD_HAT_COUNT = "hard_hat_count"
        const val COLUMN_VEST_COUNT = "vest_count"
        const val COLUMN_WORKERS_JSON = "workers_json"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_INSPECTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_FORMATTED_TIME TEXT NOT NULL,
                $COLUMN_IMAGE_PATH TEXT NOT NULL,
                $COLUMN_CLASSIFICATION TEXT NOT NULL,
                $COLUMN_CLASSIFICATION_LABEL TEXT NOT NULL,
                $COLUMN_REASON TEXT NOT NULL,
                $COLUMN_PERSON_COUNT INTEGER NOT NULL,
                $COLUMN_HARD_HAT_COUNT INTEGER NOT NULL,
                $COLUMN_VEST_COUNT INTEGER NOT NULL,
                $COLUMN_WORKERS_JSON TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_INSPECTIONS ADD COLUMN $COLUMN_WORKERS_JSON TEXT NOT NULL DEFAULT ''")
            } catch (e: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_INSPECTIONS")
                onCreate(db)
            }
        }
    }
}

/**
 * Repository lokal untuk mengelola operasi CRUD riwayat inspeksi dan penyimpanan gambar permanen.
 */
class InspectionRepository(private val context: Context) {
    private val dbHelper = InspectionDatabaseHelper(context.applicationContext)

    /**
     * Menyimpan gambar inspeksi secara permanen di storage internal aplikasi (filesDir/inspections/).
     * Menjamin foto tetap ada meski aplikasi di-restart atau cache dibersihkan.
     */
    suspend fun saveInspectionImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "inspections")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "inspect_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }
        file.absolutePath
    }

    /**
     * Menyimpan hasil inspeksi baru ke SQLite.
     */
    suspend fun insertInspection(record: InspectionRecord): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InspectionDatabaseHelper.COLUMN_TIMESTAMP, record.timestamp)
            put(InspectionDatabaseHelper.COLUMN_FORMATTED_TIME, record.formattedTime)
            put(InspectionDatabaseHelper.COLUMN_IMAGE_PATH, record.imagePath)
            put(InspectionDatabaseHelper.COLUMN_CLASSIFICATION, record.classification)
            put(InspectionDatabaseHelper.COLUMN_CLASSIFICATION_LABEL, record.classificationLabel)
            put(InspectionDatabaseHelper.COLUMN_REASON, record.reason)
            put(InspectionDatabaseHelper.COLUMN_PERSON_COUNT, record.personCount)
            put(InspectionDatabaseHelper.COLUMN_HARD_HAT_COUNT, record.hardHatCount)
            put(InspectionDatabaseHelper.COLUMN_VEST_COUNT, record.vestCount)
            put(InspectionDatabaseHelper.COLUMN_WORKERS_JSON, record.workersJson)
        }
        db.insert(InspectionDatabaseHelper.TABLE_INSPECTIONS, null, values)
    }

    /**
     * Mengambil seluruh daftar riwayat inspeksi (diurutkan dari yang terbaru).
     */
    suspend fun getAllInspections(): List<InspectionRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<InspectionRecord>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            InspectionDatabaseHelper.TABLE_INSPECTIONS,
            null,
            null,
            null,
            null,
            null,
            "${InspectionDatabaseHelper.COLUMN_TIMESTAMP} DESC"
        )

        cursor.use { c ->
            val idIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_ID)
            val tsIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_TIMESTAMP)
            val timeIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_FORMATTED_TIME)
            val pathIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_IMAGE_PATH)
            val classIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_CLASSIFICATION)
            val labelIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_CLASSIFICATION_LABEL)
            val reasonIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_REASON)
            val pIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_PERSON_COUNT)
            val hIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_HARD_HAT_COUNT)
            val vIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_VEST_COUNT)
            val wIdx = c.getColumnIndex(InspectionDatabaseHelper.COLUMN_WORKERS_JSON)

            while (c.moveToNext()) {
                val workers = if (wIdx >= 0 && !c.isNull(wIdx)) c.getString(wIdx) else ""
                list.add(
                    InspectionRecord(
                        id = c.getLong(idIdx),
                        timestamp = c.getLong(tsIdx),
                        formattedTime = c.getString(timeIdx),
                        imagePath = c.getString(pathIdx),
                        classification = c.getString(classIdx),
                        classificationLabel = c.getString(labelIdx),
                        reason = c.getString(reasonIdx),
                        personCount = c.getInt(pIdx),
                        hardHatCount = c.getInt(hIdx),
                        vestCount = c.getInt(vIdx),
                        workersJson = workers
                    )
                )
            }
        }
        list
    }

    /**
     * Mengambil 1 catatan inspeksi berdasarkan ID.
     */
    suspend fun getInspectionById(id: Long): InspectionRecord? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            InspectionDatabaseHelper.TABLE_INSPECTIONS,
            null,
            "${InspectionDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_ID)
                val tsIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_TIMESTAMP)
                val timeIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_FORMATTED_TIME)
                val pathIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_IMAGE_PATH)
                val classIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_CLASSIFICATION)
                val labelIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_CLASSIFICATION_LABEL)
                val reasonIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_REASON)
                val pIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_PERSON_COUNT)
                val hIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_HARD_HAT_COUNT)
                val vIdx = c.getColumnIndexOrThrow(InspectionDatabaseHelper.COLUMN_VEST_COUNT)
                val wIdx = c.getColumnIndex(InspectionDatabaseHelper.COLUMN_WORKERS_JSON)
                val workers = if (wIdx >= 0 && !c.isNull(wIdx)) c.getString(wIdx) else ""

                InspectionRecord(
                    id = c.getLong(idIdx),
                    timestamp = c.getLong(tsIdx),
                    formattedTime = c.getString(timeIdx),
                    imagePath = c.getString(pathIdx),
                    classification = c.getString(classIdx),
                    classificationLabel = c.getString(labelIdx),
                    reason = c.getString(reasonIdx),
                    personCount = c.getInt(pIdx),
                    hardHatCount = c.getInt(hIdx),
                    vestCount = c.getInt(vIdx),
                    workersJson = workers
                )
            } else {
                null
            }
        }
    }

    /**
     * Menghapus 1 catatan inspeksi berdasarkan id dan menghapus file gambarnya jika ada.
     */
    suspend fun deleteInspection(id: Long): Boolean = withContext(Dispatchers.IO) {
        val record = getInspectionById(id)
        if (record != null && record.imagePath.isNotBlank()) {
            val file = File(record.imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            InspectionDatabaseHelper.TABLE_INSPECTIONS,
            "${InspectionDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        rows > 0
    }

    /**
     * Menghapus semua riwayat inspeksi beserta file foto lokalnya.
     */
    suspend fun clearAllInspections(): Boolean = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "inspections")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
        val db = dbHelper.writableDatabase
        db.delete(InspectionDatabaseHelper.TABLE_INSPECTIONS, null, null)
        true
    }
}
