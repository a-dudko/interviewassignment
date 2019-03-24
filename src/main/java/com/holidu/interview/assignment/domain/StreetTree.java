package com.holidu.interview.assignment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StreetTree {

    public StreetTree() {

    }

    public StreetTree(String name, Double xCoord, Double yCoord) {
        this.name = name;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    @JsonProperty("spc_common")
    private String name;

    @JsonProperty("x_sp")
    private Double xCoord;

    @JsonProperty("y_sp")
    private Double yCoord;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getXCoord() {
        return xCoord;
    }

    public void setXCoord(Double xCoord) {
        this.xCoord = xCoord;
    }

    public Double getYCoord() {
        return yCoord;
    }

    public void setYCoord(Double yCoord) {
        this.yCoord = yCoord;
    }
}
