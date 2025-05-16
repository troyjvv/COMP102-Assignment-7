// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP102 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP102 - 2025T1, Assignment 7
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.nio.file.*;
import java.io.*;

/**
 * DiskGame is a simple game where the player must blow up disks spread across
 * a shooting range.
 * The game starts with a collection of randomly placed small disks in the upper
 * half of the graphics pane, and a gun at the bottom.
 * The gun is fixed in the center of a horizontal line below the shooting range
 * and can shoot in any direction within a 180-degree radius.
 * The player fires the gun using the mouse, by releasing it within the firing
 * zone, which is limited by an arc surrounding the upper part of the gun. This
 * determines the direction of the shot.
 * If a shot hits a disk, it will damage it.
 * If the disk is damaged enough, it will explode and may damage nearby disks,
 * if they are within range.
 * The player has a limited number of shots, and the goal is to cause as much
 * damage as possible.
 * Each disk has a score based on the amount of damage it sustained—the greater
 * the damage, the higher the score. The game's total score is the sum of the
 * scores for all the disks.
 * The game ends when the player runs out of shots or when all the disks have
 * exploded.
 */

public class DiskGame{
    // Constants for the game geometry: the disks in the shooting range should
    // all be in the rectangle starting at (0,0) with a width of 500 and a
    // height of 150
    // The gun should be on the line at y = 300
    private static final double GAME_WIDTH = 500;
    private static final double SHOOTING_RANGE_Y = 150; // lowest point that a disk can be
    private static final double GUN_X = GAME_WIDTH/2;   // current x position of the gun
    private static final double GUN_Y = 300;
    private static final double SHOOTING_CIRCLE = GUN_Y-SHOOTING_RANGE_Y;

    //Constants for game logic
    private static final int DEFAULT_NUMBER_OF_SHOTS = 30;
    private static final int DEFAULT_NUMBER_OF_DISKS = 30;
    private int numShots = DEFAULT_NUMBER_OF_SHOTS;
    private int numDisks = DEFAULT_NUMBER_OF_DISKS;

    //Fields for the game state
    private double score = 0;                        // current score
    private int shotsRemaining = this.numShots;      // How many shots are left

    private ArrayList <Disk> disks = new ArrayList<Disk>(); // The list of disks

    /**
     * Sets up the user interface:
     * Set up the sliders, buttons and mouselistener
     */
    public void setupGUI(){
        /*# YOUR CODE HERE */
        UI.addSlider("Number of Disks", 1, 100, this.numDisks, this::setNumDisks);
        UI.addSlider("Number of Shots", 1, 100, this.numShots, this::setNumShots);

        UI.addButton("Restart", this::startGame);
        UI.setMouseMotionListener(this::doMouse);

        UI.addButton("Quit", UI::quit);
        UI.setDivider(0);
    }

    /**
     * Set the number of disks for the next game
     * Hint: Remember to cast to an int
     */
    public void setNumDisks(double value){
        /*# YOUR CODE HERE */
        this.numDisks = (int) value; // converts double from silder into int
    }

    /**
     * Set the number of shots for the next game
     * Hint: Remember to cast to an int
     */
    public void setNumShots(double value){
        /*# YOUR CODE HERE */
        this.numShots = (int) value;
    }

    /**
     * Set the fields of the game to their initial values,
     * Create a new list of disks
     * redraw the game
     */
    public void startGame(){ 
        this.shotsRemaining = this.numShots;
        this.score = 0;
        this.initialiseDisks();
        this.redraw();
        updateScore();
    }

    /**
     * Make a new ArrayList of disks with new disks at random positions
     * within the shooting range.
     * Remember to use the CONSTANTS
     * B-grade level: ensure than none of them are overlapping.
     */
    public void initialiseDisks(){
        /*# YOUR CODE HERE */
        this.disks = new ArrayList<Disk>();

        for (int i = 0; i < this.numDisks; i++) {
            double x = Math.random() * GAME_WIDTH; // random x for each disk between the game width
            double y = Math.random() * SHOOTING_RANGE_Y; // random y for each disk between the shooting range

            Disk d = new Disk(x, y); // make a new disk object
            this.disks.add(d); // add it to the arrarylist
            d.draw();
        }
    }

