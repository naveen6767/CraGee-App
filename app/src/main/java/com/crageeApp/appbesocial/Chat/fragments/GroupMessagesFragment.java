package com.crageeApp.appbesocial.Chat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterGroupChatlist;
import com.crageeApp.appbesocial.Models.ModelGroupChat;
import com.crageeApp.appbesocial.Models.ModelGroupChatlist;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupMessagesFragment extends Fragment {


    private View GroupMessagesFragmentView;
    private RecyclerView rVGroupChatList;
    private FirebaseAuth userAuth;
    private DatabaseReference chatUserRef,groupsRef;
    private FirebaseUser currentUser;
    private List<ModelGroupChatlist> groupChatlist;
    private List<ModelGroups> groupsList;
    private AdapterGroupChatlist adapterGroupChatlist;
    private String saveCurrentDate;
    public static final String TAG = "Groups Messages";
    public GroupMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        GroupMessagesFragmentView= inflater.inflate(R.layout.fragment_group_messages, container, false);

        //init the list here
        groupChatlist=new ArrayList<>();
        groupsList=new ArrayList<>();
        //init the recycler view here
        rVGroupChatList=GroupMessagesFragmentView.findViewById(R.id.group_chatList_recyclerView);

        Calendar calForDate =Calendar.getInstance();
        SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        //init the fire base here
        userAuth= FirebaseAuth.getInstance();
        currentUser=userAuth.getCurrentUser();
        chatUserRef= FirebaseDatabase.getInstance().getReference("Users");
        chatUserRef.keepSynced(true);
        groupsRef= FirebaseDatabase.getInstance().getReference("Groups");
        groupsRef.keepSynced(true);
        chatUserRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("GroupChatList")
                .orderByChild("groupChatTime")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupChatlist.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren())
                        {
                            Log.i(TAG, "onDataChange: "+snapshot);
                            ModelGroupChatlist sentChatList=snapshot.getValue(ModelGroupChatlist.class);
                            groupChatlist.add(sentChatList);

                            loadGroupChats();

                            //adapter
                            adapterGroupChatlist=new AdapterGroupChatlist(getContext(),groupsList);

                            //notify adapter
                            adapterGroupChatlist.notifyDataSetChanged();

                            //set recycler view adapter
                            rVGroupChatList.setAdapter(adapterGroupChatlist);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return GroupMessagesFragmentView;
    }



    private void loadGroupChats() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelGroups modelGroups=ds.getValue(ModelGroups.class);
                    for (ModelGroupChatlist groupList:groupChatlist){
                        Log.i(TAG, "inside Load groups chats"+groupList.getGroupID());
                        if (modelGroups.getGroupId().equals(groupList.getGroupID())){
                            Log.i(TAG, "inside the model groups"+modelGroups);
                            groupsList.add(modelGroups);
                            Log.i(TAG, "inside the groupsList"+groupsList);

                        }

                        //adapter
                        adapterGroupChatlist=new AdapterGroupChatlist(getContext(),groupsList);

                        //notify adapter
                        adapterGroupChatlist.notifyDataSetChanged();

                        //set recycler view adapter
                        rVGroupChatList.setAdapter(adapterGroupChatlist);

                        //set the last message
                        lastMessage(groupList.getGroupID(),groupList.getGroupChatKey());
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void lastMessage(final String groupID, String groupChatKey) {
        Log.i(TAG, "lastMessage: groupID"+groupID);
        Log.i(TAG, "lastMessage: groupChatKey"+groupChatKey);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Group Chats");
        reference.keepSynced(true);

        reference.child(groupChatKey)
                .child(saveCurrentDate)
                .orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage="default";
                Log.i(TAG, "inside the datasnapshot"+dataSnapshot.getChildren());
                Log.i(TAG, "children in the database"+dataSnapshot.getChildrenCount());
                Log.i(TAG, "datasnapshot values"+dataSnapshot );
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelGroupChat groupChat=dataSnapshot1.getValue(ModelGroupChat.class);
                    Log.i(TAG, "in the group chat message "+groupChat.getMessage());
                    Log.i(TAG, "in the group chat "+groupChat.getMessage());
                    theLastMessage=groupChat.getMessage();
                   /* if (groupChat.getSender().equals(currentUser.getUid()))
                    {
                        theLastMessage=groupChat.getMessage();
                        Log.i(TAG, "accessing the last message"+theLastMessage);
                    }

                    */
                }
                adapterGroupChatlist.setLastMessageMap(groupID,theLastMessage);
                adapterGroupChatlist.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
