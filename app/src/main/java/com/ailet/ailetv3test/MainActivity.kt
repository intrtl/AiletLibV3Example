package com.ailet.ailetv3test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.view.isVisible
import com.ailet.lib3.api.Ailet
import com.ailet.lib3.api.client.method.domain.start.AiletMethodStart
import com.ailet.lib3.feature.stockcamera.DefaultStockCameraFeature

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val features = setOf(
            DefaultStockCameraFeature()
        )

        // токен начальной авторизации, предоставленный командой Ailet
        val accessToken = "token"

        // инициализация библиотеки с вашим токеном и выбранными модулями
        Ailet.initialize(this, accessToken, features)

        findViewById<Button>(R.id.btInit).setOnClickListener {
            findViewById<Button>(R.id.pbWait).isVisible = true
            Ailet.getClient().init("login", "password")
                .execute({ result ->

                    runOnUiThread {
                        findViewById<Button>(R.id.pbWait).isVisible = false
                        findViewById<Button>(R.id.btStart).isVisible = true
                    }

                }, { throwable ->
                    println(throwable.message)
                })
        }


        findViewById<Button>(R.id.btStart).setOnClickListener {
            Ailet.getClient()
                .start(AiletMethodStart.StoreId.External("storeId"), "visitId")
                .execute({ result ->

                }, { throwable ->
                    println(throwable.message)
                })
        }
    }
}