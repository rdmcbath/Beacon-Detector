package com.mcbath.rebecca.beacondetectordemo;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.ArrayList;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by Rebecca McBath
 * on 2019-07-08.
 */
public class BeaconApplication extends Application implements BootstrapNotifier {
	private static final String TAG = "BeaconApplication";
	private RegionBootstrap regionBootstrap;
	private BackgroundPowerSaver backgroundPowerSaver;
	private boolean haveDetectedBeaconsSinceBoot = false;
	private MainActivity mainActivity = null;
	private String cumulativeLog = "";
	private NotificationManager nm = null;

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate - BEACON APPLICATION");

		BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

		// By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
		// find a different type of beacon, you must specify the byte layout for that beacon's
		// advertisement with a line like below.  The example shows how to find a beacon with the
		// same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
		// layout expression for other beacon types, do a web search for "setBeaconLayout"
		// including the quotes.
		beaconManager.getBeaconParsers().clear();
		// Detect iBeacon:
		beaconManager.getBeaconParsers().add(new BeaconParser(). setBeaconLayout(Utils.LAYOUT_IBEACON));
		// Detect AltBeacon:
		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_ALTBEACON));
//		// Detect Eddystone (UID) frame:
//		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_UID));
//		// Detect Eddystone telemetry (TLM) frame:
//		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_TLM));
//		// Detect Eddystone URL frame:
//		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_URL));

		beaconManager.setDebug(true);

		// Uncomment the code below to use a foreground service to scan for beacons. This unlocks
		// the ability to continually scan for long periods of time in the background on Andorid 8+
		// in exchange for showing an icon at the top of the screen and a always-on notification to
		// communicate to users that your app is using resources in the background.
		//

		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.ic_search);
		builder.setContentTitle("Scanning for Beacons...");
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
		);
		builder.setContentIntent(pendingIntent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
					"Beacon Detection", NotificationManager.IMPORTANCE_DEFAULT);
			channel.setDescription("Scanning in the background");
			NotificationManager notificationManager = (NotificationManager) getSystemService(
					Context.NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(channel);
			builder.setChannelId(channel.getId());
		}
		beaconManager.enableForegroundServiceScanning(builder.build(), 456);

		// For the above foreground scanning service to be useful, you need to disable
		// JobScheduler-based scans (used on Android 8+) and set a fast background scan
		// cycle that would otherwise be disallowed by the operating system.
		beaconManager.setEnableScheduledScanJobs(false);
		beaconManager.setBackgroundBetweenScanPeriod(0l);
		beaconManager.setBackgroundScanPeriod(1100);
		beaconManager.setForegroundBetweenScanPeriod(0l);
		beaconManager.setForegroundScanPeriod(1100);
		Log.d(TAG, "setting up background monitoring for beacons and power saving");

		double scanPeriod = beaconManager.getBackgroundScanPeriod();
		Log.d(TAG, "background scan period in Alt Beacon library is set to " + scanPeriod);

		// wake up the app when a beacon is seen
		Region region = new Region("backgroundRegion", null, null, null);

		BeaconManager.setDebug(true);
//		Region region1 = new Region("backgroundRegion1", Identifier.parse("9f06af5f-3b9a-4878-babc-9363e6838219"), null, null);
//		Region region2 = new Region("backgroundRegion2", Identifier.parse("2c3b5ade-cf0f-4c3f-b49f-c857e2a077e4"), null, null);
//		Region region3 = new Region("backgroundRegion3", Identifier.parse("07964534-ff2b-41b7-9cd1-f61634f78f54"), null, null);
//		ArrayList<Region> regions = new ArrayList<>();
//		regions.add(region1);
//		regions.add(region2);
//		regions.add(region3);

		regionBootstrap = new RegionBootstrap(this, region);

		// simply constructing this class and holding a reference to it in your custom Application
		// class will automatically cause the BeaconLibrary to save battery whenever the application
		// is not visible.  This reduces bluetooth power usage by about 60%
		backgroundPowerSaver = new BackgroundPowerSaver(this);

		// If you wish to test beacon detection in the Android Emulator, you can use code like this:
