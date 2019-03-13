/**
 * THE MEASURE OF ALL THING
 * Dan Hett (hellodanhett@gmail.com)
 *
 * TODO
 * - colour selection per patterns
 * - dead zone in the middle of the detection space (TAKE FROM INPUT SOURCE)
 * - fix colour handling in the main class
 */
import at.mukprojects.console.*;
import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;
Tile tile;
Console console;
float rectSize = 800;
float scaleFactor = 1.1;
float realRectSize = rectSize * scaleFactor;

Boolean DEBUG_MODE = true;
Boolean USE_OSC = false; // disable this to animate automatically
Boolean INVERT_COLOURS = true; // set to true for black background with white lines
Boolean USE_CODE_COLOURS = true; // set to true to ignore the AI cols and generate at runtime

void setup() {
  fullScreen(P2D);
  frameRate(30);
  //size(800,800,P2D);

  surface.setTitle("THE MEASURE OF ALL THINGS");

  tile = new Tile(this, width/2, height/2, scaleFactor);

  if(USE_OSC) {
    oscP5 = new OscP5(this,13000);
    myRemoteLocation = new NetAddress("127.0.0.1",12000);
  }

  if(DEBUG_MODE) 
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
  if(INVERT_COLOURS)
    background(0);
  else
    background(255);

  if(DEBUG_MODE) {
    noFill();
    stroke(255, 0, 0);
    rect(width / 2 - realRectSize / 2, height / 2 - realRectSize / 2, rectSize * scaleFactor, rectSize * scaleFactor);
  }

  tile.draw();

  if(DEBUG_MODE) 
    drawConsole();
}

void drawConsole() {
  console.draw();  
  console.print();
}

void oscEvent(OscMessage theOscMessage) {
  if(USE_OSC)
    tile.updateValue(theOscMessage.get(0).floatValue());
}
