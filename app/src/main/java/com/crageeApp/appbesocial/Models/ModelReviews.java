package com.crageeApp.appbesocial.Models;

public class ModelReviews {
    private String review,reviewerId,reviewerImage,reviewerName,timeStamp,reviewDate;
    private float rating;

    public ModelReviews() {
    }

    public ModelReviews(String review, String reviewerId, String reviewerImage, String reviewerName, String timeStamp, String reviewDate, float rating) {
        this.review = review;
        this.reviewerId = reviewerId;
        this.reviewerImage = reviewerImage;
        this.reviewerName = reviewerName;
        this.timeStamp = timeStamp;
        this.reviewDate = reviewDate;
        this.rating = rating;
    }
    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerImage() {
        return reviewerImage;
    }

    public void setReviewerImage(String reviewerImage) {
        this.reviewerImage = reviewerImage;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

}
