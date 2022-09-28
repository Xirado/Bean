package at.xirado.bean.command.precondition

import at.xirado.bean.Application

interface Precondition<T> {
    fun check(application: Application, obj: T): Boolean
}