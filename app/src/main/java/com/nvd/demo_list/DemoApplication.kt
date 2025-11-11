package com.nvd.demo_list

import android.app.Application
import com.nvd.demo_list.utils.SplashManager

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo SplashManager để bắt đầu check API
        SplashManager.initialize(this)
    }
}

