package at.xirado.bean.io.exception

class MissingPropertyException(fileName: String? = null, property: String) : RuntimeException() {

    override val message = if (fileName == null) "Missing property: $property" else "Missing property: $property in file: $fileName"
}