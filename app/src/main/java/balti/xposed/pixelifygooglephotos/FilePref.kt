package balti.xposed.pixelifygooglephotos;

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FilePref {


    private val prefFile by lazy {
        File(
            File(Constants.SHARED_PREF_PATH), Constants.SHARED_PREF_FILE_NAME
        )
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        val data = readDataFromFile()
        return defaultValue?.let { data?.optString(key, it) }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val data = readDataFromFile()
        return data?.optBoolean(key, defaultValue) ?: defaultValue
    }

    fun putString(key: String, value: String?) {
        val data = readDataFromFile() ?: JSONObject()
        data.put(key, value)
        writeDataToFile(data)
    }

    fun putBoolean(key: String, value: Boolean) {
        val data = readDataFromFile() ?: JSONObject()
        data.put(key, value)
        writeDataToFile(data)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        val data = readDataFromFile()
        return data?.optInt(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        val data = readDataFromFile() ?: JSONObject()
        data.put(key, value)
        writeDataToFile(data)
    }

    // Add more put methods for other data types
    fun putStringSet(key: String, values: Set<String>?) {
        val data = readDataFromFile() ?: JSONObject()
        data.put(key, if (values != null) JSONArray(values) else null)
        writeDataToFile(data)
    }

    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? {
        val data = readDataFromFile()
        val jsonArray = data?.optJSONArray(key)
        return if (jsonArray != null) {
            val result = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.getString(i))
            }
            result
        } else {
            defaultValue
        }
    }

    private fun readDataFromFile(): JSONObject? {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            val inputStream = process.inputStream

            // Grant read permissions to the file using root
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.write("cat ${prefFile.absolutePath}\n")
                writer.flush()
            }

            // Read the file content
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)

                }
                JSONObject(stringBuilder.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun writeDataToFile(data: JSONObject) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream

            // Grant write permissions to the file using root
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.write("echo '${data.toString()}' > ${prefFile.absolutePath}\n")
                writer.flush()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}