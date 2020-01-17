package com.example.jobbkalender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jobbkalender.DataClasses.WorkdayEvent;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<WorkdayEvent> {

    private Context mContext;

    public EventListAdapter(@NonNull Context context, int resource, @NonNull List<WorkdayEvent> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String jobName = getItem(position).getJob().getName();
        String eventTimeSpan = "Fra " + getItem(position).getStartTime() + " til " + getItem(position).getEndTime();
        String salary = "Lønn: " + getItem(position).getJob().getSalary()+ "kr";

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.event_list_layout,parent,false);

        TextView textView1 = convertView.findViewById(R.id.textViewEventListName);
        TextView textView2 = convertView.findViewById(R.id.textViewEventListDuration);
        TextView textView3 = convertView.findViewById(R.id.textViewEventListSalary);

        textView1.setText(jobName);
        textView2.setText(eventTimeSpan);
        textView3.setText(salary);

        return convertView;
    }
}
