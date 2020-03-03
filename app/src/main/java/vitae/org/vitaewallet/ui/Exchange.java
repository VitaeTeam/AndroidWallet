package vitae.org.vitaewallet.ui;

import android.content.Intent;
//import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;
import org.json.JSONException;
import org.sendj.core.Coin;
import org.sendj.core.InsufficientMoneyException;
import org.sendj.core.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.sendj.core.Address;
import org.sendj.uri.SendURI;
import vitae.org.vitaewallet.R;
import vitae.org.vitaewallet.rate.RequestVitaeRateException;
import vitae.org.vitaewallet.service.VitaeWalletService;
import vitae.org.vitaewallet.ui.base.BaseDrawerActivity;
import vitae.org.vitaewallet.ui.base.dialogs.SimpleTextDialog;
import vitae.org.vitaewallet.utils.DialogsUtil;
import vitae.org.vitaewallet.utils.NavigationUtils;

import static vitae.org.vitaewallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static vitae.org.vitaewallet.service.IntentsConstants.DATA_TRANSACTION_HASH;

public class Exchange extends BaseDrawerActivity {

    private View root;
    private EditText edit_amount;
    private EditText edit_btc_address;
    private TextView edit_amount_btc;
    private Button btn_donate;
    private Button btn_swap;
    private SimpleTextDialog errorDialog;
    private Address address;
    private static final String URL = "https://api.instaswap.io";

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.donations_fragment,container);
        edit_amount = (EditText) root.findViewById(R.id.edit_amount);
        edit_btc_address = (EditText) root.findViewById(R.id.edit_btc_address);
        edit_amount_btc = (TextView) root.findViewById(R.id.edit_amount_btc);
        btn_donate = (Button) root.findViewById(R.id.btn_donate);
        btn_swap = (Button) root.findViewById(R.id.btn_swap);
        btn_donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    RequestTask c = new RequestTask();
                    c.execute("n");
                    //send();
                }catch (Exception e){
                    e.printStackTrace();
                    showErrorDialog(e.getMessage());
                }
            }





        });

        btn_swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    RequestRate rate = new RequestRate();
                    rate.execute("n");
                    //send();
                }catch (Exception e){
                    e.printStackTrace();
                    showErrorDialog(e.getMessage());
                }
            }





        });



    }






    class RequestTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            //String addressStr = "worked";
            //address = module.getReceiveAddress();
            String result = null;
            String addressReceive = edit_btc_address.getText().toString();
            String amountStr = edit_amount.getText().toString();

            if (amountStr.length() < 1) {return "Amount not valid";//throw new IllegalArgumentException("Amount not valid");
            }
            if (amountStr.length() == 1 && amountStr.equals(".")) {//throw new IllegalArgumentException("Amount not valid");
                return "Amount not valid";
                }
            if (amountStr.charAt(0) == '.') {
                amountStr = "0" + amountStr;
            }

            String urlold = "http://164.68.97.79:8081/" + amountStr;
            String minamount = "0";
            try {
                BasicHttpParams basicHttpParams = new BasicHttpParams();
                HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
                HttpClient client = new DefaultHttpClient(basicHttpParams);
                HttpGet httpGet = new HttpGet(urlold);
                httpGet.setHeader("Content-type", "application/json");
                HttpResponse httpResponse = client.execute(httpGet);
                InputStream inputStream = null;
                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                result = null;
                if (inputStream != null) result = convertInputStreamToString(inputStream);
                //if (httpResponse.getStatusLine().getStatusCode() == 200) {
                JSONObject jsonObject = new JSONObject(result);
                //JSONObject jsonObject1 = new JSONObject(jsonObject.getString("kingscoin"));
                //System.out.println( "Coolness: " + jsonObject1 );
                result = new String(jsonObject.getString("response"));
                JSONObject jsonObject2 = new JSONObject(result);
                minamount = new String(jsonObject2.getString("min"));
                //}

            } catch (IOException e) {
                e.printStackTrace();
                //throw new RequestVitaeRateException(e);
            } catch (JSONException e) {
                e.printStackTrace();
                //throw new RequestVitaeRateException(e);
            }


            String urladdrvalidate = "http://164.68.97.79:8081/addrvalidate/" + addressReceive;
            String isvalidaddr="false";
            try { String isvalidaddrstr;
                BasicHttpParams basicHttpParams = new BasicHttpParams();
                HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
                HttpClient client = new DefaultHttpClient(basicHttpParams);
                HttpGet httpGet = new HttpGet(urladdrvalidate);
                httpGet.setHeader("Content-type", "application/json");
                HttpResponse httpResponse = client.execute(httpGet);
                InputStream inputStream = null;
                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                isvalidaddrstr = null;
                if (inputStream != null) isvalidaddrstr = convertInputStreamToString(inputStream);
                //if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //JSONObject jsonObject = new JSONObject(isvalidaddrstr);
                isvalidaddr = isvalidaddrstr;
                Log.d("myTag--addrvali", isvalidaddrstr);

            } catch (IOException e) {
                e.printStackTrace();
                //throw new RequestVitaeRateException(e);
            }



            Coin amount = Coin.parseCoin(amountStr);
            Log.d("myTag--minamnt", minamount);
            double amnt = Double.parseDouble(amountStr);
            double minamnt = Double.parseDouble(minamount);
            if (amount.isZero()) {//throw new IllegalArgumentException("Amount zero, please correct it");
                return "Amount zero, please correct it";
                }
            if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT))
            {//throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, " + Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
                return "Amount must be greater than the minimum amount accepted from miners";
                }
            if (amount.isGreaterThan(Coin.valueOf(vitaeModule.getAvailableBalance()))) {//throw new IllegalArgumentException("Insuficient balance");
                return "Insuficient balance";
                 }
            if(amnt<minamnt){
                return "Min. Amount to exchange is "+minamount;
            }

            if(isvalidaddr.equals("false")){
                return "Invalid BTC Receive Address";
            }





            Address addressRefund = vitaeModule.getReceiveAddress();//"Vj78xyhZ43sqNMDEajeYKyLeDepmVZRWL9";
            String url = "http://164.68.97.79:8081/exchange/"+amountStr+"/"+addressReceive+"/"+addressRefund;

            try{
            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
            HttpClient client = new DefaultHttpClient(basicHttpParams);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = client.execute(httpGet);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            result = null;
                if (inputStream != null) result = convertInputStreamToString(inputStream);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    JSONObject jsonObject = new JSONObject(result);
                    //JSONObject jsonObject1 = new JSONObject(jsonObject.getString("kingscoin"));
                    //System.out.println( "Coolness: " + jsonObject1 );
                    ////result = new String(jsonObject.getString("response"));
                    result = jsonObject.toString();
                    //JSONArray jsonArray = new JSONArray(result);
                    //result = new String(jsonArray.getJSONObject(0).getString("response"));
                }

        } catch (IOException e) {
            e.printStackTrace();
            //throw new RequestVitaeRateException(e);
        } catch (JSONException e) {
                e.printStackTrace();
                //throw new RequestVitaeRateException(e);
            }
        return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            Log.d("myTag--swapplaced", result);


            try{String result1;
                try{JSONObject jsonObject = new JSONObject(result);
                    result1 = new String(jsonObject.getString("apiInfo"));
                    Log.d("myTag", result1);
                }
                catch (Exception e){
                    result1="error";
                }
                if(result1.equals("OK"))
                send(result);
                else throw new Exception(result);
            }catch (Exception e){
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }
        }
    }

    class RequestRate extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            String result = null;
            try {
                //String addressStr = "worked";

                String amountStr = edit_amount.getText().toString();
                if (amountStr.length() < 1) {return "Amount not valid";//throw new IllegalArgumentException("Amount not valid");
                }
                if (amountStr.length() == 1 && amountStr.equals(".")) {//throw new IllegalArgumentException("Amount not valid");
                    return "Amount not valid";}
                if (amountStr.charAt(0) == '.') {
                    amountStr = "0" + amountStr;
                }
                Coin amount = Coin.parseCoin(amountStr);
                if (amount.isZero()) {//throw new IllegalArgumentException("Amount zero, please correct it");
                    return "Amount zero, please correct it";}
                if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT))
                {//throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, " + Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
                    return "Amount must be greater than the minimum amount accepted from miners";}
                if (amount.isGreaterThan(Coin.valueOf(vitaeModule.getAvailableBalance()))) {//throw new IllegalArgumentException("Insuficient balance");
                return "Insuficient balance";}
                String url = "http://164.68.97.79:8081/" + amountStr;
                try {
                    BasicHttpParams basicHttpParams = new BasicHttpParams();
                    HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
                    HttpClient client = new DefaultHttpClient(basicHttpParams);
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.setHeader("Content-type", "application/json");
                    HttpResponse httpResponse = client.execute(httpGet);
                    InputStream inputStream = null;
                    // receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();
                    result = null;
                    if (inputStream != null) result = convertInputStreamToString(inputStream);
                    //if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    JSONObject jsonObject = new JSONObject(result);
                    //JSONObject jsonObject1 = new JSONObject(jsonObject.getString("kingscoin"));
                    //System.out.println( "Coolness: " + jsonObject1 );
                    result = new String(jsonObject.getString("response"));
                    JSONObject jsonObject2 = new JSONObject(result);
                    result = new String(jsonObject2.getString("getAmount"));
                    //}

                } catch (IOException e) {
                    e.printStackTrace();
                    //throw new RequestVitaeRateException(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    //throw new RequestVitaeRateException(e);
                }
                Log.d("myTag", result);
                //return result;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Insuficient balance");
            }
            return result;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            try{
                //todo : Parse response to find getamount and update textview using settext
                //if(android.text.TextUtils.isDigitsOnly(result))
                edit_amount_btc.setText(result);
                //else throw new Exception(result);
            }catch (Exception e){
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }

        }
    }







    private void send(String result) throws RequestVitaeRateException {
        try {
            //RequestTask c = new RequestTask();
            //c.execute("n");
            String result1,result2;
            Log.d("myTag-insend()", result);
            JSONObject jsonObject = null;
            JSONObject jsonObject1 = null;
            try {
                jsonObject = new JSONObject(result);
                result1 = new String(jsonObject.getString("response"));
                jsonObject1 = new JSONObject(result1);
                result2 = new String(jsonObject.getString("depositWallet"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("myTag-insend()", "result2hardcodedused");
                result2="Vty7tFuLHMdGNuPHuMCN6fxXien11rGCB4";
            }




            // create the tx
            //String addressStr = edit_address.getText().toString();
            //String addressStr = VitaeContext.DONATE_ADDRESS;

            //result2="Vty7tFuLHMdGNuPHuMCN6fxXien11rGCB4";
            String addressStr = result2;//"Vty7tFuLHMdGNuPHuMCN6fxXien11rGCB4";
            if (!vitaeModule.chechAddress(addressStr))
                throw new IllegalArgumentException("Address not valid");
            String amountStr = edit_amount.getText().toString();
            if (amountStr.length() < 1) throw new IllegalArgumentException("Amount not valid");
            if (amountStr.length()==1 && amountStr.equals(".")) throw new IllegalArgumentException("Amount not valid");
            if (amountStr.charAt(0)=='.'){
                amountStr = "0"+amountStr;
            }
            Coin amount = Coin.parseCoin(amountStr);
            if (amount.isZero()) throw new IllegalArgumentException("Amount zero, please correct it");
            if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT)) throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, "+Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
            if (amount.isGreaterThan(Coin.valueOf(vitaeModule.getAvailableBalance())))
                throw new IllegalArgumentException("Insuficient balance");
            String memo = "Exchanged!";
            // build a tx with the default fee
            Transaction transaction = vitaeModule.buildSendTx(addressStr, amount, memo,vitaeModule.getReceiveAddress());
            // send it
            vitaeModule.commitTx(transaction);
            Intent intent = new Intent(vitae.org.vitaewallet.ui.Exchange.this, VitaeWalletService.class);
            intent.setAction(ACTION_BROADCAST_TRANSACTION);
            intent.putExtra(DATA_TRANSACTION_HASH,transaction.getHash().getBytes());
            startService(intent);

            //Toast.makeText(this,R.string.donation_thanks,Toast.LENGTH_LONG).show();
            onBackPressed();

        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insuficient balance");
        }

    }

    private void showErrorDialog(String message) {
        if (errorDialog==null){
            errorDialog = DialogsUtil.buildSimpleErrorTextDialog(this,getResources().getString(R.string.invalid_inputs),message);
        }else {
            errorDialog.setBody(message);
        }
        errorDialog.show(getFragmentManager(),getResources().getString(R.string.send_error_dialog_tag));
    }
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream,"ISO-8859-1"));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationUtils.goBackToHome(this);
    }
}

