package com.pistolcaffe.nintendoapitest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_nuguri_portal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val HAPPY_FLOWER = "HappyFlower"
private const val GREETING = "Greeting"
private const val AHA = "Aha"
private const val CLAP = "Clapping"

class NuguriPortalActivity : AppCompatActivity() {

    private val nintendoViewModel: NintendoViewModel by viewModels {
        NintendoViewModelFactory(NintendoApplication.app.repository)
    }

    private lateinit var auth: Auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuguri_portal)
        auth = intent.extras!!.getSerializable("auth") as Auth

        GlideApp.with(this).load(auth.user.image).circleCrop().into(image)
        name.text = auth.user.name
        landName.text = SpannableString("${auth.user.land.name}섬").apply {
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.purple_500
                    )
                ), length - 1, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        menuHappy.setOnClickListener { sendReaction(HAPPY_FLOWER) }
        menuAha.setOnClickListener { sendReaction(AHA) }
        menuClap.setOnClickListener { sendReaction(CLAP) }
        menuHi.setOnClickListener { sendReaction(GREETING) }

        sendBtn.setOnClickListener { sendMessage() }

        Handler(Looper.getMainLooper()).postDelayed({
            loadingGroup.visibility = View.GONE
            content.visibility = View.VISIBLE
        }, 2000)
    }

    private fun sendMessage(manual: String? = null) {
        val message = manual ?: messageEt.text!!.toString()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                nintendoViewModel.message(auth.acToken, auth.pSession, message, "keyboard")
            }
        }
    }

    private fun sendReaction(body: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                nintendoViewModel.message(auth.acToken, auth.pSession, body, "emoticon")
            }
        }
    }

    fun onBookmarkItemClick(v: View) {
        when (v) {
            bookmark1 -> {
                sendMessage("이름은 양동표류기 입니다.")
                sendReaction(GREETING)
            }
            bookmark2 -> {
                sendMessage("감사합니다!!")
                sendReaction(HAPPY_FLOWER)
            }
            bookmark3 -> {
                sendMessage("축하드려요!!")
                sendReaction(CLAP)
            }
        }
    }
}