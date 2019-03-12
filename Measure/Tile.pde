import org.gicentre.handy.*;
import ch.bildspur.postfx.builder.*;
import ch.bildspur.postfx.pass.*;
import ch.bildspur.postfx.*;

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

  int current = 5;
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

  int ANIM_STEP = 0;
  int CURRENT_STEP = 0;
  int DRAW_STEPS = 0;
  int PENCIL_STEPS = 0;
  int PEN_STEPS = 0;
  int COLOUR_STEPS = 0;

  String animationDirection = "up";

  int halfWidth = width/2;
  int roughness = 6;
  float randomModifier = 0.5;
  int mashRoughness = 100;

  int holdCount = 0;
  int holdThreshold = 150; // frames to keep the final design on for

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

  void updateValue(float val) {
    //doPositionCheck(width * val);
  }

  void loadSVGs() {
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

  void updateSketch() {
    if(current < max) 
      current++;
    else
      current = 1;

    loadSVGs();
  }

  void createDrawingTools() {
    pencil = HandyPresets.createPencil(reference);
    pencil.setGraphics(surface);
    pencil.setStrokeWeight(1);
    pencil.setRoughness(0.1);
    pencil.setUseSecondaryColour(false);

    pen = HandyPresets.createMarker(reference);
    pen.setGraphics(surface);
    pen.setRoughness(1);

    if(reference.INVERT_COLOURS)
      pen.setStrokeColour(255);
  }

  void populateBase() {
    pencilShapes = new PShape[base.getChildCount()];

    for (int i = 0; i < base.getChildCount(); i++) {
      PShape shape = base.getChild(i);
      pencilShapes[i] = shape;
    }
  }

  void populateOverlay() {
    penShapes = new PShape[overlay.getChildCount()];

    for (int i = 0; i < overlay.getChildCount(); i++) {
      PShape shape = overlay.getChild(i);
      penShapes[i] = shape;
    }
  }

  void draw() {
    drawColours();

    surface.beginDraw();
    surface.noFill();
    surface.clear();
    surface.ellipseMode(CORNER);
    surface.translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    drawBase();
    drawOverlay();

    surface.endDraw();
    
    image(surface, xPos - (width*0.5), yPos - (height*0.5));

    if(!reference.USE_OSC && frameCount % 1 == 0)
      calculateAnimation();

    doPositionCheck(mouseX);

    //fx.render()
    //.vignette(0.5 * randomModifier, 0.6 * randomModifier)
    //.compose();

    if(reference.DEBUG_MODE)
      updateReadout();
  }

  void doPositionCheck(float input) {
    if(input < width/2) {
      randomModifier = 1-(input / halfWidth);
    } 
    else {            
      randomModifier = ((input - halfWidth) / halfWidth);
    }

    pencil.setRoughness((randomModifier * roughness) + 0.1); // fixes circle render bug
    pencil.setStrokeWeight(1 - randomModifier);
    pen.setRoughness((randomModifier * roughness));
    pen.setStrokeWeight(2 - (randomModifier * 2));
  }

  void calculateAnimation() {
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

  void updateReadout() {
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
  void drawBase() {
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
  void drawOverlay() {
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

  float mash(float in) {
    return random(in-(mashRoughness*randomModifier), in+(mashRoughness*randomModifier));
  }

  /**
   * COLOURS
   */  
  void drawColours() {
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
          try {
            pg.pushMatrix();
            pg.translate(mash(0) * 2, mash(0) * 2);
            pg.shape(colours.getChild(i), 0, 0); 
            pg.popMatrix();
          }
          catch(NullPointerException e)  {
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
}
