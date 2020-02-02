package com.example.jobbkalender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jobbkalender.DataClasses.Job;

import java.util.List;

public class JobListAdapter extends ArrayAdapter<Job> {
    private Context mContext;
    private List<Job> jobs;
    public JobListAdapter(@NonNull Context context, int resource, @NonNull List<Job> objects) {
        super(context, resource, objects);
        mContext = context;
        jobs = objects;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.job_list_layout,parent,false);
        TextView textView = convertView.findViewById(R.id.textViewAllJobsList);
        ImageView imageView = convertView.findViewById(R.id.imageViewAllJobsList);
        textView.setText(jobs.get(position).getName());
        try {
            Uri uri = Uri.parse(jobs.get(position).getImage());
            imageView.setImageURI(uri);
        } catch (NullPointerException n){
            imageView.setImageResource(R.drawable.contacts);
        }

        return convertView;
    }
}
