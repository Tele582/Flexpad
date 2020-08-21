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



        }



        /*btn_make_payment = findViewById(R.id.btn_make_payment);

        btn_make_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/


}