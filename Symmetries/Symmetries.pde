import org.gicentre.handy.*;

HandyRenderer pencil;
HandyRenderer pen;
PShape base;
PShape overlay;

PShape[] penShapes;

void setup() {
  //fullScreen();
  size(1024, 768);
  background(255);
  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");

  pencil = HandyPresets.createPencil(this);
  //h1.setStrokeWeight(0.5);
  pencil.setRoughness(0);

  pen = HandyPresets.createMarker(this);

  drawBase();
  populateOverlay();
  //drawOverlay();
}

void drawBase() {
  ellipseMode(CENTER);
  noFill();

  // draw circles and outer square
  pencil.ellipse(width/2, height/2, 800, 800);

  pushMatrix();
  translate(width/2 - 400, height/2 - 400);
  for (int i = 0; i < base.getChildCount(); i++) {
    PShape shape = base.getChild(i);

    try {
      pencil.line(
        shape.getParams()[0], 
        shape.getParams()[1], 
        shape.getParams()[2], 
        shape.getParams()[3]
        );
    } 
    catch (Exception e) {
      //e.printStackTrace();
    }
  }
  popMatrix();
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
}


int timer = 4;
int currentTime = 0;
int currentSteps = -20;
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
