package com.unicellularcomic.PshInProcessing;

import java.util.ArrayList;
import org.gwoptics.graphics.graph2D.traces.Blank2DTrace;

import processing.core.*;

class Point2D {
	public float X, Y;

	public Point2D(float x, float y) {
		X = x;
		Y = y;
	}
}

abstract class ScatterTrace extends Blank2DTrace {
	private ArrayList<Point2D> _data;
	protected float radiusMultiplier = 0.01f;
	
	protected float xRadius;
	protected float yRadius;

	public ScatterTrace() {
		_data = new ArrayList<Point2D>();
	}
	
	public void setRadius(float xAxisLength, float yAxisLength){
		xRadius = xAxisLength * radiusMultiplier;
		yRadius = yAxisLength * radiusMultiplier;
	}

	public void addPoint(float x, float y) {
		_data.add(new Point2D(x, y));
	}
	
	public void clearPoints(){
		_data.clear();
	}

	abstract protected void drawPoint(Point2D p, PGraphics canvas);

	public void TraceDraw(PGraphics canvas) {
		if (_data != null) {
			for (int i = 0; i < _data.size(); i++) {
				drawPoint(_data.get(i), canvas);
			}
		}
	}
}

class ScatterTraceCrossWhite extends ScatterTrace {
	protected void drawPoint(Point2D p, PGraphics canvas) {
		canvas.pushStyle();
		canvas.stroke(255);
		canvas.line(p.X - xRadius, p.Y, p.X + xRadius, p.Y);
		canvas.line(p.X, p.Y - yRadius, p.X, p.Y + yRadius);
		canvas.popStyle();
	}
}

class ScatterTraceCrossColor extends ScatterTrace {
	private int _color;

	public ScatterTraceCrossColor(int inColor) {
		super();
		_color = inColor;
	}

	protected void drawPoint(Point2D p, PGraphics canvas) {
		canvas.pushStyle();
		canvas.stroke(_color);
		canvas.line(p.X - xRadius, p.Y, p.X + xRadius, p.Y);
		canvas.line(p.X, p.Y - yRadius, p.X, p.Y + yRadius);
		canvas.popStyle();
	}
}

class ScatterTraceCircleColor extends ScatterTrace {
	private int _color;

	public ScatterTraceCircleColor(int inColor) {
		super();
		_color = inColor;
	}

	protected void drawPoint(Point2D p, PGraphics canvas) {
		canvas.pushStyle();
		canvas.ellipseMode(PConstants.RADIUS);
		canvas.stroke(_color);
		canvas.noFill();
		canvas.ellipse(p.X, p.Y, xRadius, yRadius);
		canvas.popStyle();
	}
}

class ScatterTraceLineColor extends ScatterTrace {
	private int _color;
	private Point2D previousP;

	public ScatterTraceLineColor(int inColor) {
		super();
		_color = inColor;
		previousP = null;
	}
	
	public void TraceDraw(PGraphics canvas) {
		previousP = null;
		super.TraceDraw(canvas);
	}

	protected void drawPoint(Point2D p, PGraphics canvas) {
		canvas.pushStyle();
		canvas.stroke(_color);
		if(previousP == null){
			canvas.point(p.X, p.Y);
		}
		else{
			canvas.line(p.X, p.Y, previousP.X, previousP.Y);
		}
		canvas.popStyle();
		previousP = p;
	}
}
