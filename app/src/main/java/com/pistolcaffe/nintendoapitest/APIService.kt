package com.pistolcaffe.nintendoapitest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://accounts.nintendo.com/"
private const val BASE_URL_S2S = "https://elifessler.com/s2s/api/"
private const val BASE_URL_USER = "https://api.accounts.nintendo.com/"
private const val BASE_URL_FLAPQ = "https://flapg.com/"
private const val BASE_URL_ZNC = "https://api-lp1.znc.srv.nintendo.net/"
private const val BASE_URL_AC = "https://web.sd.lp1.acbaa.srv.nintendo.net/"
private const val H_X_PLATFORM = "X-Platform: Android"
private const val H_X_PRODUCT_VERSION = "X-ProductVersion: 1.9.0"
private const val H_USER_AGENT = "User-Agent: OnlineLounge/1.9.0 NASDKAPI Android"
private const val H_S2S_USER_AGENT = "User-Agent: splatnet2statink/1.9.0"
private const val H_ZNC_USER_AGENT = "User-Agent: com.nintendo.znca/1.9.0 (Android/10.0)"
private const val H_VER = "x-ver: 3"

private const val H_AC_ACCEPT = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val H_AC_ACCEPT_ENCODING = "Accept-Encoding: gzip,deflate"
private const val H_AC_X_IS_APP_ANALYTICS_OPTEDIN = "x-isappanalyticsoptedin: false"
private const val H_AC_X_REQUESTED_WITH = "X-Requested-With: com.nintendo.znca"
private const val H_AC_X_DNT = "DNT: 0"
private const val H_AC_X_CONNECTION = "Connection: keep-alive"

fun api(): APIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(apiClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(APIService::class.java)
}

fun apiS2S(): S2SAPIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL_S2S)
            .client(apiClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(S2SAPIService::class.java)
}

fun apiUser(): UserAPIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL_USER)
            .client(apiClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UserAPIService::class.java)
}

fun apiFToken(): FLAPQAPIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL_FLAPQ)
            .client(apiClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FLAPQAPIService::class.java)
}

fun apiZNC(): ZNCAPIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL_ZNC)
            .client(apiClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ZNCAPIService::class.java)
}

fun apiAC(isScalar: Boolean): ACAPIService {
    return Retrofit.Builder()
            .baseUrl(BASE_URL_AC)
            .client(apiClient())
            .addConverterFactory(if (isScalar) ScalarsConverterFactory.create() else GsonConverterFactory.create())
            .build().create(ACAPIService::class.java)
}

fun apiClient(): OkHttpClient {
    return OkHttpClient.Builder().apply {
        readTimeout(1800, TimeUnit.SECONDS)
        connectTimeout(1800, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()
}

interface APIService {
    @Headers(H_X_PLATFORM, H_X_PRODUCT_VERSION, H_USER_AGENT)
    @POST("connect/1.0.0/api/session_token")
    suspend fun sessionToken(@Body req: SessionTokenReq): SessionToken

    @Headers(H_X_PLATFORM, H_X_PRODUCT_VERSION, H_ZNC_USER_AGENT)
    @POST("connect/1.0.0/api/token")
    suspend fun token(@Body req: TokenReq): Token
}

interface S2SAPIService {
    @FormUrlEncoded
    @Headers(H_S2S_USER_AGENT)
    @POST("gen2")
    suspend fun hash(
            @Field("naIdToken") naIdToken: String,
            @Field("timestamp") timestamp: String
    ): Hash
}

interface UserAPIService {
    @Headers(H_X_PLATFORM, H_X_PRODUCT_VERSION, H_ZNC_USER_AGENT)
    @GET("2.0.0/users/me")
    suspend fun userInfo(@Header("Authorization") token: String): UserInfo
}

interface FLAPQAPIService {
    @Headers(H_VER)
    @GET("ika2/api/login?public")
    suspend fun fToken(
            @Header("x-token") token: String,
            @Header("x-time") timestamp: String,
            @Header("x-guid") guid: String,
            @Header("x-hash") hash: String,
            @Header("x-iid") login: String,
    ): FTokenResult
}

interface ZNCAPIService {
    @Headers(H_X_PLATFORM, H_X_PRODUCT_VERSION, H_ZNC_USER_AGENT, "Authorization: Bearer")
    @POST("v1/Account/Login")
    suspend fun login(@Body body: LoginReq): Login

    @Headers(H_X_PLATFORM, H_X_PRODUCT_VERSION, H_ZNC_USER_AGENT)
    @POST("v2/Game/GetWebServiceToken")
    suspend fun webServiceToken(
            @Header("Authorization") token: String,
            @Body body: WebServiceTokenReq
    ): WebServiceToken
}

interface ACAPIService {
    @Headers(H_ZNC_USER_AGENT, H_AC_ACCEPT, H_AC_ACCEPT_ENCODING, H_AC_X_IS_APP_ANALYTICS_OPTEDIN, H_AC_X_REQUESTED_WITH, H_AC_X_DNT, H_AC_X_CONNECTION)
    @GET(BASE_URL_AC)
    fun accessAC(@Header("X-GameWebToken") token: String): Call<String>

    @Headers(H_ZNC_USER_AGENT, H_AC_ACCEPT, H_AC_ACCEPT_ENCODING, H_AC_X_IS_APP_ANALYTICS_OPTEDIN, H_AC_X_REQUESTED_WITH, H_AC_X_DNT, H_AC_X_CONNECTION)
    @GET("/api/sd/v1/users")
    fun animalCrossingUsers(@Header("Cookie") gToken: String): Call<AnimalCrossingUserReq>

    //@FormUrlEncoded
    @Headers(H_ZNC_USER_AGENT, H_AC_ACCEPT, H_AC_ACCEPT_ENCODING, H_AC_X_IS_APP_ANALYTICS_OPTEDIN, H_AC_X_REQUESTED_WITH, H_AC_X_DNT, H_AC_X_CONNECTION)
    @POST("/api/sd/v1/auth_token")
    fun animalCrossingTokens(
            @Header("Cookie") gToken: String,
            @Body body: AnimalCrossingTokensReq
    ): Call<AnimalCrossingTokens>

    @Headers(H_ZNC_USER_AGENT, H_AC_ACCEPT, H_AC_ACCEPT_ENCODING, H_AC_X_IS_APP_ANALYTICS_OPTEDIN, H_AC_X_REQUESTED_WITH, H_AC_X_DNT, H_AC_X_CONNECTION)
    @POST("/api/sd/v1/messages")
    suspend fun message(
            @Header("Authorization") bToken: String,
            @Header("Cookie") pSessionCookie: String,
            @Body body: MessageReq
    ): Message
}