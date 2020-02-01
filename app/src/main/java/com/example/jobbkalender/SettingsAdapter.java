package com.example.jobbkalender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jobbkalender.DataClasses.Setting;

import java.util.List;

public class SettingsAdapter extends ArrayAdapter<Setting> {
    private Context mContext;
    private List<Setting> settings;
    public SettingsAdapter(@NonNull Context context, int resource, @NonNull List<Setting> objects) {
        super(context, resource, objects);
        mContext = context;
        settings = objects;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.settings_list_layout,parent,false);
        TextView textView = convertView.findViewById(R.id.textViewSettingsList);
        ImageView imageView = convertView.findViewById(R.id.imageViewSettingImage);
        textView.setText(settings.get(position).getSettingName());
        imageView.setImageResource(settings.get(position).getSettingImageResource());
        return convertView;
    }
}

