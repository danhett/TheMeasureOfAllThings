Tile tile;
Tile tile2;
Tile tile3;
Tile tile4;

int timeDelay = 0;

void setup() {
  tile = new Tile(this, width/2, height/2, 0.8);
  //tile2 = new Tile(this, 600, 200, 0.5);
  //tile3 = new Tile(this, 200, 600, 0.5);
  //tile4 = new Tile(this, 600, 600, 0.5);

  size(1024, 768);
  frameRate(60);
  //fullScreen();
  //smooth();
}


void draw() {
  background(255);

  tile.draw();

  /*
  if(timeDelay > 20)
    tile2.draw();

  if(timeDelay > 40)
    tile3.draw();

  if(timeDelay > 60)
    tile4.draw();
  */

  timeDelay++;
}
