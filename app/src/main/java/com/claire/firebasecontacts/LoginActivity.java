package com.claire.firebasecontacts;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //填入已儲存帳號
        EditText email = findViewById(R.id.ed_email);
        SharedPreferences setting =
                getSharedPreferences("contacts", MODE_PRIVATE);
        email.setText(setting.getString("PREF_EMAIL", ""));


        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!= null){
                    Log.d("onAuthStateChanged: ", "登入：" + user.getUid());
                    userUID = user.getUid();
                    //setUserData();
                    //pushFriend("Jack");

                }else {
                    Log.d("onAuthStateChanged: ", "已登出");
                }

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
        auth.signOut();
    }

    public void login(View view) {
        final String email = ((EditText)findViewById(R.id.ed_email)).getText().toString();
        final String password = ((EditText)findViewById(R.id.ed_password)).getText().toString();

        //儲存登入Email
        SharedPreferences pref = getSharedPreferences("contacts", MODE_PRIVATE);
        pref.edit()
                .putString("PREF_EMAIL", email)
                .apply();

        Log.d("AUTH", "login: " + email + "/" + password);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Log.d("onComplete: ", "登入失敗");
                            register(email, password);
                        }
                    }
                });
    }

    private void register(final String email, final String password) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("登入問題")
                .setMessage("無此帳號，是否要以此帳號與密碼註冊?")
                .setPositiveButton("註冊", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createUser(email, password);
                    }
                })
                .setNeutralButton("取消", null)
                .show();
    }

    private void createUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String message =
                                task.isComplete()? "註冊成功" : "註冊失敗";
                        new AlertDialog.Builder(LoginActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }

    //將資料存在FireBase上
    private void setUserData(){
        //將資料儲存在[users]記錄下，在取得資料庫物件後，再取得users的參照
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("users");

        //接著呼叫child方法代表要在users記錄下新增子記錄，再呼叫setValue方法，
        //在users記錄下新增資料
        usersRef.child(userUID).child("phone").setValue("0920123456");
        usersRef.child(userUID).child("nickname").setValue("Hank");

        //需要更新記錄時，可使用Map集合配合updateChildren方法
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Hank123");
        usersRef.child(userUID).updateChildren(data,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError,
                                           @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null){
                            //正確完成
                        } else {
                            //發生錯誤
                        }
                    }
                });
    }

    //使用push方法
    private void pushFriend(String name){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("users");
        DatabaseReference friendsRef =
                usersRef.child(userUID).child("friends").push();
        Map<String, Object> friend = new HashMap<>();
        friend.put("name", name);
        friend.put("phone","22334455");
        friendsRef.setValue(friend);
        String friendId = friendsRef.getKey();
        Log.d("FRIEND",friendId + "");
    }
}
