package com.couchbase.androidtest;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AndroidtestActivity extends Activity {
	private TextView textView;
	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;

	protected static final String TAG = "EmptyApp";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		CouchbaseMobile couch = new CouchbaseMobile(getBaseContext(), mDelegate);
		couchServiceConnection = couch.startCouchbase();
		
		textView = (TextView)findViewById( R.id.batteryLevel );
        registerReceiver( batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED) );
       
    }

    public void updateBatteryCondition(int level)
    {
    textView.setText( level + "%" );
    }
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
		}

		@Override
		public void installing(int completed, int total) {
		}

		@Override
		public void exit(String error) {
		}
	};
}