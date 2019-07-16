package com.mcbath.rebecca.beacondetectordemo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mcbath.rebecca.beacondetectordemo.Model.AltBeaconModel;
import com.mcbath.rebecca.beacondetectordemo.R;

import org.altbeacon.beacon.Beacon;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rebecca McBath
 * on 2019-07-08.
 */
public class AltBeaconAdapter extends RecyclerView.Adapter<AltBeaconAdapter.ViewHolder> {
	private static final String TAG = "AltBeaconAdapter";

	private ArrayList<AltBeaconModel> list;
	private Context context;

	// Constructor
	public AltBeaconAdapter(ArrayList<AltBeaconModel> list, Context context) {
		this.list = list;
		this.context = context;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

	private TextView uuid;
		private TextView bluetoothName;
		private TextView bluetoothAddress;
		private TextView major;
		private TextView minor;
		private TextView distance;
		private TextView rssi;

		//View Holder Class Constructor
		ViewHolder (View itemView) {
			super(itemView);

			uuid = itemView.findViewById(R.id.uuid);
			major = itemView.findViewById(R.id.major);
			minor = itemView.findViewById(R.id.minor);
			distance = itemView.findViewById(R.id.distance);
			rssi = itemView.findViewById(R.id.rssi);
			bluetoothName = itemView.findViewById(R.id.device_name);
			bluetoothAddress = itemView.findViewById(R.id.mac_address);
		}
	}

	@NonNull
	@Override
	public AltBeaconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the custom layout
		View view = inflater.inflate(R.layout.item_altbeacon, parent, false);

		// Return a new holder instance
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull AltBeaconAdapter.ViewHolder holder, int position) {

		// Getting Array List within respective position
		AltBeaconModel altBeaconModel = list.get(position);

		// Checking if arrayList size > 0
		if (list.size() > 0) {

			holder.uuid.setText(altBeaconModel.getUuid());
			holder.major.setText(altBeaconModel.getMajor());
			holder.minor.setText(altBeaconModel.getMinor());
			holder.distance.setText(altBeaconModel.getDistance());
			holder.rssi.setText(altBeaconModel.getRssi());
			holder.bluetoothName.setText(altBeaconModel.getBluetoothName());
			holder.bluetoothAddress.setText(altBeaconModel.getBluetoothAddress());
		}

	}

	@Override
	public int getItemCount() {
		return list.size();
	}
}