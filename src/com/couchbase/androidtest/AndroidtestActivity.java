package com.couchbase.androidtest;

import java.util.HashMap;
import java.util.Map;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.http.AndroidHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidtestActivity extends Activity {
	private TextView textView;
	private int battlevel;
	private Handler mPeriodicEventHandler;
	private final int PERIODIC_TIMEOUT = 30000; //TIME INTERVAL BETWWEN SUCCESSIVE WORKLOADS (in msec)
	public static final String DATABASE_NAME = "test";
	//private DismissPopup mDismissPopup = new DismissPopup();
	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;
	protected CouchDbConnector couchDbConnector;
	protected CouchDbInstance dbInstance;
	
	protected ReplicationCommand pushReplication;

	public static final String TAG = "AndroidTest";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCouch();
        setContentView(R.layout.main);
        
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), mDelegate);
		couchServiceConnection = couch.startCouchbase();
		Button b1 = (Button) findViewById(R.id.button1);
		//Button b2 = (Button) findViewById(R.id.button2);
		textView = (TextView)findViewById( R.id.batteryLevel );
        registerReceiver( batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );  
        
        
        //the below block could be copied for multiple buttons and multiple workloads
        b1.setOnClickListener(new OnClickListener() {
       	 @Override
       	 public void onClick(View v) { // onClick Method
       		if(battlevel > 75){
       		
       			AlertDialog.Builder b1 = new AlertDialog.Builder(AndroidtestActivity.this);

				b1.setTitle("Please discoonect the phone from charger for ideal results");
				b1
						.setMessage("Clicking Ok will start the workload");
				b1.setIcon(R.drawable.icon);
				b1.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Toast.makeText(AndroidtestActivity.this, "Success", Toast.LENGTH_SHORT).show();
							}
						});
				b1.show();
			}
       		else{
       			Log.i("asdasd","asdasdad");
       			AlertDialog.Builder b1 = new AlertDialog.Builder(AndroidtestActivity.this);

				b1.setTitle("Please wait till the battery level is > 75% to continue");
				b1
						.setMessage("Click Ok to go back to home screen");
				b1.setIcon(R.drawable.icon);
				b1.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								//Add workload here
								Toast.makeText(AndroidtestActivity.this, "Success", Toast.LENGTH_SHORT).show();
							}
						});
				b1.show();
       		}
   	 }});
    }
 
    public void updateBatteryCondition(int level)
    {
    textView.setText( level + "%" );
    }
    
    public void batterypopup(int level)
    {
    	battlevel = level;
    }

    //RUNNING THE WORKLOAD PERIODICALLY AFTER EVERY "PERIODIC_TIMEOUT"
 	  private Runnable doPeriodicTask = new Runnable()
	    {
	       
			public void run() 
	        {
				for(int i=0; i<1000000;i++){
		    		dbInstance.replicate(pushReplication);
		    		Log.v("LETS SEE IF UPDATED", "HI");
		    	} 
	         mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_TIMEOUT);
	        }
	    };
	    
	    
    
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
    	 @Override
    	 public void onReceive( Context context, Intent intent )
    	 {
    	 int level = intent.getIntExtra( "level", 0 );
    	 updateBatteryCondition(level);
    	 batterypopup(level);
    	 }
    	 };
      
	private final ICouchbaseDelegate mDelegate = new ICouchbaseDelegate.Stub() {
		@Override
		public void couchbaseStarted(String host, int port) {
			Log.v(TAG, "Couchbase has started");
			HttpClient httpClient = new AndroidHttpClient.Builder().host(host).port(port).build();
			dbInstance = new StdCouchDbInstance(httpClient);
	    	couchDbConnector = dbInstance.createConnector(DATABASE_NAME, true);
	    	
	    	Map<String, String> newItem = new HashMap<String, String>();
	    	newItem.put("text", "sbcde");
	    	newItem.put("check", Boolean.FALSE.toString());
	    	
	    	CouchDocumentAsyncTask createTask = new CouchDocumentAsyncTask(couchDbConnector, CouchDocumentAsyncTask.OPERATION_CREATE);
	    	createTask.execute(newItem);

//	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//	    	pushReplication = new ReplicationCommand.Builder()
//	    											.source(DATABASE_NAME)
//	    											.target(prefs.getString("sync_url","http://subarvind.iriscouch.com/demo"))
//	    											.continuous(true)
//	    											.build();
//	    	
//	    	//RUNNING THE WORKLOAD FOR THE FIRST TIME
//	    	for(int i=0; i<1000000;i++){
//	    		dbInstance.replicate(pushReplication);
//	    		Log.v("LETS SEE IF UPDATED", "HI");
//	    	}
//
//			mPeriodicEventHandler = new Handler();
//		    mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_TIMEOUT);
//			
		};
			

		@Override
		public void installing(int completed, int total) {
		}

		@Override
		public void exit(String error) {
		}
		
		
	};
	
	protected void startCouch() {
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), mDelegate);
		couchServiceConnection = couch.startCouchbase(); 
		
	}

}