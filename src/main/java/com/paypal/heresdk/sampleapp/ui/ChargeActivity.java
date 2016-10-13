package com.paypal.heresdk.sampleapp.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.ExtractEditText;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.paypal.heresdk.sampleapp.XeroAPI.XeroAPI;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapper;
import com.paypal.heresdk.sampleapp.sdk.PayPalHereSDKWrapperCallbacks;
import com.paypal.heresdk.sampleapp.R;
import com.paypal.merchant.sdk.CardReaderManager;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.domain.TransactionRecord;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.SignatureType;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuthService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import static com.paypal.heresdk.sampleapp.R.id.amount;

public class ChargeActivity extends ActionBarActivity {
    private static final String LOG_TAG = ChargeActivity.class.getSimpleName();

    private boolean mIsInMiddleOfTakingPayment = false;
    private TransactionRecord mTransactionRecord = null;

    private RelativeLayout mChargeLayout;
    private RelativeLayout mPaymentSuccessfulLayout;
    private RelativeLayout mPaymentFailureLayout;
    private RelativeLayout mRefundSuccessfulLayout;
    private RelativeLayout mRefundFailureLayout;

    private XeroAPI api;
    private OAuth10aService service;
    private OAuth1AccessToken accessToken;
    private TextView mContent;
    private EditText mAmount;
    private BigDecimal amount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_charge);

        mChargeLayout = (RelativeLayout) findViewById(R.id.id_charge_layout);
        mPaymentSuccessfulLayout = (RelativeLayout) findViewById(R.id.id_payment_successful_layout);
        mPaymentFailureLayout = (RelativeLayout) findViewById(R.id.id_payment_failure_layout);
        mRefundSuccessfulLayout = (RelativeLayout) findViewById(R.id.id_refund_successful_layout);
        mRefundFailureLayout = (RelativeLayout) findViewById(R.id.id_refund_failure_layout);

        //Xero Api initialisation
        api = new XeroAPI(getApplicationContext());
        service = new ServiceBuilder()
                .apiKey(getString(R.string.api_key))
                .build(api);

        accessToken = new OAuth1AccessToken(getString(R.string.api_key), getString(R.string.api_secret));

        mAmount = (EditText) findViewById(R.id.amount);

        showChargeLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        PayPalHereSDKWrapper.getInstance().registerCardReaderEventListener();
        setListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged");
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed");

        //Lets go back to PaymentOptionsActivity instead of going back to ReaderConnectionActivity
        goBackToPaymentOptionsActivity(null);
        PayPalHereSDKWrapper.getInstance().unregisterCardReaderEventListener();
    }

    /*
     * This function will be called when Take Payment button is clicked on Charge Screen
     */
    public void onTakePaymentOptionSelected(View view) {
        Log.d(LOG_TAG, "takePayment");
        EditText amountEditText = (EditText) findViewById(R.id.amount);
        String amountText = amountEditText.getText().toString();
        hideKeyboard();
        amount = BigDecimal.ZERO;
        if (null != amountText && amountText.length() > 0) {
            amountText = String.format("%.2f", Double.parseDouble(amountText));
            amount = new BigDecimal(amountText);
        }else{
            showInvalidAmountAlertDialog();
            return;
        }

        /**
         * STEP 1: Step 1 of SDK integration is calling beginPayment of the PayPalHereSDK's TransactionManager
         */
        PayPalHereSDKWrapper.getInstance().beginPayment(amount);
        takePayment();
    }

    public void goBackToPaymentOptionsActivity(View view){
        Log.d(LOG_TAG, "goBackToPaymentOptionsActivity");

        Intent paymentOptionsIntent = new Intent(ChargeActivity.this, PaymentOptionsActivity.class);
        paymentOptionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(paymentOptionsIntent);
    }

    public void onRefundOptionSelected(View view){
        Log.d(LOG_TAG, "onRefundOptionSelected");
        PayPalHereSDKWrapper.getInstance().doRefund(mTransactionRecord, mTransactionRecord.getInvoice().getGrandTotal(), new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onRefundFailure(TransactionManager.PaymentErrors errors) {
                Log.d(LOG_TAG, "onRefundFailure error: " + errors);
                showRefundFailureLayout();
            }

            @Override
            public void onRefundSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG, "onRefundSuccess");
                showRefundSuccessfulLayout();
            }
        });
    }

    private void hideKeyboard(){
        EditText amountEditText = (EditText) findViewById(R.id.amount);
        amountEditText.setText("");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(amountEditText.getWindowToken(), 0);
    }

    /*
     * Show the charge layout and hide all other layouts
     */
    private void showChargeLayout(){
        mChargeLayout.setVisibility(View.VISIBLE);
        mPaymentSuccessfulLayout.setVisibility(View.GONE);
        mPaymentFailureLayout.setVisibility(View.GONE);
        mRefundSuccessfulLayout.setVisibility(View.GONE);
        mRefundFailureLayout.setVisibility(View.GONE);
    }

    /*
     * Show the payment successful layout and hide all other layouts
     */
    private void showPaymentSuccessfulLayout(){
        mChargeLayout.setVisibility(View.GONE);
        mPaymentSuccessfulLayout.setVisibility(View.VISIBLE);
        mPaymentFailureLayout.setVisibility(View.GONE);
        mRefundSuccessfulLayout.setVisibility(View.GONE);
        mRefundFailureLayout.setVisibility(View.GONE);
    }

    /*
     * Show the payment failure layout and hide all other layouts
     */
    private void showPaymentFailureLayout(){
        mChargeLayout.setVisibility(View.GONE);
        mPaymentSuccessfulLayout.setVisibility(View.GONE);
        mPaymentFailureLayout.setVisibility(View.VISIBLE);
        mRefundSuccessfulLayout.setVisibility(View.GONE);
        mRefundFailureLayout.setVisibility(View.GONE);
    }

    /*
     * Show the refund success layout and hide all other layouts
     */
    private void showRefundSuccessfulLayout(){
        mChargeLayout.setVisibility(View.GONE);
        mPaymentSuccessfulLayout.setVisibility(View.GONE);
        mPaymentFailureLayout.setVisibility(View.GONE);
        mRefundSuccessfulLayout.setVisibility(View.VISIBLE);
        mRefundFailureLayout.setVisibility(View.GONE);
    }

    /*
     * Show the refund failure layout and hide all other layouts
     */
    private void showRefundFailureLayout(){
        mChargeLayout.setVisibility(View.GONE);
        mPaymentSuccessfulLayout.setVisibility(View.GONE);
        mPaymentFailureLayout.setVisibility(View.GONE);
        mRefundSuccessfulLayout.setVisibility(View.GONE);
        mRefundFailureLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Listener to PayPalHere SDK Wrapper for all the events.
     */
    private void setListener(){
        PayPalHereSDKWrapper.getInstance().setListener(this, new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onSuccessfulCardRead() {
                Log.d(LOG_TAG, "onSuccessfulCardRead");
                takePayment();
            }

            @Override
            public void onMagstripeReaderDisconnected() {
                Log.d(LOG_TAG, "onMagstripeReaderDisconnected");
                //If reader got disconnected and if we are not in the middle of taking payment
                // go back to reader connect layout.
                if (!mIsInMiddleOfTakingPayment) {
                    finish();
                }
            }

            @Override
            public void onEMVReaderDisconnected() {
                Log.d(LOG_TAG, "onEMVReaderDisconnected");
                //If reader got disconnected and if we are not in the middle of taking payment
                // go back to reader connect layout.
                if (!mIsInMiddleOfTakingPayment) {
                    finish();
                }
            }
        });
    }

    private void takePayment(){
        Log.d(LOG_TAG, "takePayment");
        mIsInMiddleOfTakingPayment = true;
        /**
         * STEP 2: Step 2 is calling processPayment of PayPalHere SDK's Transaction Manager.
         */
        PayPalHereSDKWrapper.getInstance().takePayment(new PayPalHereSDKWrapperCallbacks() {
            @Override
            public void onPaymentFailure(TransactionManager.PaymentErrors errors) {
                Log.d(LOG_TAG, "takePayment onPaymentFailure");
                mIsInMiddleOfTakingPayment = false;
                showPaymentFailureLayout();

                OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.xero.com/api.xro/2.0/invoices", service);
                //request.addHeader("Accept", "application/json");
                request.addHeader("Content-Type", "text/xml");


                new postInvoice().execute();

            }

            @Override
            public void onPaymentSuccess(TransactionManager.PaymentResponse responseObject) {
                Log.d(LOG_TAG, "takePayment onPaymentSuccess");
                //settle the invoice here
                mIsInMiddleOfTakingPayment = false;
                mTransactionRecord = responseObject.getTransactionRecord();
                showPaymentSuccessfulLayout();
            }
        });
    }

    //Android requires this to be on a separate thread
    private class postInvoice extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {
            OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.xero.com/api.xro/2.0/invoice", service);
            request.addHeader("Accept", "application/json");
            request.addHeader("Content-Type", "text/xml");

            String payload = "<Invoices>"+
                    "<Invoice>"+
                    "<Type>ACCREC</Type>"+
                    "<Contact>"+
                    "<Name>PayPal Here Transaction</Name>"+
                    "</Contact>"+
                    "<Date>2016-10-13T00:00:00</Date>"+
                    "<DueDate>2016-10-28T00:00:00</DueDate>"+
                    "<LineAmountTypes>Inclusive</LineAmountTypes>"+
                    "<LineItems>"+
                    "<LineItem>"+
                    "<Description>PayPal Here Transaction</Description>"+
                    "<Quantity>1</Quantity>"+
                    "<UnitAmount>"+amount+"</UnitAmount>"+
                    "<AccountCode>200</AccountCode>"+
                    "</LineItem>"+
                    "</LineItems>"+
                    "</Invoice>"+
                    "</Invoices>";

            request.addPayload(payload);
            service.signRequest(accessToken, request);

            Log.d("Status", "Requesting......." + payload.toString());

            String responseString = "";

            try {
                Response response = request.send();
                responseString = response.getBody().toString();
                return responseString;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Exception", "Xero " + e.getLocalizedMessage());
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result){
            Log.d("Xero Result", result);
            //mContent.setText(result);
        }
    }

    private void showInvalidAmountAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChargeActivity.this);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.error_invalid_amount);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOG_TAG, "takePayment invalid amount alert dialog onClick");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
