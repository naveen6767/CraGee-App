package com.crageeApp.appbesocial.Groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.crageeApp.appbesocial.R;


public class GroupCategoriesActivity extends AppCompatActivity {


    private GridLayout groupCategoriesLayout;
    private static final String TAG = "GroupCategoriesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_categories);

        groupCategoriesLayout = findViewById(R.id.gridCategories);
        groupCategoriesLayout = findViewById(R.id.gridCategories);
        int childCount = groupCategoriesLayout.getChildCount();

        for (int i= 0; i < childCount; i++){
            CardView container = (CardView) groupCategoriesLayout.getChildAt(i);
            Log.i(TAG, "onCreate: inside container "+container);
            Log.i(TAG, "onCreate: "+container.getId());

            final int currentClickedPosition=i;
            container.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    // your click code here

                    Log.i(TAG, "onClick: "+ currentClickedPosition +"th position clicked");
                    String selectedCategory;
                    switch (currentClickedPosition){
                        case 0:
                            selectedCategory = "Relations";
                            break;
                        case 1:
                            selectedCategory = "Entertainment";
                            break;
                        case 2:
                            selectedCategory = "Business";
                            break;
                        case 3:
                            selectedCategory = "Hobbies";
                            break;
                        case 4:
                            selectedCategory = "Ecommerce";
                            break;
                        case 5:
                            selectedCategory = "Science and Tech";
                            break;
                        case 6:
                            selectedCategory = "Faith and Spirituality";
                            break;
                        case 7:
                            selectedCategory = "Animals";
                            break;
                        case 8:
                            selectedCategory = "Style";
                            break;
                        case 9:
                            selectedCategory = "Others";
                            break;
                        default:
                            selectedCategory="";
                            break;
                    }

                    sendUserToSpecificCategory(selectedCategory);


                }
            });
        }
    }
    private void sendUserToSpecificCategory(String selectedCategory) {
        Intent intentToSpecificCategory = new Intent(GroupCategoriesActivity.this,SpecificGroupCategoryActivity.class);
        intentToSpecificCategory.putExtra("category choice",selectedCategory);
        startActivity(intentToSpecificCategory);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
