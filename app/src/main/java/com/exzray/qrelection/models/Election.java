package com.exzray.qrelection.models;

import java.util.Date;

public class Election {

    private Date start = null;
    private Integer votes = 0;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
