package vitae.org.vitaewallet.ui.wallet_activity;

import vitae.org.vitaewallet.BuildConfig;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.sendj.core.Coin;
import org.sendj.core.InsufficientMoneyException;
import org.sendj.core.Transaction;
import org.sendj.uri.SendURI;
import org.sendj.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import vitae.org.vitaewallet.VitaeApplication;
import vitae.org.vitaewallet.R;
import vitae.org.vitaewallet.module.CantSweepBalanceException;
import vitae.org.vitaewallet.module.NoPeerConnectedException;
import vitae.org.vitaewallet.rate.db.VitaeRate;
import vitae.org.vitaewallet.service.IntentsConstants;
import vitae.org.vitaewallet.ui.base.BaseDrawerActivity;
import vitae.org.vitaewallet.ui.base.dialogs.DialogListener;
import vitae.org.vitaewallet.ui.base.dialogs.SimpleTextDialog;
import vitae.org.vitaewallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import vitae.org.vitaewallet.ui.qr_activity.QrActivity;
import vitae.org.vitaewallet.ui.settings_backup_activity.SettingsBackupActivity;
import vitae.org.vitaewallet.ui.transaction_send_activity.SendActivity;
import vitae.org.vitaewallet.ui.upgrade.UpgradeWalletActivity;
import vitae.org.vitaewallet.utils.DialogsUtil;
import vitae.org.vitaewallet.utils.scanner.ScanActivity;

