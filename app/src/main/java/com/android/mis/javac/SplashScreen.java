package com.android.mis.javac;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.mis.R;
import com.android.mis.javac.Home.HomeActivity;
import com.android.mis.javac.Login.LoginActivity;
import com.android.mis.utils.Callback;
import com.android.mis.utils.NetworkRequest;
import com.android.mis.utils.SessionManagement;
import com.android.mis.utils.Urls;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends AppCompatActivity implements Callback {

    Thread mSplashThread;
    Animation fadeIn;
    final SplashScreen sPlashScreen = this;
    private ProgressBar progressBar;
    private HashMap<String,String> hmap;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        logo = (ImageView)findViewById(R.id.logo);
        progressBar = (ProgressBar)findViewById(R.id.preloader);
        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        progressBar.setVisibility(View.GONE);

        SessionManagement session = new SessionManagement(getApplicationContext());
        if(session.isLoggedIn()){
            hmap = session.getTokenDetails();
            fetchMenuAndAddToCache();
        }
        else{
            waitAndMove(LoginActivity.class);
        }

    }

    private void fetchMenuAndAddToCache(){
        NetworkRequest nr = new NetworkRequest(SplashScreen.this,progressBar,progressBar,this,"get","http", Urls.menu_url,hmap,true,false,0);
        nr.setSnackbar_message(Urls.error_connection_message);
        nr.initiateRequest();
    }

    private void waitAndMove(final Class whereToMove){
        mSplashThread = new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        // Wait given period of time or exit on touch
                        logo.startAnimation(fadeIn);
                        wait(1000);
                    }
                }
                catch(InterruptedException ex){
                }

                finish();

                Intent intent = new Intent();
                intent.setClass(sPlashScreen,whereToMove);
                startActivity(intent);
                finish();
            }
        };

        mSplashThread.start();
    }

    @Override
    public void performAction(String result, int tag) {
           switch (tag){
               case 0:
                   try{
                       Log.d("result",result);
                       JSONObject json = new JSONObject(result);
                       Boolean success = json.getBoolean("success");
                       if(success){
                           waitAndMove(HomeActivity.class);
                       }
                       else{
                           finish();
                           System.exit(0);
                       }
                   }catch (Exception e){
                       Log.e("Exception",e.toString());
                   }
                   break;
           }
    }
}
