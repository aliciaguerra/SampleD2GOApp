package com.digital2go.sdk.domain;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

/**
 *@author Digital2GO
 * Created by Ulises Rosas
 */

public class GeofencesInArea {
    /**
     * Campaign_id, coverage_type
     */
    private String campaign_id,coverage_type;

    /**
     * List of points
     */
    private List<LatLng> coverage;

    private boolean displayed = false;

    /**
     * Default Constructor
     */
    public GeofencesInArea() {
    }

    public String getCampaign_id() {
        return campaign_id;
    }

    public void setCampaign_id(String campaign_id) {
        this.campaign_id = campaign_id;
    }

    public String getCoverage_type() {
        return coverage_type;
    }

    public void setCoverage_type(String coverage_type) {
        this.coverage_type = coverage_type;
    }

    public List<LatLng> getCoverage() {
        return coverage;
    }

    public void setCoverage(List<LatLng> coverage) {
        this.coverage = coverage;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }
}
