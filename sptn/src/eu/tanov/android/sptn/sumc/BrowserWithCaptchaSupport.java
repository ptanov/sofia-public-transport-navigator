package eu.tanov.android.sptn.sumc;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import eu.tanov.android.bptcommon.Browser;
import eu.tanov.android.bptcommon.utils.ActivityTracker;
import eu.tanov.android.sptn.R;

public class BrowserWithCaptchaSupport extends Browser {
    private static final String TAG = "SimpleBrowserWithCaptchaSupport";

    private static String result = null;
    private static final Object wait = new int[0];

    public BrowserWithCaptchaSupport(int error_retrieveEstimates_matching_noInfo) {
        super(error_retrieveEstimates_matching_noInfo);
    }
    @Override
    protected String getCaptchaText(final Activity context, Handler uiHandler, final Bitmap captchaImage, String stationCode) {

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                final Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setTitle(R.string.captcha_dialog_title);
                final LinearLayout panel = new LinearLayout(context);
                panel.setOrientation(LinearLayout.VERTICAL);
                final TextView label = new TextView(context);
                label.setId(1);
                label.setText(R.string.captcha_dialog_label);
                panel.addView(label);

                final ImageView image = new ImageView(context);
                image.setId(3);
                image.setImageBitmap(captchaImage);
//                try {
//                    image.getLayoutParams().height *= 3;
//                    image.getLayoutParams().width *= 3;
//                } catch (Exception e) {
//                    Log.e(TAG, "Could not scale image", e);
//                    ActivityTracker.couldNotScaleImage(context, e.getMessage());
//                }
                panel.addView(image, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
                fixImageSize(image);
                final EditText input = new EditText(context);
                input.setId(2);
                input.setSingleLine();
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI
                        | InputType.TYPE_TEXT_VARIATION_PHONETIC);
                final ScrollView view = new ScrollView(context);
                panel.addView(input);
                view.addView(panel);

                dialogBuilder.setCancelable(true)
                        .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityTracker.sofiaCaptchaSuccess(context);
                                result = input.getText().toString();

                                synchronized (wait) {
                                    wait.notifyAll();
                                }

                                dialog.dismiss();
                            }
                        }).setView(view);

                dialogBuilder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        ActivityTracker.sofiaCaptchaCancel(context);
                        result = null;
                        synchronized (wait) {
                            wait.notifyAll();
                        }
                    }
                });
                dialogBuilder.create().show();
            }

            private void fixImageSize(ImageView image) {
                try {
                    Display display = context.getWindowManager().getDefaultDisplay();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    display.getMetrics(outMetrics);
                    float scWidth = outMetrics.widthPixels * 0.8f;
                    image.getLayoutParams().width = (int) scWidth;
                    image.getLayoutParams().height = (int) (scWidth / 3f);
                } catch (Exception e) {
                    Log.e(TAG, "Could not scale image", e);
                    ActivityTracker.couldNotScaleImage(context, e.getMessage());
                }
            }

        });

        return waitForResult();
    }
    
    private static String waitForResult() {
        String localResult = null;
        // TODO very very bad code, but no time...
        try {
            synchronized (wait) {
                wait.wait();
            }
            localResult = result;
            result = null;
            if (localResult == null) {
                // user is requesting cancel
                throw new RuntimeException("Cancelled by user");
            }
            return localResult;
        } catch (InterruptedException e) {
            localResult = result;
            result = null;

            if (localResult == null) {
                // user is requesting cancel
                throw new RuntimeException("Cancelled by user");
            }

            return localResult;
        }
    }

}
