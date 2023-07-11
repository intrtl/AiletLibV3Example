package com.ailet.ailetv3test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.ailet.lib3.api.Ailet
import com.ailet.lib3.api.client.method.domain.start.AiletMethodStart
import com.ailet.lib3.feature.logger.CompositeAiletLoggerFeature
/*import com.ailet.lib3.feature.permissions.AiletPermissionsFeature
import com.ailet.lib3.feature.permissions.DefaultAiletPermissionsFeature*/
import com.ailet.lib3.feature.stockcamera.DefaultStockCameraFeature
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            supportActionBar?.title = "v3.7.0"
        }

        val features = setOf(
            DefaultStockCameraFeature(),
            loggerInstance(),
            // Используется в версии 4.0.1 и выше
            /*DefaultAiletPermissionsFeature(
                excludedPermissions = setOf(AiletPermissionsFeature.Exclude.CAMERA)
            )*/
        )

        // токен начальной авторизации, предоставленный командой Ailet
        val accessToken = "access_token"

        // инициализация библиотеки с вашим токеном и выбранными модулями
        Ailet.initialize(this, accessToken, features)

        findViewById<Button>(R.id.btInit).setOnClickListener {
            findViewById<Button>(R.id.pbWait).isVisible = true
            Ailet.getClient().init(
                findViewById<EditText>(R.id.etLogin).text.toString(),
                findViewById<EditText>(R.id.etPassword).text.toString()
            )
                .execute({ result ->

                    runOnUiThread {
                        findViewById<Button>(R.id.pbWait).isVisible = false
                        findViewById<Button>(R.id.btStart).isVisible = true
                    }

                }, { throwable ->
                    println(throwable.message)
                    makeAlertDialog(throwable.message)

                    runOnUiThread {
                        findViewById<Button>(R.id.pbWait).isVisible = false
                        findViewById<Button>(R.id.btStart).isVisible = true
                    }
                })
        }

        val externalStoreId =
            findViewById<EditText>(R.id.etExternalStoreId).text.toString().ifBlank { "testStoreId" }

        val externalVisitId =
            findViewById<EditText>(R.id.etExternalVisitId).text.toString().ifBlank { "testVisitId" }

        findViewById<Button>(R.id.btStart).setOnClickListener {
            Ailet.getClient()
                .start(AiletMethodStart.StoreId.External(externalStoreId), externalVisitId)
                .execute({ result ->

                }, { throwable ->
                    println(throwable.message)
                    makeAlertDialog(throwable.message)
                })
        }
    }

    private fun loggerInstance(): CompositeAiletLoggerFeature {
        val dump = File(filesDir, "AiletApp.log").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }
        return CompositeAiletLoggerFeature(
            application,
            CompositeAiletLoggerFeature.BugfenderConfig(
                token = "bugfender_token",
                build = BuildConfig.VERSION_CODE
            ),
            CompositeAiletLoggerFeature.DumpFileLoggerConfig(dump)
        )
    }

    private fun makeAlertDialog(message: String?) {
        AlertDialog.Builder(this).apply {
            setMessage(message)
        }.show()
    }
}