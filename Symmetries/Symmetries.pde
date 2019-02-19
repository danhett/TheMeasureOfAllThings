import org.gicentre.handy.*;

HandyRenderer pencil;
HandyRenderer pen;
PShape base;
PShape overlay;

void setup() {
  fullScreen();
  background(255);
  base = loadShape("base.svg");
  overlay = loadShape("overlay.svg");

  pencil = HandyPresets.createPencil(this);
  //h1.setStrokeWeight(0.5);
  pencil.setRoughness(0);

  pen = HandyPresets.createMarker(this);

  drawBase();
  drawOverlay();
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
      e.printStackTrace();
    }
  }
  popMatrix();
}

void drawOverlay() {
  pushMatrix();
  translate(width/2 - 400, height/2 - 400);
  for (int i = 0; i < overlay.getChildCount(); i++) {
    PShape shape = overlay.getChild(i);

    try {
      pen.line(
        shape.getParams()[0], 
        shape.getParams()[1], 
        shape.getParams()[2], 
        shape.getParams()[3]
        );
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
  } 
  popMatrix();
}

void draw() {
  //background(255);
  //shape(svg, 0, 0, 800, 800);
}
