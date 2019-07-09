package com.mcbath.rebecca.beacondetectordemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rebecca McBath
 * on 2019-07-08.
 */
public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.ViewHolder> {

	private ArrayList<ArrayList<String>> arr;

	// Constructor
	public BeaconAdapter(ArrayList<ArrayList<String>> arr) {
		this.arr = arr;
	}

	/*
	   View Holder class to instantiate views
	 */
	class ViewHolder extends RecyclerView.ViewHolder{

		//UUID
		private TextView uuid;
		//Bluetooth Name
		private TextView bluetoothName;
		//Bluetooth Mac Address
		private TextView bluetoothAddress;
		//Major
		private TextView major;
		//Minor
		private TextView minor;
		//Distance
		private TextView distance;
		//RSSI
		private TextView rssi;

		//View Holder Class Constructor
		public ViewHolder(View itemView)
		{
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

	@Override
	public BeaconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beacon, parent,false));
	}


	@Override
	public void onBindViewHolder(@NonNull BeaconAdapter.ViewHolder holder, int position) {

		// Getting Array List within respective position
		ArrayList<String> arrayList = arr.get(position);

		// Checking if arrayList size > 0
		if (arrayList.size()>0){

			// UUID
			holder.uuid.setText(arrayList.get(0));
			// Major
			holder.major.setText(arrayList.get(1));
			// Minor
			holder.minor.setText(arrayList.get(2));
			// Distance
			holder.distance.setText(arrayList.get(3));
			// Rssi
			holder.rssi.setText(arrayList.get(4));
			// Bluetooth Name
			holder.bluetoothName.setText(arrayList.get(5));
			// Bluetooth (Mac) Address
			holder.bluetoothAddress.setText(arrayList.get(6));
		}
	}

	@Override
	public int getItemCount() {
		return arr.size();
	}
}