package eu.tanov.android.sptn.sumc;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.util.TimeHelper;

public class PlainDialogBuilder implements ParserListener {
	
	private final StringBuilder contentBuilder = new StringBuilder();
	private final Activity context;
	private final AlertDialog.Builder dialogBuilder;
	
	private String lastVehicleType = null;
	private final Date date;
	private final boolean showRemainingTime;
	private final String formatMinutesAndHours;
	private final String formatOnlyMinutes;

	public PlainDialogBuilder(Activity context, Date date, boolean showRemainingTime) {
		this.context = context;
		this.date = date;
		this.showRemainingTime = showRemainingTime;

//		if (showRemainingTime) {
			this.formatOnlyMinutes = this.context.getString(R.string.remainingTime_format_onlyMinutes);
			this.formatMinutesAndHours = this.context.getString(R.string.remainingTime_format_minutesAndHours);
//		} else {
//			this.formatOnlyMinutes = null;
//			this.formatMinutesAndHours = null;
//		}
		dialogBuilder = new AlertDialog.Builder(this.context);
	}
	
	public void setStationName(String stationName) {
		dialogBuilder.setTitle(
				context.getString(R.string.format_estimates_dialog_title, 
				DateFormat.getTimeFormat(context).format(date),
				stationName
			)
		);
	}

	public void setEstimatedTime(String vehicleType, String lineNumber, String estimatedTime) {
		if (showRemainingTime) {
			estimatedTime = TimeHelper.toRemainingTime(date, estimatedTime,
					formatOnlyMinutes, formatMinutesAndHours);
		}
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
