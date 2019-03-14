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

/**
 * THE MEASURE OF ALL THING
 * Dan Hett (hellodanhett@gmail.com)
 *
 * TODO
 * - colour selection per patterns
 * - dead zone in the middle of the detection space (TAKE FROM INPUT SOURCE)
 * - fix colour handling in the main class
 */



  
OscP5 oscP5;
NetAddress myRemoteLocation;
Tile tile;
Console console;
float rectSize = 800;
float scaleFactor = 0.8f;
float realRectSize = rectSize * scaleFactor;

Boolean DEBUG_MODE = false;
Boolean USE_OSC = false; // disable this to animate automatically
Boolean INVERT_COLOURS = true; // set to true for black background with white lines
Boolean USE_CODE_COLOURS = true; // set to true to ignore the AI cols and generate at runtime

String INTERACTION_MODE = "timeline"; // "wobble" or "timeline" 

Boolean hasDoneOSCGrossHack = false;

public void setup() {
  
  frameRate(30);
  //size(800,800,P2D);

  surface.setTitle("THE MEASURE OF ALL THINGS");

  tile = new Tile(this, width/2, height/2, scaleFactor);

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
  
  // do this here once, as it blocks the main thread in setup() and causes a crash
  if(USE_OSC) {
      if(!hasDoneOSCGrossHack) {
      hasDoneOSCGrossHack = true;
      oscP5 = new OscP5(this,13000);
      myRemoteLocation = new NetAddress("127.0.0.1",12000);
    }
  }

  if(INVERT_COLOURS)
    background(0);
  else
    background(255);

  if(DEBUG_MODE) {
    noFill();
    stroke(255, 0, 0);
    rect(width / 2 - realRectSize / 2, height / 2 - realRectSize / 2, rectSize * scaleFactor, rectSize * scaleFactor);
  }

  tile.draw();

  if(DEBUG_MODE) 
    drawConsole();
}

public void drawConsole() {
  console.draw();  
  console.print();
}

