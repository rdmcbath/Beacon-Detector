package com.mcbath.rebecca.beacondetectordemo.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.mcbath.rebecca.beacondetectordemo.BeaconApplication;
import com.mcbath.rebecca.beacondetectordemo.R;
import com.mcbath.rebecca.beacondetectordemo.UI.BeaconScanFragment;

import org.altbeacon.beacon.BeaconManager;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "BeaconApp";

	private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

	private BeaconApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeToolbar();
		checkLocationPermission();
		verifyBluetooth();

		application = ((BeaconApplication) this.getApplicationContext());
		application.setMainActivity(this);
		application.enableMonitoring();

		BeaconScanFragment beaconScanFragment = new BeaconScanFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, beaconScanFragment)
				.commit();
	}

	public void initializeToolbar(){
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	public void checkLocationPermission(){

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Android M Permission check
			if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
				builder.setTitle("This app needs location access");
				builder.setMessage("Please grant location access so this app can detect beacons in the background.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@TargetApi(23)
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
								PERMISSION_REQUEST_COARSE_LOCATION);
					}

				});
				builder.show();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_COARSE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "coarse location permission granted");
				} else {
					final android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
		}
	}

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(getApplicationContext()).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						//finish();
						//System.exit(0);
					}
				});
				builder.show();
			}
		} catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					//finish();
					//System.exit(0);
				}
			});
			builder.show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		application.enableMonitoring();
		application.setMainActivity(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		application.setMainActivity(null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		application.disableMonitoring();
		application.setMainActivity(null);
	}
}