    /**
     * Respond to the mouse
     */
    public void doMouse(String action, double x, double y){
        /*# YOUR CODE HERE */
        if (action.equals("released")) {
            if (this.shotsRemaining > 0) {
                if (this.isWithinFiringZone(x, y)) {
                    this.fireShot(x, y);
                }
            } else {
                UI.println("Game over");
            }
        }
    }

    /**
     * Is the given position within the firing zone
     */
    public boolean isWithinFiringZone(double x, double y) {
        // an easy approximation is to pretend it is the enclosing rectangle.
        // It is nicer to do a little bit of geometry and get it right
        return (x >= GUN_X - SHOOTING_CIRCLE / 2) && (y >= GUN_Y - SHOOTING_CIRCLE / 2)
                && (x <= GUN_X + SHOOTING_CIRCLE / 2) && (y <= GUN_Y);
    }

    /**
     * The core mechanic of the game is to fire a shot.
     * - Update the number of shots remaining.
     * - Move the shot up the screen in the correct direction from the gun, step by step, until 
     *   it either goes off the screen or hits a disk.
     *   The shot is constantly redrawn as a line from the gun to its current position.
     * - If the shot hits a disk,
     *   - it damages the disk, 
     *   - If the disk is now broken, then
     *     it will damage its neighbours
     *     (ie, all the other disks within range will be damaged also)
     *   - it exits the loop.
     * - Redraw the game
     * - Finally, update the score,
     * - If the game is now over,  print out the score 
     * (You should define additional methods - don't do it all in one big method!)
     */
    public void fireShot(double x, double y){
        this.shotsRemaining--; //We now have one less shot left
        double shotPosX = GUN_X; //The shot starts at the top of the gun
        double shotPosY = GUN_Y; //The shot starts at the top of the gun
        // Calculate the step_X value for a step_y of -1
        double step_X = (GUN_X-x)/(y-GUN_Y);
        UI.setColor(Color.black);
        while (!this.isShotOffScreen(shotPosX, shotPosY)){ 
            shotPosY -= 1;
            shotPosX += step_X;
            UI.drawLine(GUN_X, GUN_Y, shotPosX, shotPosY);
            //check if it hits a disk... 
            /*# YOUR CODE HERE */
            Disk hit = getHitDisk(shotPosX, shotPosY);
            if (hit != null) {
                hit.damage();// if hit - damage
                if (hit.isBroken()) {
                    this.damageNeighbours(hit); // if disk is broken - damage neighbors
                }
                break; // stop shot once hits disk
            }
            UI.sleep(1);
        }
        this.redraw();
        this.updateScore();
        // If game is over, print out the score
        if ((this.haveAllDisksExploded() || this.shotsRemaining < 1)) {
            UI.setColor(Color.red);
            UI.setFontSize(24);
            UI.drawString("Your final score: " + this.score, GAME_WIDTH * 1.0 / 3.0, SHOOTING_RANGE_Y * 1.3);
        }
    }

    /**
     * Is the shot out of the screen
     */
    public boolean isShotOffScreen(double x, double y) {
        return (x < 0 || y < 0 || x > GAME_WIDTH);
    }    

    /**
     * Does the given shot hit a disk? If yes, return that disk. Else return null
     * Useful when firing a shot
     * Hint: use the isOn method of the Disk class
     */
    public Disk getHitDisk(double shotX, double shotY){
        /*# YOUR CODE HERE */
        for (Disk disk : this.disks) {
            if (disk.isOn(shotX, shotY)) {
                return disk;
            }
        }
        return null;
    }

