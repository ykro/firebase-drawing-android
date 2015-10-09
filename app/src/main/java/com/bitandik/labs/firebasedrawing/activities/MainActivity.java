package com.bitandik.labs.firebasedrawing.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bitandik.labs.firebasedrawing.FirebaseDrawingApplication;
import com.bitandik.labs.firebasedrawing.R;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.twitter.sdk.android.Twitter;

public class MainActivity extends ActionBarActivity {

    String color;
    DrawingView dv;
    int pixelSize;
    int matrixRows = 16;
    int matrixColumns = 32;
    Firebase firebaseDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dv = new DrawingView(this);
        setContentView(dv);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        FirebaseDrawingApplication app = (FirebaseDrawingApplication)getApplicationContext();

        SharedPreferences prefs = getApplication().getSharedPreferences("LEDPrefs", 0);
        String username = prefs.getString("username", null);
        setTitle(username);

        String childUsername = username.replace(".","_");
        app.addChild(childUsername);

        firebaseDataRef = app.getFirebase();

        Intent i = getIntent();
        String colorFromIntent = i.getStringExtra("color");
        if (colorFromIntent != null) {
            color = colorFromIntent;
        } else {
            color = app.getRandomColor();
        }


    }

    public void drawPixel(DataSnapshot dataSnapshot) {
        String coords[] = (dataSnapshot.getKey()).split(":");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        String color = dataSnapshot.getValue(String.class);
        dv.draw(x * pixelSize, y * pixelSize, color);
    }

    public class DrawingView extends View {
        int squareSize;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint  mBitmapPaint;

        public DrawingView(Context context) {
            super(context);
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            pixelSize = (w-matrixColumns) /matrixColumns;
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

            firebaseDataRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    drawPixel(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    drawPixel(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    String coords[] = (dataSnapshot.getKey()).split(":");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);

                    dv.clear(x * pixelSize, y * pixelSize);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }

        public void draw(int x, int y, String color) {
            mBitmapPaint.setColor(Color.parseColor(color));
            mCanvas.drawRect(x, y, x + pixelSize, y + pixelSize, mBitmapPaint);
            invalidate();
        }

        public void clear(int x, int y) {
            mBitmapPaint.setColor(Color.parseColor("#FFFFFF"));
            mCanvas.drawRect(x, y, x + pixelSize, y + pixelSize, mBitmapPaint);
            invalidate();
        }

        private void sendData(float x, float y) {
            int x0 = Math.round(x /pixelSize);
            int y0 = Math.round(y / pixelSize);
            String c = (color == "#ffffff")? null : color;
            firebaseDataRef.child(x0 + ":" + y0).setValue(c);

        }

        private void touchDown(float x, float y){
            /*
            if (y > (mBitmap.getHeight()-pixelSize-squareSize)) {
                if (y > (mBitmap.getHeight() - squareSize)){
                    color = colors[(int) Math.ceil((double)x / squareSize)-1];
                }
            } else {
                sendData(x,y);
            }
            */
            sendData(x,y);
        }

        private void touchMove(float x, float y){
            if (y < (mBitmap.getHeight()-pixelSize-squareSize)) {
                sendData(x,y);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchDown(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    break;
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            AuthData authData = firebaseDataRef.getRoot().getAuth();

            if (authData.getProvider().equals("twitter")) {
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
            }

            firebaseDataRef.getRoot().unauth();

            SharedPreferences prefs = getApplication().getSharedPreferences("LEDPrefs", 0);
            prefs.edit().clear().commit();

            FirebaseDrawingApplication app = (FirebaseDrawingApplication)getApplicationContext();
            app.setRoot();

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        } else if (id == R.id.action_color) {
            Intent i = new Intent(this, ColorPickerActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}

