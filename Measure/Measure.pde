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
import at.mukprojects.console.*;
import oscP5.*;
import netP5.*;
import codeanticode.syphon.*;

Tile tile;
Console console;
float rectSize = 800;
float scaleFactor = 0.9;
float realRectSize = rectSize * scaleFactor;

Boolean HOLD = true; // entry point, stops mic input until we're ready

Boolean DEBUG_MODE = false;
Boolean INVERT_COLOURS = true; // set to true for black background with white lines
Boolean USE_CODE_COLOURS = true; // set to true to ignore the AI cols and generate at runtime

SyphonServer server;

import ddf.minim.analysis.*;
import ddf.minim.*;

Minim minim;
AudioInput in;
FFT fft;

void setup() {
  //fullScreen(P2D);
  frameRate(60);
  size(800,800,P2D);

  surface.setTitle("THE MEASURE OF ALL THINGS");

  tile = new Tile(this, width/2, height/2, scaleFactor);

  server = new SyphonServer(this, "Measure Of All Things");
  
  minim = new Minim(this);

  in = minim.getLineIn(Minim.STEREO, 2048);
  fft = new FFT(in.bufferSize(), 44100);
}

void draw() {
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
float average = 0.0;
float step = 0.05;
float threshold = 0.01;

void handleAudioInput() {
  fft.forward(in.mix);
  
  average = (fft.getBand(low) + fft.getBand(med) + fft.getBand(high)) / 3;
  //println(average);
  
  if(!HOLD) {
    if(average > threshold) 
      tile.enableBuild();
    else
      tile.disableBuild();
  }
  
  if(DEBUG_MODE) {
    /*
    fill(255,0,0);
    rect(0, 0, fft.getBand(low) * mult, 50);
    
    fill(0,255,0);
    rect(0, 50, fft.getBand(med) * mult, 50);
    
    fill(0,0,255);
    rect(0, 100, fft.getBand(high) * mult, 50);
    */
    
    noFill();
    stroke(255);
    for(int i = 0; i < fft.specSize(); i++)
    {
      line(i, height, i, height - fft.getBand(i) * mult);
    }   
  }
}

void keyPressed() {
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
    if(threshold > 0.01) {
      threshold -= step;
      println("Moving threshold to " + threshold);
    }
  }

  if(keyCode == 72) {
    HOLD = !HOLD;
  }
}

void toggleDebug() {
  DEBUG_MODE = !DEBUG_MODE;
}