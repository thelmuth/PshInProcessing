package com.unicellularcomic.PshInProcessing;

import java.text.DecimalFormat;

import processing.core.*;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.LabelPos;
import org.spiderland.Psh.*;
import org.spiderland.Psh.ProbClass.FloatSymbolicRegression;

public class VisualSymbolicRegression extends PApplet {
	private static final long serialVersionUID = 1L;

	GA ga;
	
	Graph2D errorGraph;
	Graph2D historicalGraph;

	ScatterTrace testCasesTrace;
	ScatterTrace bestProgTrace;
	ScatterTrace bestFitnessTrace;
	
	PausePlayButton pausePlayButton;
	RestartButton restartButton;

	PFont font18;
	PFont font28;
	
	PushGPIndividual bestIndividual;
	
	boolean paused;
	boolean terminated;
	boolean restart;
	
	float gaXMin, gaXMax, gaYMin, gaYMax;
	float historicalXMax, historicalYMax;
	
	static int DIVIDER1X = 250;
	static int DIVIDER2X = 750;
	
	static int CROSSES_COLOR;
	static int CIRCLES_COLOR;
	static int HISTORICAL_COLOR;
	
	String targetFunctionString;

	public void setup() {
		size(1300, 710);
		background(0);
		paused = false;
		terminated = false;
		
		historicalXMax = 0;
		historicalYMax = 0;
		
		font18 = loadFont("Leelawadee-Bold-18.vlw");
		font28 = loadFont("Leelawadee-Bold-28.vlw");
		
		CROSSES_COLOR = color(0, 255, 0);
		CIRCLES_COLOR = color(255, 240, 0);
		HISTORICAL_COLOR = color(255, 0, 80);

		try {
            //String[] parameterStrings = loadStrings("DifficultFloatReg1.pushgp");
            String[] parameterStrings = loadStrings("floatreg0.pushgp");
            //String[] parameterStrings = loadStrings("floatreg2.pushgp");
            
            String parameters = "";
            for(int i = 0; i < parameterStrings.length; i++){
              parameters += parameterStrings[i] + '\n';
            }

            ga = GA.GAWithParameters(Params.Read(parameters));

			if (!(ga instanceof PushGP)) {
				throw new Exception(
						"ERROR: The problem-class must inherit from PushGP.");
			}

		} catch (Exception e) {
			println("There was a problem:");
			println(e);
		}

		// Buttons
		pausePlayButton = new PausePlayButton(this);
		restartButton = new RestartButton(this);
		
		int buttonsSpacing = 10;
		int buttonsWidth = pausePlayButton.W + restartButton.W + buttonsSpacing;
		int buttonsX = (DIVIDER1X - buttonsWidth) / 2;
		int buttonsY = 570;

		pausePlayButton.setPosition(buttonsX, buttonsY);
		restartButton.setPosition(
				buttonsX + pausePlayButton.W + buttonsSpacing, buttonsY);
		
		// Setup and add traces to the error graph
		setupErrorGraph();
		setupErrorGraphTraces();
		
		// Setup and add traces to the historical graph
		setupHistoricalGraph();
		setupHistoricalGraphTraces();
		
		// Get target function string
		targetFunctionString = ((PushGP)ga).GetTargetFunctionString();

	}

	public void draw() {
		if(restart){
			restart = false;
			setup();
			return;
		}

		try {
			ga.Run(1);
		} catch (Exception e) {
			println(e);
		}
		if (ga.Terminate()) {
			terminated = true;
			text("Terminated", 10, 80);
			noLoop();
		}
		
		bestIndividual = (PushGPIndividual) ga.GetBestIndividual();
		
		// Start the drawing
		background(0);

		// Top section
		fill(255);
		textFont(font28);
		textAlign(CENTER);
		text("Symbolic Regression using Psh", width / 2, 35);

		if(!targetFunctionString.isEmpty()){
			textFont(font18);
			text("Target Function: " + targetFunctionString, width / 2, 60);
		}
		
		// Top divider
		fill(160, 180, 220);
		noStroke();
		rectMode(CENTER);
		rect(width / 2, 80, width - 40, 4);
		
		// Dividers
		fill(160, 180, 220);
		noStroke();
		rectMode(CENTER);
		rect(DIVIDER1X, 365, 4, 530);
		rect(DIVIDER2X, 365, 4, 530);
		
		// Bottom divider
		fill(160, 180, 220);
		noStroke();
		rect(width / 2, 645, width - 40, 4);
		
		rectMode(CORNER);
		
		// Text at the left
		fill(255);
		textFont(font18);
		textAlign(LEFT);
		text("Generation = " + ga.GetGenerationCount(), 10, 125);
		text("Best Fitness = "
				+ new DecimalFormat("0.###").format((double) bestIndividual
						.GetFitness()), 10, 145);

		// Bottom Section
		textFont(font18);
		textAlign(LEFT);
		if(terminated){
			text("Solution Program: " + bestIndividual, 10, 660, width - 20, 50);
			
		} else{
			text("Best Program: " + bestIndividual, 10, 660, width - 20, 50);
		}
		// Render buttons
		pausePlayButton.render();
		restartButton.render();
		
		// Draw error graph
		drawErrorGraph();

		// Draw historical fitness graph
		drawHistoricalGraph();
		

	}

