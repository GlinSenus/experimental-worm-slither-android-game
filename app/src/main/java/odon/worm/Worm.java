package odon.worm;

import android.graphics.PointF;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Random;

public class Worm {

  int rectHalfSize;
  private int bodyLength;
  private int sX, sY;

  private RectF headRect;
  private PointF head;

  private float wormSpeed, bodyPointDis;
  private float dX, dY;

  private ArrayList<RectF> bodyRects;
  private ArrayList<PointF> wormBodyPoints;

  private double xd, yd;
  private double heading = -Math.PI / 2; // current direction (starts facing up)
  private double maxTurn = 0.04; // max turn per frame, in radians

  Worm(int screenX, int screenY) {
    wormBodyPoints = new ArrayList<PointF>();

    head = new PointF();
    headRect = new RectF();
    bodyRects = new ArrayList<RectF>();

    wormSpeed = (float) 0.3;
    bodyPointDis = (float) 1.3;
    bodyLength = 10;
    rectHalfSize = 2;
    sX = screenX;
    sY = screenY;

    // start/reset worm body
    reset();
  }

  public void reset() {
    heading = -Math.PI / 2;
    wormBodyPoints.clear();
    bodyRects.clear();
    // set head position and head rect
    Random r = new Random();
    head.set(new PointF(r.nextInt(100) + 50, r.nextInt(50) + 25));
    headRect.set(head.x - rectHalfSize, head.y - rectHalfSize, head.x + rectHalfSize,
        head.y + rectHalfSize);

    for (int i = 1; i < bodyLength; i++) {
      PointF p1 = new PointF(head.x, head.y + (i * bodyPointDis));
      RectF tempRect = new RectF(p1.x - rectHalfSize, p1.y - rectHalfSize, p1.x + rectHalfSize,
          p1.y + rectHalfSize);
      wormBodyPoints.add(p1);
      bodyRects.add(tempRect);
    }
  }

  public void grow(int fat) {
    for (int i = 0; i < fat; i++) {
      PointF p1 = new PointF(wormBodyPoints.get(wormBodyPoints.size() - 1).x,
          wormBodyPoints.get(wormBodyPoints.size() - 1).y);
      RectF tempRect = new RectF(p1.x - rectHalfSize, p1.y - rectHalfSize, p1.x + rectHalfSize,
          p1.y + rectHalfSize);
      wormBodyPoints.add(p1);
      bodyRects.add(tempRect);
    }
  }

  public PointF getHeadPoint() {
    return head;
  }

  public void updateWorm(double targetAngle) {

    // difference between where the joystick points and where the worm faces
    double diff = targetAngle - heading;

    // keep the difference between -PI and PI, so the worm turns the short way
    while (diff > Math.PI)
      diff -= 2 * Math.PI;
    while (diff < -Math.PI)
      diff += 2 * Math.PI;

    // turn only a little bit each frame
    if (Math.abs(diff) <= maxTurn) {
      heading = targetAngle;
    } else {
      heading += Math.signum(diff) * maxTurn;
    }

    // move head in the heading direction
    head.x = (float) (head.x + (wormSpeed * Math.cos(heading)));
    head.y = (float) (head.y + (wormSpeed * Math.sin(heading)));
    headRect.set(head.x - rectHalfSize, head.y - rectHalfSize, head.x + rectHalfSize,
        head.y + rectHalfSize);

    // make the body parts move after the other
    for (int i = 0; i < wormBodyPoints.size(); i++) {
      if (i == 0) {
        // make the first body part follow the head
        dX = wormBodyPoints.get(i).x - head.x;
        dY = wormBodyPoints.get(i).y - head.y;
        xd = bodyPointDis * Math.cos(getAngle(dX, dY));
        yd = bodyPointDis * Math.sin(getAngle(dX, dY));
        wormBodyPoints.get(i).x = (float) (head.x + xd);
        wormBodyPoints.get(i).y = (float) (head.y + yd);

        bodyRects.get(i).set(wormBodyPoints.get(i).x - rectHalfSize,
            wormBodyPoints.get(i).y - rectHalfSize, wormBodyPoints.get(i).x + rectHalfSize,
            wormBodyPoints.get(i).y + rectHalfSize);
      } else {
        // make the other body parts to maintain a distance from the other part ahead of them
        dX = wormBodyPoints.get(i).x - wormBodyPoints.get(i - 1).x;
        dY = wormBodyPoints.get(i).y - wormBodyPoints.get(i - 1).y;
        xd = bodyPointDis * Math.cos(getAngle(dX, dY));
        yd = bodyPointDis * Math.sin(getAngle(dX, dY));
        wormBodyPoints.get(i).x = (float) (wormBodyPoints.get(i - 1).x + xd);
        wormBodyPoints.get(i).y = (float) (wormBodyPoints.get(i - 1).y + yd);
        bodyRects.get(i).set(wormBodyPoints.get(i).x - rectHalfSize,
            wormBodyPoints.get(i).y - rectHalfSize, wormBodyPoints.get(i).x + rectHalfSize,
            wormBodyPoints.get(i).y + rectHalfSize);
      }
    }
  }

  // used to find the angle using point differences
  private double getAngle(float dX, float dY) {
    double angle = 0;
    if (dX != 0) {
      if (dX > 0 && dY > 0) {
        angle = Math.atan(dY / dX);
      } else if (dX > 0 && dY < 0) {
        angle = Math.toRadians(360) + (Math.atan(dY / dX));
      } else if (dX < 0 && dY > 0) {
        angle = Math.toRadians(180) + (Math.atan(dY / dX));
      } else if (dX < 0 && dY < 0) {
        angle = Math.toRadians(180) + (Math.atan(dY / dX));
      } else if (dX > 0 && dY == 0) {
        angle = 0;
      }

      else if (dX < 0 && dY == 0) {
        angle = Math.toRadians(180);
      }

    } else {
      if (dY < 0) {
        angle = Math.toRadians(270);
      }
      if (dY > 0) {
        angle = Math.toRadians(90);
      }
      if (dY == 0) {
        angle = 0;
      }
    }
    return angle;
  }

  public RectF getHeadRect() {
    return headRect;
  }

  public ArrayList<RectF> getBodyRects() {
    return bodyRects;
  }

  public double getHeading() {
    return heading;
  }

  // end of thw worm class
}