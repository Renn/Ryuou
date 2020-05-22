package org.ecnu.ryuou.menu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.ecnu.ryuou.BaseActivity;
import org.ecnu.ryuou.R;

public class AboutActivity extends BaseActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.menu.activity_about);
    }
}
