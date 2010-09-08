package com.unicellularcomic.PshInProcessing;

import processing.core.PConstants;

class PausePlayButton {
	VisualSymbolicRegression parent;
	
	int x,y;
	int W = 40;
	
	PausePlayButton(VisualSymbolicRegression inParent){
		parent = inParent;
	}
	
	PausePlayButton(VisualSymbolicRegression inParent, int inX, int inY){
		parent = inParent;
		x = inX;
		y = inY;
	}
	
	void setPosition(int inX, int inY){
		x = inX;
		y = inY;
	}
	
	// Draw the button
	void render(){
		parent.rectMode(PConstants.CORNER);
		parent.stroke(255);
		parent.fill(50);
		parent.rect(x, y, W, W);
		
		if(parent.terminated){
			parent.fill(240,0,0);
			parent.noStroke();
			parent.rect(x + (W / 4), y + (W / 4), W / 2, W / 2);
		}
		else if(parent.paused){
			// Display green triangle
			parent.fill(0,255,0);
			parent.noStroke();
			
			int tl = (int) (W * 0.6);
			int th = (int) (tl * 0.866);

			parent.triangle(x + ((W - th) / 2), y + ((W - tl) / 2),
					x + ((W - th) / 2), y + ((W + tl) / 2),
					x + ((W + th) / 2), y + (W / 2));
		}
		else{
			// Display two white bars
			parent.fill(255);
			parent.noStroke();
			
			parent.rectMode(PConstants.CENTER);
			parent.rect(x + (W * (2f/5f)), y + W / 2, W / 10, W * (3f/5f));
			parent.rect(x + (W * (3f/5f)), y + W / 2, W / 10, W * (3f/5f));
			parent.rectMode(PConstants.CORNER);
		}
		
	}
	
	boolean clicked(int mx, int my) {
		if(x <= mx && mx <= x + W && 
				y <= my && my <= y + W){
			parent.paused = !parent.paused;
			render();
			return true;
		}
		return false;
	}
	
}
