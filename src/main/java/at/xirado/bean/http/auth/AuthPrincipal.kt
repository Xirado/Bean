package at.xirado.bean.http.auth

import com.auth0.jwt.interfaces.Payload
import io.ktor.server.auth.*

class AuthPrincipal(val userId: Long, val payload: Payload) : Principal