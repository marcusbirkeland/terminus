package com.birkeland.terminus.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.PayCalculator;
import com.birkeland.terminus.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends ArrayAdapter<WorkdayEvent> {

    private Context mContext;
    private final static int VIEW_DATE = 1;
    private int viewMode;
    public EventListAdapter(@NonNull Context context, int resource, @NonNull List<WorkdayEvent> objects) {
        super(context, resource, objects);
        mContext = context;
        viewMode = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();
        try {
           final WorkdayEvent event = getItem(position);
            String jobName = event.getJob().getName();
            final String eventTimeSpan;
            if(event.isNightShift()){
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
                LocalDate startDate = LocalDate.parse(event.getDate(), dateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate endDate = startDate.plusDays(1);
                eventTimeSpan = mContext.getString(R.string.from) +" "+ event.getStartTime() +
                        " (" + startDate.getDayOfMonth() + "." + startDate.getMonthValue() + ") " + "\n" +
                        mContext.getString(R.string.to)+" " + getItem(position).getEndTime() + " (" + endDate.getDayOfMonth() + "." + endDate.getMonthValue() + ") ";
            }else{
                eventTimeSpan = "Fra " +event.getStartTime() + " til " + getItem(position).getEndTime();
            }
            List<WorkdayEvent> events = new ArrayList<>();
            events.add(event);
            PayCalculator payCalculator = new PayCalculator(events);
            event.setSalary(payCalculator.getEarnings(events));
            String salary = "Lønn: " + event.getSalary() + " kr";
            String src = event.getJob().getImage();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(R.layout.event_list_layout,parent,false);
        TextView textView1 = convertView.findViewById(R.id.textViewEventListName);
        TextView textView2 = convertView.findViewById(R.id.textViewEventListDuration);
        TextView textView3 = convertView.findViewById(R.id.textViewEventListSalary);
        ImageView imageView = convertView.findViewById(R.id.imageViewEventList);
        if(event.isNightShift()){
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        }
        if(viewMode != VIEW_DATE)
            textView1.setText(jobName);
        else{

            LocalDate date = LocalDate.parse(event.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dateString = date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
            textView1.setText(jobName + "\n" + dateString);
        }

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