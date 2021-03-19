package dev.benedikt.localize.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.benedikt.localize.api.BaseLocaleProvider
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader
import kotlin.io.path.isDirectory

/**
 * @author Benedikt Wüller
 */
class JsonLocaleProvider(private val path: Path) : BaseLocaleProvider() {

    private val gson = GsonBuilder().create()

    @ExperimentalPathApi
    override fun loadStrings(): Map<String, String> {
        if (!this.path.isDirectory()) {
            val json = this.gson.fromJson(this.path.bufferedReader(), JsonObject::class.java)
            return this.loadStrings(json)
        }

        val files = Files.list(path)
        val strings = mutableMapOf<String, String>()
        files.forEach {
            val json = this.gson.fromJson(it.bufferedReader(), JsonObject::class.java)
            strings.putAll(this.loadStrings(json))
        }
        return strings
    }

    private fun loadStrings(json: JsonObject, prefix: String? = null): Map<String, String> {
        val strings = mutableMapOf<String, String>()

        json.keySet().forEach { key ->
            val realKey = if (prefix == null) key else "$prefix.$key"
            val obj = json[key]
            if (obj.isJsonObject) {
                strings.putAll(this.loadStrings(obj.asJsonObject, realKey))
            } else {
                strings[realKey] = obj.asString
            }
        }

        return strings
    }

}
