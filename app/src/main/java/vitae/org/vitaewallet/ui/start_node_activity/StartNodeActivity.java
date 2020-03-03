package vitae.org.vitaewallet.ui.start_node_activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import global.BwktrumGlobalData;
import bwktrum.BwktrumPeer;
import bwktrum.BwktrumPeerData;
import vitae.org.vitaewallet.R;
import vitae.org.vitaewallet.ui.base.BaseActivity;
import vitae.org.vitaewallet.ui.pincode_activity.PincodeActivity;
import vitae.org.vitaewallet.ui.wallet_activity.WalletActivity;
import vitae.org.vitaewallet.utils.DialogBuilder;
import vitae.org.vitaewallet.utils.DialogsUtil;

import static global.BwktrumGlobalData.KAALI_TESTNET_SERVER;

/**
 * Created by Neoperol on 6/27/17.
 */

public class StartNodeActivity extends BaseActivity {

    private Button openDialog;
    private Button btnSelectNode;
    private Spinner dropdown;
    private ArrayAdapter<String> adapter;
    private List<String> hosts = new ArrayList<>();

    private static final List<BwktrumPeerData> trustedNodes = BwktrumGlobalData.listTrustedHosts();

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle(R.string.select_node);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Open Dialog
        openDialog = (Button) findViewById(R.id.openDialog);
        openDialog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DialogBuilder dialogBuilder = DialogsUtil.buildtrustedNodeDialog(StartNodeActivity.this, new DialogsUtil.TrustedNodeDialogListener() {
                    @Override
                    public void onNodeSelected(BwktrumPeerData bwktrumPeerData) {
                        if(!trustedNodes.contains(bwktrumPeerData)) {
                            dropdown.setAdapter(null);
                            adapter.clear();
                            hosts = new ArrayList<String>();
                            trustedNodes.add(bwktrumPeerData);
                            for (BwktrumPeerData trustedNode : trustedNodes) {
                                if (trustedNode.getHost().equals(KAALI_TESTNET_SERVER)) {
                                    hosts.add("164.68.122.247");
                                } else
                                    hosts.add(trustedNode.getHost());
                            }
                            adapter.addAll(hosts);
                            dropdown.setAdapter(adapter);
                            dropdown.setSelection(hosts.size() - 1);
                        }
                    }
                });
                dialogBuilder.show();
            }

        });

        // Node selected
        btnSelectNode = (Button) findViewById(R.id.btnSelectNode);
        btnSelectNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = dropdown.getSelectedItemPosition();
                BwktrumPeerData selectedNode = trustedNodes.get(selected);
                boolean isStarted = vitaeApplication.getAppConf().getTrustedNode()!=null;
                vitaeApplication.setTrustedServer(selectedNode);

                if (isStarted){
                    vitaeApplication.stopBlockchain();
                    // now that everything is good, start the service
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            vitaeApplication.startVitaeService();
                        }
                    }, TimeUnit.SECONDS.toMillis(5));
                }
                goNext();
                finish();
            }
        });

        dropdown = (Spinner)findViewById(R.id.spinner);

        // add connected node if it's not on the list
        BwktrumPeerData bwktrumPeer = vitaeApplication.getAppConf().getTrustedNode();
        if (bwktrumPeer!=null && !bwktrumPeer.getHost().equals(KAALI_TESTNET_SERVER)){
            trustedNodes.add(bwktrumPeer);
        }

        int selectionPos = 0;

        for (int i=0;i<trustedNodes.size();i++){
            BwktrumPeerData trustedNode = trustedNodes.get(i);
            if (bwktrumPeer!=null && bwktrumPeer.getHost().equals(trustedNode)){
                selectionPos = i;
            }
            if (trustedNode.getHost().equals(KAALI_TESTNET_SERVER)){
                hosts.add("164.68.122.247");
            }else
                hosts.add(trustedNode.getHost());
        }
        adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item,hosts){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                view.setPadding(16, 16, 16, 16);
                return view;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                view.setPadding(8, 8, 8, 8);
                return view;
            }
        };
        dropdown.setSelection(selectionPos);
        dropdown.setAdapter(adapter);
    }

    private void goNext() {
        Class clazz = null;
        if (vitaeApplication.getAppConf().getPincode()==null){
            clazz = PincodeActivity.class;
        }else {
            clazz = WalletActivity.class;
        }
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

}
