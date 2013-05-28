package com.ralcantara.android.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import com.ralcantara.android.test.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TestAndroidActivity extends Activity {
	private String url="192.168.89.60/android/webSimulation/";
	private ArrayList<String[]> rankingList=new ArrayList<String[]>();
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.rankingCreate();
    }
    
    public void startGame(View v){
    	Button b = (Button) v;
    	b.setText("Stop");
    	try{
    		TextView textView = (TextView) v.getRootView().findViewById(R.id.textView1);
    		textView.setText(this.getPhrase());
    		
    		EditText editText = (EditText) v.getRootView().findViewById(R.id.editText1);
    		editText.setText("");
    		editText.setEnabled(true);
    		editText.setKeyListener(new TextKeyListener(Capitalize.CHARACTERS, false));
    		
    		ChronometerCustom chronometer = (ChronometerCustom) v.getRootView().findViewById(R.id.chronometer1);
	    	chronometer.setBase(SystemClock.elapsedRealtime());
	    	chronometer.setFormat("%h:%m");
	    	chronometer.start();
    	}catch(Exception e){
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
        	dialog.setMessage(e.toString());
        	dialog.show();
	    }
    }
    
    public void stopGame(View v){
    	Long timer=60L;
    	Button b = (Button) v;
    	b.setText("Start");
    	try{
    		TextView textView = (TextView) v.getRootView().findViewById(R.id.textView1);
    		
    		EditText editText = (EditText) v.getRootView().findViewById(R.id.editText1);
    		editText.setEnabled(false);
    		editText.setKeyListener(null);
    		
    		ChronometerCustom chronometer = (ChronometerCustom) v.getRootView().findViewById(R.id.chronometer1);
	    	chronometer.stop();
	    	        	
	    	if(textView.getText().toString().equals(editText.getText().toString())){
		    	timer=chronometer.totalTimeMilliseconds;
		    	this.rankingCreate(timer);
	    	}else{
	    		AlertDialog dialog = new AlertDialog.Builder(this).create();
	        	dialog.setMessage("Frase Errada!");
	        	dialog.show();
	    	}
    	}catch(Exception ex){
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
        	dialog.setMessage(ex.toString());
        	dialog.show();
	    }
    }
    
    public void startStopGame(View v){
    	Button b = (Button) v;
    	if("Start".equals(b.getText())){
    		this.startGame(v);
    	}else{
    		this.stopGame(v);
    	}
    }
    
    
    private String getPhrase(){
    	String phrase="";
    	try{
	    	HttpClient hc = new DefaultHttpClient();
	    	HttpPost post = new HttpPost("http://"+this.url+"android.csv");
	
	    	HttpResponse rp = hc.execute(post);
	    	if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	    		String str = EntityUtils.toString(rp.getEntity());
	    		String phrases[] = str.split("\n");
	    		phrase=phrases[new Random().nextInt(8)];	    		
	    	}
    	}catch(IOException e){
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
        	dialog.setMessage(e.toString());
        	dialog.show();
    	}
    	
    	return phrase;
    }
    
    private String[] getRanking(){
    	String[] response=new String[5];
    	try{
	    	HttpClient hc = new DefaultHttpClient();
	    	HttpPost post = new HttpPost("http://"+this.url+"android_ranking.csv");
	
	    	HttpResponse rp = hc.execute(post);
	    	if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
	    		String str = EntityUtils.toString(rp.getEntity());
	    		response = str.split("\n");
	    	}
    	}catch(IOException e){
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
        	dialog.setMessage(e.toString());
        	dialog.show();
    	}
    	
    	return response;
    }    
    
    private String[] rankingCreate(){
    	String[] ranking=new String[5];
    	int p=0;
    	
    	String[] response=getRanking();
		for (String i : response) {
			String user=i.split(";")[1];
			String timer=i.split(";")[0];
			int position=Integer.valueOf(i.split(";")[2]);
			
			String seconds=String.format("%2s", TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(timer))).replace(" ", "0");
            String millisecondsTmp=String.format("%4s", timer).replace(" ", "0");
            millisecondsTmp=millisecondsTmp.substring(millisecondsTmp.length()-3, 5);
            	               
            this.rankingList.add(position, new String[]{user, timer, seconds.toString(), millisecondsTmp});
            ranking[p++]=seconds.toString()+"s"+millisecondsTmp+"m "+user;
		}
		
		this.rankingDesign(ranking);
		
		return ranking;
    }
    
    /*
     * **************************
     * STOP HERE
     * **************************
     */
    private String[] rankingCreate(Long newTimer){    	
    	boolean winnerFlag=false;
    	
    	String[] lastUser=null;
    	for (int i = 0; i < this.rankingList.size(); i++) {
			String[] currentTimerInfo = this.rankingList.get(i);
			Long currentTimer=Long.valueOf(currentTimerInfo[1]);
			
			if(winnerFlag==true){
				String[] lastTmp=this.rankingList.get(i);
				this.rankingList.set(i, lastUser);
				lastUser=lastTmp;
			}
			
			if(newTimer<currentTimer && winnerFlag==false){
				winnerFlag=true;
				
				lastUser=this.rankingList.get(i);
				String[] newWinnerInfo=this.getNewWinnerInfo(newTimer, i);
				this.rankingList.set(i,newWinnerInfo);
			}
		}
    	
    	String[] ranking=new String[5];
    	for (int i = 0; i < this.rankingList.size(); i++) {
			String user=this.rankingList.get(i)[0];
			String timer=this.rankingList.get(i)[1];
			
			String seconds=String.format("%2s", TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(timer))).replace(" ", "0");
            String millisecondsTmp=String.format("%4s", timer).replace(" ", "0");
            millisecondsTmp=millisecondsTmp.substring(millisecondsTmp.length()-3, 5);
            	               
            ranking[i]=seconds.toString()+"s"+millisecondsTmp+"m "+user;
		}
		this.rankingDesign(ranking);
		
		return ranking;
    }
    
    private String[] getNewWinnerInfo(Long timer, int position){
    	String seconds=String.format("%2s", TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(timer))).replace(" ", "0");
        String millisecondsTmp=String.format("%4s", timer).replace(" ", "0");
        millisecondsTmp=millisecondsTmp.substring(millisecondsTmp.length()-3, 5);
        
    	AlertDialog dialog = new AlertDialog.Builder(this).create();
    	dialog.setMessage("Voce esta no ranking! Posicao "+(position+1));
    	dialog.show();
        
    	String[] newWinnerInfo={"newWinnerName",timer.toString(),seconds, millisecondsTmp};
    	
    	return newWinnerInfo;
    }
    
    private boolean rankingDesign(String[] ranking){
    	try{
	        ListView listView = (ListView) this.findViewById(R.id.listView1);
	        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ranking));
        }catch(Exception e){
        	AlertDialog dialog = new AlertDialog.Builder(this).create();
        	dialog.setMessage(e.toString());
        	dialog.show();        	
        	return false;
        }        
        return true;
    }
}