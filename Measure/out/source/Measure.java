import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import at.mukprojects.console.*; 
import oscP5.*; 
import netP5.*; 
import codeanticode.syphon.*; 
import ddf.minim.analysis.*; 
import ddf.minim.*; 
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
 * THE MEASURE OF ALL THINGS
 * Dan Hett (hellodanhett@gmail.com)
 * 
 * Created for the "Sheherezade" residency that I took part in, 
 * on location in Lahore, Pakistan in March 2019.
 *
 * The code is heavily commented for clarity, but is 
 * fairly messy as much of it was written either on
 * aeroplanes or on rooftops in Lahore, or hotel rooms. 
 * 
 * Distributed under the Do What The Fuck You Want public license.
 */





Tile tile;
Console console;
float rectSize = 800;
float scaleFactor = 0.9f;
float realRectSize = rectSize * scaleFactor;

Boolean DEBUG_MODE = false;
Boolean INVERT_COLOURS = true; // set to true for black background with white lines
Boolean USE_CODE_COLOURS = true; // set to true to ignore the AI cols and generate at runtime

SyphonServer server;




Minim minim;
AudioInput in;
FFT fft;

public void setup() {
  //fullScreen(P2D);
  frameRate(60);
  

  surface.setTitle("THE MEASURE OF ALL THINGS");

  tile = new Tile(this, width/2, height/2, scaleFactor);

  server = new SyphonServer(this, "Measure Of All Things");
  
  minim = new Minim(this);

  in = minim.getLineIn(Minim.STEREO, 2048);
  fft = new FFT(in.bufferSize(), 44100);
}

public void draw() {
  if (INVERT_COLOURS)
    background(0);
  else
    background(255);

  if (DEBUG_MODE) {
    noFill();
    stroke(255, 0, 0);
    rect(width / 2 - realRectSize / 2, height / 2 - realRectSize / 2, rectSize * scaleFactor, rectSize * scaleFactor);
  }

  tile.draw();
  
  server.sendScreen();
  
  handleAudioInput();
}

// ranges
int low = 100;
int med = 300;
int high = 800;

// visual multiplier
int mult = 100;

// usable output and threshold
float average = 0.0f;
float step = 0.05f;
float threshold = 0.01f;

public void handleAudioInput() {
  fft.forward(in.mix);
  
  average = (fft.getBand(low) + fft.getBand(med) + fft.getBand(high)) / 3;
  //println(average);
  
  if(average > threshold) 
    tile.enableBuild();
  else
    tile.disableBuild();
  
  if(DEBUG_MODE) {
    fill(255,0,0);
    rect(0, 0, fft.getBand(low) * mult, 50);
    
    fill(0,255,0);
    rect(0, 50, fft.getBand(med) * mult, 50);
    
    fill(0,0,255);
    rect(0, 100, fft.getBand(high) * mult, 50);
    
    noFill();
    stroke(255);
    for(int i = 0; i < fft.specSize(); i++)
    {
      line(i, height, i, height - fft.getBand(i) * mult);
    }   
  }
}

public void keyPressed() {
  if(keyCode == 32) 
    toggleDebug();

  // go up and down the patterns
  if(keyCode == RIGHT)
    tile.nextPattern();
  if(keyCode == LEFT)
    tile.prevPattern();

  // increase or decrease the threshold for audio triggering
  if(keyCode == UP) {
    threshold += step;
    println("Moving threshold to " + threshold);
  }

  if(keyCode == DOWN) {
    if(threshold > 0.01f) {
      threshold -= step;
      println("Moving threshold to " + threshold);
    }
  }
}