public void oscEvent(OscMessage theOscMessage) {
  if(USE_OSC) {
    tile.updateValue(theOscMessage.get(0).floatValue());
  }
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

  int current = 1;
  int max = 8;

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

  int ANIM_STEP = 0;
  int CURRENT_STEP = 0;
  int DRAW_STEPS = 0;
  int PENCIL_STEPS = 0;
  int PEN_STEPS = 0;
  int COLOUR_STEPS = 0;

  String animationDirection = "up";

  int halfWidth = width/2;
  int roughness = 6;
  float randomModifier = 0;
  int mashRoughness = 100;

  int holdCount = 0;
  int holdThreshold = 150; // frames to keep the final design on for

   int[][] cols = { 
    {0xff262e69, 0xff44a5be, 0xff15624c, 0xff8b7350, 0xffbca99d},
    {0xffcb6149, 0xffce642e, 0xffb19077, 0xfff3e5be, 0xff613839},
    {0xffe2a6a6, 0xff4abed3, 0xff228f9c, 0xffc52353, 0xffffddb5},
    {0xff7ab191, 0xff35334b, 0xffdfa834, 0xffe8d7bb, 0xfff9caa5},
    {0xffffefe4, 0xffa38976, 0xffc92027, 0xff0d886d, 0xff414da0},
    {0xffd5d2c9, 0xff63beb7, 0xff989dc3, 0xff283679, 0xff9f5d3d},
    {0xffced2db, 0xffcda32c, 0xff2e56a6, 0xff49aa7c, 0xff965924},
    {0xff05698f, 0xff0382a6, 0xff49896b, 0xffd1aa49, 0xff699097},
    {0xfff04f3c, 0xff8180bd, 0xfff1e8e1, 0xffffd79e, 0xff3b5233},
    {0xffefe8de, 0xffbc892c, 0xff272a6b, 0xffca2127, 0xff7c3d21},
    {0xff6b83b4, 0xff56a57e, 0xffb5976a, 0xff27326f, 0xfffef5f9},
    {0xff5481ae, 0xffe45625, 0xfff16577, 0xff0e2917, 0xffede8eb},
    {0xffb76728, 0xff40617a, 0xffeebf39, 0xffc5cad8, 0xffd46b34},
    {0xffe6e5e1, 0xffa66227, 0xff2c2c2e, 0xffc8ae90, 0xffa67e3f},
    {0xff017ea9, 0xff2d2b71, 0xffe9e1cc, 0xff44999b, 0xff0989ae}
  };

  // input colours from the artwork
  int found1, found2, found3, found4, found5, found6, found7, found8, found9, found10;
  
  // output colours
  int col1, col2, col3, col4, col5;

  int[] colsTempList = { col1, col2, col3, col4, col5};

  Tile(Measure ref, int _xPos, int _yPos, float _scaleFactor) {
    reference = ref;
    xPos = _xPos;
    yPos = _yPos;
    scaleFactor = _scaleFactor;
    
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
    doPositionCheck(width * val);
  }

  public void loadSVGs() {
    updateColourSelection();

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

    if(reference.INVERT_COLOURS)
      pen.setStrokeColour(255);
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

    if(reference.INTERACTION_MODE == "wobble")
      calculateAnimation();

    if(!reference.USE_OSC) 
      doPositionCheck(mouseX); // input here
    //else
      //doPositionCheck(mouseX);

    //fx.render()
    //.vignette(0.5 * randomModifier, 0.6 * randomModifier)
    //.compose();

    if(reference.DEBUG_MODE)
      updateReadout();
  }

  public void doPositionCheck(float input) {
    if(input < width/2) {
      randomModifier = 1-(input / halfWidth);
    } 
    else {            
      randomModifier = ((input - halfWidth) / halfWidth);
    }

    if(reference.INTERACTION_MODE == "wobble") {
      pencil.setRoughness((randomModifier * roughness) + 0.1f); // fixes circle render bug
      pencil.setStrokeWeight(1 - randomModifier);
      pen.setRoughness((randomModifier * roughness));
      pen.setStrokeWeight(2 - (randomModifier * 2));
    }

    if(reference.INTERACTION_MODE == "timeline") {
      //println(int(DRAW_STEPS * randomModifier));
      
      try {
        CURRENT_STEP = DRAW_STEPS - PApplet.parseInt(DRAW_STEPS * randomModifier);
      }
      catch(Exception e)  {
        // nom nom nom 
      }
    }
  }

  public void calculateAnimation() {
    if(animationDirection == "up") {
      if(ANIM_STEP < (DRAW_STEPS-1)) {
        ANIM_STEP++;
      }
      else  {
        if(holdCount < holdThreshold) {
          holdCount++;
        }
        else {
          holdCount = 0;
          animationDirection = "down";
        }
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
    
    if(reference.INVERT_COLOURS)
      fill(255, 255, 255);
    else 
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
    if(reference.INTERACTION_MODE == "timeline")
      return in;

    return random(in-(mashRoughness*randomModifier), in+(mashRoughness*randomModifier));
  }

  /**
   * DRAW COLOURS
   */  

  PShape drawShape; 
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

    if(reference.USE_CODE_COLOURS)
      colours.disableStyle();

    if(CURRENT_STEP > PENCIL_STEPS+PEN_STEPS) {
      pg.beginDraw();
      pg.clear();
      pg.noStroke();
      for(int i = 0; i <= limit; i++) {  
          try {
            pg.pushMatrix();
            pg.translate(mash(0) * 2, mash(0) * 2);

            // make the PShape
            drawShape = colours.getChild(i);
            
            // if needed, re-colour it
            if(reference.USE_CODE_COLOURS) {
              pg.fill(getCorrectColour(getPShapeFillColor(drawShape)));
            }
            
            // draw the sucker
            pg.shape(drawShape, 0, 0);
            
            pg.popMatrix();
          }
          catch(Exception e)  {
            // nom nom nom 
          }
      }
      pg.endDraw();

      tint(255, 255 - (200 * randomModifier));
      image(pg, 0, 0);
      tint(255, 255);
    }

    popMatrix();
  }

  /** 
   * COLOUR SELECTION
   */
  public void updateColourSelection() {
    found1 = 0xff000000;
    found2 = 0xff000000;
    found3 = 0xff000000;
    found4 = 0xff000000;
    found5 = 0xff000000;
    found6 = 0xff000000;
    found7 = 0xff000000;
    found8 = 0xff000000;
    found9 = 0xff000000;
    found10 = 0xff000000;

    int rand = PApplet.parseInt(random(cols.length));
    col1 = cols[rand][0];
    col2 = cols[rand][1];
    col3 = cols[rand][2];
    col4 = cols[rand][3];
    col5 = cols[rand][4];

    colsTempList[0] = col1;
    colsTempList[1] = col2;
    colsTempList[2] = col3;
    colsTempList[3] = col4;
    colsTempList[4] = col5;
  }

  int returnColour;

  // TODO: this is fucking horrible, make not horrible
  public int getCorrectColour(int inCol) {
    if(found1 == 0xff000000 || found1 == inCol) { found1 = inCol; return col1; }
    if(found2 == 0xff000000 || found2 == inCol) { found2 = inCol; return col2; }
    if(found3 == 0xff000000 || found3 == inCol) { found3 = inCol; return col3; }
    if(found4 == 0xff000000 || found4 == inCol) { found4 = inCol; return col4; }
    if(found5 == 0xff000000 || found5 == inCol) { found5 = inCol; return col5; }
    if(found6 == 0xff000000 || found6 == inCol) { found6 = inCol; return col1; }
    if(found7 == 0xff000000 || found7 == inCol) { found7 = inCol; return col2; }
    if(found8 == 0xff000000 || found8 == inCol) { found8 = inCol; return col3; }
    if(found9 == 0xff000000 || found9 == inCol) { found9 = inCol; return col4; }
    if(found10 == 0xff000000 || found10 == inCol) { found10 = inCol; return col5; }

    return 0xffFF0000;
    //return colsTempList[int(random(colsTempList.length))];
  }

  public int getPShapeFillColor(final PShape sh) {
    try {
      final java.lang.reflect.Field f = 
        PShape.class.getDeclaredField("fillColor");
  
      f.setAccessible(true);
      return f.getInt(sh);
    }
 
    catch (ReflectiveOperationException cause) {
      throw new RuntimeException(cause);
    }
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
