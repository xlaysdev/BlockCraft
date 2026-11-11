package dev.xlays.blockcraft;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GameSurface game;
    private float downX, downY;
    private boolean moving;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512 | 4096);
        FrameLayout root = new FrameLayout(this);
        game = new GameSurface(this);
        root.addView(game);
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        controls.setOrientation(LinearLayout.VERTICAL);
        String[][] dirs = {{" ","UP"," "},{"LEFT","DOWN","RIGHT"}};
        for (String[] line : dirs) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            for (String d : line) {
                Button b = new Button(this);
                b.setText(d);
                b.setAlpha(.72f);
                row.addView(b, new LinearLayout.LayoutParams(58,52));
                if (!d.trim().isEmpty()) b.setOnTouchListener((v,e)->{
                    game.setMove(d, e.getAction()!=MotionEvent.ACTION_UP && e.getAction()!=MotionEvent.ACTION_CANCEL);
                    return true;
                });
            }
            controls.addView(row);
        }
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.LEFT);
        cp.setMargins(12,0,0,12);
        root.addView(controls,cp);

        LinearLayout actions = new LinearLayout(this);
        Button breakB = new Button(this); breakB.setText("BREAK");
        Button placeB = new Button(this); placeB.setText("PLACE");
        Button jumpB = new Button(this); jumpB.setText("JUMP");
        actions.addView(breakB); actions.addView(placeB); actions.addView(jumpB);
        FrameLayout.LayoutParams ap = new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.RIGHT);
        ap.setMargins(0,0,12,16); root.addView(actions,ap);
        breakB.setOnClickListener(v->game.modify(false));
        placeB.setOnClickListener(v->game.modify(true));
        jumpB.setOnClickListener(v->game.jump());
        TextView hint = new TextView(this);
        hint.setText("BLOCKCRAFT  •  Drag right side to look");
        hint.setTextColor(0xffffffff); hint.setTextSize(13); hint.setPadding(14,8,14,8);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        root.addView(hint,hp);
        game.setOnTouchListener((v,e)->{
            if(e.getX()>getResources().getDisplayMetrics().widthPixels*.38f){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();moving=true;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE && moving){game.look((e.getX()-downX)*.22f,(e.getY()-downY)*.18f);downX=e.getX();downY=e.getY();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP){moving=false;return true;}
            }
            return false;
        });
        setContentView(root);
    }
    @Override protected void onResume(){super.onResume();if(game!=null)game.onResume();}
    @Override protected void onPause(){if(game!=null)game.onPause();super.onPause();}
}
