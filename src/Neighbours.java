import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Blend;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;

import javax.xml.namespace.QName;

import java.lang.Thread;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.exit;
import static java.lang.System.out;


/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test methods uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    class Actor {
        final Color color;               // Color an existing JavaFX class
        boolean isSatisfied;      // false by default

        Actor(Color color) {      // Constructor to initialize
            this.color = color;
            this.isSatisfied = false;
        }  
        
        public void setSatisfied(boolean isSatisfied) {
            this.isSatisfied = isSatisfied;
        }
        // Constructor, used to initialize
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used directly in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors
    boolean allHappy = false;
    final double[] dist = {0.50, 0.25, 0.25};
    final Random random = new Random();

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        double threshold = 0.7;
        if(!(allHappy)){
        allHappy = notePops(world, threshold, dist);
        int[][] nullIndexes = scanNullpositions(world, dist);
        int[][] shuffledIndexes = shuffleIndex(nullIndexes);
        int[][] unhappyActorIndexes = scanUnhappyPositions(world, dist);
        
        world = relocatePops(world, shuffledIndexes, unhappyActorIndexes);
        }
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST, see below!

        // %-distribution of RED, BLUE and NONE
        // Number of locations (places) in world (must be a square)
        int nLocations = 90000;   // Should also try 90 000
        int n = (int) sqrt(nLocations);

        // TODO
        Actor[][] nullworld = new Actor[n+2][n+2];
        nullworld = populateWorld(nullworld, n, dist);
        world = nullworld;
        // Should be last
        fixScreenSize(nLocations);
    }

    // TODO Many methods here, break down of init() and updateWorld()
    Actor[][] populateWorld(Actor[][] nullworld, int nLocations, double dist[]){
        //Putting actors in random places.
        for(double i=nLocations*nLocations*dist[1]; i > 0; i--){
            int xpos = random.nextInt(nLocations)+1;
            int ypos = random.nextInt(nLocations)+1;
            
            if (nullworld[xpos][ypos] == null){
                nullworld[xpos][ypos] = new Actor(Color.BLUE);
            } 
            else {
                //if position is already taken: try again.
                i++;
            }
        }
        for(double i=nLocations*nLocations*dist[0]; i > 0; i--){
            int xpos = random.nextInt(nLocations)+1;
            int ypos = random.nextInt(nLocations)+1;
            
            if (nullworld[xpos][ypos] == null){
                nullworld[xpos][ypos] = new Actor(Color.RED);
            } 
            else {
                //if position is already taken: try again.
                i++;
            }
        }
        return nullworld;
    }
        //checks notes whoes unhappy or not
        boolean notePops(Actor[][] nullworld, double threshold, double dist[]){
            boolean allHappy = false;
            int happyPopSize = 0;
            int popSize = (int) (((world.length-2)*(world.length-2))*(dist[0]+dist[1]));
            for(int i = 1; i < world.length-1; i++){
                for(int j = 1; j < world.length-1; j++){
                    if(world[i][j] != null){
                        if(isHappy(world, i, j, threshold)){
                        world[i][j].setSatisfied(true);
                        happyPopSize++;
                        }
                        else{
                        world[i][j].setSatisfied(false);;
                        }
                    }
                }
            }
            
            if(popSize==happyPopSize){
                allHappy = true;
            }
            return allHappy;
        }

        //Scans world for the indexes of unhappy actors and returns array of null indexes
        int[][] scanUnhappyPositions(Actor[][] nullworld, double[] dist){
            int size = (int) (((nullworld.length-2)*(nullworld.length-2))*(dist[2]));
            int[][] unhappyIndexes = new int[size][2];
            int countIndex = 0;

            for(double i=size; i > 0; i--){
                int xpos = random.nextInt(nullworld.length-2)+1;
                int ypos = random.nextInt(nullworld.length-2)+1;
                
                if(nullworld[xpos][ypos] != null){
                    if(!(nullworld[xpos][ypos].isSatisfied)){
                        unhappyIndexes[countIndex][0] = xpos;
                        unhappyIndexes[countIndex][1] = ypos;
                        countIndex++;
                        
                    }
                    else{
                        i++;
                    }
                } 
                else {
                    //if position is already taken: try again.
                    i++;
                }
            }
            return unhappyIndexes;
        }

        //relocates population
        Actor[][] relocatePops(Actor[][] nullworld, int[][] shuffledNullIndexes, int[][] shuffledActorIndexes){
            for(int i = 0; i < shuffledNullIndexes.length; i++){
                int newActorIndex0 = shuffledActorIndexes[i][0];
                int newActorIndex1 = shuffledActorIndexes[i][1];
                int newNullIndex0 = shuffledNullIndexes[i][0];
                int newNullIndex1 = shuffledNullIndexes[i][1];
                
                nullworld[newNullIndex0][newNullIndex1] = nullworld[newActorIndex0][newActorIndex1];
                nullworld[newActorIndex0][newActorIndex1] = null;
            }
            return nullworld;
        }

        //Scans nullworld for the indexes of nulls and returns array of null indexes
        int[][] scanNullpositions(Actor[][] nullworld, double[] dist){
            int size = (int) (((nullworld.length-2)*(nullworld.length-2))*dist[2]);
            int[][] nullIndexes = new int[size][2];

            int countIndex = 0;
            for(int i = 1; i < nullworld.length-1; i++){
                for(int j = 1; j < nullworld.length-1; j++){
                    if(nullworld[i][j] == null){
                        nullIndexes[countIndex][0] = i;
                        nullIndexes[countIndex][1] = j;
                        countIndex++;
                    }

                }

            }
            return nullIndexes;
        }
    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size && 0 <= col && col < size;
    }

    // ----------- Utility methods -----------------

    // TODO Method to change format of data, generate random etc.
    
            //Checks if actor is happy
            boolean isHappy(Actor[][] nullworld, int x, int y, double threshold){
                boolean happy = false;

                double sumNeighborsColor = -1;
                double sumNeighbors = -1;
                for(int i = x-1; i <= x+1; i++){
                    for(int j = y-1; j <= y+1; j++){
                        if(nullworld[i][j] == null){

                        }
                        else if(nullworld[i][j].color == nullworld[x][y].color){
                            sumNeighborsColor++;
                            sumNeighbors++;
                        }
                        else{
                            sumNeighbors++;
                        }
    
                    }
    
                }
                if(sumNeighbors == 0){
                    happy = false;
                }
                else if( (sumNeighborsColor / sumNeighbors) >= threshold){
                    happy = true;
                }
                return happy;
            }
        
            //Shuffles an n x 2 array filled with indexes
            int[][] shuffleIndex(int[][] shuffledindexes){
    
                for (int i = 0; i < shuffledindexes.length; i++) {
                    int randomIndexToSwap = random.nextInt(shuffledindexes.length-1);
                    int temp0 = shuffledindexes[randomIndexToSwap][0];
                    int temp1 = shuffledindexes[randomIndexToSwap][1];
                    shuffledindexes[randomIndexToSwap][0] = shuffledindexes[i][0];
                    shuffledindexes[randomIndexToSwap][1] = shuffledindexes[i][1];
                    shuffledindexes[i][0] = temp0;
                    shuffledindexes[i][1] = temp1;
                }
                return shuffledindexes;
            }    
    void printWorld(Actor[][] prtWorld){
        for(int i = 0; i < prtWorld.length; i++){
            for(int j = 0; j < prtWorld.length; j++){
                out.print(prtWorld[i][j]);

            }
            out.println("");
        }
    }

    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work. Important!!!!
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {null, null, null, null, null},
                {null, new Actor(Color.RED), new Actor(Color.RED), null, null, },
                {null, null, new Actor(Color.BLUE), null, null},
                {null, new Actor(Color.RED), null, new Actor(Color.BLUE), null},
                {null, null, null, null, null}
        };
        double th = 0.5;   // Simple threshold used for testing
        /*
        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));   // This is a single test
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));
        */
        // TODO  More tests here. Implement and test one method at the time
        // TODO Always keep all tests! Easy to rerun if something happens



        exit(0);
    }

    // ******************** NOTHING to do below this row, it's JavaFX stuff  **************

    double width = 500;   // Size for window
    double height = 500;
    final double margin = 50;
    double dotSize;

    void fixScreenSize(int nLocations) {
        // Adjust screen window
        dotSize = (double) 9000 / nLocations;
        if (dotSize < 1) {
            dotSize = 2;
        }
        width = sqrt(nLocations) * dotSize + 2 * margin;
        height = width;
    }

    long lastUpdateTime;
    final long INTERVAL = 450_000_000;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long now) {
                long elapsedNanos = now - lastUpdateTime;
                if (elapsedNanos > INTERVAL) {
                    updateWorld();
                    renderWorld(gc);
                    lastUpdateTime = now;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = (int) (dotSize * col + margin);
                int y = (int) (dotSize * row + margin);
                if (world[row][col] != null) {
                    g.setFill(world[row][col].color);
                    g.fillOval(x, y, dotSize, dotSize);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
