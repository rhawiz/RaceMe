package com.example.rawand.raceme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector.SimpleOnGestureListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChallengesActivity extends BaseActivity {
    private TabHost tabHost;
    private AutoCompleteTextView friendSearchView;
    private String[] userList;
    private User[] userDetailedList;
    private User currentUserSelected;
    private HashMap userMap;
    ArrayAdapter arrayAdapter;
    ListView friendsListView;
    StableArrayAdapter adapter;
    private TextView noFriendsView;
    private Button clearFriendSearchButton;
    private ImageButton addFriendButton;
    private AdapterView.OnItemClickListener findFriendsTextListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_challenges, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        friendSearchView = (AutoCompleteTextView) findViewById(R.id.find_friend_textview);


        final InitFriendsScreenTask friendsScreenTask = new InitFriendsScreenTask();
        friendsScreenTask.execute();

        findFriendsTextListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                currentUserSelected = userDetailedList[pos];
                Log.w("CHALLENGESACTIVITY",userDetailedList.toString());
                Toast.makeText(getApplicationContext(), String.valueOf(pos), Toast.LENGTH_LONG).show();
                Log.w("CHALLENGESACTIVITY",String.valueOf(pos));
                if(userDetailedList != null) {
                    Toast.makeText(ChallengesActivity.this, userDetailedList[pos].toString(), Toast.LENGTH_LONG).show();
                }
            }
        };




        friendsListView = (ListView) findViewById(R.id.friends_list_view);

        noFriendsView = (TextView) findViewById(R.id.no_friends_textview);

        clearFriendSearchButton = (Button) findViewById(R.id.clear_friend_text_button);

        clearFriendSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendSearchView.setText("");
            }
        });

        addFriendButton = (ImageButton) findViewById(R.id.add_friend_button);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),String.valueOf(currentUserSelected == null), Toast.LENGTH_LONG).show();
                if(currentUserSelected != null){
                    Log.w("CHALLENGESACTIVITY", currentUserSelected.toString());
                }
            }
        });









    }


    /**
     * Initialise tabs
     */
    private void initTabs(){


        tabHost = (TabHost) findViewById(R.id.challenges_tabhost);
        tabHost.setup();

        TabSpec tabSpec = tabHost.newTabSpec("friends");
        tabSpec.setContent(R.id.challenges_friend_tab);
        tabSpec.setIndicator("Friends");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("leaderboard");
        tabSpec.setContent(R.id.challenges_leaderboard_tab);
        tabSpec.setIndicator("Leaderboard");
        tabHost.addTab(tabSpec);

        tabSpec  = tabHost.newTabSpec("achievement");
        tabSpec.setContent(R.id.challenges_achievement_tab);
        tabSpec.setIndicator("Achievements");
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
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }


    public class InitFriendsScreenTask extends AsyncTask<Void, Void, Boolean> {



        public InitFriendsScreenTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String userId = SaveSharedPreference.getUserDetails(ChallengesActivity.this).getUserId();


            userMap = DatabaseHelper.friendsGetUsers(userId);

            adapter = new StableArrayAdapter(ChallengesActivity.this,
                    android.R.layout.simple_list_item_1, DatabaseHelper.getFriendListSimple(userId));


            return true;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                /*userList = (String[]) userMap.get("displayList");
                userDetailedList = (User[]) userMap.get("detailList");

                arrayAdapter = new ArrayAdapter(ChallengesActivity.this, android.R.layout.select_dialog_item, userList);

                friendSearchView.setThreshold(1);
                friendSearchView.setAdapter(arrayAdapter);
                friendSearchView.setOnItemClickListener(findFriendsTextListener);

                friendSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                        String selection = (String) parent.getItemAtPosition(position);
                    }
                });

                friendsListView.setAdapter(adapter);
                if(adapter.getCount() == 0){
                    noFriendsView.setVisibility(View.VISIBLE);
                    friendsListView.setVisibility(View.GONE);
                }*/
            }
        }

        @Override
        protected void onCancelled() {

        }


    }

}
