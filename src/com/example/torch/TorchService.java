package com.example.torch;

import java.util.List;

import javax.net.ssl.ManagerFactoryParameters;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Filter;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;

public class TorchService extends Service {
	public final static String Tag = "TorchService";
	public final static String ACTION = "OPEN_FLASHLIGHT_BACKGROUND";
	public final static int NOTIFY_ID = 430725; 
	public final static String StartCheckBroast = "com.torch.START_CHECK_BROAST";
	public final static String TurnTorchBroast = "com.torch.TURN_TORCH_BROAST";
	
	private CheckRunnable checkRunnable = null;
	private boolean onOff = false;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(Tag, "onCreate");
		registerReceiver(new StartCheckBroadcast(), new IntentFilter(StartCheckBroast));
		registerReceiver(new TurnTorchBroadcast(), new IntentFilter(TurnTorchBroast));
		
		checkRunnable = new CheckRunnable();
		setMsgNotification();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.d(Tag, "onCreate");
		return 1;
	}
	

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	public void setMsgNotification(){
		NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mNotify = new Notification();//(R.drawable.notify_torch, "通知", System.currentTimeMillis());
		mNotify.icon = R.drawable.notify_torch;
		//放置到正在运行状态栏
		mNotify.flags = Notification.FLAG_ONGOING_EVENT;
		
		RemoteViews mContentView = new RemoteViews(getPackageName(), R.drawable.notify_torch);
		mContentView.setTextViewText(R.drawable.notify_torch, "手电筒");
		
		mNotify.contentView = mContentView;
		
		Intent intent = new Intent(this, TorchService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotify.contentIntent = contentIntent;
		mManager.notify(NOTIFY_ID, mNotify);
	}
	
	class TurnTorchBroadcast extends BroadcastReceiver{
		public final String Tag = "TurnTorchBroadcast";
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(Tag, "onReceive");
			
			//从activity发出的广播，打开或关闭手电筒
			if(intent.getBooleanExtra("onOff", false)){
				onOff = !onOff;
				Torch.getInstance().turnOnOffTorch(onOff);
			}
		}
	}	
	
	class StartCheckBroadcast extends BroadcastReceiver{
		public final String Tag = "StartCheckBroadcast";

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(Tag, "onReceive");
			
			//如果activity在后台运行，并且手电筒此时是打开的，那么则开启检查线程，否则不开启检查线程
			if(intent.getBooleanExtra("startCheck", false)){
				if(Torch.getInstance().isOpened()){
					if(checkRunnable != null && checkRunnable.isRunning() == false){
						checkRunnable.setDoCheck(true);
						Log.d(Tag, Boolean.toString(checkRunnable.isRunning()));
						new Thread(checkRunnable).start();
					}
				}
			}else{
				checkRunnable.setDoCheck(false);
			}
		}
	}
	
	class CheckRunnable implements Runnable{
		private final static String Tag = "CheckRunnable";
		
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		private boolean isRunning = false;
		private boolean doCheck = false;
		
		public void run() {
			// TODO Auto-generated method stub
			if(isRunning){
				return;
			}
			
			isRunning = true;
			while(doCheck){
				/**获取当前正在运行的任务栈列表，越是靠近当前运行的任务栈会被排在第一位**/
				List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
				
				/**获取当前最顶端的任务栈，即前台任务栈**/
				RunningTaskInfo runningTaskInfo = runningTasks.get(0);
				
				/**获取当前任务栈的最顶端Activity**/
				ComponentName topActivity = runningTaskInfo.topActivity;
				
				/**获取应用的包名**/
				String packageName = topActivity.getPackageName();
				
				if(packageName.contains("camera")){
					Log.d(Tag, "camera is opened, now turn off the torch");
					Torch.getInstance().turnOnOffTorch(false);
					break;
				}
				
				SystemClock.sleep(2000);
			}
			isRunning = false;
		}
		
		public boolean isRunning(){
			return isRunning;
		}
		
		public void setDoCheck(boolean check){
			doCheck = check;
		}
	}
	
}


class Torch {
	private final static String Tag = "Torch";
	private static Torch torchInstance = null;
	
	private Camera camera = null;
	private boolean isOpened = false;
	
	private Torch(){};
	
	public static Torch getInstance(){
		if(torchInstance == null){
			torchInstance = new Torch();
		}
		return torchInstance;
	}
	
	public boolean turnOnOffTorch(boolean onoff){
		if(onoff){
			if(!isOpened){
				camera = Camera.open();
				Camera.Parameters param = camera.getParameters();
				param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				camera.setParameters(param);
				camera.startPreview();
				isOpened = true;
			}
		}else{
			if(isOpened){
				camera.stopPreview();
				camera.release();
				isOpened = false;
			}
		}
		return isOpened;
	}
	
	public boolean isOpened(){
		return isOpened;
	}
}