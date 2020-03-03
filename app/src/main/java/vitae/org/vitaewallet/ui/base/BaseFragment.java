package vitae.org.vitaewallet.ui.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import vitae.org.vitaewallet.VitaeApplication;
import vitae.org.vitaewallet.module.VitaeModule;

/**
 * Created by kaali on 6/29/17.
 */

public class BaseFragment extends Fragment {

    protected VitaeApplication vitaeApplication;
    protected VitaeModule vitaeModule;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vitaeApplication = VitaeApplication.getInstance();
        vitaeModule = vitaeApplication.getModule();
    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getActivity(),permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
