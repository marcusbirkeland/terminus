package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.R;
import com.birkeland.terminus.customViews.ToggleRadioButton;

import static android.content.Context.MODE_PRIVATE;

public class RepeatEventDialogFragment extends DialogFragment {

    public RepeatEventDialogFragment() {
    }

    public interface OnInputListener{
        void sendRepeat(boolean isEachWeek, boolean isEveryOtherWeek, int numMonths);
    }
    private RepeatEventDialogFragment.OnInputListener mOnInputListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View dialogView = View.inflate(getContext(),R.layout.repeat_dialog,null);

        builder.setMessage(getString(R.string.repeat))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save),new DialogInterface.OnClickListener() {
                    private boolean weekly;
                    private boolean biweekly;
                    private int repeatMonths;
                    public void onClick(DialogInterface dialog, int id) {
                        closeKeyboard();
                        final RadioButton radioButtonEachWeek = dialogView.findViewById(R.id.toggleRadioButtonEveryWeek);
                        final RadioButton radioButtonEveryOtherWeek = dialogView.findViewById(R.id.toggleRadioButtonEveryOtherWeek);
                        final EditText editTextNum = dialogView.findViewById(R.id.editTextNumMonths);
                        if(radioButtonEachWeek.isChecked())
                            weekly = true;
                        else if(radioButtonEveryOtherWeek.isChecked()){
                            biweekly = true;
                        }
                        if(editTextNum.getText().toString().isEmpty()){
                            repeatMonths= Integer.parseInt(editTextNum.getHint().toString());
                        }else{
                            try{
                               repeatMonths = Integer.parseInt( editTextNum.getText().toString());
                            } catch (NumberFormatException e){
                                Log.e("Repeat dialog: ", e.toString());
                                return;
                            }
                        }
                        mOnInputListener.sendRepeat(weekly,biweekly,repeatMonths);
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
            mOnInputListener = (RepeatEventDialogFragment.OnInputListener) getActivity();
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
