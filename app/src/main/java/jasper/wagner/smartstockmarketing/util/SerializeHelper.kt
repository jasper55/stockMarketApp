package jasper.wagner.smartstockmarketing.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

//    inline fun <reified T> deserializeToArrayList(jsonString: String): ArrayList<T> {
//        val gson = Gson()
//        return gson.fromJson(jsonString, Array<T>::class.java).asList() as ArrayList<T>
//    }

    inline fun <reified T> deserializeToArrayList(jsonString: String): ArrayList<T> {
        val gson = Gson()
        return gson.fromJson(jsonString, object : TypeToken<List<T>>() {}.type) as ArrayList<T>
    }


}