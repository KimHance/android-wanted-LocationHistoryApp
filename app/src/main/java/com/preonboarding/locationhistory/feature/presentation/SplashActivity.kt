package com.preonboarding.locationhistory.feature.presentation

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.preonboarding.locationhistory.R
import com.preonboarding.locationhistory.base.BaseActivity
import com.preonboarding.locationhistory.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>(R.layout.activity_splash) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}