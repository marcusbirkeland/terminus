package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.R;

import static android.content.Context.MODE_PRIVATE;

public class EditTaxDialogFragment extends DialogFragment {

    public static final int TAX_SELECTED = 123213;
    private String selectedTable;
    public EditTaxDialogFragment() {
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View dialogView = View.inflate(getContext(), R.layout.select_tax_dialog_layout,null);
        final TextView textViewSetTaxTable = dialogView.findViewById(R.id.textViewPickTaxTable);
        final RadioButton radioButtonPercentage = dialogView.findViewById(R.id.radioButtonTaxPercentage);
        final RadioButton radioButtonTable = dialogView.findViewById(R.id.radioButtonTaxTable);
        final EditText editTextPercentage = dialogView.findViewById(R.id.editTextTaxPercentage);

        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        String currentTable = pref.getString("TAXTABLE","");
        if (!currentTable.equals(""))
            textViewSetTaxTable.setText(currentTable);
        editTextPercentage.setText(""+ (pref.getFloat("TAXPERCENTAGE",0)));

        if(pref.getBoolean("ISTAXTABLE",false))
            radioButtonTable.setChecked(true);
        else
            radioButtonPercentage.setChecked(true);

        // Har ikke satt radio buttons i noen gruppe, derfor må vi programmere de her:
        radioButtonPercentage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioButtonPercentage.isChecked())
                    radioButtonTable.setChecked(false);
            }
        });
        radioButtonTable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioButtonTable.isChecked())
                    radioButtonPercentage.setChecked(false);
            }
        });
        textViewSetTaxTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Åpne tabellpicker
                editTextPercentage.clearFocus();
                showTaxTablePicker();
                radioButtonTable.setChecked(true);
            }
        });
        editTextPercentage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    radioButtonPercentage.setChecked(true);
            }
        });

        builder.setMessage(getString(R.string.choose_tax_method))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final EditText editTextTaxPercentage = dialogView.findViewById(R.id.editTextTaxPercentage);
                        if(radioButtonPercentage.isChecked()){
                            if(editTextTaxPercentage.getText().toString().equals("")){
                                Toast toast=Toast.makeText(getContext(),getString(R.string.error_eneter_tax_percentage),Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            setTaxPercentage(editTextTaxPercentage.getText().toString());

                        }else if(radioButtonTable.isChecked()){
                            if(selectedTable == null ||selectedTable.equals("")){
                                if(textViewSetTaxTable.getText().length() == 4){
                                    setSelectedTable(textViewSetTaxTable.getText().toString());
                                }else{
                                    Toast toast=Toast.makeText(getContext(),getString(R.string.error_eneter_tax_table),Toast.LENGTH_SHORT);
                                    toast.show();
                                    return;
                                }
                            }
                            setTaxTable();
                        }else{
                            Toast toast=Toast.makeText(getContext(),getString(R.string.error_choose_a_tax_option),Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(getTargetRequestCode(),TAX_SELECTED,intent);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Avbryt
            }
        });
        return builder.create();
    }

    public void showTaxTablePicker(){
        TablePickerDialogFragment tablePicker = new TablePickerDialogFragment();
        tablePicker.setTargetFragment(this,0);
        tablePicker.show(this.getFragmentManager(),"Table picker");
    }
    private void setSelectedTable (String tableID){
        TextView textViewPickTax = getDialog().findViewById(R.id.textViewPickTaxTable);
        textViewPickTax.setText(tableID);
        selectedTable = tableID;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Selected table", data.getStringExtra("Table"));
        setSelectedTable(data.getStringExtra("Table"));
    }

    private void saveTaxTable(String tableID){
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("TAXTABLE",tableID);
        editor.putBoolean("ISTAXTABLE",true);
        editor.apply();
    }

    private void setTaxTable (){
        if(!selectedTable.equals("") && selectedTable != null){
            // Lagrer i sharedprefs
            saveTaxTable(selectedTable);
        }
    }

    private void saveTaxPercentage (float percentage){
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("TAXPERCENTAGE", percentage);
        editor.putBoolean("ISTAXTABLE",false);
        editor.apply();
    }

    private void setTaxPercentage(String taxPercentageText){
        if(taxPercentageText != null && !taxPercentageText.equals("")) {
            float taxPercentage;
            taxPercentage = Float.parseFloat(taxPercentageText);
            if (taxPercentage > 100 ){
                taxPercentage = 100;
            }
            // Lagrer skatteprosent i sharedprefs
            saveTaxPercentage(taxPercentage);
        }
    }
}

