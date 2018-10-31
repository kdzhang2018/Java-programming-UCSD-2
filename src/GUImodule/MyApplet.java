package GUImodule;

import processing.core.*;

public class MyApplet extends PApplet{
	private String URL = "http://images.clipartpanda.com/tropical-beach-clipart-tropical-clip-art1.jpg";
	private PImage backgroundImg;
	
	public void setup(){
		size(300,300);
		backgroundImg = loadImage(URL, "jpg");
		
	}
	
	public void draw(){
		backgroundImg.resize(width, 0);
		image(backgroundImg, 0, 0);
		fill(255,209,0);
		ellipse(width/4, height/5, width/5, height/5);
	}
	
}
