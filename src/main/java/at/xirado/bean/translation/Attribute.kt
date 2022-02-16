package at.xirado.bean.translation

class Attribute {

    val key: String
    val value: String

    constructor(key: String, value: String) {
        this.key = key
        this.value = value
    }

    constructor(key: String, value: Int) {
        this.key = key
        this.value = value.toString()
    }

}
