package com.androidtutorialpoint.firebasegrocerylistapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class changePSW extends AppCompatActivity {

    Button submit, Cancel;
    EditText newPsw, repeat,oldPsw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);
        submit = (Button) findViewById(R.id.pswSubmit);
        Cancel = (Button) findViewById(R.id.pswCancle);
        newPsw = (EditText) findViewById(R.id.pswTxt1);
        repeat = (EditText) findViewById(R.id.pswTxt2);
        oldPsw = (EditText) findViewById(R.id.oldPsw);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit.setText("");
                Cancel.setText("");
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. not null
                if(oldPsw.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"old password could not be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPsw.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"new password could not be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                //valid
                if(newPsw.getText().toString().length()<6)
                {
                    Toast.makeText(getApplicationContext(),"length of password must larger than 6",Toast.LENGTH_SHORT).show();
                    return;
                }
                //2. equal
                if(!repeat.getText().toString().equals(newPsw.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"not same",Toast.LENGTH_SHORT).show();
                    return;
                }


                //3. change
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.w("old",oldPsw.getText().toString());
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),oldPsw.getText().toString());
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            user.updatePassword(newPsw.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Log.w("password update:","success");
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(changePSW.this,"set new password fail",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(changePSW.this,"wrong old password",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
