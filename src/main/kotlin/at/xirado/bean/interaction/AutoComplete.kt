package at.xirado.bean.interaction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoComplete(val option: String)
