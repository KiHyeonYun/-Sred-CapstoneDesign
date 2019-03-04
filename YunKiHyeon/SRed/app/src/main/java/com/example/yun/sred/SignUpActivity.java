package com.example.yun.sred;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yun.sred.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email, password, name, confirm_password;
    private DatabaseReference databaseReference;
    private Button signup_user;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("회원가입");

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("userData");
        email = (EditText) findViewById(R.id.signupActivity_email);
        password = (EditText) findViewById(R.id.signupActivity_password);
        confirm_password = (EditText) findViewById(R.id.signupActivity_password2);
        name = (EditText) findViewById(R.id.signupActivity_name);
        signup_user = (Button) findViewById(R.id.signupActivity_button_signup);
        signup_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null) {
                    if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                        Toast.makeText(SignUpActivity.this, "비밀번호가 서로 다릅니다.", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(SignUpActivity.this, "빈칸없이 채워주세요.", Toast.LENGTH_LONG).show();
                    return;
                }


                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                //푸쉬알람을 할 때 이름이 뜨게 담아두는코드.
                                final String uid = task.getResult().getUser().getUid();
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);

                                UserModel userModel = new UserModel();
                                userModel.userName = name.getText().toString();
                                userModel.NewUser = "yes";
                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                //정보가 저장 된건가 확인 (지울꺼)
                                Toast.makeText(SignUpActivity.this, userModel.NewUser, Toast.LENGTH_SHORT).show();

                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SignUpActivity.this, "메일인증을 하시면 가입이 완료됩니다.", Toast.LENGTH_LONG).show();
                                        user = mAuth.getCurrentUser();
                                        sendEmailVerification();
                                    }

                                });
                            }
                        });
            }
        });
    }

    private void sendEmailVerification() {

        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                    finish();

                } else {
                    Toast.makeText(SignUpActivity.this,
                            "인증메일 전송 실패.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}