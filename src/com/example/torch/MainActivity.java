package com.example.torch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final String Tag = "TorchActivity";
	
	private static Button  LightLambButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Tag, "onCreate");
		
		setContentView(R.layout.activity_main);
		
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(TorchService.ACTION);
		startService(serviceIntent);

		LightLambButton = (Button)findViewById(R.id.TorchSwitch);
		LightLambButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle bundle = new Bundle();
				bundle.putBoolean("onOff", true);
				sendMessage(TorchService.TurnTorchBroast, bundle);
			}
		});
	}
	
	@Override
	public void onStart(){
		super.onStart();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Bundle bundle = new Bundle();
		bundle.putBoolean("startCheck", false);
		sendMessage(TorchService.StartCheckBroast, bundle);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Bundle bundle = new Bundle();
		bundle.putBoolean("startCheck", true);
		sendMessage(TorchService.StartCheckBroast, bundle);
	}
	
	private void sendMessage(String service, Bundle bundle){
		Intent intent = new Intent();
		intent.setAction(service);
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
	}
}
