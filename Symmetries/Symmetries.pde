import org.gicentre.handy.*;

HandyRenderer pencil;
HandyRenderer pen;
PShape base;
PShape overlay;

PShape[] pencilShapes;
PShape[] penShapes;

int SVG_LINE = 4;
int SVG_CIRCLE = 31;

void setup() {
  fullScreen();
  //size(1024, 768);
  background(255);

  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");

  pencil = HandyPresets.createPencil(this);
  //h1.setStrokeWeight(0.5);
  pencil.setRoughness(0.1);

  pen = HandyPresets.createMarker(this);

  populateBase();
  populateOverlay();
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
  drawOverlay();
  drawBase();
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
      println(pencilShapes[pencilcurrentSteps].getKind());
      println("---");

      if (pencilShapes[pencilcurrentSteps].getKind() == SVG_LINE) {
        pencil.line(
          pencilShapes[pencilcurrentSteps].getParam(0), 
          pencilShapes[pencilcurrentSteps].getParam(1), 
          pencilShapes[pencilcurrentSteps].getParam(2), 
          pencilShapes[pencilcurrentSteps].getParam(3)
          );
      }
      else if (pencilShapes[pencilcurrentSteps].getKind() == SVG_CIRCLE) {
        ellipseMode(CORNER);
        noFill();
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
