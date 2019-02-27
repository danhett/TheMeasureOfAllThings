import org.gicentre.handy.*;

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

  Symmetries reference;
  int xPos;
  int yPos;
  float scaleFactor;

  Tile(Symmetries ref, int _xPos, int _yPos, float _scaleFactor) {
    println("[tile]");

    reference = ref;
    xPos = _xPos;
    yPos = _yPos;
    scaleFactor = _scaleFactor;

    //blendMode(MULTIPLY);

    noFill();
    ellipseMode(CORNER);


    //pg = createGraphics(int(800 * scaleFactor), int(800 * scaleFactor));
    pg = createGraphics(800, 800);

    loadSVGs();
    createDrawingTools();
    populateBase();
    populateOverlay();
  }

  void loadSVGs() {
    base = loadShape("base.svg");
    overlay = loadShape("overlay.svg");
    colours = loadShape("colours.svg");
  }


  void createDrawingTools() {
    pencil = HandyPresets.createPencil(reference);
    pencil.setStrokeWeight(0.5);
    pencil.setRoughness(0.1);

    pen = HandyPresets.createMarker(reference);
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
    //background(255);

    //if (finished)
     // background(255);

    //if (canDraw)
      drawColours();

    drawBase();
    drawOverlay();
  }


  int penciltimer = 2;
  int pencilcurrentTime = 0;
  int pencilcurrentSteps = 0;
  int pencilmaxSteps;
  void drawBase() {
    pencilmaxSteps = pencilShapes.length;

    pushMatrix();

    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    if (pencilcurrentTime < penciltimer) {
      pencilcurrentTime++;
    } else {
      pencilcurrentTime = 0;

      if (pencilcurrentSteps < pencilmaxSteps) {     
        pencilcurrentSteps++;
      }
    }

    for(int i = 0; i < pencilcurrentSteps; i++) {
        pencil.setSeed(1234);
        
        if (pencilShapes[i].getKind() == SVG_LINE) {
          pencil.line(
            pencilShapes[i].getParam(0) * scaleFactor, 
            pencilShapes[i].getParam(1) * scaleFactor, 
            pencilShapes[i].getParam(2) * scaleFactor, 
            pencilShapes[i].getParam(3) * scaleFactor
          );
        } else if (pencilShapes[i].getKind() == SVG_CIRCLE) {

          pencil.ellipse(
            pencilShapes[i].getParam(0) * scaleFactor, 
            pencilShapes[i].getParam(1) * scaleFactor, 
            pencilShapes[i].getParam(2) * scaleFactor, 
            pencilShapes[i].getParam(3) * scaleFactor
          );
        }   
      }

    popMatrix();
  }

  int timer = 2;
  int currentTime = 0;
  int currentSteps = -30;
  int maxSteps;
  void drawOverlay() {
    maxSteps = penShapes.length;

    pushMatrix();

    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    if (currentTime < timer) {
      currentTime++;
    } else {
      currentTime = 0;

      if (currentSteps < maxSteps) {     
        currentSteps++;
      }
      else {
        canDraw = true;
      }
    }

    for(int i = 0; i < currentSteps; i++) {
      pen.setSeed(1234);
      
        pen.line(
          penShapes[i].getParam(0) * scaleFactor, 
          penShapes[i].getParam(1) * scaleFactor, 
          penShapes[i].getParam(2) * scaleFactor, 
          penShapes[i].getParam(3) * scaleFactor
        );
      }

    popMatrix();
  }

  int shapetimer = 2;
  int shapecurrentTime = 0;
  int shapecurrentSteps = 0;
  int shapemaxSteps;
  void drawColours() {
    shapemaxSteps = colours.getChildCount();

    pushMatrix();
    translate(xPos - 400 * scaleFactor, yPos - 400 * scaleFactor);

    if(canDraw) {
      scale(scaleFactor, scaleFactor);

      if (shapecurrentTime < shapetimer) {
        shapecurrentTime++;
      } else {
        shapecurrentTime = 0;

        //colours.disableStyle();

        if (shapecurrentSteps < shapemaxSteps) {
          pg.beginDraw();
          //pg.fill(255, 0, 0, 100);
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
