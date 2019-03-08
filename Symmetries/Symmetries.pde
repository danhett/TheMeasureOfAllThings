import at.mukprojects.console.*;
import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;
Tile tile;
Console console;

Boolean DEBUG_MODE = false;
Boolean USE_OSC = false;

void setup() {
  fullScreen(P2D);

  tile = new Tile(this, width/2, height/2, 0.9, DEBUG_MODE);

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

void draw() {
  background(255);

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
