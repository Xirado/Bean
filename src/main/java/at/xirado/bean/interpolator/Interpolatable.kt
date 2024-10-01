package at.xirado.bean.interpolator

interface Interpolatable<T> {
    fun interpolate(interpolator: Interpolator, context: InterpolationContext): T
}