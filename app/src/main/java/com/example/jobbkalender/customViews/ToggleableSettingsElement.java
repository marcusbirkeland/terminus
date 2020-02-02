package com.example.jobbkalender.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.example.jobbkalender.R;

public class ToggleableSettingsElement extends View {
    public ToggleableSettingsElement(Context context) {
        super(context,null, R.layout.toggleable_settings_element);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