    /**
     * Inflict damage on all the neighbours of the given disk
     * (ie, all disks that are within range of the disk, and are not already broken)
     * Note, it should not inflict more damage on the given disk.
     * Useful when firing a shot
     * Hint: make use of Disk class methods
     *
     * For A-grade-level, this should be able to cause a chain reaction 
     *  so that neighbours that are damaged to their limit will explode and
     *  damage their neighbours, ....
     *
     * You should use an ITERATIVE approach to get full marks for this task
     */
    public void damageNeighbours(Disk disk){
        /*# YOUR CODE HERE */
        for (Disk d : this.disks) {
            if (d != disk && !d.isBroken() && disk.isWithinRange(d)) {
                d.damage();
            }
        }
    }

    /**
     * Are all the disks exploded?
     * Useful for telling whether the game is over.
     */
    public boolean haveAllDisksExploded(){
        /*# YOUR CODE HERE */
        int totDisksBroken = 0;
        for (Disk disk : disks) {
            if (disk.isBroken()) {
                totDisksBroken++;
            }

        }
        if (totDisksBroken == this.numDisks) { // if the total disks broken is the same as number of disks
            return true;
        } else {
            return false;
        }
    }
    /**
     * Update the score field, by summing the scores of each disk
     * Score is 150 for exploded disks, 50 for disks with 2 hits, and 20 for disks with 1 hit.
     */
    public void updateScore(){
        // Hint: Each Disk can report how many points they are worth:
        // Iterate through the ArrayList, adding up the total score of the disks.
        /*# YOUR CODE HERE */
        double totalScore = 0;
        for (Disk disk : disks) { // for each disk count up the score and add it to a total
            totalScore += disk.score();
        }
        this.score = totalScore; // then update it to the private score declaration
        System.out.println("Current score: " + this.score);
    }


    /**
     *  Redraws the game:
     *  - the boundary of the shooting range (done for you)
     *  - the shooting zone in gray (done for you)
     *  - the gun in black (done for you)
     *  - the disks
     *  - the pile of remaining shot
     */
    public void redraw(){
        UI.clearGraphics();
        //Redraw the boundary of the shooting range
        UI.setColor(Color.black);
        UI.drawRect(0,0, GAME_WIDTH, GUN_Y);
        UI.setColor(Color.gray);
        UI.drawLine(0, SHOOTING_RANGE_Y, GAME_WIDTH, SHOOTING_RANGE_Y);

        // Redraw the shooting zone in gray
        UI.setColor(Color.lightGray);
        UI.fillArc(GUN_X-SHOOTING_CIRCLE/2, GUN_Y-SHOOTING_CIRCLE/2, SHOOTING_CIRCLE, SHOOTING_CIRCLE, 0, 180);

        // Redraw the gun in black
        UI.setColor(Color.black);
        UI.fillRect(GUN_X-5, GUN_Y-5, 10, 10);

        // Redraw the disks, and
        // the pile of small red squares illustrating the remaining rounds
        /*# YOUR CODE HERE */
        for (Disk d : this.disks) { // redraw all disks
            d.draw();
        }
        double shotindX = 5;
        double shotindY = GUN_Y - 10; 
        UI.setColor(Color.red);
        for (int i = 0; i < numShots; i++) {
            UI.fillRect(shotindX, shotindY,6, 6);
            shotindX += 8;

            if ((i + 1) % 10 == 0) {  // after every 10 shots
            shotindX = 5;        // reset x position
            shotindY -= 8;       // move up
            }
        }
    }

    /**
     * Ask the user for a file to open,
     * then read all the game attributes
     * (which must mirror what was saved in the saveGame method)
     * re-create the game
     */    
    public void loadGame(){
        /*# YOUR CODE HERE */

    }

    /**
     * Ask the user to select a file and save the current game to the selected file
     * You need to save:
     * - The current score and the number of remaining shots
     * - The coordinates and the damage of each disk
     *   Hint: use the toString method
     */
    public void saveGame(){
        /*# YOUR CODE HERE */

    }

    public static void main(String[] args){
        DiskGame dg = new DiskGame();
        dg.setupGUI();
        dg.startGame();
    }

}
