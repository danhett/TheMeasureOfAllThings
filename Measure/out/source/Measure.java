import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import at.mukprojects.console.*; 
import oscP5.*; 
import netP5.*; 
import org.gicentre.handy.*; 
import ch.bildspur.postfx.builder.*; 
import ch.bildspur.postfx.pass.*; 
import ch.bildspur.postfx.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Measure extends PApplet {




  
OscP5 oscP5;
NetAddress myRemoteLocation;
Tile tile;
Console console;

Boolean DEBUG_MODE = false;
Boolean USE_OSC = false; // disable this to animate automatically

public void setup() {
  
  frameRate(30);
  //size(800,800,P2D);

  tile = new Tile(this, width/2, height/2, 0.9f, DEBUG_MODE, USE_OSC);

  if(USE_OSC) {
    oscP5 = new OscP5(this,13000);
    myRemoteLocation = new NetAddress("127.0.0.1",12000);
  }

  if(DEBUG_MODE) 
    setupConsole();
}

public void setupConsole() {
  console = new Console(this);
  console.start();
}

public void mousePressed() {
  tile.updateSketch();
}

public void draw() {
  background(255);

  tile.draw();

  if(DEBUG_MODE) 
    drawConsole();
}

public void drawConsole() {
  console.draw();  
  console.print();
}

public void oscEvent(OscMessage theOscMessage) {
  if(USE_OSC)
    tile.updateValue(theOscMessage.get(0).floatValue());
}





class Tile {
  Measure reference;
  PostFX fx;
  HandyRenderer pencil;
  HandyRenderer pen;
  PShape base;
  PShape overlay;
  PShape colours;

  int xPos;
  int yPos;
  float scaleFactor;
  float[] params;

  PShape[] pencilShapes;
  PShape[] penShapes;

  PImage paper;

  int SVG_LINE = 4;
  int SVG_CIRCLE = 31;

  int current = 4;
  int max = 5;

  PGraphics surface;
  PGraphics pg;

  int penciltimer = 2;
  int pencilcurrentTime = 0;
  int pencilcurrentSteps = 0;
  int pencilmaxSteps;
  int timer = 2;
  int currentTime = 0;
  int currentSteps = -30;
  int maxSteps;
  int shapetimer = 2;
  int shapecurrentTime = 0;
  int shapecurrentSteps = 0;
  int shapemaxSteps;

  Boolean finished = false;
  Boolean DEBUG_MODE = false;
  Boolean USE_OSC = false;

  int ANIM_STEP = 0;
  int CURRENT_STEP = 0;
  int DRAW_STEPS = 0;
  int PENCIL_STEPS = 0;
  int PEN_STEPS = 0;
  int COLOUR_STEPS = 0;

  String animationDirection = "up";

  int halfWidth = width/2;
  int roughness = 6;
  float randomModifier = 0.5f;
  int mashRoughness = 100;

  Tile(Measure ref, int _xPos, int _yPos, float _scaleFactor, Boolean _debug, Boolean _osc) {
    reference = ref;
    xPos = _xPos;
    yPos = _yPos;
    scaleFactor = _scaleFactor;
    DEBUG_MODE = _debug;
    USE_OSC = _osc;
    
    fx = new PostFX(reference); 

    noFill();
    noStroke();
    ellipseMode(CORNER);

    pg = createGraphics(800, 800);
    surface = createGraphics(width, height);

    createDrawingTools();

    loadSVGs();
  }

  public void updateValue(float val) {
    //doPositionCheck(width * val);
  }

  public void loadSVGs() {
    base = loadShape("patterns/pattern" + current + "/pencil.svg");
    overlay = loadShape("patterns/pattern" + current + "/pen.svg");
    colours = loadShape("patterns/pattern" + current + "/colour.svg");

    PENCIL_STEPS = base.getChildCount();
    PEN_STEPS =  overlay.getChildCount();
    COLOUR_STEPS = colours.getChildCount();

    DRAW_STEPS = PENCIL_STEPS + PEN_STEPS + COLOUR_STEPS;

    populateBase();
    populateOverlay();
  }

  public void updateSketch() {
    if(current < max) 
      current++;
    else
      current = 1;

    loadSVGs();
  }

  public void createDrawingTools() {
    pencil = HandyPresets.createPencil(reference);
    pencil.setGraphics(surface);
    pencil.setStrokeWeight(1);
    pencil.setRoughness(0.1f);
    pencil.setUseSecondaryColour(false);

    pen = HandyPresets.createMarker(reference);
    pen.setGraphics(surface);
    pen.setRoughness(1);
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
    drawColours();

    surface.beginDraw();
    surface.noFill();
    surface.clear();
    surface.ellipseMode(CORNER);
    surface.translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    drawBase();
    drawOverlay();

    surface.endDraw();
    
    image(surface, xPos - (width*0.5f), yPos - (height*0.5f));

    if(!USE_OSC && frameCount % 1 == 0)
      calculateAnimation();

    doPositionCheck(mouseX);

    //fx.render()
    //.vignette(0.5 * randomModifier, 0.6 * randomModifier)
    //.compose();

    if(DEBUG_MODE)
      updateReadout();
  }

  public void doPositionCheck(float input) {
    if(input < width/2) {
      randomModifier = 1-(input / halfWidth);
    } 
    else {            
      randomModifier = ((input - halfWidth) / halfWidth);
    }

    pencil.setRoughness((randomModifier * roughness) + 0.1f); // fixes circle render bug
    pencil.setStrokeWeight(1 - randomModifier);
    pen.setRoughness((randomModifier * roughness));
    pen.setStrokeWeight(2 - (randomModifier * 2));
  }

  public void calculateAnimation() {
    if(animationDirection == "up") {
      if(ANIM_STEP < (DRAW_STEPS-1)) {
        ANIM_STEP++;
      }
      else  {
        animationDirection = "down";
      }
    }
    else {
      if(ANIM_STEP > 0) {
        ANIM_STEP--;
      }
      else {
        updateSketch();
        animationDirection = "up";
      }
    }

    CURRENT_STEP = ANIM_STEP;
  }

  public void updateReadout() {
    fill(255, 50, 0);
    text("THE MEASURE OF ALL THINGS", 20, 60);
    fill(0, 0, 0);
    text("- - - - - - - - - - - - -", 20, 80);
    text(round(frameRate) + " fps", 20, 100);
    text("pattern " + current + " of " + max, 20, 120);
    text(CURRENT_STEP + " / " + DRAW_STEPS, 20, 140);
    noFill();
  }


  /**
   * PENCIL
   */
  public void drawBase() {
    pencilmaxSteps = pencilShapes.length;

    int limit = pencilmaxSteps;

    if(CURRENT_STEP < pencilmaxSteps) {
      limit = CURRENT_STEP;
    }
         
    for(int i = 0; i < limit; i++) {
        if(frameCount % 2 == 0)
          pencil.setSeed(1234);
        
          int kind = pencilShapes[i].getKind();
          params = pencilShapes[i].getParams();

          if (kind == SVG_LINE) {
            pencil.line(
              mash(params[0]) * scaleFactor, 
              mash(params[1]) * scaleFactor, 
              mash(params[2]) * scaleFactor, 
              mash(params[3]) * scaleFactor
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
  }

  /**
   * PEN
   */
  public void drawOverlay() {
    maxSteps = penShapes.length;

    int limit = PEN_STEPS;

    if(CURRENT_STEP > PENCIL_STEPS && CURRENT_STEP < PENCIL_STEPS + maxSteps) {
      limit = CURRENT_STEP - PENCIL_STEPS;
    }
    else if(CURRENT_STEP < PENCIL_STEPS + 1){
      limit = 0;
    }

    for(int i = 0; i < limit; i++) {
      pen.setSeed(1234);

      params = penShapes[i].getParams();
      
      pen.line(
        mash(params[0]) * scaleFactor, 
        mash(params[1]) * scaleFactor, 
        mash(params[2]) * scaleFactor, 
        mash(params[3]) * scaleFactor
      );
    }
  }

  public float mash(float in) {
    return random(in-(mashRoughness*randomModifier), in+(mashRoughness*randomModifier));
  }

  /**
   * COLOURS
   */  
  public void drawColours() {
    shapemaxSteps = colours.getChildCount();

    pushMatrix();
    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    scale(scaleFactor, scaleFactor);

    int limit = PEN_STEPS+PENCIL_STEPS;

    if(CURRENT_STEP > (PENCIL_STEPS+PEN_STEPS) && CURRENT_STEP < (PENCIL_STEPS+PEN_STEPS) + COLOUR_STEPS) {
      limit = CURRENT_STEP - PENCIL_STEPS - PEN_STEPS;
    }
    else if(CURRENT_STEP < PEN_STEPS + PENCIL_STEPS + 1){
      limit = 0;
    }

    if(CURRENT_STEP > PENCIL_STEPS+PEN_STEPS) {
      pg.beginDraw();
      pg.clear();
      for(int i = 0; i <= limit; i++) {
        if(randomModifier < 0.2f) {
          pg.shape(colours.getChild(i), 0, 0); 
        }
      }
      pg.endDraw();

      image(pg, 0, 0);
    }

    popMatrix();
  }
}
  public void settings() {  fullScreen(P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Measure" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
