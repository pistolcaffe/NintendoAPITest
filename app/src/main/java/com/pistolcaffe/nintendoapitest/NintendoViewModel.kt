package com.pistolcaffe.nintendoapitest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class NintendoViewModel(private val repository: NintendoRepository) : ViewModel() {

    private var cachedGToken = ""
    private var parkSession = ""
    val liveData = MutableLiveData<Auth>()

    fun start(tokenCode: String, codeVerifier: String) = viewModelScope.launch {
        val sessionToken = requestSessionToken(tokenCode, codeVerifier)
        val webServiceToken = requestWebServiceTokenWithSessionToken(sessionToken)
        requestCookiesForAnimalCrossing(webServiceToken)
        Timber.e("sessionToken: $sessionToken webServiceToken: $webServiceToken")
    }

    suspend fun requestSessionToken(tokenCode: String, codeVerifier: String): String {
        val sessionToken = api().sessionToken(SessionTokenReq(tokenCode, codeVerifier))
        return sessionToken.session_token
    }

    suspend fun requestWebServiceTokenWithSessionToken(sessionToken: String): String {
        val apiToken = api().token(TokenReq(sessionToken))
        Timber.e("token: $apiToken")
        val userInfo = apiUser().userInfo("Bearer ${apiToken.access_token}")
        Timber.e("userInfo: $userInfo")

        val guid = UUID.randomUUID().toString()
        val timeStamp = System.currentTimeMillis().toString()

        val nsoFToken = requestFToken(apiToken.id_token, guid, timeStamp, "nso")
        val apiAccessToken = requestApiLogin(userInfo, nsoFToken)
        val appFToken = requestFToken(apiAccessToken, guid, timeStamp, "app")
        return requestWebServiceToken(apiAccessToken, appFToken)
    }

    suspend fun requestFToken(token: String, guid: String, timeStamp: String, type: String): FToken {
        val hash = apiS2S().hash(token, timeStamp)
        return apiFToken().fToken(token, timeStamp, guid, hash.hash, type).result
    }

    suspend fun requestApiLogin(userInfo: UserInfo, nsoFToken: FToken): String {
        return apiZNC().login(LoginReq(LoginReqParams(userInfo.language,
                userInfo.country,
                userInfo.birthday,
                nsoFToken.f,
                nsoFToken.p1,
                nsoFToken.p2,
                nsoFToken.p3))).result.webApiServerCredential.accessToken
    }

    suspend fun requestWebServiceToken(token: String, appFToken: FToken): String {
        return apiZNC().webServiceToken("Bearer $token", WebServiceTokenReq(WebServiceTokenReqParams(
                appFToken.f,
                appFToken.p1,
                appFToken.p2,
                appFToken.p3
        ))).result.accessToken
    }

    suspend fun requestCookiesForAnimalCrossing(accessToken: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val acRsp = apiAC(true).accessAC(accessToken).execute()
            for (h in acRsp.headers()) {
                if (h.first == "Set-Cookie" && h.second.contains("_gtoken")) {
                    cachedGToken = h.second.split(";")[0].split("=")[1]
                    Timber.e("_gToken: $cachedGToken")
                    break
                }
            }
            val userResponse = apiAC(false).animalCrossingUsers("_gtoken=$cachedGToken").execute().body()!!
            val acTokenResponse = apiAC(false).animalCrossingTokens("_gtoken=$cachedGToken", AnimalCrossingTokensReq(userResponse.users[0].id)).execute()
            for (h in acTokenResponse.headers()) {
                if (h.first == "Set-Cookie" && h.second.contains("_park_session")) {
                    parkSession = h.second.split(";")[0].split("=")[1]
                    Timber.e("_park_session: $parkSession")
                    break
                }
            }
            liveData.postValue(Auth(cachedGToken, parkSession, acTokenResponse.body()!!.token, userResponse.users[0]))
        }
    }

    suspend fun message(bToken: String, pSession: String, body: String, type: String) {
        apiAC(false).message("Bearer $bToken", pSession, MessageReq(body, type))
    }
}

class NintendoViewModelFactory(private val repository: NintendoRepository) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NintendoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NintendoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}