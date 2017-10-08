package com.unimelb.jigarthakkar.safedrivesystem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

/**
 * Created by Tang on 10/7/17.
 */

public class TimeUtil {

    static String mYear;
    static String mMonth;
    static String mDay;
    static String mWeek;

    public static String getTime(){

        final Calendar c = Calendar.getInstance();

        mYear = String.valueOf(c.get(Calendar.YEAR));
        mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        mWeek = String.valueOf(c.get(Calendar.DAY_OF_WEEK));

        if("1".equals(mWeek)){
            mWeek = "Sunday";
        }else if("2".equals(mWeek)){
            mWeek = "Monday";
        }else if("3".equals(mWeek)){
            mWeek ="Tuesday";
        }else if("4".equals(mWeek)){
            mWeek ="Wednesday";
        }else if("5".equals(mWeek)){
            mWeek ="Thursday";
        }else if("6".equals(mWeek)){
            mWeek ="Friday";
        }else if("7".equals(mWeek)){
            mWeek ="Saturday";
        }
        return mYear + "-" + mMonth + "-" + mDay + " " + "/ " + mWeek;
    }

    public static String getTimeDifference(String startTime, String endTime) {

        String timeString = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date parseStart = dateFormat.parse(startTime);
            Date parseEnd = dateFormat.parse(endTime);

            long diff = parseEnd.getTime() - parseStart.getTime();
            long days = diff/(1000 * 60 * 60 * 24);
            timeString = Long.toString(days);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeString;
    }

}
