package com.zyyoona7.customime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zyyoona7.customime.view.CustomKeyBoard;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomKeyBoard customKeyBoard=(CustomKeyBoard)findViewById(R.id.custom_keyboard);
        customKeyBoard.setOnEditSearchListener(new CustomKeyBoard.OnEditSearchListener() {
            @Override
            public void onSearch(String text) {
                Log.e(TAG, "onSearch text="+text);
            }
        });
    }
}
