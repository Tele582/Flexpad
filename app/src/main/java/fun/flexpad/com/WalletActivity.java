package fun.flexpad.com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class WalletActivity extends AppCompatActivity {

    private EditText mCardNumber;
    private EditText mCardExpiry;
    private EditText mCardCVV;
    private EditText mPaymentAmount;
    TextView mWallet;
    int new_wallet_bal, former_wallet_balance;

    MaterialEditText eMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pay");

        mCardNumber = findViewById(R.id.til_card_number);
        mCardExpiry = findViewById(R.id.til_card_expiry);
        mCardCVV = findViewById(R.id.til_card_cvv);
        mPaymentAmount = findViewById(R.id.til_transfer_amount);
        mWallet = findViewById(R.id.wallet_balance);

        eMAIL = findViewById(R.id.email);

        Objects.requireNonNull(mCardExpiry).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() == 2 && !s.toString().contains("/")) {
                    s.append("/");
                }
            }
        });
        initializePaystack();
        Button btn_top_wallet = findViewById(R.id.btn_top_wallet);
        btn_top_wallet.setOnClickListener(v -> performCharge());
    }

    private void initializePaystack() {
        PaystackSdk.initialize(getApplicationContext());
        PaystackSdk.setPublicKey("pk_test_63e612c34e72f2ecb73e190a7cf9acab4b2b79da");
    }

    public Charge charge = new Charge();

    public void performCharge(){
        String cardNumber = mCardNumber.getText().toString();
        String cardExpiry = mCardExpiry.getText().toString();
        String cvv = mCardCVV.getText().toString();

        String[] cardExpiryArray = cardExpiry.split("/");
        int expiryMonth = Integer.parseInt(cardExpiryArray[0]);
        int expiryYear = Integer.parseInt(cardExpiryArray[1]);

        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
        //card.isValid();

        double interest = 1.02;
        //former_wallet_balance

        new_wallet_bal = Integer.parseInt(mPaymentAmount.getText().toString()) + former_wallet_balance;

        charge.setAmount((int) (Double.parseDouble(mPaymentAmount.getText().toString()) * 100 * interest));
        //This way, I get 0.47% of every transaction after Paystack fees.

        charge.setEmail("motelejesu@gmail.com");
        charge.setCard(card);
        //////System.out.println(99);
        PaystackSdk.chargeCard(this, charge, new Paystack.TransactionCallback() { //change here to wallet top-up
            @Override
            public void onSuccess(Transaction transaction) {
                Log.d("Send Money Activity", "onSuccess: " + transaction.getReference());
                //parseResponse(transaction.getReference());

                mWallet.setText("Wallet Balance (₦): " + new_wallet_bal); // + mPaymentAmount
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                Log.d("Send Money Activity", "beforeValidate: " + transaction.getReference());
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                Log.d("Send Money Activity", "onError: " + error.getLocalizedMessage());
            }
        });

        former_wallet_balance = new_wallet_bal; ////
        int aa = former_wallet_balance;
    }

    private void parseResponse(String transactionReference) {
        String message = "Payment Successful - " + transactionReference + "\n You sent #" + charge.getAmount()/100;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        startActivity(new Intent(WalletActivity.this, PaymentActivity.class));
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Store values between instances here
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();  // Put the values from the UI

        editor.putInt("CurrentWalletBalance", former_wallet_balance);

        // Commit to storage
        editor.commit();
    }
}


