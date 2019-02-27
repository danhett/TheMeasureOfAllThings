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
PShape colours;

PShape[] pencilShapes;
PShape[] penShapes;

int SVG_LINE = 4;
int SVG_CIRCLE = 31;

PGraphics pg;

Boolean canDraw = false;
Boolean finished = false;

public void setup() {
  
  background(255);

  blendMode(DARKEST);

  noFill();
  ellipseMode(CORNER);


  pg = createGraphics(800, 800);

  loadSVGs();
  createDrawingTools();
  populateBase();
  populateOverlay();
}

public void loadSVGs() {
  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");
  colours = loadShape("colours.svg");
}


public void createDrawingTools() {
  pencil = HandyPresets.createPencil(this);
  pencil.setStrokeWeight(1);
  pencil.setRoughness(0.1f);

  pen = HandyPresets.createMarker(this);
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
  if (finished)
    background(255);

  if (canDraw)
    drawColours();


  drawBase();
  drawOverlay();
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
    } else {
      canDraw = true;
    }
  }

  popMatrix();
}

int penciltimer = 2;
int pencilcurrentTime = 0;
int pencilcurrentSteps = 0;
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
      if (pencilShapes[pencilcurrentSteps].getKind() == SVG_LINE) {
        pencil.line(
          pencilShapes[pencilcurrentSteps].getParam(0), 
          pencilShapes[pencilcurrentSteps].getParam(1), 
          pencilShapes[pencilcurrentSteps].getParam(2), 
          pencilShapes[pencilcurrentSteps].getParam(3)
          );
      } else if (pencilShapes[pencilcurrentSteps].getKind() == SVG_CIRCLE) {

        pencil.ellipse(
          pencilShapes[pencilcurrentSteps].getParam(0), 
          pencilShapes[pencilcurrentSteps].getParam(1), 
          pencilShapes[pencilcurrentSteps].getParam(2), 
          pencilShapes[pencilcurrentSteps].getParam(3)
          );
      }

      pencilcurrentSteps++;
    }
  }

  popMatrix();
}

int shapetimer = 2;
int shapecurrentTime = 0;
int shapecurrentSteps = 0;
int shapemaxSteps;
public void drawColours() {
  shapemaxSteps = colours.getChildCount();

  //translate(width/2 - 400, height/2 - 400);

  if (shapecurrentTime < shapetimer) {
    shapecurrentTime++;
  } else {
    shapecurrentTime = 0;

    if (shapecurrentSteps < shapemaxSteps) {
      pg.beginDraw();
      pg.shape(colours.getChild(shapecurrentSteps), 0, 0); 
      pg.endDraw();

      shapecurrentSteps++;
    } else {
      finished = true;
    }
  }

  image(pg, width/2 - 400, height/2 - 400);
}


// STACK - main array with SketchLine, SketchEllipse, SketchRect and SketchText maybe
// drawSketchObject methods add them to the stack
// draw() updates each one and handles timing maybe

class Tile {
  Tile(Symmetries ref) {
    println("[tile]");

    //markerLines = createGraphics(1024, 768);
    //pg.smooth();

    //h1 = HandyPresets.createPencil(ref);
    //h1.setStrokeWeight(0.5);
    //h1.setRoughness(1);
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
