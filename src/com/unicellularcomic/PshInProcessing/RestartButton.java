package com.unicellularcomic.PshInProcessing;

import processing.core.PConstants;

class RestartButton {
	VisualSymbolicRegression parent;
	
	int x,y;
	int W = 90;
	int H = 40;
	
	String displayText = "RESTART";
	
	RestartButton(VisualSymbolicRegression inParent){
		parent = inParent;
	}
	
	RestartButton(VisualSymbolicRegression inParent, int inX, int inY){
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
		parent.rect(x, y, W, H);
		
		parent.textAlign(PConstants.CENTER);
		parent.fill(0,200,0);
		parent.text(displayText, x + (W/2), y + 27);
		parent.textAlign(PConstants.LEFT);
		
	}
	
	boolean clicked(int mx, int my) {
		if(x <= mx && mx <= x + W && 
				y <= my && my <= y + H){
			return true;
		}
		return false;
	}
	
}
