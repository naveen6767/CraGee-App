package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelReviews;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterReviews extends RecyclerView.Adapter<AdapterReviews.MyHolder> {

    private static final String TAG ="Adapter Reviews" ;
    private Context context;
    private List<ModelReviews> reviewsList;

    private String myUid;
     private DatabaseReference reviewsRef;  //for reviews database node
 

    public AdapterReviews(Context context, List<ModelReviews> reviewsList) {
        this.context = context;
        this.reviewsList = reviewsList;
 
        //initialize the fire base here
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reviewsRef = FirebaseDatabase.getInstance().getReference("Users");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout allpostlayout.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_reviews_layout,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        final String publisherId =reviewsList.get(position).getReviewerId();
        if (reviewsList.get(position).getReviewerName()!=null&& !reviewsList.get(position).getReviewerName().equals("")){
            String userName=reviewsList.get(position).getReviewerName();
            holder.reviewer_userName.setText(userName);
        }
        if (reviewsList.get(position).getReviewerImage()!=null&& !reviewsList.get(position).getReviewerImage().equals(""))
        {
            String userDp =reviewsList.get(position).getReviewerImage();
            //set reviewer Dp
            try {
                Picasso.get().load(userDp).placeholder(R.drawable.profile_image).into(holder.image_reviewer);

            }catch (Exception e)
            {
                Picasso.get().load(R.drawable.profile_image).into(holder.image_reviewer);
            }
        }
        if (reviewsList.get(position).getReview()!=null&& !reviewsList.get(position).getReview().equals("")){
            String reviewText =reviewsList.get(position).getReview();
            holder.text_reviewed.setText(reviewText);
        }
        if (reviewsList.get(position).getReviewDate()!=null&& !reviewsList.get(position).getReviewDate().equals("")){
            String reviewedDate =reviewsList.get(position).getReviewDate();
            holder.date_reviewed.setText(reviewedDate);
        }


        float ratingsNo =reviewsList.get(position).getRating();
        holder.ratingBarReviews.setRating(ratingsNo);
        holder.ratingBarReviews.setIsIndicator(true);

    }



    @Override
    public int getItemCount() {
        return reviewsList.size();
    }
    
    //view holder class
    class MyHolder extends RecyclerView.ViewHolder
    {
        //views from the row_reviews_layout.xml are defined here
        CircleImageView image_reviewer;
        TextView reviewer_userName,text_reviewed,date_reviewed;
        RatingBar ratingBarReviews;
        
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initialize all the views here
            image_reviewer=itemView.findViewById(R.id.reviewer_Image);
            reviewer_userName=itemView.findViewById(R.id.reviewer_Name);
            text_reviewed=itemView.findViewById(R.id.reviewed_text);
            date_reviewed=itemView.findViewById(R.id.reviewed_Date);
            ratingBarReviews=itemView.findViewById(R.id.rating_all_reviews);

        }
    }
}