	public void mousePressed() {
		if(restartButton.clicked(mouseX, mouseY)){
			loop();
			restart = true;
			return;
		}
		
		if(terminated){
			noLoop();
			return;
		}
		
		if(pausePlayButton.clicked(mouseX, mouseY)){
			if (paused)
				noLoop();
			else
				loop();
		}
	}

	private void setupErrorGraph() {
		errorGraph = new Graph2D(this, 400, 400, true);
		
		// Find x and y min and max based on test cases
		findGAOptimums();
		
		errorGraph.setAxisColour(255, 255, 255);
		errorGraph.setFontColour(255, 255, 255);

		errorGraph.position.x = 300;
		errorGraph.position.y = 150;

		errorGraph.setYAxisLabel("y");
		errorGraph.setXAxisLabel("x");
		
		errorGraph.setYAxisLabelPos(LabelPos.END);
		errorGraph.setXAxisLabelPos(LabelPos.END);

		float numberOfTicksY = 5f;
		float numberOfTicksX = 5f;
		errorGraph.setYAxisTickSpacing((gaYMax - gaYMin) / (numberOfTicksY - 1) * 0.99f);
		errorGraph.setXAxisTickSpacing((gaXMax - gaXMin) / (numberOfTicksX - 1));
		
		errorGraph.setXAxisMinorTicks(4);
		errorGraph.setYAxisMinorTicks(4);
		
		errorGraph.setYAxisMin(gaYMin);
		errorGraph.setYAxisMax(gaYMax);
		errorGraph.setYAxisLabelAccuracy(2);

		errorGraph.setXAxisMin(gaXMin);
		errorGraph.setXAxisMax(gaXMax);
		errorGraph.setXAxisLabelAccuracy(2);
	}
	
	private void findGAOptimums() {
		float yfactor = 1.5f;
		
		float xmin = Float.MAX_VALUE;
		float xmax = -Float.MAX_VALUE;
		float ymin = Float.MAX_VALUE;
		float ymax = -Float.MAX_VALUE;
		
		for(GATestCase testCase : ga._testCases){
			float tcx = (Float) testCase._input;
			float tcy = (Float) testCase._output;
			
			xmin = Math.min(xmin, tcx);
			xmax = Math.max(xmax, tcx);
			ymin = Math.min(ymin, tcy);
			ymax = Math.max(ymax, tcy);
		}
	
		gaXMin = xmin;
		gaXMax = xmax;
		if(ymin > 0){
			gaYMin = ymin / yfactor;
		}
		else{
			gaYMin = ymin * yfactor;
		}
		if(ymax < 0){
			gaYMax = ymax / yfactor;
		}
		else{
			gaYMax = ymax * yfactor;
		}
	}
	
	private void setupErrorGraphTraces() {
		testCasesTrace = new ScatterTraceCrossColor(CROSSES_COLOR);
		bestProgTrace = new ScatterTraceCircleColor(CIRCLES_COLOR);
		
		errorGraph.addTrace(testCasesTrace);
		errorGraph.addTrace(bestProgTrace);
		
		testCasesTrace.setRadius(gaXMax - gaXMin, gaYMax - gaYMin);
		bestProgTrace.setRadius(gaXMax - gaXMin, gaYMax - gaYMin);

		// Add test case points
		for(GATestCase testCase : ga._testCases){
			float tcx = (Float) testCase._input;
			float tcy = (Float) testCase._output;
			testCasesTrace.addPoint(tcx, tcy);
		}
		
	}

