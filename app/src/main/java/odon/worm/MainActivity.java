package odon.worm;

import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.graphics.*;
import java.util.*;
// Import these at the top of your Activity file:
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends Activity {
  GameView gv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(),
        getWindow().getDecorView());
    if (controller != null) {
      // This hides both the status bar and the navigation bar completely
      controller.hide(WindowInsetsCompat.Type.systemBars());
      // Allows the user to swipe from the edge to temporarily reveal bars
      controller.setSystemBarsBehavior(
          WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
    gv = new GameView(this);
    setContentView(gv);
  }

  @Override
  protected void onPause() {
    gv.paused = true; // replaces gv.tmpPause = true
    gv.stopLoop();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    gv.startLoop(); // does nothing until the surface exists
  }

  class GameView extends SurfaceView {

    Thread thread1;

    SurfaceHolder sh;
    Canvas c;
    Paint p1, p2;

    volatile boolean running;
    volatile boolean paused;
    volatile boolean waitingToStart;
    volatile boolean surfaceReady;

    boolean holding;
    boolean start;
    volatile boolean gameover, tmpPause;
    boolean fruitEaten;

    int screenX, screenY;

    float touchX, touchY;
    float dragX, dragY;
    float dX, dY;

    GameJoystick gj;

    Worm worm;

    Viewport vp;

    World mw;

    private Fruit fruit;
    private int fats;

    ArrayList<Fruit> fruitsOnMap;

    private Random random;
    private Random randFat;

    private long timer;

    GameView(Context context) {
      super(context);
      sh = getHolder();

      c = new Canvas();
      p1 = new Paint();
      p2 = new Paint();
      // get screen resolution
      Display display = getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      screenX = size.x;
      screenY = size.y;

      holding = false;
      start = false;
      gameover = false;
      tmpPause = false;
      fruitEaten = false;

      gj = new GameJoystick(screenX, screenY);

      worm = new Worm(screenX, screenY);
      vp = new Viewport(screenX, screenY);
      vp.setWorldCentre(worm.getHeadPoint().x, worm.getHeadPoint().y);

      mw = new World();
      fruit = new Fruit();

      fats = 0;

      fruitsOnMap = new ArrayList<Fruit>();
      timer = 0;
      random = new Random();
      randFat = new Random();

      // at the end of the GameView constructor:
      sh.addCallback(new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
          surfaceReady = true;
          startLoop();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
          surfaceReady = false;
          stopLoop();
        }
      });

      waitingToStart = true;
    }

    private void drawPlayState() {
      if (!sh.getSurface().isValid())
        return;
      c = sh.lockCanvas();
      if (c == null)
        return;
      try {
        // draw the background
        c.drawColor(Color.rgb(18, 20, 31));
        // draw the fruits
        p1.setStyle(Paint.Style.FILL);

        for (int j = 0; j < fruitsOnMap.size(); j++) {
          Fruit f = fruitsOnMap.get(j);
          p1.setColor(f.getColor()); // each fruit uses its own color
          c.drawRoundRect(
              vp.worldToScreen(f.getfruitRect().left, f.getfruitRect().top,
                  f.getfruitRect().width(), f.getfruitRect().height()),
              f.getfruitRect().width() / 2 * vp.pixelsPerMetreX,
              f.getfruitRect().height() / 2 * vp.pixelsPerMetreY, p1);
        }

        // set paint properties
        p1.setColor(Color.rgb(255, 77, 109));
        p1.setStyle(Paint.Style.STROKE);
        p1.setStrokeWidth(8);

        // draw World Rectangle/ borders
        c.drawRect(vp.worldToScreen(mw.getWorldRect().left, mw.getWorldRect().top,
            mw.getWorldRect().width(), mw.getWorldRect().height()), p1);

        // change paint properties
        p1.setColor(Color.argb(255, 120, 150, 255));
        p1.setStrokeWidth(2);
        p1.setStyle(Paint.Style.FILL);

        p1.setColor(Color.argb(255, 255, 140, 140));
        p1.setStyle(Paint.Style.FILL);
        // draw the body of the worm
        for (int i = worm.getBodyRects().size() - 1; i > -1; i--) {

          c.drawRoundRect(
              vp.worldToScreen(worm.getBodyRects().get(i).left, worm.getBodyRects().get(i).top,
                  worm.getBodyRects().get(i).width(), worm.getBodyRects().get(i).height()),
              worm.getBodyRects().get(i).width() / 2 * vp.pixelsPerMetreX,
              worm.getBodyRects().get(i).height() / 2 * vp.pixelsPerMetreY, p1);
          p2.setColor(Color.argb(255, 155, 40, 40));
          p2.setStyle(Paint.Style.STROKE);
          c.drawRoundRect(
              vp.worldToScreen(worm.getBodyRects().get(i).left, worm.getBodyRects().get(i).top,
                  worm.getBodyRects().get(i).width(), worm.getBodyRects().get(i).height()),
              worm.getBodyRects().get(i).width() / 2 * vp.pixelsPerMetreX,
              worm.getBodyRects().get(i).height() / 2 * vp.pixelsPerMetreY, p2);
        }

        // draw the head of the worm
        p2.setColor(Color.argb(255, 205, 90, 90));
        p2.setStyle(Paint.Style.FILL);
        c.drawRoundRect(
            vp.worldToScreen(worm.getHeadRect().left, worm.getHeadRect().top,
                worm.getHeadRect().width(), worm.getHeadRect().height()),
            worm.rectHalfSize * vp.pixelsPerMetreX, worm.rectHalfSize * vp.pixelsPerMetreX, p2);

        if (fruitEaten) {
          p1.setColor(Color.argb(255, 117, 255, 80));
          p1.setTextSize(23);
          p1.setFakeBoldText(true);
          c.drawText("+" + fats,
              (float) (vp.worldToScreenPoint(worm.getHeadPoint().x, worm.getHeadPoint().y).x
                  + (worm.rectHalfSize * 2 * vp.pixelsPerMetreX * Math.cos(gj.angle))),
              (float) (vp.worldToScreenPoint(worm.getHeadPoint().x, worm.getHeadPoint().y).y
                  + (worm.rectHalfSize * 2 * vp.pixelsPerMetreY * Math.sin(gj.angle))),
              p1);
          if (System.currentTimeMillis() - timer > 1000) {
            fruitEaten = false;
          }
        }

        // draw the joystick control when touching the screen
        if (holding) {

          p1.setColor(Color.YELLOW);

          p1.setStyle(Paint.Style.FILL);

          // draw the center of the joystick
          c.drawCircle(gj.getCenter().x, gj.getCenter().y, 15, p1);

          p1.setStyle(Paint.Style.STROKE);
          p1.setStrokeWidth(3);

          // draw the joystick circular border
          c.drawCircle(gj.getCenter().x, gj.getCenter().y, gj.radius, p1);

          p1.setStyle(Paint.Style.FILL);
          // draw the joystick tracker ball.
          c.drawCircle(gj.getTrackerBallPoint().x, gj.getTrackerBallPoint().y, 15, p1);
        }
        if (gameover) {
          p1.setColor(Color.WHITE);
          p1.setStyle(Paint.Style.FILL);
          p1.setTextSize(60);
          p1.setTextAlign(Paint.Align.CENTER);

          // Vertical centering: shift down by half of the text height
          float y = screenY / 2f - (p1.descent() + p1.ascent()) / 2f;
          c.drawText("GAMEOVER", screenX / 2f, y, p1);

          p1.setTextSize(32);
          c.drawText("Tap to restart", screenX / 2f, y + 40, p1);

          p1.setTextAlign(Paint.Align.LEFT); // reset so other text is not affected
        }

        if (waitingToStart) {
          p1.setColor(Color.WHITE);
          p1.setStyle(Paint.Style.FILL);
          p1.setTextSize(50);
          p1.setTextAlign(Paint.Align.CENTER);

          float y = screenY / 2f - (p1.descent() + p1.ascent()) / 2f;
          c.drawText("Tap to Start", screenX / 2f, y, p1);

          p1.setTextAlign(Paint.Align.LEFT);
        }

        if (paused && !gameover && !waitingToStart) {

          p1.setColor(Color.WHITE);
          p1.setStyle(Paint.Style.FILL);
          p1.setTextSize(60);
          p1.setTextAlign(Paint.Align.CENTER);

          float y = screenY / 2f - (p1.descent() + p1.ascent()) / 2f;
          c.drawText("PAUSED", screenX / 2f, y, p1);

          p1.setTextSize(32);
          c.drawText("Tap to continue", screenX / 2f, y + 40, p1);

          p1.setTextAlign(Paint.Align.LEFT);
        }

      } finally {
        sh.unlockCanvasAndPost(c);
      }
    }

    // game update method
    private void updatePlaystate() {
      // update the joystick control with touch inputs when
      double target = worm.getHeading(); // default: keep going straight

      if (holding) {
        dX = dragX - touchX;
        dY = dragY - touchY;
        gj.update(dX, dY, touchX, touchY);

        // steer only after the finger moved at least 10 pixels from the touch point
        if (dX * dX + dY * dY > 100) {
          target = gj.getAngle();
        }
      }

      worm.updateWorm(target);

      // sets the coordinates of the worm head as the focus of the viewport camera
      vp.setWorldCentre(worm.getHeadPoint().x, worm.getHeadPoint().y);

      if (gameover) {
        tmpPause = true;
        // worm.reset();

      }

      if (worm.getHeadRect().left < mw.getWorldRect().left
          || worm.getHeadRect().right > mw.getWorldRect().right) {
        gameover = true;

      }
      if (worm.getHeadRect().top < mw.getWorldRect().top
          || worm.getHeadRect().bottom > mw.getWorldRect().bottom) {
        gameover = true;

      }

      create100Fruit();

      for (int i = fruitsOnMap.size() - 1; i >= 0; i--) {
        if (RectF.intersects(fruitsOnMap.get(i).getfruitRect(), worm.getHeadRect())) {
          fruitsOnMap.remove(i);
          fats = randFat.nextInt(6);
          fruitEaten = true;
          timer = System.currentTimeMillis();
          worm.grow(1);
        }
      }

    }

    private void create100Fruit() {
      if (fruitsOnMap.size() < 200) {
        fruit = new Fruit();
        fruit.setFruit(
            new PointF(random.nextInt(mw.width - 10) + 5, random.nextInt(mw.height - 10) + 5));
        fruitsOnMap.add(fruit);
      }

    }

    // getting touch inputs
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
      switch (ev.getAction() & MotionEvent.ACTION_MASK) {

        case MotionEvent.ACTION_DOWN:
          holding = true;
          paused = false;
          waitingToStart = false;

          touchX = ev.getX();
          touchY = ev.getY();
          dragX = touchX; // avoids a jump from the old drag position
          dragY = touchY;

          gj.setCenter(touchX, touchY);

          if (gameover) { // restart on the next fresh touch
            gameover = false;
            tmpPause = false;
            worm.reset();
          }
          break;

        case MotionEvent.ACTION_MOVE:
          holding = true;
          dragX = ev.getX();
          dragY = ev.getY();
          break;

        case MotionEvent.ACTION_UP:
          holding = false; // nothing else needed here
          break;
      }
      return true;
    }

    public synchronized void startLoop() {
      if (running || !surfaceReady)
        return;
      running = true;
      thread1 = new Thread() {
        public void run() {
          while (running) {
            long start = System.currentTimeMillis();
            if (!tmpPause && !paused && !waitingToStart)
              updatePlaystate();

            drawPlayState(); // always draw, even when paused
            long wait = 16 - (System.currentTimeMillis() - start);
            if (wait > 0) {
              try {
                Thread.sleep(wait);
              } catch (InterruptedException e) {
              }
            }
          }
        }
      };
      thread1.start();
    }

    public synchronized void stopLoop() {
      running = false;
      if (thread1 != null) {
        try {
          thread1.join();
        } catch (InterruptedException e) {
        }
        thread1 = null;
      }
    }

  }

}
