package com.example.gamewithnoname.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.gamewithnoname.R;
import com.example.gamewithnoname.ServerConnection.ConnectionServer;
import com.example.gamewithnoname.activities.FriendsModeActivity;
import com.example.gamewithnoname.callbacks.CreateGameCallbacks;
import com.example.gamewithnoname.callbacks.JoinGameCallbacks;
import com.example.gamewithnoname.callbacks.SimpleCallbacks;
import com.example.gamewithnoname.models.User;
import com.example.gamewithnoname.utils.UserLocation;

import static com.example.gamewithnoname.utils.Constants.CREATOR;
import static com.example.gamewithnoname.utils.Constants.JOINER;
import static com.example.gamewithnoname.utils.Constants.WAIT_GAME;
import static java.lang.Integer.parseInt;

public class DialogSecondMode implements Dialog.OnShowListener {

    private final static String TAG = "HITS/DialogSecondMode";
    private Dialog dialog;
    private int time;
    // todo: there is strings for transfer to string.xml

    @Override
    public void onShow(DialogInterface dialogInterface) {

        Log.i(TAG, "onShow");
        dialog = (Dialog) dialogInterface;
        dialog.setContentView(R.layout.alert_second_mode);

        configBtnCreateGame();
        configBtnJoinGame();

    }

    private void configBtnJoinGame() {
        Button btn = dialog.findViewById(R.id.btn_join);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(dialog.getContext(),
                        FriendsModeActivity.class);
                JoinGameCallbacks Callback = new JoinGameCallbacks() {

                    @Override
                    public void invalidLink() {
                        Toast.makeText(dialog.getContext(),
                                "Ссылка некорректна / такой игры нет",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void gameIsStarted() {
                        Toast.makeText(dialog.getContext(),
                                "Игра уже началась",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success(Integer key) {
                        intent.putExtra("stage", WAIT_GAME);
                        intent.putExtra("type", JOINER);
                        dialog.getContext().startActivity(intent);
                    }

                    @Override
                    public void someProblem(Throwable t) {
                        Toast.makeText(dialog.getContext(),
                                "Возникли технические неполадки",
                                Toast.LENGTH_SHORT).show();
                        Log.i(TAG, t.getMessage());
                    }
                };
                EditText editText = dialog.findViewById(R.id.editText3);
                String link = editText.getText().toString();
                ConnectionServer.getInstance().initJoinGame(
                        User.getName(),
                        link
                );
                ConnectionServer.getInstance().connectJoinGame(Callback);
            }
        });
    }

    private void configBtnCreateGame() {
        Button btn = dialog.findViewById(R.id.btn_create);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    EditText editText = dialog.findViewById(R.id.text_alert_2m);
                    String s = editText.getText().toString();
                    time = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    Toast.makeText(dialog.getContext(),
                            "Input valid time!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                final Intent intent = new Intent(dialog.getContext(),
                        FriendsModeActivity.class);
                CreateGameCallbacks callback = new CreateGameCallbacks() {
                    @Override
                    public void aLotOfGames() {
                        // такая проблема может возникнуть
                        // если игр сколько возможных комбинаций
                        // ссылок приглашений
                        Toast.makeText(dialog.getContext(),
                                "Игра не создалась",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success(int key) {
                        intent.putExtra("stage", WAIT_GAME);
                        intent.putExtra("type", CREATOR);
                        dialog.getContext().startActivity(intent);
                        dialog.cancel();
                    }

                    @Override
                    public void someProblem(Throwable t) {
                        Log.i(TAG, t.getMessage());
                    }
                };

                RadioButton radioButton = dialog.findViewById(R.id.radio_button_in_team);
                int type = (radioButton.isChecked() ? 1 : 2);

                ConnectionServer.getInstance().initCreateGame(
                        User.getName(),
                        time,
                        type
                );
                ConnectionServer.getInstance().connectCreateGame(callback);

            }
        });

    }
}