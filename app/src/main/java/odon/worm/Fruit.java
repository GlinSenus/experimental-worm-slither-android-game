package odon.worm;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import java.util.Random;

public class Fruit {

  private static final Random rand = new Random();
  private static final int[] PALETTE = { 0xFF4DD0E1, // cyan
      0xFFB2FF59, // lime
      0xFFFFEB3B, // yellow
      0xFFFFA726, // orange
      0xFFB388FF, // violet
      0xFF69F0AE, // mint
      0xFF40C4FF, // sky blue
      0xFFEA80FC // orchid
  };
  public int fatContents;
  private RectF fruitRect;
  private int fruitSize;
  private int color;

  public Fruit() {
    fatContents = 1;
    fruitRect = new RectF();
    fruitSize = rand.nextInt(3) + 1;
    int alpha = 160 + rand.nextInt(96);
    int base = PALETTE[rand.nextInt(PALETTE.length)];
    color = Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base));
  }

  public void setFatContent(int newFat) {
    fatContents = newFat;
  }

  public void setFruit(PointF p1) {
    fruitRect.set(p1.x, p1.y, p1.x + fruitSize, p1.y + fruitSize);
  }

  public RectF getfruitRect() {
    return fruitRect;
  }

  public int getColor() {
    return color;
  }

  public int getSize() {
    return fruitSize;
  }
}