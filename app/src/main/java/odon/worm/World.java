package odon.worm;

import android.graphics.RectF;

public class World {
  int width, height;
  RectF worldRect;

  World() {
    width = 300;
    height = 300;
    worldRect = new RectF(0, 0, width, height);
  }

  public RectF getWorldRect() {
    return worldRect;
  }
}