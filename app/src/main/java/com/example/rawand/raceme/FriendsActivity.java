package com.example.rawand.raceme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.mysql.jdbc.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Activity class for Friends screen.
 * Will contain any methods and variable related to the friends screen.
 */
public class FriendsActivity extends BaseActivity {
    private TabHost tabHost;
    private AutoCompleteTextView friendSearchView;
    private User selectedUser;
    private User currentUser;
    private ArrayList<User> userList;
    private ArrayList<User> friendsList;
    private ArrayList<User> friendRequests;
    private UserArrayAdapter friendListAdapter;
    private ListView friendsListView;
    private UserArrayAdapter userSearchAdapter;
    private TextView noFriendsView;
    private TextView noRequestsView;
    private Button clearFriendSearchButton;
    private ImageButton addFriendButton;
    private AdapterView.OnItemClickListener findFriendsTextListener;

    private ListView friendRequestsListView;
    private UserArrayAdapter friendRequestsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_friends, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        currentUser = SaveSharedPreference.getUserDetails(FriendsActivity.this);


        final InitFriendsScreenTask friendsScreenTask = new InitFriendsScreenTask();
        friendsScreenTask.execute();

        initFriendsTabContent();
        initRequestsTabContent();


    }


    /**
     * Initialise friend request tab.
     */
    private void initRequestsTabContent(){
        friendRequestsListView = (ListView) findViewById(R.id.friend_requests_list_view);
        noRequestsView = (TextView) findViewById(R.id.no_requests_textview);

        friendRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(FriendsActivity.this)
                        .setTitle("Confirm request")
                        .setMessage("Do you wish to add " + friendRequestsAdapter.getItem(position).getFirstname() + " as friend?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                AcceptFriendRequestTask acceptRequest = new AcceptFriendRequestTask(friendRequestsAdapter.getItem(position),currentUser);
                                acceptRequest.execute();
                            }})
                        .setNegativeButton("Reject", null).show();
            }
        });

    }


    /**
     * Initialise friends tab
     */
    private void initFriendsTabContent(){

        friendSearchView = (AutoCompleteTextView) findViewById(R.id.find_friend_textview);

        findFriendsTextListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                selectedUser = userSearchAdapter.getItem(pos);
                friendSearchView.setEnabled(false);
            }
        };

        friendSearchView.setThreshold(1);
        friendSearchView.setOnItemClickListener(findFriendsTextListener);

        friendsListView = (ListView) findViewById(R.id.friends_list_view);

        noFriendsView = (TextView) findViewById(R.id.no_friends_textview);

        clearFriendSearchButton = (Button) findViewById(R.id.clear_friend_text_button);

        clearFriendSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendSearchView.setText("");
                friendSearchView.setEnabled(true);
                selectedUser = null;
            }


        });

        addFriendButton = (ImageButton) findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedUser != null){
                    SendFriendRequestTask friendRequestTask = new SendFriendRequestTask(currentUser,selectedUser);
                    friendRequestTask.execute();
                    friendSearchView.setText("");
                    friendSearchView.setEnabled(true);
                    selectedUser = null;
                }else{
                    Utilities.showToast("Could not find user '" + friendSearchView.getText().toString() + "'", Toast.LENGTH_LONG,FriendsActivity.this);
                }
            }
        });
    }

    /**
     * Initialise tabs
     */
    private void initTabs(){


        tabHost = (TabHost) findViewById(R.id.friends_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("friends");
        tabSpec.setContent(R.id.friends_friend_tab);
        tabSpec.setIndicator("Friends");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("friend_requests");
        tabSpec.setContent(R.id.friends_requests_tab);
        tabSpec.setIndicator("Requests");
        tabHost.addTab(tabSpec);


        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            TransitionAnimations transition = new TransitionAnimations();
            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if(nextTab > 2)nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }
            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if(nextTab < 0)nextTab = 2;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });



    }

    /**
     * Custom ArrayAdapter class to handle and store ArrayList of User objects when searching for friends.
     */

    private class UserArrayAdapter extends ArrayAdapter<User> {

        HashMap<User, Integer> mIdMap = new HashMap<User, Integer>();

        public UserArrayAdapter(Context context, int textViewResourceId,
                                List<User> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            User item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }


    /**
     * Initialise friends screen threaded task.
     * Will query database and populate list with the data retrieved.
     */

    public class InitFriendsScreenTask extends AsyncTask<Void, Void, Boolean> {



        public InitFriendsScreenTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String userId = currentUser.getUserId();

            userList = DatabaseHelper.getNotFriendsList(userId);
            friendsList = DatabaseHelper.getFriendsList(userId);

            friendRequests = DatabaseHelper.getFriendRequestUsers(userId);

            friendRequestsAdapter = new UserArrayAdapter(FriendsActivity.this,android.R.layout.simple_list_item_1, friendRequests);

            userSearchAdapter = new UserArrayAdapter(FriendsActivity.this,android.R.layout.select_dialog_item, userList);

            friendListAdapter = new UserArrayAdapter(FriendsActivity.this,android.R.layout.simple_list_item_1, friendsList);

            return true;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {

                friendSearchView.setAdapter(userSearchAdapter);

                friendsListView.setAdapter(friendListAdapter);

                friendRequestsListView.setAdapter(friendRequestsAdapter);

                if(friendRequestsListView.getCount() == 0){
                    noRequestsView.setVisibility(View.VISIBLE);
                    friendRequestsListView.setVisibility(View.GONE);
                }

                if(friendListAdapter.getCount() == 0){
                    noFriendsView.setVisibility(View.VISIBLE);
                    friendsListView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected void onCancelled() {

        }


    }


    /**
     * Async task to send a friend request and update database tables accordingly
     * Will show a toast which contains a message notifying the user whether the friend request was sent successfully or not.
     */
    public class SendFriendRequestTask extends AsyncTask<Void, Void, Boolean> {

        private User userSender;
        private User userReceiver;

        public SendFriendRequestTask(User userSender, User userReceiver) {
            this.userSender = userSender;
            this.userReceiver = userReceiver;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if(DatabaseHelper.sendFriendRequest(userSender,userReceiver))
                return true;

            return false;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Utilities.showToast("Friend request sent.", Toast.LENGTH_LONG,FriendsActivity.this);
            }else{
                Utilities.showToast("Could not send friend request. Possible network issue.", Toast.LENGTH_LONG,FriendsActivity.this);
            }
        }

        @Override
        protected void onCancelled() {

        }


    }

    /**
     * Async task to accept a friend request. Will update database tables accordingly
     * Will show a toast which contains a message notifying the user whether the request was accepted successfully or not.
     */
    public class AcceptFriendRequestTask extends AsyncTask<Void, Void, Boolean> {

        private User userSender;
        private User userReceiver;

        public AcceptFriendRequestTask(User userSender, User userReceiver) {
            this.userSender = userSender;
            this.userReceiver = userReceiver;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if(DatabaseHelper.acceptFriendRequest(userSender, userReceiver))
                return true;

            return false;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Utilities.showToast("Friend request accepted.", Toast.LENGTH_LONG,FriendsActivity.this);
                finish();
                startActivity(getIntent());
            }else{
                Utilities.showToast("Could not accept friend request. Possible network issue.", Toast.LENGTH_LONG,FriendsActivity.this);
            }
        }

        @Override
        protected void onCancelled() {

        }


    }

}
