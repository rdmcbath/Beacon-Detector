package com.mcbath.rebecca.beacondetectordemo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcbath.rebecca.beacondetectordemo.Model.EddystoneBeaconModel;
import com.mcbath.rebecca.beacondetectordemo.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rebecca McBath
 * on 2019-07-16.
 */
public class EddystoneAdapter extends RecyclerView.Adapter<EddystoneAdapter.ViewHolder> {
	private static final String TAG = "EddystoneAdapter";

	private ArrayList<EddystoneBeaconModel> list;
	private Context context;

	// Constructor
	public EddystoneAdapter(ArrayList<EddystoneBeaconModel> list, Context context) {
		this.list = list;
		this.context = context;
	}

	// Provide a direct reference to each of the views within a data item
	// Used to cache the views within the item layout for fast access
	public class ViewHolder extends RecyclerView.ViewHolder {
		// Your holder should contain a member variable
		// for any view that will be set as you render a row
		private TextView namespace;
		private TextView bluetoothName;
		private TextView bluetoothAddress;
		private TextView instanceId;
		private TextView txPower;
		private TextView distance;
		private TextView rssi;

		// We also create a constructor that accepts the entire item row
		// and does the view lookups to find each subview
		ViewHolder(View itemView) {
			// Stores the itemView in a public final member variable that can be used
			// to access the context from any ViewHolder instance.
			super(itemView);

			namespace = itemView.findViewById(R.id.namespace);
			instanceId = itemView.findViewById(R.id.instance);
			txPower = itemView.findViewById(R.id.tx_power);
			distance = itemView.findViewById(R.id.distance);
			rssi = itemView.findViewById(R.id.rssi);
			bluetoothName = itemView.findViewById(R.id.device_name);
			bluetoothAddress = itemView.findViewById(R.id.mac_address);
		}
	}

	@NonNull
	@Override
	public EddystoneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the custom layout
		View view = inflater.inflate(R.layout.item_eddystone_beacon, parent, false);

		// Return a new holder instance
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		// Getting Array List within respective position
		EddystoneBeaconModel beaconModel = list.get(position);

			holder.namespace.setText(beaconModel.getNameSpace());
			holder.instanceId.setText(beaconModel.getInstanceId());
			holder.rssi.setText(beaconModel.getRssi());
			holder.bluetoothName.setText(beaconModel.getBluetoothName());
			holder.bluetoothAddress.setText(beaconModel.getBluetoothAddress());
			holder.txPower.setText(beaconModel.getTxPower());
			holder.distance.setText(beaconModel.getDistance());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}
}