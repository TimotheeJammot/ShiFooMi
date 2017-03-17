package com.m2dl.shifoomi;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.m2dl.shifoomi.actions.PlayTurn;
import com.m2dl.shifoomi.database.game.GameMoveType;
import com.m2dl.shifoomi.database.room.Room;
import com.m2dl.shifoomi.service.game.GameListener;
import com.m2dl.shifoomi.service.game.GameScore;
import com.m2dl.shifoomi.service.game.GameService;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements GameListener {

    private ImageView imageViewRock;
    private ImageView imageViewPaper;
    private ImageView imageViewScissors;
    private ImageView imageViewOpponentHand;
    private ImageView imageViewAnnouncement;

    private TextView textViewPlayerScore;
    private TextView textViewOpponentScore;

    private String userId;
    private String gameId;

    private boolean playReady;
    private int turn;
    private GameMoveType playerGameMove;
    private GameMoveType opponentGameMove;
    private int timerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        gameId = intent.getStringExtra("gameId");
        userId = intent.getStringExtra("userId");

        imageViewRock = (ImageView) findViewById(R.id.imageViewRock);
        imageViewPaper = (ImageView) findViewById(R.id.imageViewPaper);
        imageViewScissors = (ImageView) findViewById(R.id.imageViewScissors);
        imageViewOpponentHand = (ImageView) findViewById(R.id.imageViewOpponentHand);
        imageViewAnnouncement = (ImageView) findViewById(R.id.imageViewAnnouncement);

        textViewPlayerScore = (TextView) findViewById(R.id.textViewPlayerScore);
        textViewOpponentScore = (TextView) findViewById(R.id.textViewOpponentScore);

        playReady = false;
        imageViewRock.setClickable(true);
        imageViewPaper.setClickable(true);
        imageViewScissors.setClickable(true);
        imageViewRock.setClickable(true);
        imageViewRock.setEnabled(false);
        imageViewPaper.setEnabled(false);
        imageViewScissors.setEnabled(false);

        imageViewRock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewRock.setSelected(true);
                imageViewRock.setEnabled(false);
                imageViewPaper.setEnabled(false);
                imageViewScissors.setEnabled(false);
                imageViewPaper.setVisibility(View.INVISIBLE);
                imageViewScissors.setVisibility(View.INVISIBLE);

                playerGameMove = GameMoveType.ROCK;
            }
        });

        imageViewPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewPaper.setSelected(true);
                imageViewRock.setEnabled(false);
                imageViewPaper.setEnabled(false);
                imageViewScissors.setEnabled(false);
                imageViewRock.setVisibility(View.INVISIBLE);
                imageViewScissors.setVisibility(View.INVISIBLE);

                playerGameMove = GameMoveType.PAPER;
            }
        });

        imageViewScissors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewScissors.setSelected(true);
                imageViewRock.setEnabled(false);
                imageViewPaper.setEnabled(false);
                imageViewScissors.setEnabled(false);
                imageViewRock.setVisibility(View.INVISIBLE);
                imageViewPaper.setVisibility(View.INVISIBLE);

                playerGameMove = GameMoveType.SCISSORS;
            }
        });
        GameService.getInstance().addListener(userId, this);
    }

    @Override
    public void opponentPlayed(GameMoveType gameMoveType) {

        opponentGameMove = gameMoveType;
    }

    @Override
    public void scoreUpdated(GameScore score) {
        int playerScore = score.getPlayerScore();
        int opponentScore = score.getOpponentScore();

        textViewPlayerScore.setText(Integer.toString(playerScore));
        textViewOpponentScore.setText(Integer.toString(opponentScore));
    }

    @Override
    public void roundStart() {

        playReady = true;
        imageViewRock.setEnabled(true);
        imageViewPaper.setEnabled(true);
        imageViewScissors.setEnabled(true);
        imageViewRock.setVisibility(View.VISIBLE);
        imageViewPaper.setVisibility(View.VISIBLE);
        imageViewScissors.setVisibility(View.VISIBLE);
        imageViewRock.setSelected(false);
        imageViewPaper.setSelected(false);
        imageViewScissors.setSelected(false);

        playerGameMove = GameMoveType.LOOSE;
        opponentGameMove = GameMoveType.LOOSE;

        launchAnnouncementTimer();
    }

    public int getOpponentImageId() {

        int imageId = 0;
        switch (opponentGameMove) {
            case ROCK:
                imageId = getResources().getIdentifier("hand_opponent_rock", "drawable", getPackageName());
                break;
            case PAPER:
                imageId = getResources().getIdentifier("hand_opponent_paper", "drawable", getPackageName());
                break;
            case SCISSORS:
                imageId = getResources().getIdentifier("hand_opponent_scissors", "drawable", getPackageName());
                break;
            case LOOSE:
                imageId = getResources().getIdentifier("no_hand", "drawable", getPackageName());
        }

        return imageId;
    }

    public int getRoundResultImageId() {
        int imageId = 0;
        if (playerGameMove == GameMoveType.ROCK && opponentGameMove == GameMoveType.SCISSORS
                || playerGameMove == GameMoveType.PAPER && opponentGameMove == GameMoveType.ROCK
                || playerGameMove == GameMoveType.SCISSORS && opponentGameMove == GameMoveType.PAPER) {
            imageId = getResources().getIdentifier("thumbsup_image", "drawable", getPackageName());
        }
        else if (playerGameMove == GameMoveType.ROCK && opponentGameMove == GameMoveType.PAPER
                || playerGameMove == GameMoveType.PAPER && opponentGameMove == GameMoveType.SCISSORS
                || playerGameMove == GameMoveType.SCISSORS && opponentGameMove == GameMoveType.ROCK) {
            imageId = getResources().getIdentifier("thumbsdown_image", "drawable", getPackageName());
        }
        else if (playerGameMove == GameMoveType.ROCK && opponentGameMove == GameMoveType.ROCK
                || playerGameMove == GameMoveType.PAPER && opponentGameMove == GameMoveType.PAPER
                || playerGameMove == GameMoveType.SCISSORS && opponentGameMove == GameMoveType.SCISSORS) {
            imageId = getResources().getIdentifier("thumbsmiddle_image", "drawable", getPackageName());
        }
        else {
            imageId = getResources().getIdentifier("empty", "drawable", getPackageName());
        }

        return imageId;
    }

    public void launchAnnouncementTimer() {
        timerCount = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                imageViewAnnouncement.post(new Runnable() {
                    @Override
                    public void run() {
                        int announcementImageId = -1;
                        switch (timerCount) {
                            case 0:
                                announcementImageId = getResources().getIdentifier("empty", "drawable", getPackageName());
                                break;
                            case 1:
                                announcementImageId = getResources().getIdentifier("countdown_3", "drawable", getPackageName());
                                break;
                            case 2:
                                announcementImageId = getResources().getIdentifier("countdown_2", "drawable", getPackageName());
                                break;
                            case 3:
                                announcementImageId = getResources().getIdentifier("countdown_1", "drawable", getPackageName());
                                break;
                            case 4:
                                imageViewOpponentHand.setImageResource(getOpponentImageId());
                                announcementImageId = getRoundResultImageId();
                                new PlayTurn(gameId, userId, playerGameMove, turn++);
                                break;
                            default:
                        }
                        timerCount++;

                        if (announcementImageId != -1) {
                            imageViewAnnouncement.setImageResource(announcementImageId);
                        }
                    }
                });
            }
        }, 1000);
    }
}
