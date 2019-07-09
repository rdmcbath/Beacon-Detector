package com.mcbath.rebecca.beacondetectordemo;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rebecca McBath
 * on 2019-07-08.
 */
public class BeaconScanFragment extends Fragment implements BeaconConsumer {
	private static final String TAG = "BeaconSearchFragment";

	private RelativeLayout emptyView;
	private RecyclerView rv;
	private RecyclerView.LayoutManager layoutManager;
	private RecyclerView.Adapter adapter;
	private BeaconManager beaconManager;
	private ProgressBar pb;
	BeaconApplication application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getActivity() != null) {

			application = ((BeaconApplication) this.getApplicationContext());

			beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

			// To detect proprietary beacons, you must add a line like below corresponding to your beacon
			// type. Do a web search for "setBeaconLayout" to get the proper expression.
			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_IBEACON));
			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_ALTBEACON));
//			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_UID));
//			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_URL));
//			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(Utils.LAYOUT_EDDYSTONE_TLM));

			//Binding to the BeaconService.
			beaconManager.bind(this);

		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_beacon_scan, container, false);

		emptyView = v.findViewById(R.id.empty_view_layout);
		rv = v.findViewById(R.id.scan_recycler);
		pb = v.findViewById(R.id.pb);

		// Setting up the layout manager to be linear
		layoutManager = new LinearLayoutManager(getActivity());
		rv.setLayoutManager(layoutManager);

		return v;
	}

	@Override
	public void onBeaconServiceConnect() {

		//Constructing a new Region object to be used for Ranging or Monitoring
		Region region = new Region("backgroundRegion", null, null, null);

		//Specifies a class that should be called each time the BeaconService sees or stops seeing a Region of beacons.
		beaconManager.addMonitorNotifier(new MonitorNotifier() {

			/*
				This override method is run when a beacon comes into range.
			*/
			@Override
			public void didEnterRegion(Region region) {
				Log.d(TAG, "ENTER ------------------->");
				try {
					application.didEnterRegion(region);
					//Tells the BeaconService to start looking for beacons that match the passed Region object
					// and provides updates on the estimated distance every second while beacons in the Region
					// are visible.
					beaconManager.startRangingBeaconsInRegion(region);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
//				application.sendNotification();
//				Log.d(TAG, "Send Notification of new beacon found");
			}

			/*
				 This override method is run when a previously found beacon is no longer in range.
			 */
			@Override
			public void didExitRegion(Region region) {
				Log.d(TAG, "EXIT----------------------->");
				try {
					application.didExitRegion(region);
					// Tell the BeaconService to stop looking for beacons
					// that match the passed Region object and providing mDistance
					// information for them.
					beaconManager.stopRangingBeaconsInRegion(region);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			/*
				This override method will determine the state for the device, whether device is in range
			   of beacon or not, if yes then i = 1 and if no then i = 0
			*/
			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				System.out.println( "I have just switched from seeing/not seeing beacons: " + state);
			}
		});

		// Specifies a class that should be called each time the BeaconService gets ranging data,
		// which is nominally once per second when beacons are detected.
		beaconManager.addRangeNotifier(new RangeNotifier() {

			/*
			   This Override method tells us all the collections of beacons and their details that
			   are detected within the range by device
			 */
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

				// Checking if the Beacon inside the collection (ex. list) is there or not
				// if Beacon is detected then size of collection is > 0
				if (beacons.size() > 0) {

					if (isAdded()) {
						try {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {

									emptyView.setVisibility(View.GONE);
									rv.setVisibility(View.VISIBLE);

								}
							});
						} catch (Exception e) {
							Log.d(TAG, e.toString());
						}

						final ArrayList<ArrayList<String>> arrayList = new ArrayList<>();

						// Iterating through collection of Beacons
						for (Beacon b : beacons) {

							//UUID
							String uuid = String.valueOf(b.getId1());
							//Major
							String major = String.valueOf(b.getId2());
							//Minor
							String minor = String.valueOf(b.getId3());
							//Distance
							double distance1 = b.getDistance();
							String distance = String.format(Locale.getDefault(), "%.2f", distance1);
							//RSSI
							String rssi = String.valueOf(b.getRssi());
							//Bluetooth Name
							String bluetoothName = String.valueOf(b.getBluetoothName());
							//Bluetooth Address
							String bluetoothAddress = String.valueOf(b.getBluetoothAddress());

							ArrayList<String> arr = new ArrayList<String>();
							arr.add(uuid);
							arr.add(major);
							arr.add(minor);
							arr.add(distance + " meters");
							arr.add(rssi);
							arr.add(bluetoothName);
							arr.add(bluetoothAddress);

							arrayList.add(arr);
						}

						try {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {

									// Setting Up the Adapter for Recycler View
									adapter = new BeaconAdapter(arrayList);
									rv.setAdapter(adapter);
									adapter.notifyDataSetChanged();
								}
							});
						} catch (Exception e) {
							Log.d(TAG, e.toString());
						}

					}
					// if Beacon is not detected then size of collection is = 0
					else {

						if (isAdded()) {

							try {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										emptyView.setVisibility(View.VISIBLE);
										rv.setVisibility(View.GONE);
									}
								});
							} catch (Exception e) {
								Log.d(TAG, e.toString());
							}
						}
					}
				}
			}
		});

		try {
			//Tell the BeaconService to start looking for beacons that match the passed Region object.
			beaconManager.startMonitoringBeaconsInRegion(region);

		} catch (RemoteException e) { Log.d(TAG, e.toString()); }
	}

	@Override
	public Context getApplicationContext() {
		return getActivity().getApplicationContext();
	}

	@Override
	public void unbindService(ServiceConnection serviceConnection) {
		getActivity().unbindService(serviceConnection);
	}

	@Override
	public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
		return getActivity().bindService(intent, serviceConnection, i);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy called");
		//Unbinds an Android Activity or Service to the BeaconService to avoid a leak.
		beaconManager.unbind(this);
		application.disableMonitoring();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView called");
		//Unbinds an Android Activity or Service to the BeaconService to avoid a leak.
		beaconManager.unbind(this);
		application.disableMonitoring();
	}

	@Override
	public void onResume() {
		super.onResume();
		application.enableMonitoring();
		beaconManager.bind(this);
		Log.d(TAG, "onResume called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause called");
		beaconManager.unbind(this);
		application.disableMonitoring();
	}
}

