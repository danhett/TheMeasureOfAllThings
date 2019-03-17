/**
 * THE MEASURE OF ALL THINGS
 * Dan Hett (hellodanhett@gmail.com)
 * 
 * Created for the "Sheherezade" residency that I took part in, 
 * on location in Lahore, Pakistan in March 2019.
 *
 * The code is heavily commented for clarity, but is 
 * fairly messy as much of it was written either on
 * aeroplanes or on rooftops in Lahore, or hotel rooms. 
 * 
 * Distributed under the Do What The Fuck You Want public license.
 */
import at.mukprojects.console.*;
import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress myRemoteLocation;
Tile tile;
Console console;
float rectSize = 800;
float scaleFactor = 0.9;
float realRectSize = rectSize * scaleFactor;

Boolean DEBUG_MODE = false;
Boolean USE_OSC = false; // disable this to animate automatically
Boolean INVERT_COLOURS = true; // set to true for black background with white lines
Boolean USE_CODE_COLOURS = true; // set to true to ignore the AI cols and generate at runtime

String INTERACTION_MODE = "wobble"; // "wobble" or "timeline" 

Boolean hasDoneOSCGrossHack = false;

void setup() {
  fullScreen(P2D);
  frameRate(40);
  //size(1000,800,P2D);

  surface.setTitle("THE MEASURE OF ALL THINGS");

  tile = new Tile(this, width/2, height/2, scaleFactor);

  if (DEBUG_MODE) 
    setupConsole();
}

void setupConsole() {
  console = new Console(this);
  console.start();
}

void mousePressed() {
  tile.updateSketch();
}

void draw() {

  // do this here once, as it blocks the main thread in setup() and causes a crash
  if (USE_OSC) {
    if (!hasDoneOSCGrossHack) {
      hasDoneOSCGrossHack = true;
      oscP5 = new OscP5(this, 13000);
      myRemoteLocation = new NetAddress("127.0.0.1", 12000);
    }
  }

  if (INVERT_COLOURS)
    background(0);
  else
    background(255);

  if (DEBUG_MODE) {
    noFill();
    stroke(255, 0, 0);
    rect(width / 2 - realRectSize / 2, height / 2 - realRectSize / 2, rectSize * scaleFactor, rectSize * scaleFactor);
  }

  tile.draw();

  if (DEBUG_MODE) 
    drawConsole();
}

void drawConsole() {
  console.draw();  
  console.print();
}

void oscEvent(OscMessage theOscMessage) {
  if (USE_OSC) {
    tile.updateValue(theOscMessage.get(0).floatValue());
  }
}
