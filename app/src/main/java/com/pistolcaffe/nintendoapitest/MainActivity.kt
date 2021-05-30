package com.pistolcaffe.nintendoapitest

import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.and
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

private const val INIT_URL = "https://accounts.nintendo.com/connect/1.0.0/authorize?scope=openid%20user%20user.birthday%20user.mii%20user.screenName&response_type=session_token_code&session_token_code_challenge_method=S256&theme=login_form&redirect_uri=npf71b963c1b7b6d119://auth&client_id=71b963c1b7b6d119"

class MainActivity : AppCompatActivity() {
    private val nintendoViewModel: NintendoViewModel by viewModels {
        NintendoViewModelFactory(NintendoApplication.app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val state = generateRandom(36)
        val codeVerifier = generateRandom(32)
        val codeChallenge = calculateChallenge(codeVerifier)
        Timber.e("state: $state codeVerifier: $codeVerifier codeChallenge: $codeChallenge")

        val url = StringBuilder(INIT_URL).apply {
            append("&state=").append(state)
            append("&session_token_code_challenge=").append(codeChallenge)
        }.toString()

        schemaEt.doOnTextChanged { text, start, before, count ->
            Timber.e("text: $text")
        }

        val a = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        a.addPrimaryClipChangedListener {
            Timber.e("primary: ${a.primaryClip?.description}")
        }

        analyzeBtn.setOnClickListener {
            try {
                val queryList = schemaEt.text.split("#")[1].split("&")
                var sessionTokenCode = ""
                for (q in queryList) {
                    if (q.contains("session_token_code")) {
                        sessionTokenCode = q.split("=")[1]
                        break
                    }
                }
                Timber.e("sessionTokenCode: $sessionTokenCode")
                nintendoViewModel.start(sessionTokenCode, codeVerifier)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(url)), "앱 선택"))

        nintendoViewModel.liveData.observe(this, {
            Timber.e("it: $it")
            startActivity(Intent(this@MainActivity, NuguriPortalActivity::class.java).apply {
                putExtra("auth", it)
            })
            finish()
        })
    }

    private fun generateRandom(length: Int): String {
        val flags = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE
        return Base64.encodeToString(SecureRandom().generateSeed(length), flags)
    }

    private fun calculateChallenge(codeVerifier: String): String {
        val sh: MessageDigest = MessageDigest.getInstance("SHA-256")
        sh.update(codeVerifier.toByteArray())
        val byteData = sh.digest()
        val flags = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE
        val codeChallenge = Base64.encodeToString(byteData, flags)
        Timber.e("byteData: $byteData codeChallenge: $codeChallenge")
        return codeChallenge
    }

    private fun sha256(str: String): String {
        return try {
            val sh: MessageDigest = MessageDigest.getInstance("SHA-256")
            sh.update(str.toByteArray())
            val byteData: ByteArray = sh.digest()
            val sb = StringBuffer()
            for (i in byteData.indices) sb.append(
                    ((byteData[i] and 0xff) + 0x100).toString(16).substring(1)
            )
            sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }
}