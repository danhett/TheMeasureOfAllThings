import org.gicentre.handy.*;

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

void setup() {
  fullScreen();
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

void loadSVGs() {
  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");
  colours = loadShape("colours.svg");
}


void createDrawingTools() {
  pencil = HandyPresets.createPencil(this);
  pencil.setStrokeWeight(1);
  pencil.setRoughness(0.1);

  pen = HandyPresets.createMarker(this);
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
void drawOverlay() {
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
void drawBase() {
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
void drawColours() {
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
