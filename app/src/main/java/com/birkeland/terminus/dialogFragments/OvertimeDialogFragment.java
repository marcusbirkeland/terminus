package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.R;

import java.text.ParseException;

public class OvertimeDialogFragment extends DialogFragment {

    private int percentage;
    public OvertimeDialogFragment(int overtimePercentage) {
        this.percentage = overtimePercentage;
    }

    public interface OnInputListener{
        void sendOvertime(double percentage);
    }
    private OvertimeDialogFragment.OnInputListener mOnInputListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = View.inflate(getContext(), R.layout.overtime_dialog,null);
        final EditText editTextPercentage = dialogView.findViewById(R.id.editTextOvertimePercentage);
        editTextPercentage.setText(percentage + "");
        builder.setMessage(getString(R.string.overtime))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeKeyboard();
                        try {
                            percentage = Integer.parseInt(editTextPercentage.getText().toString());
                        }catch (NumberFormatException e){
                            Log.e("Overtime Dialog", e.toString());
                            return;
                        }
                        mOnInputListener.sendOvertime(percentage);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeKeyboard();
                // Avbryt
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (OvertimeDialogFragment.OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e("onAttach", "On attach exception!");
        }
        super.onAttach(context);
    }
    public void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager inputMethodManager= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
}
