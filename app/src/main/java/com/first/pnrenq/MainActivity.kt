
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

import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

import com.first.pnrenq.ui.navigation.AppNavigation

import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemePreferences
import com.first.pnrenq.ui.theme.isDarkAppearance

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        /* =================================================
           SPLASH EXIT ANIMATION
        ================================================= */

        splashScreen.setOnExitAnimationListener {
                splashView ->

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

                interpolator =
                    DecelerateInterpolator()

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

        /* =================================================
           COMPOSE APPLICATION
        ================================================= */

        setContent {

            val themePreferences = remember {
                ThemePreferences(
                    applicationContext
                )
            }

            val settings =
                themePreferences.settings

            val systemDark =
                isSystemInDarkTheme()

            val darkAppearance = isDarkAppearance(
                mode = settings.mode,
                systemDark = systemDark
            )

            /* =============================================
               SYSTEM BAR ICON CONTRAST

               Dark appearance:
               light status-bar icons

               Light appearance:
               dark status-bar icons
            ============================================= */

            SideEffect {

                val controller =
                    WindowCompat.getInsetsController(
                        window,
                        window.decorView
                    )

                controller.isAppearanceLightStatusBars =
                    !darkAppearance

                controller.isAppearanceLightNavigationBars =
                    !darkAppearance
            }

            /* =============================================
               THEME / TIME FORMAT PROVIDERS
            ============================================= */

            CompositionLocalProvider(
                LocalPnrTimeFormat provides
                        settings.timeFormat
            ) {

                PnrenqTheme(
                    settings = settings
                ) {

                    AppNavigation(
                        themeSettings = settings,

                        onThemeModeSelected =
                            themePreferences::setMode,

                        onThemePaletteSelected =
                            themePreferences::setPalette,

                        onThemeStyleSelected =
                            themePreferences::setStyle,

                        onTimeFormatSelected =
                            themePreferences::setTimeFormat,

                        onThemeReset =
                            themePreferences::reset
                    )
                }
            }
        }
    }
}
