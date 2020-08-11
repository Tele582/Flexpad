package fun.flexpad.com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class SendMoneyActivity extends AppCompatActivity {

    //Button btn_make_payment;
    private EditText mCardNumber;
    private EditText mCardExpiry;
    private EditText mCardCVV;
    private EditText mPaymentAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pay");

        mCardNumber = findViewById(R.id.til_card_number);
        mCardExpiry = findViewById(R.id.til_card_expiry);
        mCardCVV = findViewById(R.id.til_card_cvv);
        mPaymentAmount = findViewById(R.id.til_transfer_amount);

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
        Button btn_make_payment = findViewById(R.id.btn_make_payment);
        btn_make_payment.setOnClickListener(v -> performCharge());
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

        card.isValid(); //////maybe remove this / unnecessary;

        charge.setAmount(Integer.parseInt(mPaymentAmount.getText().toString()) * 100);
        charge.setEmail("motelejesu@gmail.com");
        charge.setCard(card);

        PaystackSdk.chargeCard(this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                Log.d("Send Money Activity", "onSuccess: " + transaction.getReference());
                parseResponse(transaction.getReference());


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
    }

    private void parseResponse(String transactionReference) {
        String message = "Payment Successful - " + transactionReference + "\n You sent #" + charge.getAmount()/100;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        startActivity(new Intent(SendMoneyActivity.this, PaymentActivity.class));

    }

        /*btn_make_payment = findViewById(R.id.btn_make_payment);

        btn_make_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/


}