public void toggleDebug() {
  DEBUG_MODE = !DEBUG_MODE;
}
/**
 * THE MEASURE OF ALL THINGS
 * Dan Hett (hellodanhett@gmail.com)
 * 
 * Created for the "Sheherezade" residency that I took part in, 
 * on location in Lahore, Pakistan in March 2019.
 *
 * The code is heavily commented for clarity, but is 
 * fairly messy as much of it was written either on
 * aeroplanes or on rooftops in Lahore, or hotel rooms. 
 * 
 * Distributed under the Do What The Fuck You Want public license.
 */





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

  // codes returned when we check for what the child node is in the SVG's
  int SVG_LINE = 4; 
  int SVG_CIRCLE = 31;

  // counters. change 'max' if more patterns are added, obviously
  int current = 1;
  int trans = 1;
  int max = 6;

  // draw surfaces for render passes
  PGraphics surface;
  PGraphics pg;

  // big bunch of clunky timers that control the animation steps. filth, sorry
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

  // counters for the progress of each animation component 
  int ANIM_STEP = 0;
  int CURRENT_STEP = 0;
  int DRAW_STEPS = 0;
  int PENCIL_STEPS = 0;
  int PEN_STEPS = 0;
  int COLOUR_STEPS = 0;

  String animationDirection = "up";

  // messiness, used when distorting the artwork
  int halfWidth = width/2;
  int roughness = 6;
  float randomModifier = 0;
  int mashRoughness = 1;

  // frames to keep the final design on for. change holdThreshold for longer/shorter
  int holdCount = 0;
  int holdThreshold = 10;

  // colours! sampled from various sources. 
  // see: https://www.instagram.com/p/Bu6Y5WeHmYK/
  /*
  color[][] cols = { 
    {#262e69, #44a5be, #15624c, #8b7350, #bca99d}, 
    {#cb6149, #ce642e, #b19077, #f3e5be, #613839}, 
    {#e2a6a6, #4abed3, #228f9c, #c52353, #ffddb5}, 
    {#7ab191, #35334b, #dfa834, #e8d7bb, #f9caa5}, 
    {#ffefe4, #a38976, #c92027, #0d886d, #414da0}, 
    {#d5d2c9, #63beb7, #989dc3, #283679, #9f5d3d}, 
    {#ced2db, #cda32c, #2e56a6, #49aa7c, #965924}, 
    {#05698f, #0382a6, #49896b, #d1aa49, #699097}, 
    {#f04f3c, #8180bd, #f1e8e1, #ffd79e, #3b5233}, 
    {#efe8de, #bc892c, #272a6b, #ca2127, #7c3d21}, 
    {#6b83b4, #56a57e, #b5976a, #27326f, #fef5f9}, 
    {#5481ae, #e45625, #f16577, #0e2917, #ede8eb}, 
    {#b76728, #40617a, #eebf39, #c5cad8, #d46b34}, 
    {#e6e5e1, #a66227, #2c2c2e, #c8ae90, #a67e3f}, 
    {#017ea9, #2d2b71, #e9e1cc, #44999b, #0989ae}
  };
  */

  int[][] cols = { 
    {0xffFFFFFF, 0xffFFFFFF, 0xffFFFFFF, 0xffFFFFFF, 0xffFFFFFF}
  };

  // input colours from the artwork
  int found1, found2, found3, found4, found5, found6, found7, found8, found9, found10;

  // output colours
  int col1, col2, col3, col4, col5;
  int[] colsTempList = { col1, col2, col3, col4, col5};
  
  Boolean canBuild = false;

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

  public void nextPattern() {
    if(trans < max)
      trans++;
  }

  public void prevPattern() {
    if(trans > 0)
      trans--;
  }

  public void updateSketch() {
    // SEQUENTIAL
    //if (current < max) 
      //current++;
   // else
      //current = 1;

    // RANDOM
    //current = int(random(max) + 1);
    //println("SWITCHING TO " + current);

    // MANUAL
    if(current != trans)
      current = trans;

    loadSVGs();
  }

  public void createDrawingTools() {
    pencil = HandyPresets.createPencil(reference);
    pencil.setGraphics(surface);
    pencil.setStrokeWeight(1);
    pencil.setStrokeColour(150);
    pencil.setRoughness(0.1f);
    pencil.setUseSecondaryColour(false);

    pen = HandyPresets.createMarker(reference);
    pen.setStrokeWeight(3);
    pen.setGraphics(surface);
    pen.setRoughness(1);

    if (reference.INVERT_COLOURS)
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

    //if (reference.INTERACTION_MODE == "wobble")
    if(canBuild)
      calculateAnimation();

    //if(!reference.USE_OSC) 
    //doPositionCheck(mouseX); // input here
    //else
    //doPositionCheck(mouseX);

    //fx.render()
    //.vignette(0.5 * randomModifier, 0.6 * randomModifier)
    //.compose();

    if (reference.DEBUG_MODE)
      updateReadout();

    //saveFrame();
  }

  public void doPositionCheck(float input) {
    if (input < width/2) {
      randomModifier = 1-(input / halfWidth);
    } else {            
      randomModifier = ((input - halfWidth) / halfWidth);
    }

    pencil.setRoughness((randomModifier * roughness) + 0.1f); // fixes circle render bug when zero
    //pencil.setStrokeWeight(1.5 - randomModifier);
    pencil.setStrokeWeight(2);
    pen.setRoughness((randomModifier * roughness));
    pen.setStrokeWeight(2 - (randomModifier * 2));
  }

  public void calculateAnimation() {
    if (animationDirection == "up") {
      if (ANIM_STEP < (DRAW_STEPS-1)) {
        ANIM_STEP++;
      } else {
        if (holdCount < holdThreshold) {
          holdCount++;
        } else {
          holdCount = 0;
          animationDirection = "down";
        }
      }
    } else {
      if (ANIM_STEP > 0) {
        ANIM_STEP-=20;
      } else {
        updateSketch();
        animationDirection = "up";
      }
    }

    CURRENT_STEP = ANIM_STEP;
  }

  public void updateReadout() {
    fill(255, 50, 0);
    text("THE MEASURE OF ALL THINGS", 50, 60);

    if (reference.INVERT_COLOURS)
      fill(255, 255, 255);
    else 
      fill(0, 0, 0);

    text("- - - - - - - - - - - - -", 50, 80);
    text("FPS: " + round(frameRate) + "     STEP: " + CURRENT_STEP + " / " + DRAW_STEPS, 50, 100);
    
    if(current != trans) 
      text("Current pattern: " + current + "/" + max + ". MOVING TO " + trans + " NEXT.", 50, 120);
    else
      text("Current pattern: " + current + "/" + max, 50, 120);

    fill(255);
    text("- - - - - - - - - - - - -", 50, 140);

    if(reference.average > reference.threshold) {
      fill(0, 255, 0);
    } 
    else {
      fill(255, 0, 0);
    }

    text("AV. VOLUME: " + reference.average, 50, 180);
    text("THRESHOLD:  " + reference.threshold, 50, 200);

    noFill();
  }


  /**
   * PENCIL
   */
  public void drawBase() {
    pencilmaxSteps = pencilShapes.length;

    int limit = pencilmaxSteps;

    if (CURRENT_STEP < pencilmaxSteps) {
      limit = CURRENT_STEP;
    }

    for (int i = 0; i < limit; i++) {
      //if(frameCount % 2 == 0)
      pencil.setSeed(1234);

      try {
        int kind = pencilShapes[i].getKind();
        params = pencilShapes[i].getParams();

        if (kind == SVG_LINE) {
          pencil.line(
            mash(params[0]) * scaleFactor, 
            mash(params[1]) * scaleFactor, 
            mash(params[2]) * scaleFactor, 
            mash(params[3]) * scaleFactor
            );
        } else if (kind == SVG_CIRCLE) {
          pencil.ellipse(
            params[0] * scaleFactor, 
            params[1] * scaleFactor, 
            params[2] * scaleFactor, 
            params[3] * scaleFactor
            );
        }
      }
      catch(Exception e) {
        // nom nom nom
      }
    }
  }

  /**
   * PEN
   */
  public void drawOverlay() {
    maxSteps = penShapes.length;

    int limit = PEN_STEPS;

    if (CURRENT_STEP > PENCIL_STEPS && CURRENT_STEP < PENCIL_STEPS + maxSteps) {
      limit = CURRENT_STEP - PENCIL_STEPS;
    } else if (CURRENT_STEP < PENCIL_STEPS + 1) {
      limit = 0;
    }

    for (int i = 0; i < limit; i++) {
      pen.setSeed(1234);

      int kind = penShapes[i].getKind();
      params = penShapes[i].getParams();

      if (kind == SVG_LINE) {
        pen.line(
          mash(params[0]) * scaleFactor, 
          mash(params[1]) * scaleFactor, 
          mash(params[2]) * scaleFactor, 
          mash(params[3]) * scaleFactor
          );
      }
    }
  }

  // used to quickly distort things. bit gross but works great, fight me
  public float mash(float in) {
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

    if (CURRENT_STEP > (PENCIL_STEPS+PEN_STEPS) && CURRENT_STEP < (PENCIL_STEPS+PEN_STEPS) + COLOUR_STEPS) {
      limit = CURRENT_STEP - PENCIL_STEPS - PEN_STEPS;
    } else if (CURRENT_STEP < PEN_STEPS + PENCIL_STEPS + 1) {
      limit = 0;
    }

    if (reference.USE_CODE_COLOURS)
      colours.disableStyle();

    if (CURRENT_STEP > PENCIL_STEPS+PEN_STEPS) {
      pg.beginDraw();
      pg.clear();
      pg.noStroke();
      for (int i = 0; i <= limit; i++) {  
        try { // LOL added this catch while projecting on the side of the building. works tho.
          pg.pushMatrix();
          pg.translate(mash(0) * 2, mash(0) * 2);

          // make the PShape
          drawShape = colours.getChild(i);

          // if needed, re-colour it
          if (reference.USE_CODE_COLOURS) {
            pg.fill(getCorrectColour(getPShapeFillColor(drawShape)));
          }

          // draw the sucker
          pg.shape(drawShape, 0, 0);

          pg.popMatrix();
        }
        catch(Exception e) {
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
    if (found1 == 0xff000000 || found1 == inCol) { 
      found1 = inCol; 
      return col1;
    }
    if (found2 == 0xff000000 || found2 == inCol) { 
      found2 = inCol; 
      return col2;
    }
    if (found3 == 0xff000000 || found3 == inCol) { 
      found3 = inCol; 
      return col3;
    }
    if (found4 == 0xff000000 || found4 == inCol) { 
      found4 = inCol; 
      return col4;
    }
    if (found5 == 0xff000000 || found5 == inCol) { 
      found5 = inCol; 
      return col5;
    }
    if (found6 == 0xff000000 || found6 == inCol) { 
      found6 = inCol; 
      return col1;
    }
    if (found7 == 0xff000000 || found7 == inCol) { 
      found7 = inCol; 
      return col2;
    }
    if (found8 == 0xff000000 || found8 == inCol) { 
      found8 = inCol; 
      return col3;
    }
    if (found9 == 0xff000000 || found9 == inCol) { 
      found9 = inCol; 
      return col4;
    }
    if (found10 == 0xff000000 || found10 == inCol) { 
      found10 = inCol; 
      return col5;
    }

    return 0xffFF0000; // if this colours displays, something went wrong
    //return colsTempList[int(random(colsTempList.length))]; // or randomise for lols
  }

  // hacky way to get the fill colour from an imported SVG shape. 
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
  
  public void enableBuild() {
     canBuild = true; 
  }
  
  public void disableBuild() {
     canBuild = false; 
  }
}
  public void settings() {  size(800,800,P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Measure" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
