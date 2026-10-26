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

  public void update(double dX,double dY, float tX, float tY){
		
		if(dX!=0){
			if(dX>0 && dY>0){
				
				angle=Math.atan(dY/dX);
			}
			else if(dX>0 && dY<0){
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
		

		
		if(dY/Math.sin(angle)>radius){
			tbPoint.x= (float) (cPoint.x+(radius*Math.cos(angle)));
			tbPoint.y= (float) (cPoint.y+(radius*Math.sin(angle)));
		}else{
			tbPoint.x=(float) (cPoint.x+dX);
			tbPoint.y=(float) (cPoint.y+dY);
		}
		
	}

  public double getAngle() {
    return angle;
  }

  public void setCenter(float tX, float tY) {
    cPoint.x = tX;
    cPoint.y = tY;
  }

  public PointF getCenter() {
    return cPoint;
  }

  public PointF getTrackerBallPoint() {
    return tbPoint;
  }

}