package com.couchbase.androidtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ReplicationCommand;
import org.ektorp.ViewQuery;
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
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

public class AndroidtestActivity extends Activity {
	private TextView textView;
	private Handler mPeriodicEventHandler;
	private final int PERIODIC_EVENT_TIMEOUT = 1;
	public static final String DATABASE_NAME = "grocery-sync";
	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;
	protected CouchDbConnector couchDbConnector;

	protected static final String TAG = "AndroidTest";

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
    
 	  private Runnable doPeriodicTask = new Runnable()
	    {
	        public void run() 
	        {

	    	Map<String, String> newItem = new HashMap<String, String>();
	    	//newItem.put("_id", id);
	    	newItem.put("text", "abcde");
	    	//newItem.put("check", Boolean.FALSE.toString());
	    	//newItem.put("created_at", currentTimeString);
	    	ViewQuery q = new ViewQuery()
	    					.allDocs()
	    					.includeDocs(true);
	    	//bulkDocs.add(BulkDeleteDocument.of(toBeDeleted));
	    	//List<DocumentOperationResult> executeBulk(Collection<?> objects);
	    					
	    	//for (int i=0;i<100;i++){			
	    	CouchDocumentAsyncTask createTask = new CouchDocumentAsyncTask(couchDbConnector, CouchDocumentAsyncTask.OPERATION_CREATE);
	    	createTask.execute(newItem);
	    	Log.v("LETS SEE IF UPDATED", "HI");
	    	//}
	            mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);
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
			CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
			couchDbConnector = dbInstance.createConnector(DATABASE_NAME, true);
			
			mPeriodicEventHandler = new Handler();
		    mPeriodicEventHandler.postDelayed(doPeriodicTask, PERIODIC_EVENT_TIMEOUT);
			
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