package at.xirado.bean.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import org.tomlj.Toml

// I go Toml -> Json -> Java because ktoml does not support table arrays
// This doesn't need to be fast because it is only used on startup

inline fun <reified T> tomlDecode(
    input: String,
    serializer: KSerializer<T> = serializer(),
    json: Json = Json.Default,
): T {
    val tomlResult = Toml.parse(input)

    if (tomlResult.hasErrors()) {
        val message = tomlResult.errors().joinToString("\n")
        throw RuntimeException("Failed to parse toml string: $message")
    }

    val jsonString = tomlResult.toJson()
    return json.decodeFromString(serializer, jsonString)
}

fun tomlToJsonObject(
    input: String,
    json: Json = Json.Default,
): JsonObject {
    val tomlResult = Toml.parse(input)

    if (tomlResult.hasErrors()) {
        val message = tomlResult.errors().joinToString("\n")
        throw RuntimeException("Failed to parse toml string: $message")
    }

    val jsonString = tomlResult.toJson()
    return json.decodeFromString<JsonObject>(jsonString)
}