	private void drawErrorGraph() {

		// Update best program trace
		bestProgTrace.clearPoints();

		for (GATestCase testCase : ga._testCases) {
			float tcx = (Float) testCase._input;
			float bestProgOutput = ((FloatSymbolicRegression) ga)
					.GetIndividualTestCaseResult(bestIndividual, testCase);
			bestProgTrace.addPoint(tcx, bestProgOutput);
		}

		bestProgTrace.generate();

		// Info above first graph
		textAlign(CENTER);
		fill(255);
		text("Best Program Values vs. Test Case Values", 500, 125);
		
		// Info below error graph
		noStroke();
		fill(50);
		stroke(255);
		//rect(DIVIDER1X + 50, height - 55, 400, 50);
		rect(DIVIDER1X + 50, 570, 400, 50);
		
		int crossX = DIVIDER1X + 60;
		int crossY = 585;
		int crossRadius = 5;
		
		textAlign(LEFT);
		stroke(CROSSES_COLOR);
		noFill();
		line(crossX - crossRadius, crossY, crossX + crossRadius, crossY);
		line(crossX, crossY - crossRadius, crossX, crossY + crossRadius);
		
		ellipseMode(RADIUS);
		stroke(CIRCLES_COLOR);
		ellipse(crossX, crossY + 20, crossRadius, crossRadius);
		ellipseMode(CENTER);
		
		fill(255);
		text("= Test Case", crossX + crossRadius + 5, crossY + crossRadius);
		text("= Best Program", crossX + crossRadius + 5, crossY + crossRadius + 20);
		
		errorGraph.draw();
	}

	private void setupHistoricalGraph() {
		historicalGraph = new Graph2D(this, 400, 400, true);
		
		historicalGraph.setAxisColour(255, 255, 255);
		historicalGraph.setFontColour(255, 255, 255);

		historicalGraph.position.x = 850;
		historicalGraph.position.y = 150;

		historicalGraph.setYAxisLabel("Error");
		historicalGraph.setXAxisLabel("Generation");
		
		historicalGraph.setYAxisLabelPos(LabelPos.MIDDLE);
		historicalGraph.setXAxisLabelPos(LabelPos.MIDDLE);

		//float numberOfTicksY = 5f;
		float numberOfTicksX = 5f;
		//historicalGraph.setYAxisTickSpacing(20);
		historicalGraph.setXAxisTickSpacing((ga.GetMaxGenerations()) / (numberOfTicksX - 1));
		
		historicalGraph.setXAxisMinorTicks(4);
		historicalGraph.setYAxisMinorTicks(4);
		
		historicalGraph.setYAxisMin(0);
		//historicalGraph.setYAxisMax(200);
		historicalGraph.setYAxisLabelAccuracy(1);

		historicalGraph.setXAxisMin(0);
		historicalGraph.setXAxisMax(ga.GetMaxGenerations());
		historicalGraph.setXAxisLabelAccuracy(0);
	}

	private void setupHistoricalGraphTraces() {
		bestFitnessTrace = new ScatterTraceLineColor(HISTORICAL_COLOR);
		historicalGraph.addTrace(bestFitnessTrace);
	}

	private void drawHistoricalGraph() {
		textAlign(CENTER);
		fill(255);
		text("Historical Best Program Fitnesses", 1050, 125);
		textAlign(LEFT);

		// Get fitness to add to graph
		float bestFitness = bestIndividual.GetFitness();

		setHistoricalGraphX(ga.GetGenerationCount() + 1.0f);
		setHistoricalGraphY(bestFitness * 1.1f);
		
		bestFitnessTrace.setRadius(historicalXMax, historicalYMax);

		bestFitnessTrace.addPoint(ga.GetGenerationCount(), bestFitness);
		
		bestFitnessTrace.generate();
		historicalGraph.draw();
	}

	private void setHistoricalGraphX(float newXMax) {
		historicalXMax = Math.max(historicalXMax, newXMax);
		historicalGraph.setXAxisMax(historicalXMax);
		
		if(historicalXMax < 10){
			historicalGraph.setXAxisTickSpacing(1);
			historicalGraph.setXAxisMinorTicks(0);
		}
		else if(historicalXMax < 50){
			historicalGraph.setXAxisTickSpacing(5);
			historicalGraph.setXAxisMinorTicks(4);
		}
		else if(historicalXMax < 200){
			historicalGraph.setXAxisTickSpacing(20);
			historicalGraph.setXAxisMinorTicks(3);
		}
		else if(historicalXMax < 1000){
			historicalGraph.setXAxisTickSpacing(100);
			historicalGraph.setXAxisMinorTicks(4);
		}
		else {
			historicalGraph.setXAxisTickSpacing(500);
			historicalGraph.setXAxisMinorTicks(4);
		}
	}

	private void setHistoricalGraphY(float newYMax) {
		float numberOfTicksY = 5f;
		
		historicalYMax = Math.max(historicalYMax, newYMax);
		historicalGraph.setYAxisMax(historicalYMax);
		historicalGraph.setYAxisTickSpacing(historicalYMax / (numberOfTicksY - 1));	
	}

}
