import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.gicentre.handy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class symmetries extends PApplet {



HandyRenderer h1, h2, h3, h4;
float c = 300;
float b = c * 1.5f;
PGraphics pg;
Boolean drawn = false;

int stage = 0;
int tick = 0;
int maxTick = 50;

public void setup()
{  
  

  pg = createGraphics(1024, 768);
  pg.smooth();

  h1 = HandyPresets.createPencil(this);
  h1.setStrokeWeight(0.5f);
  h1.setRoughness(1);

  h2 = HandyPresets.createMarker(this);
  h2.setGraphics(pg);
  h2.setStrokeWeight(2);
  //noLoop();

  drawMatrix();
}

public void drawMatrix() {
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
    rect(width/2, height/2, c/1.42f, c/1.42f);
    pushMatrix();
    translate(width/2, height/2);
    rotate(radians(45));
    h1.rect(0, 0, c/1.42f, c/1.42f);
    popMatrix();
  }

  if (stage >=8) {
    // lines
    pushMatrix();
    translate(width/2, height/2);
    for (int i = 0; i < 8; i++) {
      rotate(radians(360/PApplet.parseFloat(8)));
      //ellipse(80, 0, 30, 30);
      h1.line(0, 0, c/1.4f, 0);
    }
    popMatrix();
  }
}

public void draw()
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
public void mousePressed() {
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

public void keyPressed() {
  lastX = 0;
  lastY = 0;
}
  public void settings() {  size(800, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "symmetries" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
