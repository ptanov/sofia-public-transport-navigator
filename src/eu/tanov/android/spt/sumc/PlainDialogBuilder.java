package eu.tanov.android.spt.sumc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import eu.tanov.android.spt.R;

public class PlainDialogBuilder implements ParserListener {
//TODO add server time in dialog
//TODO add text "get from m.sumc.bg
//TODO add option for remaining time, not estimate hour
	
	private final StringBuilder contentBuilder = new StringBuilder();
	private final Activity context;
	private final AlertDialog.Builder dialogBuilder;
	
	private String lastVehicleType = null;

	public PlainDialogBuilder(Activity context) {
		this.context = context;

		dialogBuilder = new AlertDialog.Builder(this.context);
	}
	
	public void setStationName(String stationName) {
		dialogBuilder.setTitle(stationName);
	}

	public void setEstimatedTime(String vehicleType, String lineNumber, String estimatedTime) {
		if (!vehicleType.equals(lastVehicleType)) {
			lastVehicleType = vehicleType;
			contentBuilder.append(localizedVehicleType(vehicleType));
			contentBuilder.append(":\n");
		}
		contentBuilder.append("	");
		contentBuilder.append(lineNumber);
		contentBuilder.append(": ");
		contentBuilder.append(estimatedTime);
		contentBuilder.append("\n");
	}

	//XXX very bad code:
	private String localizedVehicleType(String vehicleType) {
		if ("busHeader".equals(vehicleType)) {
			return context.getResources().getString(R.string.vehicleType_bus);
		} else if ("trolleyHeader".equals(vehicleType)) {
			return context.getResources().getString(R.string.vehicleType_trolley);
		} else if ("tramHeader".equals(vehicleType)) {
			return context.getResources().getString(R.string.vehicleType_tram);
		} else {
			throw new IllegalArgumentException("Unknown vehicle type: "+vehicleType);
		}
	}

	public AlertDialog create() {
		contentBuilder.append(context.getString(R.string.legal_sumc_plain));
		dialogBuilder.setMessage(contentBuilder.toString())
		       .setCancelable(true)
		       .setPositiveButton(R.string.buttonOk,
		    		   new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.dismiss();
				           }
		       			}
		       );
		return dialogBuilder.create();
	}
}
