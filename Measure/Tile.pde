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
  float randomModifier = 0.5;
  int mashRoughness = 100;

  int holdCount = 0;
  int holdThreshold = 150; // frames to keep the final design on for

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

  // input colours from the artwork
  color found1, found2, found3, found4, found5, found6, found7, found8, found9, found10;
  
  // output colours
  color col1, col2, col3, col4, col5;

  color[] colsTempList = { col1, col2, col3, col4, col5};

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
   * DRAW COLOURS
   */  

  PShape drawShape; 
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

  /** 
   * COLOUR SELECTION
   */
  void updateColourSelection() {
    found1 = #000000;
    found2 = #000000;
    found3 = #000000;
    found4 = #000000;
    found5 = #000000;
    found6 = #000000;
    found7 = #000000;
    found8 = #000000;
    found9 = #000000;
    found10 = #000000;

    int rand = int(random(cols.length));
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

  color returnColour;

  // TODO: this is fucking horrible, make not horrible
  color getCorrectColour(color inCol) {
    if(found1 == #000000 || found1 == inCol) { found1 = inCol; return col1; }
    if(found2 == #000000 || found2 == inCol) { found2 = inCol; return col2; }
    if(found3 == #000000 || found3 == inCol) { found3 = inCol; return col3; }
    if(found4 == #000000 || found4 == inCol) { found4 = inCol; return col4; }
    if(found5 == #000000 || found5 == inCol) { found5 = inCol; return col5; }
    if(found6 == #000000 || found6 == inCol) { found6 = inCol; return col1; }
    if(found7 == #000000 || found7 == inCol) { found7 = inCol; return col2; }
    if(found8 == #000000 || found8 == inCol) { found8 = inCol; return col3; }
    if(found9 == #000000 || found9 == inCol) { found9 = inCol; return col4; }
    if(found10 == #000000 || found10 == inCol) { found10 = inCol; return col5; }

    return #FF0000;
    //return colsTempList[int(random(colsTempList.length))];
  }

  color getPShapeFillColor(final PShape sh) {
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
