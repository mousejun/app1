package com.community.app_1113

import android.content.Intent
import android.content.IntentSender
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateRequestCode = 123 // 아무 숫자나 상관없는 요청 코드
    private val handler = Handler(Looper.getMainLooper())
    private var isIntroShown = false

    // 유연한 업데이트의 다운로드 상태를 감지하는 리스너
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // 다운로드가 완료되면 사용자에게 알림
            showUpdateDownloadedSnackbar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 인앱 업데이트 매니저 초기화
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // 유연한 업데이트 리스너 등록
        appUpdateManager.registerListener(installStateUpdatedListener)

        // 앱 업데이트 확인 시작
        checkForAppUpdate()

        // 홈 화면에 바로가기 추가 (첫 실행 시)
        addShortcutToHomeScreen()

        // 인트로 화면 표시
        if (savedInstanceState == null) {
            showIntroScreen()
        } else {
            // 재생성 시에는 테마 변경 후 북마크 화면 표시
            setTheme(R.style.Theme_App1113)
            showBookmarkScreen()
        }
    }

    private fun showIntroScreen() {
        setContentView(R.layout.activity_intro)
        isIntroShown = true
        handler.postDelayed({
            if (isIntroShown) {
                showBookmarkScreen()
            }
        }, 2000)
    }

    private fun showBookmarkScreen() {
        isIntroShown = false
        // 테마를 일반 테마로 변경
        setTheme(R.style.Theme_App1113)
        val introLayout = findViewById<View>(R.id.introLayout)
        val fragmentContainer = findViewById<View>(R.id.fragmentContainer)
        if (introLayout != null && fragmentContainer != null) {
            // Fragment를 먼저 추가하고 나서 전환
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BookmarkFragment(), "BookmarkFragment")
                .commitAllowingStateLoss()
            // Fragment가 준비된 후 전환
            handler.postDelayed({
                fragmentContainer.visibility = View.VISIBLE
                introLayout.visibility = View.GONE
            }, 50)
        } else {
            setContentView(android.R.id.content)
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, BookmarkFragment(), "BookmarkFragment")
                .commit()
        }
    }

    private fun checkForAppUpdate() {
        // Play 스토어에서 업데이트 정보를 가져옴
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            // 업데이트가 존재하는지 확인
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // 즉시 업데이트(강제 업데이트)가 가능한지 확인
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // 강제 업데이트 흐름 시작
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE, // 즉시 업데이트 타입 (강제)
                            this,
                            updateRequestCode
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("MainActivity", "Error starting immediate update flow", e)
                    }
                }
                // 유연한 업데이트가 가능한지 확인
                else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // 유연한 업데이트 흐름 시작
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE, // 유연한 업데이트 타입
                            this,
                            updateRequestCode
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("MainActivity", "Error starting flexible update flow", e)
                    }
                }
            }
        }
    }

    // 업데이트 다운로드가 완료되었을 때 스낵바를 보여주는 함수
    private fun showUpdateDownloadedSnackbar() {
        val rootView = findViewById<android.view.View>(android.R.id.content)
        Snackbar.make(
            rootView,
            "새로운 버전이 다운로드되었습니다.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("설치") {
                // "설치" 버튼을 누르면 업데이트 완료
                appUpdateManager.completeUpdate()
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 앱이 다시 포그라운드로 돌아왔을 때 업데이트 상태 확인
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // 즉시 업데이트가 진행 중이고 완료되지 않았으면 다시 업데이트 흐름 시작
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                updateRequestCode
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e("MainActivity", "Error resuming immediate update flow", e)
                        }
                    }
                }
                // 유연한 업데이트 다운로드가 완료되었으면 스낵바 표시
                else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showUpdateDownloadedSnackbar()
                }
            }
    }

    /**
     * 홈 화면에 바로가기 아이콘을 추가하는 함수
     */
    private fun addShortcutToHomeScreen() {
        val prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE)
        val isShortcutAdded = prefs.getBoolean(AppConstants.KEY_SHORTCUT_ADDED, false)
        
        // 이미 추가된 경우 스킵
        if (isShortcutAdded) {
            return
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 이상에서는 ShortcutManager 사용
                val shortcutManager = getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                    val shortcutInfo = ShortcutInfo.Builder(this, "app_shortcut")
                        .setShortLabel(getString(R.string.app_name))
                        .setLongLabel(getString(R.string.app_name))
                        .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                        .setIntent(Intent(Intent.ACTION_MAIN).apply {
                            setClassName(this@MainActivity, MainActivity::class.java.name)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        })
                        .build()
                    
                    shortcutManager.requestPinShortcut(shortcutInfo, null)
                    
                    // 추가 완료 플래그 저장
                    prefs.edit().putBoolean(AppConstants.KEY_SHORTCUT_ADDED, true).apply()
                }
            } else {
                // Android 8.0 미만에서는 기존 방식 사용
                val addIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(Intent.ACTION_MAIN).apply {
                    setClassName(this@MainActivity, MainActivity::class.java.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name))
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, 
                    Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher))
                addIntent.putExtra("duplicate", false)
                
                sendBroadcast(addIntent)
                
                // 추가 완료 플래그 저장
                prefs.edit().putBoolean(AppConstants.KEY_SHORTCUT_ADDED, true).apply()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "홈 화면 바로가기 추가 실패", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티가 종료될 때 리스너 등록 해제 (메모리 누수 방지)
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}