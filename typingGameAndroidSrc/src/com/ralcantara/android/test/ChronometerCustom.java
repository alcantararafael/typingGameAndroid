package com.ralcantara.android.test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class ChronometerCustom extends Chronometer{
	public long start=Calendar.getInstance().getTimeInMillis();
	public Long timer=0L;
	public boolean StartStop=false;
	
	public Long seconds=0L;
	public Long milliseconds=0L;
	public Long totalTimeMilliseconds=0L;
	
    public ChronometerCustom(Context context, AttributeSet attrs) {
            super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {    		
            super.onDraw(canvas);
            
            String timerStr="";
            if(StartStop){
	            this.timer=Calendar.getInstance().getTimeInMillis()-start;
	            if(this.timer>=60000){
	            	this.stop();
	            }
            }
            
            this.seconds=TimeUnit.MILLISECONDS.toSeconds(this.timer);
            String millisecondsTmp=String.format("%4s", this.timer.toString()).replace(" ", "0");
            millisecondsTmp=millisecondsTmp.substring(millisecondsTmp.length()-3, 4);
            this.milliseconds=Long.valueOf(millisecondsTmp);
            
            timerStr=String.format("%d:%d", this.seconds, this.milliseconds);
            this.setText(timerStr);
    }
 
    @Override
	public void start() {
    	this.start=Calendar.getInstance().getTimeInMillis();
    	this.StartStop=true;
    }
    
    @Override
	public void stop() {
    	this.StartStop=false;
    	this.totalTimeMilliseconds=(this.seconds*1000)+this.milliseconds;
    }
}
