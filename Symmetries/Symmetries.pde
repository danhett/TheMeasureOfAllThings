Tile tile;

import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;

void setup() {
  tile = new Tile(this, width/2, height/2, 0.9);
  size(1024, 768);
  //frameRate(60);
  //fullScreen();
  //oscP5 = new OscP5(this,13000);
  //myRemoteLocation = new NetAddress("127.0.0.1",12000);
}


void draw() {
  background(255);

  tile.draw();
}

/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage theOscMessage) {
  //tile.updateValue(theOscMessage.get(0).floatValue());
}
