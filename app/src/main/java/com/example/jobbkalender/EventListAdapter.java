package com.example.jobbkalender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.ui.home.HomeFragment;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import static com.example.jobbkalender.MainActivity.DELETE_EVENT;

public class EventListAdapter extends ArrayAdapter<WorkdayEvent> {

    private Context mContext;

    public EventListAdapter(@NonNull Context context, int resource, @NonNull List<WorkdayEvent> objects) {
        super(context, resource, objects);
        mContext = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();
        try {
           final WorkdayEvent event = getItem(position);
            String jobName = event.getJob().getName();
            String eventTimeSpan = "Fra " +event.getStartTime() + " til " + getItem(position).getEndTime();
            PayCaluclator payCaluclator = new PayCaluclator();
            List<WorkdayEvent> events = new ArrayList<>();
            events.add(event);
            String salary = "Lønn: " + (int) payCaluclator.getTotalEarningsGross(events) + " kr";
            String src = event.getJob().getImage();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.event_list_layout,parent,false);
        TextView textView1 = convertView.findViewById(R.id.textViewEventListName);
        TextView textView2 = convertView.findViewById(R.id.textViewEventListDuration);
        TextView textView3 = convertView.findViewById(R.id.textViewEventListSalary);
        ImageView imageView = convertView.findViewById(R.id.imageViewEventList);

        textView1.setText(jobName);
        textView2.setText(eventTimeSpan);
        textView3.setText(salary);
        ImageLoader imageLoader = ImageLoader.getInstance();
        int defaultImage = mContext.getResources().getIdentifier("@drawable/contacts",null,mContext.getPackageName());
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .cacheOnDisc(true).resetViewBeforeLoading(true)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageOnLoading(defaultImage).build();

        imageLoader.displayImage("file://"+src, imageView, options);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("EVENT",event);
                    Intent intent = new Intent(mContext, ViewEvent.class);
                    intent.putExtra("EVENTBUNDLE",bundle);
                    Activity activity = (Activity)mContext;
                    activity.startActivityForResult(intent,DELETE_EVENT);
            }
            });
        } catch (NullPointerException e){
            Log.d("Null", "Job class is null");
        }

        return convertView;
    }

    private void setupImageLoader(){
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
    }
}
