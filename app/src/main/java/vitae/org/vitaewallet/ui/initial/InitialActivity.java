package vitae.org.vitaewallet.ui.initial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import vitae.org.vitaewallet.VitaeApplication;
import vitae.org.vitaewallet.ui.splash_activity.SplashActivity;
import vitae.org.vitaewallet.ui.wallet_activity.WalletActivity;
import vitae.org.vitaewallet.utils.AppConf;

/**
 * Created by kaali on 8/19/17.
 */

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VitaeApplication vitaeApplication = VitaeApplication.getInstance();
        AppConf appConf = vitaeApplication.getAppConf();
        // show report dialog if something happen with the previous process
        Intent intent;
        if (!appConf.isAppInit() || appConf.isSplashSoundEnabled()){
            intent = new Intent(this, SplashActivity.class);
        }else {
            intent = new Intent(this, WalletActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
