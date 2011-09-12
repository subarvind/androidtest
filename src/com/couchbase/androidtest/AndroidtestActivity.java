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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class AndroidtestActivity extends Activity {
	private TextView textView;
	private Handler mPeriodicEventHandler;
	private final int PERIODIC_TIMEOUT = 30000; //TIME INTERVAL BETWWEN SUCCESSIVE WORKLOADS (in msec)
	public static final String DATABASE_NAME = "test";
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
		
		//createGroceryItem();
		textView = (TextView)findViewById( R.id.batteryLevel );
        registerReceiver( batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );    	
    }
    
  
    public void updateBatteryCondition(int level)
    {
    textView.setText( level + "%" );
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

	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	pushReplication = new ReplicationCommand.Builder()
	    											.source(DATABASE_NAME)
	    											.target(prefs.getString("sync_url","http://subarvind.iriscouch.com/demo"))
	    											.continuous(true)
	    											.build();
	    	
	    	//RUNNING THE WORKLOAD FOR THE FIRST TIME
	    	for(int i=0; i<1000000;i++){
	    		dbInstance.replicate(pushReplication);
	    		Log.v("LETS SEE IF UPDATED", "HI");
	    	}

			mPeriodicEventHandler = new Handler();
		    mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_TIMEOUT);
			
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