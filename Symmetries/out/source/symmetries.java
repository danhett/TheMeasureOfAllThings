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

public class Symmetries extends PApplet {

Tile tile;
Tile tile2;
Tile tile3;
Tile tile4;

int timeDelay = 0;

public void setup() {
  tile = new Tile(this, width/2, height/2, 0.8f);
  //tile2 = new Tile(this, 600, 200, 0.5);
  //tile3 = new Tile(this, 200, 600, 0.5);
  //tile4 = new Tile(this, 600, 600, 0.5);

  
  frameRate(60);
  //fullScreen();
  //smooth();
}


public void draw() {
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


class Tile {
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

  int CURRENT_STEP = 0;
  int DRAW_STEPS = 0;

  int PENCIL_STEPS = 0;
  int PEN_STEPS = 0;
  int COLOUR_STEPS = 0;

  Symmetries reference;
  int xPos;
  int yPos;
  float scaleFactor;
  float[] params;

  Tile(Symmetries ref, int _xPos, int _yPos, float _scaleFactor) {
    reference = ref;
    xPos = _xPos;
    yPos = _yPos;
    scaleFactor = _scaleFactor;

    noFill();
    noStroke();
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

    PENCIL_STEPS = base.getChildCount();
    PEN_STEPS =  overlay.getChildCount();
    COLOUR_STEPS = colours.getChildCount();

    DRAW_STEPS = PENCIL_STEPS + PEN_STEPS + COLOUR_STEPS;

    println(DRAW_STEPS);
  }


  public void createDrawingTools() {
    pencil = HandyPresets.createPencil(reference);
    pencil.setStrokeWeight(1);
    pencil.setRoughness(0.5f);

    pen = HandyPresets.createMarker(reference);
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
    //drawColours();
    drawBase();
    drawOverlay();
    
    updateReadout();
  }

  public void updateReadout() {
    CURRENT_STEP = PApplet.parseInt(PApplet.parseFloat(mouseX) / width * DRAW_STEPS);

    fill(255, 0, 0);
    text(frameRate + "FPS", 20, 20);
    text(CURRENT_STEP + " / " + DRAW_STEPS, 20, 40);
    noFill();
  }


/*
BASE
*/
  int penciltimer = 2;
  int pencilcurrentTime = 0;
  int pencilcurrentSteps = 0;
  int pencilmaxSteps;
  public void drawBase() {
    pencilmaxSteps = pencilShapes.length;

    pushMatrix();

    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    /*
    if (pencilcurrentTime < penciltimer) {
      pencilcurrentTime++;
    } else {
      pencilcurrentTime = 0;

      if (pencilcurrentSteps < pencilmaxSteps) {     
        pencilcurrentSteps++;
      }
    }
    */

    int limit = pencilmaxSteps;

    if(CURRENT_STEP < pencilmaxSteps) {
      limit = CURRENT_STEP;
    }
                
    for(int i = 0; i < limit; i++) {
        pencil.setSeed(1234);
        
          int kind = pencilShapes[i].getKind();
          params = pencilShapes[i].getParams();

          if (kind == SVG_LINE) {
            pencil.line(
              params[0] * scaleFactor, 
              params[1] * scaleFactor, 
              params[2] * scaleFactor, 
              params[3] * scaleFactor
            );
          } 
          else if (kind == SVG_CIRCLE) {
            pencil.ellipse(
              params[0] * scaleFactor, 
              params[1] * scaleFactor, 
              params[2] * scaleFactor, 
              params[3] * scaleFactor
            );
        }  
    }

    popMatrix();
  }

/*
OVERLAY
*/
  int timer = 2;
  int currentTime = 0;
  int currentSteps = -30;
  int maxSteps;
  public void drawOverlay() {
    maxSteps = penShapes.length;

    pushMatrix();

    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    /*
    if (currentTime < timer) {
      currentTime++;
    } 
    else {
      currentTime = 0;

      if (currentSteps < maxSteps) {     
        currentSteps++;
      }
      else {
        canDraw = true;
      }
    }
    */

    int limit = PEN_STEPS;

    if(CURRENT_STEP > PENCIL_STEPS && CURRENT_STEP < PENCIL_STEPS + maxSteps) {
      limit = CURRENT_STEP - PENCIL_STEPS;

      println(limit);
    }
    else if(CURRENT_STEP < PENCIL_STEPS + 1){
      limit = 0;
    }

    for(int i = 0; i < limit; i++) {
      pen.setSeed(1234);

      params = penShapes[i].getParams();
      
      pen.line(
        params[0] * scaleFactor, 
        params[1] * scaleFactor, 
        params[2] * scaleFactor, 
        params[3] * scaleFactor
      );
    }

    popMatrix();
  }

/*
COLOURS
*/
  int shapetimer = 2;
  int shapecurrentTime = 0;
  int shapecurrentSteps = 0;
  int shapemaxSteps;
  public void drawColours() {
    shapemaxSteps = colours.getChildCount();

    pushMatrix();
    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    if(canDraw) {
      //scale(scaleFactor, scaleFactor);

      if (shapecurrentTime < shapetimer) {
        shapecurrentTime++;
      } else {
        shapecurrentTime = 0;

        colours.disableStyle();

        if (shapecurrentSteps < shapemaxSteps) {
          pg.beginDraw();
          pg.fill(random(200), random(200), 0, 100);
          pg.noStroke();
          pg.shape(colours.getChild(shapecurrentSteps), 0, 0); 
          pg.endDraw();

          shapecurrentSteps++;
        } else {
          finished = true;
        }
      }

      image(pg, 0, 0);
    }

    popMatrix();
  }
}
  public void settings() {  size(1024, 768); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Symmetries" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
