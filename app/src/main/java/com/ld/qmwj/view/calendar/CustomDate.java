package com.ld.qmwj.view.calendar;

import java.io.Serializable;

public class CustomDate implements Serializable{


    private static final long serialVersionUID = 1L;
    public int year;
    public int month;
    public int day;
    public int week;

    public CustomDate(int year,int month,int day){
        if(month > 12){
            month = 1;
            year++;
        }else if(month <1){
            month = 12;
            year--;
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CustomDate(){
        this.year = DateUtil.getYear();
        this.month = DateUtil.getMonth();
        this.day = DateUtil.getCurrentMonthDay();
    }

    public static CustomDate modifiDayForObject(CustomDate date,int day){
        CustomDate modifiDate = new CustomDate(date.year,date.month,day);
        return modifiDate;
    }
    @Override
    public String toString() {
        String tempM=month+"";
        String tempD=day+"";
        if(month<10)
            tempM="0"+month;
        if(day<10)
            tempD="0"+day;
        return year+"_"+tempM+"_"+tempD;
    }

    @Override
    public boolean equals(Object o) {
        boolean b=false;
        CustomDate temp= (CustomDate) o;
        if(temp.year==this.year&&temp.month==this.month&&temp.day==this.day)
            b=true;
        return b;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }



}
