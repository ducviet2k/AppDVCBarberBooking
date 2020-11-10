package com.example.dvcbaberbooking.Model;

import com.google.firebase.firestore.FieldValue;

public class MyNotification {
    private String uid, title, conten;
    private boolean read;
    private FieldValue serverTimestamp;

    public MyNotification() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConten() {
        return conten;
    }

    public void setConten(String conten) {
        this.conten = conten;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public FieldValue getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(FieldValue serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
