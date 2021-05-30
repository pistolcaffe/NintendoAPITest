package com.pistolcaffe.nintendoapitest

import java.io.Serializable

data class SessionTokenReq(
        val session_token_code: String,
        val session_token_code_verifier: String,
        val client_id: String = "71b963c1b7b6d119"
)

data class SessionToken(val code: String, val session_token: String)

data class TokenReq(
        val session_token: String,
        val grant_type: String = "urn:ietf:params:oauth:grant-type:jwt-bearer-session-token",
        val client_id: String = "71b963c1b7b6d119"
)

data class Token(val access_token: String, val id_token: String)

data class HashReq(val naIdToken: String, val timestamp: String)
data class Hash(val hash: String)

data class UserInfo(
        val nickname: String,
        val language: String,
        val birthday: String,
        val country: String
)

data class FTokenResult(val result: FToken)
data class FToken(val f: String, val p1: String, val p2: String, val p3: String)

data class LoginReq(val parameter: LoginReqParams)

data class LoginReqParams(
        val language: String,
        val naCountry: String,
        val naBirthday: String,
        val f: String,
        val naIdToken: String,
        val timestamp: String,
        val requestId: String
)

data class Login(val result: LoginResult)
data class LoginResult(val webApiServerCredential: WebApiServerCredential)
data class WebApiServerCredential(val accessToken: String)

data class WebServiceTokenReq(val parameter: WebServiceTokenReqParams)
data class WebServiceTokenReqParams(
        val f: String,
        val registrationToken: String,
        val timestamp: String,
        val requestId: String,
        val id: Long = 4953919198265344
)

data class WebServiceToken(val result: WebServiceTokenResult)
data class WebServiceTokenResult(val accessToken: String)

data class AnimalCrossingUserReq(val users: List<AnimalCrossingUser>)
data class AnimalCrossingUser(
        val id: String,
        val name: String,
        val image: String,
        val land: AnimalCrossingUserLand
): Serializable

data class AnimalCrossingUserLand(
        val id: String,
        val name: String,
        val displayId: Int
): Serializable

data class AnimalCrossingTokensReq(val userId: String)
data class AnimalCrossingTokens(val token: String, val expireAt: Long)

data class Auth(
        val gToken: String,
        val pSession: String,
        val acToken: String,
        val user: AnimalCrossingUser
): Serializable

data class MessageReq(val body: String, val type: String)
data class Message(val a: String)