package com.androidtutorialpoint.firebasegrocerylistapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private final String TAG = "MenuActivity";

    DatabaseReference mDB;
    DatabaseReference mListItemRef;
    private ListView mFrigItemList;     //Reference to the listview GUI component
    private ListAdapter frigAdapter;
    private LinearLayout frig_list;
    private LinearLayout shopping_list;
    private LinearLayout accoount_list;

    private String Uid;
    private ArrayList<ListItem> myListItems;

    // Account Page related
    private Button logOut;
    private FirebaseAuth auth;
    Profile profile;
    ProfilePictureView avatar;
    Bitmap bmp = null;
    Button pwdBtn;
    TextView nameTv;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    frig_list.setVisibility(View.VISIBLE);
                    shopping_list.setVisibility(View.GONE);
                    accoount_list.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dashboard:
                    frig_list.setVisibility(View.GONE);
                    shopping_list.setVisibility(View.VISIBLE);
                    accoount_list.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_notifications:
                    frig_list.setVisibility(View.GONE);
                    shopping_list.setVisibility(View.GONE);
                    accoount_list.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Initialize items
        frig_list = (LinearLayout)findViewById(R.id.frig_list);
        shopping_list = (LinearLayout)findViewById(R.id.shopping);
        accoount_list = (LinearLayout)findViewById(R.id.account);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        myListItems = new ArrayList<>();
        mFrigItemList = (ListView)findViewById(R.id.frig_item_list);
        frigAdapter = new CustomAdapter(this.getBaseContext(), myListItems);
        mFrigItemList.setAdapter(frigAdapter);

        /*
            Account page related initialization
         */

        bmp = BitmapFactory.decodeResource(getResources(),R.drawable.defaultavater);

        avatar = (ProfilePictureView) findViewById(R.id.avatar);
        avatar.setDefaultProfilePicture(bmp);
        profile = Profile.getCurrentProfile();

        //change password button
        pwdBtn = (Button) findViewById(R.id.PWDbtn);
        pwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(),changePSW.class);
                startActivity(intent);

            }
        });

        //qpx
        //logout:
        auth = FirebaseAuth.getInstance();
        // who is User
        nameTv = (TextView) findViewById(R.id.UserNameTxt);
        nameTv.setText((CharSequence) auth.getCurrentUser().getEmail());

        logOut = (Button) findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                auth.signOut();

                if(profile!=null)
                    LoginManager.getInstance().logOut();

                if(auth.getCurrentUser()==null)
                {
                    Intent intent =  new Intent(getApplication(),LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });

        /*
            Menu page initialization
         */

        // database related operations
        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDB= FirebaseDatabase.getInstance().getReference();
        mListItemRef = mDB.child("listItem").child(Uid);
        updateUI();

        mListItemRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG+"Added",dataSnapshot.getValue(ListItem.class).toString());
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG+"Changed",dataSnapshot.getValue(ListItem.class).toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG+"Removed",dataSnapshot.getValue(ListItem.class).toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG+"Moved",dataSnapshot.getValue(ListItem.class).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG+"Cancelled",databaseError.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_delete_all:
                deleteAllListItems();
                break;
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteAllListItems(){
        FirebaseDatabase.getInstance().getReference().child("listItem").removeValue();
        myListItems.clear();
        Toast.makeText(this,"Items Deleted Successfully",Toast.LENGTH_SHORT).show();
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        ListItem listItem=dataSnapshot.getValue(ListItem.class);
        myListItems.add(listItem);
        /*
        for(DataSnapshot data : dataSnapshot.getChildren()){
            ListItem template = data.getValue(ListItem.class);
            // use this object and store it into an ArrayList<Template> to use it further
            myListItems.add(template);*/
        updateUI();
    }

    private void updateUI(){
        frigAdapter = new CustomAdapter(getBaseContext(), myListItems);
        if (frigAdapter != null)
        {
            mFrigItemList.setAdapter(frigAdapter);
        }
    }

    public void createNewListItem() {
        // Create new List Item  at /listItem

        final String key = FirebaseDatabase.getInstance().getReference().child("listItem").push().getKey();
        LayoutInflater li = LayoutInflater.from(this);
        View getListItemView = li.inflate(R.layout.dialog_get_list_item, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(getListItemView);

        final EditText userInput = (EditText) getListItemView.findViewById(R.id.editTextDialogUserInput);
        final EditText Expiration = (EditText) getListItemView.findViewById(R.id.editExpirationDialogUserInput);
        final EditText Tags = (EditText) getListItemView.findViewById(R.id.editTagsDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // get user input and set it to result
                        // edit text
                        String listItemText = userInput.getText().toString();
                        String listItemExpiration = Expiration.getText().toString();
                        String listItemTags = Tags.getText().toString();
                        ListItem listItem = new ListItem(listItemText, listItemExpiration, listItemTags);
                        Map<String, Object> listItemValues = listItem.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/listItem/" + key, listItemValues);
                        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

                    }
                }).create()
                .show();

    }

}
