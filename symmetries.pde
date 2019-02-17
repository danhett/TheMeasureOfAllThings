import org.gicentre.handy.*;

HandyRenderer h1, h2, h3, h4;
float c = 300;
float b = c * 1.5;
PGraphics pg;
Boolean drawn = false;

int stage = 0;
int tick = 0;
int maxTick = 50;

void setup()
{  
  size(800, 600);

  pg = createGraphics(1024, 768);
  pg.smooth();

  h1 = HandyPresets.createPencil(this);
  h1.setStrokeWeight(0.5);
  h1.setRoughness(1);

  h2 = HandyPresets.createMarker(this);
  h2.setGraphics(pg);
  h2.setStrokeWeight(2);
  //noLoop();

  drawMatrix();
}

void drawMatrix() {
  stroke(0, 0, 0, 100);
  strokeWeight(1);
  h1.setSeed(1234);

  noFill();

  if (stage >= 1) {
    // horizontal line
    h1.line(width/2 - c, height/2, width/2 + c, height/2);
  }

  if (stage >= 2) {
    // guide circle
    h1.ellipse(width/2, height/2, c, c);
  }

  if (stage >= 3) {
    // arcs
    stroke(204, 102, 0, 50);
    h1.ellipse(width/2 - c/2, height/2, b, b);
    h1.ellipse(width/2 + c/2, height/2, b, b);
  }

  if (stage >= 4) {
    // centre dividing line
    stroke(204, 102, 0, 100);
    h1.line(width/2, height/2 - b/2, width/2, height/2 + b/2);
  }

  if (stage >= 5) {
    // four circles
    stroke(204, 0, 100, 100);
    h1.ellipse(width/2 - c/2, height/2, c, c);
    h1.ellipse(width/2 + c/2, height/2, c, c);
    h1.ellipse(width/2, height/2 - c/2, c, c);
    h1.ellipse(width/2, height/2 + c/2, c, c);
  }

  if (stage >=6) {
    // box
    stroke(0, 0, 0, 100);
    rectMode(CENTER);
    h1.rect(width/2, height/2, c, c);
  }

  if (stage >= 7) {
    // inner boxes
    rect(width/2, height/2, c/1.42, c/1.42);
    pushMatrix();
    translate(width/2, height/2);
    rotate(radians(45));
    h1.rect(0, 0, c/1.42, c/1.42);
    popMatrix();
  }

  if (stage >=8) {
    // lines
    pushMatrix();
    translate(width/2, height/2);
    for (int i = 0; i < 8; i++) {
      rotate(radians(360/float(8)));
      //ellipse(80, 0, 30, 30);
      h1.line(0, 0, c/1.4, 0);
    }
    popMatrix();
  }
}

void draw()
{
   if(tick < maxTick) {
    tick++;
  }
  else {
   stage++;
   tick = 0;
  }
  
  background(255);
  drawMatrix();
  image(pg, 0, 0);
}

int lastX = 0;
int lastY = 0;
void mousePressed() {
  if (lastX == 0) {
    lastX = mouseX;
    lastY = mouseY;
  } else {
    pg.beginDraw();
    //pg.strokeWeight(3);
    //pg.stroke(0);
    h2.line(lastX, lastY, mouseX, mouseY);
    lastX = mouseX;
    lastY = mouseY;
    pg.endDraw();
  }
}

void keyPressed() {
  lastX = 0;
  lastY = 0;
}
