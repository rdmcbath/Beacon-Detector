package com.mcbath.rebecca.beacondetectordemo.UI;

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
import android.widget.TextView;
import com.mcbath.rebecca.beacondetectordemo.Adapter.Adapter;
import com.mcbath.rebecca.beacondetectordemo.BeaconApplication;
import com.mcbath.rebecca.beacondetectordemo.Model.AltBeaconModel;
import com.mcbath.rebecca.beacondetectordemo.Model.BeaconTypes;
import com.mcbath.rebecca.beacondetectordemo.Model.EddystoneBeaconModel;
import com.mcbath.rebecca.beacondetectordemo.R;
import com.mcbath.rebecca.beacondetectordemo.Utils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	private static final String TAG = "BeaconApp";

	private RecyclerView rv;
	private RecyclerView.LayoutManager layoutManager;
	private RecyclerView.Adapter adapter;
	private List<BeaconTypes> beaconTypesList = null;
	private BeaconManager beaconManager;
	private ProgressBar pb;
	private TextView emptyView;
	BeaconApplication beaconApplication;

	public BeaconScanFragment() {
	// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getActivity() != null) {

			beaconApplication = (BeaconApplication) getApplicationContext();

			beaconManager = BeaconManager.getInstanceForApplication(getActivity());

			// To detect proprietary beacons, you must add a line like below corresponding to your beacon
			// type. Do a web search for "setBeaconLayout" to get the proper expression.
			beaconManager.getBeaconParsers().add(new BeaconParser("IBeacon").setBeaconLayout(Utils.LAYOUT_IBEACON));
			beaconManager.getBeaconParsers().add(new BeaconParser("AlBeacon").setBeaconLayout(Utils.LAYOUT_ALTBEACON));
			beaconManager.getBeaconParsers().add(new BeaconParser("Eddystone-UID").setBeaconLayout(Utils.LAYOUT_EDDYSTONE_UID));
			beaconManager.getBeaconParsers().add(new BeaconParser("Eddystone-TLM").setBeaconLayout(Utils.LAYOUT_EDDYSTONE_TLM));
//			beaconManager.getBeaconParsers().add(new BeaconParser("Eddystone-URL").setBeaconLayout(Utils.LAYOUT_EDDYSTONE_URL));

			//Binding to the BeaconService.
			beaconManager.bind(this);

		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_beacon_scan, container, false);

		emptyView = v.findViewById(R.id.empty_view);
		rv = v.findViewById(R.id.scan_recycler);
		pb = v.findViewById(R.id.pb);

		// Setting up the layout manager to be linear
		layoutManager = new LinearLayoutManager(getActivity());
		rv.setLayoutManager(layoutManager);

		emptyView.setVisibility(View.VISIBLE);
		pb.setVisibility(View.VISIBLE);

		return v;
	}

	@Override
	public void onBeaconServiceConnect() {
		Log.d(TAG, "onBeaconServiceConnect called");

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
					beaconApplication.didEnterRegion(region);
					//Tells the BeaconService to start looking for beacons that match the passed Region object
					// and provides updates on the estimated distance every second while beacons in the Region
					// are visible.
					beaconManager.startRangingBeaconsInRegion(region);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			/*
				 This override method is run when a previously found beacon is no longer in range.
			 */
			@Override
			public void didExitRegion(Region region) {
				Log.d(TAG, "EXIT----------------------->");
				try {
					beaconApplication.didExitRegion(region);
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
				Log.d(TAG, "I have just switched from seeing/not seeing beacons: " + state);
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
				Log.d(TAG, "didRangeBeaconsInRegion called");

				// Checking if the AltBeaconModel inside the collection (ex. list) is there or not
				// if AltBeaconModel is detected then size of collection is > 0
				if (beacons.size() > 0) {

					// Iterating through collection of Beacons
					for (Beacon beacon : beacons) {

						if (beacon.getServiceUuid() == 0xfeaa) { // Eddystone frame uses a service Uuid of 0xfeaa
							Log.d(TAG, "didRangeBeaconsInRegion: this is an EDDYSTONE frame");

							final ArrayList<EddystoneBeaconModel> arrayListEd = new ArrayList<>();
							EddystoneBeaconModel model = new EddystoneBeaconModel();

							model.setNameSpace(String.valueOf(beacon.getId1()));
							model.setInstanceId(String.valueOf(beacon.getId2()));
//							model.setBluetoothName(String.valueOf(beacon.getBluetoothName()));
//							model.setBluetoothAddress(String.valueOf(beacon.getBluetoothAddress()));
//							model.setRssi(String.valueOf(beacon.getRssi()));
//							model.setTxPower(String.valueOf(beacon.getTxPower()));
							double distance1 = beacon.getDistance();
							String distance = String.format(Locale.getDefault(), "%.2f", distance1);
							model.setDistance(distance);

							arrayListEd.add(model);

							try {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {

										emptyView.setVisibility(View.GONE);
										pb.setVisibility(View.GONE);

										// Setting Up the Adapter for Recycler View
										adapter = new Adapter(beaconTypesList, getActivity());
										rv.setAdapter(adapter);
										((Adapter) adapter).setBeaconTypesList(arrayListEd);
									}
								});
							} catch (Exception e) {
								Log.d(TAG, e.toString());
							}

						} else { // AltBeacon or iBeacon frame
							Log.d(TAG, "didRangeBeaconsInRegion: this is an ALTBEACON frame");

							final ArrayList<AltBeaconModel> arrayListAlt = new ArrayList<>();
							AltBeaconModel model = new AltBeaconModel();

							model.setUuid(String.valueOf(beacon.getId1()));
							model.setMajor(String.valueOf(beacon.getId2()));
							model.setMajor(String.valueOf(beacon.getId3()));
							double distance1 = beacon.getDistance();
							String distance = String.format(Locale.getDefault(), "%.2f", distance1);
							model.setDistance(distance);
							model.setRssi(String.valueOf(beacon.getRssi()));
							model.setBluetoothName(String.valueOf(beacon.getBluetoothName()));
							model.setBluetoothAddress(String.valueOf(beacon.getBluetoothAddress()));

							arrayListAlt.add(model);

							try {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {

										emptyView.setVisibility(View.GONE);
										pb.setVisibility(View.GONE);

										// Setting Up the Adapter for Recycler View
										adapter = new Adapter(beaconTypesList, getActivity());
										rv.setAdapter(adapter);
										((Adapter) adapter).setBeaconTypesList(arrayListAlt);
									}
								});
							} catch (Exception e) {
								Log.d(TAG, e.toString());
							}
						}

						// if AltBeaconModel is not detected then size of collection is = 0
						if (beacons.size() == 0) {
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

		} catch (RemoteException e) {
			Log.d(TAG, e.toString());
		}
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
		beaconApplication.disableMonitoring();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView called");
		//Unbinds an Android Activity or Service to the BeaconService to avoid a leak.
		beaconManager.unbind(this);
		beaconApplication.disableMonitoring();
	}

	@Override
	public void onResume() {
		super.onResume();
		beaconApplication.enableMonitoring();
		beaconManager.bind(this);
		Log.d(TAG, "onResume called");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause called");
		beaconManager.unbind(this);
	}
}