//		BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
//		if (BeaconManager.getBeaconSimulator() != null) {
//			((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
//		}
	}

	public void disableMonitoring() {
		if (regionBootstrap != null) {
			regionBootstrap.disable();
			regionBootstrap = null;
		}
	}
	public void enableMonitoring() {

		Region region = new Region("backgroundRegion", null, null, null);
//		Region region1 = new Region("backgroundRegion1", Identifier.parse("9f06af5f-3b9a-4878-babc-9363e6838219"), null, null);
//		Region region2 = new Region("backgroundRegion2", Identifier.parse("2c3b5ade-cf0f-4c3f-b49f-c857e2a077e4"), null, null);
//		Region region3 = new Region("backgroundRegion3", Identifier.parse("07964534-ff2b-41b7-9cd1-f61634f78f54"), null, null);
//		ArrayList<Region> regions = new ArrayList<>();
//		regions.add(region1);
//		regions.add(region2);
//		regions.add(region3);

		regionBootstrap = new RegionBootstrap(this, region);
	}

	@Override
	public void didEnterRegion(Region region) {
		// In this example, this class sends a notification to the user whenever a Beacon
		// matching a Region (defined above) are first seen.
		Log.d(TAG, "did enter region.");

		if (!haveDetectedBeaconsSinceBoot) {
			Log.d(TAG, "auto launching MainActivity");

			// The very first time since boot that we detect an beacon, we launch MainActivity
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// Important:  make sure to add android:launchMode="singleInstance" in the manifest
			// to keep multiple copies of this activity from getting created if the user has
			// already manually launched the app.
			this.startActivity(intent);
			haveDetectedBeaconsSinceBoot = true;

		} else {
			if (mainActivity != null) {
				// If the Monitoring Activity is visible, we log info about the beacons we have
				// seen on its display
				Log.d(TAG, "I see a beacon" );
			} else {
				// If we have already seen beacons before, but the monitoring activity is not in
				// the foreground, we send a notification to the user on subsequent detections.
				Log.d(TAG, "Sending notification.");
				sendNotification();
			}
		}
	}

	@Override
	public void didExitRegion(Region region) {
		Log.d(TAG, "I no longer see a beacon" );
	}

	@Override
	public void didDetermineStateForRegion(int state, Region region) {
		Log.d(TAG, "Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
	}

	public void sendNotification() {

		String msg = "New beacon found!";
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_search)
				.setColor(ContextCompat.getColor(this, R.color.colorPrimary))
				.setContentTitle(msg)
				.setContentText("Open the app to see details") // message for notification
				.setLights(Color.GREEN, 5000, 1000)
				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
				.setVibrate(new long[]{0, 500, 500}) //delay, on, off, again...
				.setOnlyAlertOnce(true)
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setTicker(msg)
				.setAutoCancel(true); // clear notification after click

		// If android v8.0 Oreo or higher, use notification channel.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Log.d(TAG, "newBeaconNotification: Device is Android 8.0 or higher");
			String channelId = "com.mcbath.rebecca.beacondetectordemo.channel";
			CharSequence name = "BeaconDetectorDemo";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel notifChannel = new NotificationChannel(channelId, name, importance);
			NotificationManager mNotificationManager = getNotificationManager();
			mNotificationManager.createNotificationChannel(notifChannel);
			mBuilder.setChannelId(channelId);
		}

		mBuilder.setContentIntent(getActivityIntent(this));
		NotificationManager mNotificationManager = getNotificationManager();
		mNotificationManager.notify(1, mBuilder.build());
	}

	private NotificationManager getNotificationManager() {
		if (nm == null) {
			this.nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return nm;
	}

	protected static PendingIntent getActivityIntent(Context applicationContext) {

		Intent intent = new Intent(applicationContext, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		int requestID = (int) System.currentTimeMillis();

		return PendingIntent.getActivity(applicationContext, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void setMainActivity(MainActivity activity) {
		this.mainActivity = activity;
	}

//	private void logToDisplay(String line) {
//		cumulativeLog += (line + "\n");
//		if (this.mainActivity() != null) {
//			this.mainActivity.updateLog(cumulativeLog);
//		}
//	}

	public String getLog() {
		return cumulativeLog;
	}
}

