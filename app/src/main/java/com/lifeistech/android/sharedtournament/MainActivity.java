package com.lifeistech.android.sharedtournament;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {
    int joinNum = 8;


    FirebaseDatabase database = FirebaseDatabase.getInstance();

    static LinearLayout layout;
    public static TextView auth;
    static final int requestCodePassword = 0;


    ImageView[] imageR1, imageR2, imageR3;
    ImageView firstWinner, authImage;
    GridLayout gridLayout;
    TextView[] textView;

    String[] players;
    String[] r1Winner, r2Winner;
    List<String> winp;


    int[] upSide1 = {0, 0}, bottomSide1 = {0, 0};
    int[] upSide2 = {0}, bottomSide2 = {0};
    int done1R = -1;
    int done2R = -1;
    int done3R = -1;
    public static int logOnOff = 0;

    int n = 0;

    String str, gName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = (TextView) findViewById(R.id.auth);


        layout = (LinearLayout) findViewById(R.id.linearLayout);
        Intent intent = getIntent();
        str = intent.getStringExtra("createdId");
        gName = intent.getStringExtra("gameName");
        Log.d("newCreatedID", str);
        Log.d("gameName", gName);

        //一人１トーナメント各自で作れる．あとで新しくつくるとき消すように保存．


        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(str);
        actionBar.setIcon(R.drawable.read_only);
        actionBar.setTitle(gName);


        //Set2,Loginで生成した配列を取得
        players = intent.getStringArrayExtra("players");
        Log.d("playersClass", players.getClass().toString());

        for (int i = 0; i < players.length; i++) {
            Log.d("player[]", players[i]);
        }

        DatabaseReference ref = database.getReference(str);


        gridLayout = (GridLayout) findViewById(R.id.gridLayout);

        textView = new TextView[8];
        for (int i = 0; i < textView.length; i++) {
            textView[i] = (TextView) findViewById(getResources().getIdentifier("name" + (i + 1), "id", getPackageName()));
        }

        //BYEは灰色にする
        for (int i = 0; i < textView.length; i++) {
            if (players[i].indexOf("BYE") != -1) {
                textView[i].setTextColor(Color.parseColor("#D3D3D3"));
                Log.d("matches", "bye");
            } else {
                textView[i].setTextColor(Color.parseColor("#000000"));
            }
            textView[i].setText(players[i]);
        }

        imageR1 = new ImageView[4];
        for (int i = 0; i < imageR1.length; i++) {
            imageR1[i] = (ImageView) findViewById(getResources().getIdentifier("r1p" + (1 + i), "id", getPackageName()));
        }

        imageR2 = new ImageView[2];
        imageR2[0] = (ImageView) findViewById(R.id.r2w1);
        imageR2[1] = (ImageView) findViewById(R.id.r2w2);

        imageR3 = new ImageView[1];
        imageR3[0] = (ImageView) findViewById(R.id.r3w1);
        firstWinner = (ImageView) findViewById(R.id.firstWinner);


        r1Winner = new String[4];
        r2Winner = new String[2];



        try {
            //結果を読み込み
            ref.child("Round").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    renewalResult(dataSnapshot, 1);
                    renewalResult(dataSnapshot, 2);
                    renewalResult(dataSnapshot, 3);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            ref.child("Status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (int i = 0; i < players.length; i++) {
                        if (players[i].equals(dataSnapshot.child("player" + i + "/name").getValue().toString())) {

                            switch (Integer.parseInt(dataSnapshot.child("player" + i + "/winPoint").getValue().toString())) {
                                case 1:
                                    textView[i].setBackgroundColor(Color.parseColor("#F9DAD2"));
                                    break;
                                case 2:
                                    textView[i].setBackgroundColor(Color.parseColor("#F98262"));
                                    break;
                                case 3:
                                    textView[i].setBackgroundColor(Color.parseColor("#FC3B00"));
                                    break;
                                default:
                                    textView[i].setBackgroundColor(Color.parseColor("#FFF5EE"));
                                    break;
                            }
                        }
                    }
                    winp = new ArrayList<String>();
                    for (int i = 0; i < players.length; i++) {
                        int n = Integer.parseInt(dataSnapshot.child("player" + i + "/winPoint").getValue().toString());
                        winp.add("ベスト " + (int) (players.length / (Math.pow(2, n))) + "       " + players[i]);
                    }
                    Collections.sort(winp);
                    System.out.println(winp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } catch (Exception e) {
            Log.d("Status/", "playerはカラ");
        }
    }

    public void renewalResult(DataSnapshot dataSnapshot, int round) {
        String r = "Round" + round + ":";
        ImageView[] imageR = new ImageView[joinNum / (int) Math.pow(2, round)];
        ImageView[] nextImage = new ImageView[imageR.length / 2];
        String[] winner = new String[imageR.length];
        int topWon = 0, bottomWon = 0;
        int topDone = 0, bottomDone = 0, bothDone = 0, done = -1;
        int[] upSide = new int[imageR.length / 2], bottomSide = new int[imageR.length / 2];

        //round回戦
        if (round == 1) {
            imageR = imageR1;
            nextImage = imageR2;
            winner = r1Winner;
            upSide = upSide1;
            bottomSide = bottomSide1;

            topWon = R.drawable.one_topup;
            bottomWon = R.drawable.one_bottomdown;

            topDone = R.drawable.two_top_done;
            bottomDone = R.drawable.two_bottom_done;
            bothDone = R.drawable.two_both_done;
            done = done1R;
        } else if (round == 2) {
            imageR = imageR2;
            nextImage = imageR3;
            winner = r2Winner;
            upSide = upSide2;
            bottomSide = bottomSide2;

            topWon = R.drawable.two_topup;
            bottomWon = R.drawable.two_bottomdown;

            topDone = R.drawable.three_top_done;
            bottomDone = R.drawable.three_bottom_done;
            bothDone = R.drawable.three_both_done;
            done = done2R;
        } else if (round == 3) {
            imageR = imageR3;
            topWon = R.drawable.three_topup;
            bottomWon = R.drawable.three_bottomdown;
            done = done3R;
        }

        for (int i = 0; i < imageR.length; i++) {
            //round回戦の結果取得
            String w = String.valueOf(dataSnapshot.child(r + i + "/winner").getValue());
            String l = (String) dataSnapshot.child(r + i + "/loser").getValue();
            int upP = parseInt(dataSnapshot.child(r + i + "/up").getValue().toString());
            int downP = parseInt(dataSnapshot.child(r + i + "/down").getValue().toString());

            //勝者を格納
            if (upP > downP) {
                imageR[i].setBackgroundResource(topWon);
                winner[i] = w;
                done = i;
            } else if (upP < downP) {
                imageR[i].setBackgroundResource(bottomWon);
                winner[i] = w;
                done = i;
            }

            //nextImageRの画像変更, round回戦完了
            if (done == i) {
                if (nextImage.length != 0) {

                    if (i % 2 == 0) {
                        nextImage[i / 2].setBackgroundResource(topDone);
                        upSide[i / 2] = 1;
                    } else {
                        nextImage[i / 2].setBackgroundResource(bottomDone);
                        bottomSide[i / 2] = 1;
                    }
                    if (bottomSide[i / 2] == 1 && upSide[i / 2] == 1) {
                        nextImage[i / 2].setBackgroundResource(bothDone);
                    }
                } else {
                    firstWinner.setImageResource(R.drawable.red_line);
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            //WriteLogIn
            case R.id.logIn:
                if (logOnOff == 0) {
                    DialogFragment dialog = new WritableLogin();
                    Bundle args = new Bundle();
                    args.putString("gameId", str);
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "writable");
                } else if (logOnOff == 1) {
                    DialogFragment dialog2 = new UnableWriteFragment();
                    dialog2.show(getFragmentManager(), "logOff");
                }


                auth.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        MenuItem menuItem = item;

                        if (auth.getText().toString().equals("編集可能")) {
                            menuItem.setIcon(R.drawable.writeread);
                            Toast.makeText(getApplicationContext(), "編集可能になりました", Toast.LENGTH_SHORT).show();

                        } else {
                            menuItem.setIcon(R.drawable.read_only);
                            Toast.makeText(getApplicationContext(),"ログアウトしました",Toast.LENGTH_SHORT).show();

                        }

                    }
                });

                break;
            //ResultNow
            case R.id.result:

                DialogFragment dialogFragment = new ResultNowFragment();
                Bundle argument = new Bundle();
                argument.putString("userId", str);
                argument.putStringArray("players", winp.toArray(new String[0]));
                dialogFragment.setArguments(argument);
                dialogFragment.show(getFragmentManager(), "result");

                break;
            case R.id.dispose:
                finish();
        }


        return true;
    }


    public void round(View v, int round) {
        ImageView[] imageR = new ImageView[joinNum / (int) Math.pow(2, round)];
        String[] previousWinner = new String[imageR.length * 2];
        if (round == 1) {
            imageR = imageR1;
            previousWinner = players;
        } else if (round == 2) {
            imageR = imageR2;
            previousWinner = r1Winner;
        } else if (round == 3) {
            imageR = imageR3;
            previousWinner = r2Winner;
        }
        DialogFragment dialog = new RoundDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", str);
        args.putString("editable", auth.getText().toString());

        for (int i = 0; i < imageR.length; i++) {
            if (v == imageR[i]) {
                System.out.println("Winner=" + previousWinner[2 * i]);
                if (previousWinner[2 * i] != null && previousWinner[2 * i + 1] != null) {
                    args.putInt("id", i);
                    //上から何番目か
                    for (int j = 0; j < players.length; j++) {
                        if (previousWinner[2 * i].equals(players[j])) {
                            args.putInt(round + "rUpImage", j);
                        }
                        if (previousWinner[2 * i + 1].equals(players[j])) {
                            args.putInt(round + "rDownImage", j);
                        }
                    }
                    args.putString((round - 1) + "rUpWinner", previousWinner[2 * i]);
                    args.putString((round - 1) + "rDownWinner", previousWinner[2 * i + 1]);
                    dialog.setArguments(args);
                    dialog.show(getFragmentManager(), "round");

                } else {
                    Toast.makeText(getApplicationContext(), (round - 1) + "回戦の勝敗を決めて下さい", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }

    public void round1(View v) {
        round(v, 1);
    }

    public void round2(View v) {
        round(v, 2);
    }

    public void round3(View v) {
        round(v, 3);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 戻るボタンの処理
            // 編集しているときにメモボタンを押したときは警告をする
            Snackbar.make(layout, "終了しますか？（データは保存されています）", Snackbar.LENGTH_LONG).setAction("YES", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).show();

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
