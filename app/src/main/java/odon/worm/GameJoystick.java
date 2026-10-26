package odon.worm;

import android.graphics.PointF;


public class GameJoystick {

  double angle;

  public PointF cPoint;
  public PointF tbPoint;

  public int radius;

  GameJoystick(int sX, int sY) {

    cPoint = new PointF();
    tbPoint = new PointF();
    angle = 0;
    radius = 80;
  }

  // update method of the joystick control
  public void update(double dX,double dY, float tX, float tY){
		
		//the ff conditions will determine the direction where
		//the worm head should move to, by getting the diff. between
		//the joystick center and the current touch position.
		if(dX!=0){
			if(dX>0 && dY>0){
				
				//getting the angle using reverse tangent math. trigonometric function
				angle=Math.atan(dY/dX);
				//the result angle is measured in radians, not degrees.
			}
			else if(dX>0 && dY<0){
				//(it must be radians)
				angle=Math.toRadians(360)+(Math.atan(dY/dX));
			} 
			else if(dX<0 && dY>0){
				angle=Math.toRadians(180)+(Math.atan(dY/dX));
			}
			else if(dX<0 && dY<0){
				angle=Math.toRadians(180)+(Math.atan(dY/dX));
			}
			else if(dX>0 && dY==0){
				angle=0;
			}

			else if(dX<0 && dY==0){
				angle=Math.toRadians(180);
			}
		}
		
		else{
			if(dY<0){
				angle=Math.toRadians(270);
			}
			if(dY>0){
				angle=Math.toRadians(90);
			}
			if(dY==0){
				angle=0;
	    }
		}
		

		//sets the position of the tracker ball
		
		if(dY/Math.sin(angle)>radius){
			tbPoint.x= (float) (cPoint.x+(radius*Math.cos(angle)));
			tbPoint.y= (float) (cPoint.y+(radius*Math.sin(angle)));
		}else{
			tbPoint.x=(float) (cPoint.x+dX);
			tbPoint.y=(float) (cPoint.y+dY);
		}
		
	}

  // angle-getting method
  public double getAngle() {
    return angle;
  }

  // method for setting joystick center
  public void setCenter(float tX, float tY) {
    cPoint.x = tX;
    cPoint.y = tY;
  }

  // method getting joystick center point
  public PointF getCenter() {
    return cPoint;
    // method for getting tracker ball point for drawing
  }

  public PointF getTrackerBallPoint() {
    return tbPoint;
  }

  // end of Game Joystick class
}