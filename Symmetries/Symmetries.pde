Tile tile;
Tile tile2;
Tile tile3;
Tile tile4;

int delay = 0;

void setup() {
  tile = new Tile(this, width/2, height/2, 0.5);
  //tile2 = new Tile(this, 800, 200, 0.5);
  //tile3 = new Tile(this, 400, 600, 0.5);
  //tile4 = new Tile(this, 800, 600, 0.5);

  fullScreen();
}


void draw() {
  background(255);

  tile.draw();

  //if(delay > 20)
   // tile2.draw();

  //if(delay > 40)
  //  tile3.draw();

  //if(delay > 60)
    //tile4.draw();

  delay++;
}
