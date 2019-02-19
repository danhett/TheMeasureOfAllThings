import org.gicentre.handy.*;

// STACK - main array with SketchLine, SketchEllipse, SketchRect and SketchText maybe
// drawSketchObject methods add them to the stack
// draw() updates each one and handles timing maybe

class Sketcher {
  Sketcher(Symmetries ref) {
    println("[sketch]");

    //markerLines = createGraphics(1024, 768);
    //pg.smooth();

    //h1 = HandyPresets.createPencil(ref);
    //h1.setStrokeWeight(0.5);
    //h1.setRoughness(1);
  }

  void drawSketchLine(int xS, int yS, int xE, int yE, int dur) {
    //h1.line(xS, xS, xE, yE);
  }

  void draw() {
  }
}