import static android.Manifest.permission.CAMERA;
import static vitae.org.vitaewallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static vitae.org.vitaewallet.service.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static vitae.org.vitaewallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static vitae.org.vitaewallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {

    private static final int SCANNER_RESULT = 122;

    private View root;
    private View container_txs;
    private FloatingActionButton fab_add;

    private TextView txt_value;
    private TextView txt_unnavailable;
    private TextView txt_local_currency;
    private TextView txt_watch_only;
    private VitaeRate vitaeRate;
    private TransactionsFragmentBase txsFragment;

    // Receiver
    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter vitaeServiceFilter = new IntentFilter(ACTION_NOTIFICATION);
    private BroadcastReceiver vitaeServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NOTIFICATION)){
                if(intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)){
                    updateBalance();
                    txsFragment.refresh();
                }
            }

        }
    };

    @Override
    protected void beforeCreate(){
        /*
        if (!appConf.isAppInit()){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        }
        // show report dialog if something happen with the previous process
        */
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.my_wallet);
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_alpha_amount,header_container);
        header_container.setVisibility(View.VISIBLE);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        container_txs = root.findViewById(R.id.container_txs);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) containerHeader.findViewById(R.id.txt_watch_only);

        GetVersionCode c = new GetVersionCode();
        c.execute();

        // Open Send
        fab_add = (FloatingActionButton) root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vitaeModule.isWalletWatchOnly()){
                    Toast.makeText(v.getContext(),R.string.error_watch_only_mode,Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(v.getContext(), SendActivity.class));
            }
        });

        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);

    }


    private class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;
            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=vitae.cryptodev.kaali&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;


        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            //String currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            //PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String currentVersion = BuildConfig.VERSION_NAME;
            String currentVersionvalue = currentVersion.replace(".","");
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
            String onlineVersionvalue = onlineVersion.replace(".","");
                if (Integer.parseInt(currentVersionvalue) < Integer.parseInt(onlineVersionvalue)) {
                    //show dialogint in = Integer.valueOfInteger.parseInt(
                    showUpdateDialog();
                }
            }
            //else{
                //showVersioncheckFailMessage();
            //}
            //Log.d("update", "Current version " + currentVersionvalue + "playstore version " + onlineVersionvalue);
        }
    }

    /*public void showVersioncheckFailMessage() {
        final boolean isForceUpdate = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WalletActivity.this);

        alertDialogBuilder.setTitle(WalletActivity.this.getString(R.string.app_name));
        alertDialogBuilder.setMessage(WalletActivity.this.getString(R.string.app_update));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.app_updatedialog_update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                WalletActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                finish();;
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.app_updatedialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isForceUpdate) {
                    finish();
                }
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    } */

    public void showUpdateDialog() {
        final boolean isForceUpdate = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WalletActivity.this);

        alertDialogBuilder.setTitle(WalletActivity.this.getString(R.string.app_name));
        alertDialogBuilder.setMessage(WalletActivity.this.getString(R.string.app_update));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.app_updatedialog_update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                WalletActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.app_updatedialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isForceUpdate) {
                    finish();
                }
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);

        init();

        // register
        localBroadcastManager.registerReceiver(vitaeServiceReceiver,vitaeServiceFilter);

        updateState();
        updateBalance();

        // check if this wallet need an update:
        try {
            if(vitaeModule.isBip32Wallet() && vitaeModule.isSyncWithNode()){
                if (!vitaeModule.isWalletWatchOnly() && vitaeModule.getAvailableBalanceCoin().isGreaterThan(Transaction.DEFAULT_TX_FEE)) {
                    Intent intent = UpgradeWalletActivity.createStartIntent(
                            this,
                            getString(R.string.upgrade_wallet),
                            "An old wallet version with bip32 key was detected, in order to upgrade the wallet your coins are going to be sweeped" +
                                    " to a new wallet with bip44 account.\n\nThis means that your current mnemonic code and" +
                                    " backup file are not going to be valid anymore, please write the mnemonic code in paper " +
                                    "or export the backup file again to be able to backup your coins." +
                                    "\n\nPlease wait and not close this screen. The upgrade + blockchain sychronization could take a while."
                                    +"\n\nTip: If this screen is closed for user's mistake before the upgrade is finished you can find two backups files in the 'Download' folder" +
                                    " with prefix 'old' and 'upgrade' to be able to continue the restore manually."
                                    + "\n\nThanks!",
                            "sweepBip32"
                    );
                    startActivity(intent);
                }
            }
        } catch (NoPeerConnectedException e) {
            e.printStackTrace();
        }
    }

    private void updateState() {
        txt_watch_only.setVisibility(vitaeModule.isWalletWatchOnly()?View.VISIBLE:View.GONE);
    }

    private void init() {
        // Start service if it's not started.
        vitaeApplication.startVitaeService();

        if (!vitaeApplication.getAppConf().hasBackup()){
            long now = System.currentTimeMillis();
            if (vitaeApplication.getLastTimeRequestedBackup()+1800000L<now) {
                vitaeApplication.setLastTimeBackupRequested(now);
                SimpleTwoButtonsDialog reminderDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                        this,
                        getString(R.string.reminder_backup),
                        getString(R.string.reminder_backup_body),
                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                startActivity(new Intent(WalletActivity.this, SettingsBackupActivity.class));
                                dialog.dismiss();
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        }
                );
                reminderDialog.setLeftBtnText(getString(R.string.button_dismiss));
                reminderDialog.setLeftBtnTextColor(Color.BLACK);
                reminderDialog.setRightBtnText(getString(R.string.button_ok));
                reminderDialog.show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister
        //localBroadcastManager.unregisterReceiver(localReceiver);
        localBroadcastManager.unregisterReceiver(vitaeServiceReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_qr){
            startActivity(new Intent(this, QrActivity.class));
            return true;
        }else if (item.getItemId()==R.id.action_scan){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class),SCANNER_RESULT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Create a list of Data objects
    public List<TransactionData> fill_with_data() {

        List<TransactionData> data = new ArrayList<>();

        data.add(new TransactionData("Sent Vitae", "18:23", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));
        data.add(new TransactionData("Sent Vitae", "1 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "3 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "3 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));

        data.add(new TransactionData("Sent Vitae", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "one week ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Vitae", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));

        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode==RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    String usedAddress;
                    if (vitaeModule.chechAddress(address)){
                        usedAddress = address;
                    }else {
                        SendURI vitaeUri = new SendURI(address);
                        usedAddress = vitaeUri.getAddress().toBase58();
                    }
                    DialogsUtil.showCreateAddressLabelDialog(this,usedAddress);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"Bad address",Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),permission);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void updateBalance() {
        Coin availableBalance = vitaeModule.getAvailableBalanceCoin();
        txt_value.setText(!availableBalance.isZero()?availableBalance.toFriendlyString():"0 Vitae");
        Coin unnavailableBalance = vitaeModule.getUnnavailableBalanceCoin();
        txt_unnavailable.setText(!unnavailableBalance.isZero()?unnavailableBalance.toFriendlyString():"0 Vitae");
        if (vitaeRate == null)
            vitaeRate = vitaeModule.getRate(vitaeApplication.getAppConf().getSelectedRateCoin());
        if (vitaeRate!=null) {
            txt_local_currency.setText(
                    vitaeApplication.getCentralFormats().format(
                            new BigDecimal(availableBalance.getValue() * vitaeRate.getValue().doubleValue()).movePointLeft(8)
                    )
                    + " "+vitaeRate.getCoin()
            );
        }else {
            txt_local_currency.setText("0 USD");
        }
    }
}
