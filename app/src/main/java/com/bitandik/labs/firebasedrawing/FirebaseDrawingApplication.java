package com.bitandik.labs.firebasedrawing;

import com.firebase.client.Firebase;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class FirebaseDrawingApplication extends android.app.Application {

    private static final String TWITTER_KEY = "<TWITTER KEY>";
    private static final String TWITTER_SECRET = "<TWITTER SECRET>";
    String[] colors = {"#ffffff","#000000","#ff0000","#00ff00","#0000ff","#8888ff","#ff88dd","#ff8888","#ff0055","#ff8800","#00ff88","#ccff00","#0088ff","#440088","#ffff88","#88ffff"};

    private String FIREBASE_URL = "https://led-drawing.firebaseio.com/";
    Firebase dataReference;

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Firebase.setAndroidContext(this);
        dataReference = new Firebase(FIREBASE_URL);
    }

    public void addChild(String child) {
        dataReference = dataReference.getRoot().child(child);
    }

    public void setRoot() {
        dataReference = dataReference.getRoot();
    }

    public Firebase getFirebase() {
        return dataReference;
    }

    public String[] getColors() {
        return colors;
    }

    public String getRandomColor(){
        Random r = new Random();
        return colors[r.nextInt(colors.length)];
    }
}
