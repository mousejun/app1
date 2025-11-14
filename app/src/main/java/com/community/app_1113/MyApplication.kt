package com.community.app_1113

import android.app.Application
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 구글 모바일 광고 SDK 초기화
        MobileAds.initialize(this) {}
    }
}