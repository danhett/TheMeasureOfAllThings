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

    for(int i = 0; i < currentSteps; i++) {
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
