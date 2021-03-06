package com.example.gamewithnoname.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gamewithnoname.R;
import com.example.gamewithnoname.ServerConnection.ConnectionServer;
import com.example.gamewithnoname.callbacks.CheckGWBCallbacks;
import com.example.gamewithnoname.callbacks.CheckGameCallbacks;
import com.example.gamewithnoname.callbacks.SignUpCallbacks;
import com.example.gamewithnoname.dialogs.DialogSecondMode;
import com.example.gamewithnoname.maps.MapInGame;
import com.example.gamewithnoname.models.responses.UserResponse;
import com.example.gamewithnoname.utils.UserLocation;
import com.example.gamewithnoname.callbacks.SignInCallbacks;
import com.example.gamewithnoname.models.User;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;

import static com.example.gamewithnoname.utils.Constants.PLAY_GAME;
import static com.example.gamewithnoname.utils.Constants.WAIT_GAME;

public class MainActivity extends AppCompatActivity {

    private final String TAG = String.format("%s/%s",
            "HITS", "MainActivity");
    private static final int REQUEST_LOCATION = 123;

    private static SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey("4431f62e-4cef-4ce6-b1d5-07602abde3fd"); // todo: remove pls
        setContentView(R.layout.activity_main);
    }

    private void beginLogin(final String username, final String password) {

        ConnectionServer.getInstance().initSignIn(username, password);

        ConnectionServer.getInstance().connectLogin(new SignInCallbacks() {

            @Override
            public void baseSettingsAccount(String name, String password) {
                ((TextView) findViewById(R.id.textUsername)).setText(name);
            }

            @Override
            public void capital(Integer money, Integer rating) {
                ((TextView) findViewById(R.id.textCoins)).setText(money.toString());
                ((TextView) findViewById(R.id.textRating)).setText(rating.toString());
            }

            @Override
            public void statsData(Integer mileage) {
                ((TextView) findViewById(R.id.textAllKm)).setText(String.format(getResources().getString(R.string.activity_main_coins), mileage));
            }

            @Override
            public void otherSettingsAccount(
                    Integer sex,
                    String birthday,
                    String dateSignUp,
                    Boolean showHints,
                    String token
            ) {
                User.setHints(showHints);
            }

            @Override
            public void success(UserResponse userResponse) {
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                permissionsChecker(false);
                new User(userResponse);
            }

            @Override
            public void permissionDenied() {
                permissionsChecker(true);
            }

            @Override
            public void someProblem(@Nullable Throwable t) {
                if (t != null)
                    Log.i(TAG, t.getMessage());
                permissionsChecker(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        if (saveLogin) {
            Log.i(TAG, loginPreferences.getString("username", ""));
            Log.i(TAG, loginPreferences.getString("password", ""));
            beginLogin(
                    loginPreferences.getString("username", ""),
                    loginPreferences.getString("password", "")
            );
        } else {
            permissionsChecker(true);
        }

    }

    public void permissionsChecker(boolean login) {
        ArrayList<String> allPermissions = new ArrayList<>();

        allPermissions.add(Manifest.permission.INTERNET);
        allPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        allPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        allPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

        for (int i = 0; i < allPermissions.size(); i++) {
            int res = ContextCompat.checkSelfPermission
                    (this, allPermissions.get(i));
            if (res != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{allPermissions.get(i)},
                        REQUEST_LOCATION);
            }
        }

        if (login)
            loginUser();
        UserLocation.SetUpLocationListener(this);

    }

    private void loginUser() {
        Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intentLogin, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String result = data.getStringExtra("resultLogin");
        if (result == null || !result.equals("1")) {
            // Человек так и не залогинился!!
            Toast.makeText(this,
                    R.string.login_activity_you_need_reg,
                    Toast.LENGTH_LONG).show();
            loginUser(); // пока не залогинишься так и будешь
        }
    }

    public void processButtonPressing(View view) {
        ConstraintLayout me = findViewById(R.id.include_me);
        switch (view.getId()) {
            case R.id.buttonCatchBot: {
                CheckGWBCallbacks callback = new CheckGWBCallbacks() {
                    @Override
                    public void isFree() {
                        Intent intentStart = new Intent(MainActivity.this, ParametersActivity.class);
                        startActivity(intentStart);
                    }

                    @Override
                    public void gameExist(int alpha, Double speed, int time, int stops, Double bla, Double blo, Double ela, Double elo) {

                        Intent intentStart = new Intent(MainActivity.this, MapInGame.class);

                        intentStart.putExtra("botStartLatitude", bla);
                        intentStart.putExtra("botStartLongitude", blo);
                        intentStart.putExtra("finishLatitude", ela);
                        intentStart.putExtra("finishLongitude", elo);
                        intentStart.putExtra("speed", speed);
                        intentStart.putExtra("alpha", alpha);
                        intentStart.putExtra("time", time);
                        intentStart.putExtra("stops", stops);

                        startActivity(intentStart);

                    }
                };

                ConnectionServer.getInstance().initCheckGWB(User.getName());
                ConnectionServer.getInstance().connectCheckGWB(callback);
                break;
            }
            case R.id.buttonStatistics: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                Intent intentStat = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intentStat);
                break;
            }
            case R.id.buttonFriends: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                Intent intentFriends = new Intent(MainActivity.this, FriendsActivity.class);
                startActivity(intentFriends);
                break;
            }
            case R.id.buttonInfo: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                Intent intentInfo = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intentInfo);
                break;
            }
            case R.id.buttonAutho: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
                Intent authoIntent = new Intent(this, LoginActivity.class);
                startActivity(authoIntent);
                break;
            }
            case R.id.btn_me: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                else {
                    findViewById(R.id.include_me).setVisibility(View.VISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.INVISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_selected));
                }
                break;
            }
            case R.id.buttonWithFriends: {
                if (me.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.include_me).setVisibility(View.INVISIBLE);
                    findViewById(R.id.buttonCatchBot).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    (findViewById(R.id.btn_me)).setBackground(ContextCompat.getDrawable(this, R.drawable.sea_button_unselected));
                }
                ConnectionServer.getInstance().initCheckGame(
                        User.getToken()
                );
                ConnectionServer.getInstance().connectCheckGame(new CheckGameCallbacks() {
                    @Override
                    public void inRun(String link, Integer type) {
                        Intent fmIntent = new Intent(MainActivity.this, FriendsModeActivity.class);
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.you_have_unfinished_game), // И она уже идет
                                Toast.LENGTH_LONG).show();
                        fmIntent.putExtra("type", type);
                        fmIntent.putExtra("stage", PLAY_GAME);
                        startActivity(fmIntent);
                    }

                    @Override
                    public void inWait(String link, Integer type) {
                        Intent fmIntent = new Intent(MainActivity.this, FriendsModeActivity.class);
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.you_have_unfinished_game), // Но её еще не запустили
                                Toast.LENGTH_LONG).show();
                        fmIntent.putExtra("type", type);
                        fmIntent.putExtra("stage", WAIT_GAME);
                        startActivity(fmIntent);
                    }

                    @Override
                    public void inFree() {
                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setOnShowListener(new DialogSecondMode());
                        dialog.show();
                    }
                });
                break;
            }
            default:
                break;
        }
    }
}
