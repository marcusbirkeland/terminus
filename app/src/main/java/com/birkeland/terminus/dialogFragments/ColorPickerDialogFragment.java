package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.R;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class ColorPickerDialogFragment extends DialogFragment {

    int startColor;
    public ColorPickerDialogFragment(int color) {
        startColor = color;
    }
    public interface OnInputListener{
        void sendColor(int color);
    }

    private ColorPickerDialogFragment.OnInputListener mOnInputListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        com.skydoves.colorpickerview.ColorPickerDialog.Builder builder =  new com.skydoves.colorpickerview.ColorPickerDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(getString(R.string.pick_color))
                .setPositiveButton(getString(R.string.save),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                Log.d("Color selected: ", envelope.getHexCode());
                                mOnInputListener.sendColor(envelope.getColor());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .attachAlphaSlideBar(false) // default is true. If false, do not show the AlphaSlideBar.
                .attachBrightnessSlideBar(true);  // default is true. If false, do not show the BrightnessSlideBar.
        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (ColorPickerDialogFragment.OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e("onAttach", "On attach exception!");
        }
        super.onAttach(context);
    }
}
