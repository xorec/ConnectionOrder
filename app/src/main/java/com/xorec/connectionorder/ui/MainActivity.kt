package com.xorec.connectionorder.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.xorec.connectionorder.R

/* Класс MainActivity не содержит в себе ничего кроме FragmentContainerView, который нужен
*  для компонента Navigation. Естественно, поскольку в данном приложении всего один экран, можно было бы
*  работать непосредственно с MainActivity, но для демонстрации используется подход, который используется
*  в реальных приложениях. */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiy_main)

        /* Согласно скриншотам bottom navigation bar приложения должен быть окрашен в цвет фона приложения,
        *  что значит, что, если используются программные кнопки, они должны быть окрашены в контрастный
        *  цвет, поскольку в светлой теме фон светлый, а в темной теме фон темный. Для API level >= 27
        *  существует атрибут XML темы, который позволяет получить это поведение (windowLightNavigationBar).
        *  Редактируя значение данного атрибута для темы, можно добиться темных кнопок в светлой теме [true]
        *  и светлых кнопок в темной теме [false]. Для API level 26 этот атрибут использовать нельзя,
        *  но можно программно указать, что нужны темные кнопки, как это сделано ниже. Указывается только вариант
        *  с темными кнопками, поскольку на API level 26 темной темы нет. Для API level < 26 конфигурировать
        *  поведение кнопок на bottom navigation bar не представляется возможным. По этой причине для
        *  API level 23, 24, 25 (у данного приложения minSdk стоит 23) есть отдельные файлы с описанием темы,
        *  где не задействуется атрибут navigationBarColor, т.е. bottom navigation bar не окрашивается вообще. */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            WindowInsetsControllerCompat(this.window, this.window.decorView).isAppearanceLightNavigationBars = true
        }
    }
}