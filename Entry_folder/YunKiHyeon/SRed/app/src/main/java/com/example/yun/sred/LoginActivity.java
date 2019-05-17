package com.example.yun.sred;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseDialog {

    private static final String TAG = "LoginActivity";


    private Switch autoLogin;
    private EditText email_et, password_et;
    private BootstrapButton login_button, signin_button,re_verify;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;


    //자동로그인에 필요함
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_login);

        autoLogin = findViewById(R.id.auto_login);
        email_et = findViewById(R.id.LoginActivity_email);
        password_et = findViewById(R.id.LoginActivity_password);
        login_button = findViewById(R.id.LoginActivity_button_login);
        signin_button = findViewById(R.id.LoginActivity_button_signup);
        re_verify = findViewById(R.id.loginactivity_button_REverity);
        re_verify.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        mPref = getSharedPreferences("setting", 0);
        mPrefEdit = mPref.edit();

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
            }
        });
        //로그인 리스너
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                loginEvent();
            }
        });

        //메일인증상태가 아닐 시 재전송 버튼을 활성화시켜줌.
        re_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerification();
            }
        });

        //자동로그인 버튼의 상태가 바뀌면 동작하는 리스너
        autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mPrefEdit.putString("switch", "1");
                    mPrefEdit.commit();
                    Log.d("switch", "on");
                }
                else {
                    mPrefEdit.putString("switch", "0");
                    mPrefEdit.commit();
                    Log.d("switch", "off");
                }
            }
        });
        //자동로그인
        autologin();


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                //로그인 부분
                if(user !=null){
                    if (user.isEmailVerified() == true) {
                        if(autoLogin.isChecked()){
                            Toast.makeText(getApplicationContext(),user.getDisplayName()+"님 환영합니다.",Toast.LENGTH_SHORT).show();
                            recordCkeck();
                        }
                    }
                    else { }
                }
            }
        };
    }

    //로그인 이벤트 메소드
    void loginEvent(){
        String email = email_et.getText().toString();
        String pass = password_et.getText().toString();

        if(!TextUtils.isEmpty(email)) {

            if(!TextUtils.isEmpty(pass)) {
                //로그인 메소드, 아이디와 비밀번호를 firebase에 전달하고 가입이 되어있다면 로그인 시켜주는 메소드
                mAuth.signInWithEmailAndPassword(email_et.getText().toString(), password_et.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                else if (task.isSuccessful()) {
                                    user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        if (user.isEmailVerified() == true) {
                                            Toast.makeText(getApplicationContext(),user.getDisplayName()+"님 환영합니다.",Toast.LENGTH_SHORT).show();
                                            //작업중
                                            recordCkeck();


                                        } else if (user.isEmailVerified() == false) {
                                            Toast.makeText(LoginActivity.this, "E-mail 인증해주세요!", Toast.LENGTH_SHORT).show();
                                            re_verify.setVisibility(View.VISIBLE);

                                        }
                                    }
                                }
                            }
                        });
            }
            else {
                Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        else {
            Toast.makeText(LoginActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        //로그인 인터페이스 리스너 생성
    }

    //인증메일 재전송 메소드
    private void sendEmailVerification() {

        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "인증메일 재전송 완료",
                            Toast.LENGTH_SHORT).show();
                    re_verify.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "인증메일 전송 실패.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //스타트하면
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }
    //정지하면
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
        hideProgressDialog();
    }
    //자동로그인값 확인
    private void autologin(){
        if(mPref.getString("switch", "").equals("1")) {
            autoLogin.setChecked(true);
            showProgressDialog();
            Log.d("setting", "true");
        } else {
            autoLogin.setChecked(false);
            Log.d("setting", "false");
        }
    }


    private boolean recordCkeck(){
        final boolean result = false;

        //DB에서 user -> uid -> NewUser를 찾고 한번만 이벤트를 발생시켜서 데이터를 가지고옴.  singlevalueEvent로 해야 1번만 불러옴.
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(user.getUid())
                .child("NewUser")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "recordCkeck  :  "+dataSnapshot.getValue());

                if(dataSnapshot.getValue().equals("yes")) {
                    Intent intent_r = new Intent(LoginActivity.this, RecordActivity.class);
                    startActivity(intent_r);
                    finish();
                }

                else if(dataSnapshot.getValue().equals("No")){
                        Intent intent_m = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent_m);
                        finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "ERROR DataBase");
            }
        });

        return result;
    }
}
