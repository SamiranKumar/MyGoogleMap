package com.appsghor.shomu.mygooglemap.util;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import timber.log.Timber;

public interface PermissionsUtil {

    static void checkPermissions(Activity activity, List<String> PermissionsList, IPermissionsCheck iPermissionsCheck) {

        if (!checkPlayServices(activity)) {
            iPermissionsCheck.isAllPermissionsGranted(false);
            return;
        }

        Dexter.withActivity(activity)
                .withPermissions(PermissionsList)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {
                            iPermissionsCheck.isAllPermissionsGranted(true);
                        } else {
                            iPermissionsCheck.isAllPermissionsGranted(false);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        iPermissionsCheck.isAllPermissionsGranted(false);
                        Timber.e("There was an error: " + error.toString());
                    }
                })

                //.withListener(dialogMultiplePermissionsListener)

                .check();

    }


    static boolean checkPlayServices(Activity activity) {
        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            }
            return false;
        }
        return true;
    }


    interface IPermissionsCheck {
        void isAllPermissionsGranted(boolean isGranted);
    }


}
