package jasper.wagner.smartstockmarketing.util

import com.google.gson.Gson

object SerializeHelper {

    // Serialize a single object.
    fun serializeToJson(any: Any): String {
        val gson = Gson()
        return gson.toJson(any)
    }

    // Deserialize to single object.
    inline fun <reified T> deserializeFromJson(jsonString: String): T {
        val gson = Gson()
        return gson.fromJson(jsonString, T::class.java)
    }
}