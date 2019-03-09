/** ORIGINAL CODE CREDITS: **/
// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan
// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/

/** HORRIBLE MODIFICATIONS, MARCH 2019 / DAN HETT: https://github.com/danhett **/
import org.openkinect.processing.*;
import netP5.*;
import oscP5.*;

OscP5 oscP5;
NetAddress myRemoteLocation;
OscMessage signal;

// The kinect stuff is happening in another class
KinectTracker tracker;

void setup() {
  size(512, 424);

  // NEW
  oscP5 = new OscP5(this,12000);
  myRemoteLocation = new NetAddress("127.0.0.1",13000);

  tracker = new KinectTracker(this);
}

void draw() {
  background(255);

  tracker.track();  
  tracker.display();

  // raw location
  PVector v1 = tracker.getPos();
  fill(50, 100, 250, 200);
  noStroke();
  ellipse(v1.x, v1.y, 20, 20);

  // "lerped" location
  PVector v2 = tracker.getLerpedPos();
  fill(100, 250, 50, 200);
  noStroke();
  ellipse(v2.x, v2.y, 20, 20);

  // send the lerped location over OSC
  signal = new OscMessage("/x");
  signal.add(v2.x / width); 
  oscP5.send(signal, myRemoteLocation); 

  // Display some info
  //int t = tracker.getThreshold();
  //fill(0);
  //text("threshold: " + t + "    " +  "framerate: " + int(frameRate) + "    " +
   // "UP increase threshold, DOWN decrease threshold", 10, 500);
}

// Adjust the threshold with key presses
void keyPressed() {
  int t = tracker.getThreshold();
  if (key == CODED) {
    if (keyCode == UP) {
      t +=10;
      tracker.setThreshold(t);
    } else if (keyCode == DOWN) {
      t -=10;
      tracker.setThreshold(t);
    }
  }
}
