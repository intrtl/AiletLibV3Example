package com.ailet.ailetv3test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.ailet.lib3.api.Ailet;
import com.ailet.lib3.api.client.method.domain.start.AiletMethodStart;

import java.util.ArrayList;

public class MainActivityJava extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);


        Button btInit = findViewById(R.id.btInitJava);
        btInit.setOnClickListener(view -> {
                    Ailet.getClient().init(
                            "login",
                            "password",
                            null,
                            false,
                            null,
                            true
                    ).execute(
                            result -> {

                                Ailet.getClient().start(
                                        new AiletMethodStart.StoreId.External("storeId"),
                                        "visitId",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        new ArrayList<>(),
                                        new AiletMethodStart.LaunchConfig()
                                ).execute(result1 -> {
                                            Log.i("Test", "Result");

                                            return null;
                                        },
                                        throwable -> {
                                            Log.e("Test", "Error");

                                            return null;
                                        },
                                        () -> {
                                            Log.i("test", "Complete");

                                            return null;
                                        });


                                return null;
                            },
                            throwable -> {
                                Log.e("Test", "Error");

                                return null;
                            },
                            () -> {
                                Log.i("test", "Complete");

                                return null;
                            }
                    );
                }

        );
    }
}