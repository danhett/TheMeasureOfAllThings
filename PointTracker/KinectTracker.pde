/** ORIGINAL CODE CREDITS: **/
// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan
// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/

/** HORRIBLE MODIFICATIONS, MARCH 2019 / DAN HETT: https://github.com/danhett **/

class KinectTracker {

  int threshold = 700;
  PVector loc;
  PVector lerpedLoc;
  int[] depth;
  PImage display;
  Kinect2 kinect2;

  // new, allows zero values when nobody's there
  int trackCount = 0;
  int trackThreshold = 200;

  KinectTracker(PApplet pa) {
    kinect2 = new Kinect2(pa);
    kinect2.initDepth();
    kinect2.initDevice();

    display = createImage(kinect2.depthWidth, kinect2.depthHeight, RGB);

    loc = new PVector(0, 0);
    lerpedLoc = new PVector(0, 0);
  }

  void track() {
    depth = kinect2.getRawDepth();

    if (depth == null) return;

    float sumX = 0;
    float sumY = 0;
    float count = 0;

    for (int x = 0; x < kinect2.depthWidth; x++) {
      for (int y = 0; y < kinect2.depthHeight; y++) {
        // Mirroring the image
        int offset = kinect2.depthWidth - x - 1 + y * kinect2.depthWidth;
        // Grabbing the raw depth
        int rawDepth = depth[offset];

        // Testing against threshold
        if (rawDepth > 0 && rawDepth < threshold) {
          sumX += x;
          sumY += y;
          count++;
        }
      }
    }
    // As long as we found something
    if (count != 0) {
      loc = new PVector(sumX/count, sumY/count);
    }

    // Interpolating the location, doing it arbitrarily for now
    lerpedLoc.x = PApplet.lerp(lerpedLoc.x, loc.x, 0.3f);
    lerpedLoc.y = PApplet.lerp(lerpedLoc.y, loc.y, 0.3f);
  }

  PVector getLerpedPos() {
    if (trackCount < trackThreshold) {
      println("nobody there");
      lerpedLoc.x = 0;
      lerpedLoc.y = 0;
    } else {
      println("someone there!");
    }

    return lerpedLoc;
  }

  PVector getPos() {
    return loc;
  }

  void display() {
    PImage img = kinect2.getDepthImage();

    trackCount = 0;

    if (depth == null || img == null) return;

    display.loadPixels();
    for (int x = 0; x < kinect2.depthWidth; x++) {
      for (int y = 0; y < kinect2.depthHeight; y++) {
        // mirroring image
        int offset = (kinect2.depthWidth - x - 1) + y * kinect2.depthWidth;
        // Draw the raw depth either way
        int rawDepth = depth[offset];
        int pix = x + y*display.width;
        if (rawDepth > 0 && rawDepth < threshold) {
          // if we're within the threshold, show a colour for visual reference
          display.pixels[pix] = color(0, 200, 50);
          trackCount++;
        } else {
          display.pixels[pix] = img.pixels[offset];
        }
      }
    }
    display.updatePixels();

    image(display, 0, 0);
  }

  int getThreshold() {
    return threshold;
  }

  void setThreshold(int t) {
    threshold =  t;
  }
}
