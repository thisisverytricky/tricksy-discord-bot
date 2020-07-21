package net.mikej.bots.tricksy.models;

import java.util.List;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;

@BsonDiscriminator
public class Ink {

    @BsonId
    private String _id;
    private String fullName;
    private Boolean approved;
    private String reviewLink;
    private String submittedBy;
    private String primaryImage;
    private List<String> alternateImages;
    private List<String> alternateNames;

    public Ink() {}


    public String get_id() {
        return this._id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean isApproved() {
        return this.approved;
    }

    public Boolean getApproved() {
        return this.approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getReviewLink() {
        return this.reviewLink;
    }

    public void setReviewLink(String reviewLink) {
        this.reviewLink = reviewLink;
    }

    public String getSubmittedBy() {
        return this.submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getPrimaryImage() {
        return this.primaryImage;
    }

    public void setPrimaryImage(String primaryImage) {
        this.primaryImage = primaryImage;
    }

    public List<String> getAlternateImages() {
        return this.alternateImages;
    }

    public void setAlternateImages(List<String> alternateImages) {
        this.alternateImages = alternateImages;
    }

    public List<String> getAlternateNames() {
        return this.alternateNames;
    }

    public void setAlternateNames(List<String> alternateNames) {
        this.alternateNames = alternateNames;
    }

}