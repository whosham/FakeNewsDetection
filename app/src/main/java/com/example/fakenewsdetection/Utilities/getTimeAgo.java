package com.example.fakenewsdetection.Utilities;

import android.icu.util.GregorianCalendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

//Class receive time stamp as yyyy-MM-dd HH:mm:ss and return tiime ago

public class getTimeAgo {

    public static String getTimeAgo(String time) {

        String result="";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date past = format.parse(time);
            Date now = new Date();


            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            String day = dayFormat.format(past);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
            String month = monthFormat.format(past);
            SimpleDateFormat yearFormat = new SimpleDateFormat("YY");
            String year = yearFormat.format(past);



            //Calendar date = new GregorianCalendar(past);
            //date.setTime(theDate);
            long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds<60)
            {
                result ="Just now";
                // System.out.println(seconds+"Just now");

            }
            else if(minutes<60)
            {

                result =minutes+" minutes ago";
                //System.out.println(minutes+" minutes ago");
            }
            else if(hours<24)
            {
                result=(hours+" hours ago") ;
                //System.out.println(hours+" hours ago");
            }
            else if (hours < 48 && hours >24 ){
                result=("Yesterday");
                ///System.out.println("Yesterday");
            }
            else if (days > 7 && days <30) {
                result=(days+" days ago") ;
            }

            else
           {
                  result=(day +"/"+month+ "/" +year );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

}
