package com.mcbath.rebecca.beacondetectordemo;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import org.altbeacon.beacon.BeaconParser;

import java.util.List;

/**
 * Created by Rebecca McBath
 * on 2019-07-08.
 */
public class Utils {

	private static final String TAG = "Utils";

	//-- Different Beacon Formats supported by AltBeacon
	static final String LAYOUT_ALTBEACON = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
	static final String LAYOUT_EDDYSTONE_TLM = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
	static final String LAYOUT_EDDYSTONE_UID = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
	static final String LAYOUT_EDDYSTONE_URL = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v";
	static final String LAYOUT_IBEACON = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

	public boolean isBluetoothEnabled() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		return mBluetoothAdapter.isEnabled();
	}

	public void enableBluetooth() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
	}

	public void disableBluetooth() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
		}
	}

	static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

		for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
			Log.d(TAG, String.format("Service:%s", runningServiceInfo.service.getClassName()));
			if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
				return true;
			}
		}
		return false;
	}

	static boolean isEddystone() {

		BeaconParser beaconParser = new BeaconParser();
		String beaconLayout = beaconParser.getLayout();

		if (BeaconParser.EDDYSTONE_UID_LAYOUT.equals(beaconLayout)) {

			return true;
		}

		Log.d(TAG, "isEddystone layout = " + isEddystone());

		return false;
	}
}

