import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.gicentre.handy.*; 
import org.gicentre.handy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Symmetries extends PApplet {



HandyRenderer pencil;
HandyRenderer pen;
PShape base;
PShape overlay;

PShape[] pencilShapes;
PShape[] penShapes;

public void setup() {
  
  //size(1024, 768);
  background(255);
  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");

  pencil = HandyPresets.createPencil(this);
  //h1.setStrokeWeight(0.5);
  pencil.setRoughness(0.5f);

  pen = HandyPresets.createMarker(this);

  //drawBase();
  populateBase();
  populateOverlay();
}

public void populateBase() {
  pencilShapes = new PShape[base.getChildCount()];

  for (int i = 0; i < base.getChildCount(); i++) {
    PShape shape = base.getChild(i);
    pencilShapes[i] = shape;
  }
}

public void populateOverlay() {
  penShapes = new PShape[overlay.getChildCount()];

  for (int i = 0; i < overlay.getChildCount(); i++) {
    PShape shape = overlay.getChild(i);
    penShapes[i] = shape;
  }
}


public void draw() {
  drawOverlay();
  drawBase();
}


int timer = 4;
int currentTime = 0;
int currentSteps = -30;
int maxSteps;
public void drawOverlay() {
  maxSteps = penShapes.length;

  pushMatrix();
  translate(width/2 - 400, height/2 - 400);

  if (currentTime < timer) {
    currentTime++;
  } else {
    currentTime = 0;

    if (currentSteps < maxSteps) {
      try {
        pen.line(
          penShapes[currentSteps].getParams()[0], 
          penShapes[currentSteps].getParams()[1], 
          penShapes[currentSteps].getParams()[2], 
          penShapes[currentSteps].getParams()[3]
          );
      } 
      catch (Exception e) {
        //e.printStackTrace();
      }
      
      currentSteps++; 
    } 
  }

  popMatrix();
}

int penciltimer = 2;
int pencilcurrentTime = 0;
int pencilcurrentSteps = -10;
int pencilmaxSteps;
public void drawBase() {
  pencilmaxSteps = pencilShapes.length;

  pushMatrix();
  translate(width/2 - 400, height/2 - 400);

  if (pencilcurrentTime < penciltimer) {
    pencilcurrentTime++;
  } else {
    pencilcurrentTime = 0;

    if (pencilcurrentSteps < pencilmaxSteps) {
      try {
        pencil.line(
          pencilShapes[pencilcurrentSteps].getParams()[0], 
          pencilShapes[pencilcurrentSteps].getParams()[1], 
          pencilShapes[pencilcurrentSteps].getParams()[2], 
          pencilShapes[pencilcurrentSteps].getParams()[3]
          );
      } 
      catch (Exception e) {
        //e.printStackTrace();
      }
      
      pencilcurrentSteps++; 
    } 
  }

  popMatrix();
}


// STACK - main array with SketchLine, SketchEllipse, SketchRect and SketchText maybe
// drawSketchObject methods add them to the stack
// draw() updates each one and handles timing maybe

class Sketcher {
  Sketcher(Symmetries ref) {
    println("[sketch]");

    //markerLines = createGraphics(1024, 768);
    //pg.smooth();

    //h1 = HandyPresets.createPencil(ref);
    //h1.setStrokeWeight(0.5);
    //h1.setRoughness(1);
  }

  public void drawSketchLine(int xS, int yS, int xE, int yE, int dur) {
    //h1.line(xS, xS, xE, yE);
  }

  public void draw() {
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Symmetries" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
