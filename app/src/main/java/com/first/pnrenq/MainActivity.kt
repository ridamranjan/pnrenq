
package com.first.pnrenq

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.first.pnrenq.ui.navigation.AppNavigation
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemePreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        /* ---------- Install splash screen ---------- */

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        /* ---------- Smooth exit animation ---------- */

        splashScreen.setOnExitAnimationListener { splashView ->

            val view = splashView.view

            val fadeOut = ObjectAnimator.ofFloat(
                view,
                View.ALPHA,
                1f,
                0f
            )

            val riseUp = ObjectAnimator.ofFloat(
                view,
                View.TRANSLATION_Y,
                0f,
                -view.height * 0.04f
            )

            AnimatorSet().apply {
                playTogether(
                    fadeOut,
                    riseUp
                )

                duration = 260L

                interpolator = DecelerateInterpolator()

                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(
                            animation: Animator
                        ) {
                            splashView.remove()
                        }
                    }
                )

                start()
            }
        }

        /* ---------- Compose application ---------- */

        setContent {

            val themePreferences = remember {
                ThemePreferences(applicationContext)
            }

            val settings = themePreferences.settings

            PnrenqTheme(
                settings = settings
            ) {
                AppNavigation(
                    themeSettings = settings,

                    onThemeModeSelected =
                        themePreferences::setMode,

                    onThemePaletteSelected =
                        themePreferences::setPalette,

                    onThemeReset =
                        themePreferences::reset
                )
            }
        }
    }
}
