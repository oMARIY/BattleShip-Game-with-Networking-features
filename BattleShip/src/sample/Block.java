package sample;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Random;


public class Block extends Rectangle {

    private int rowVal;//row Index value
    private int colVal;//column Index value
    private boolean isShip = false;//checks if the block is a ship
    private boolean isSelected = false;//checks if the block has been clicked

   /*
   * The Block class extends from the Rectangle class in the JAVAFX shapes packages. The Rectangles is what creates
   * the block on the board when you run the application. I extended the Rectangle class  in the Block class
    * rather than use it as I need to do other things with the Rectangles created i.e. check if they are a ship
    * or get their postion on the board (the window). A ship is a block, but not all blocks are ships
   */

    public Block(int rowVal, int colVal){
        super(30,30);
        setFill(Color.BLACK);
        setStroke(Color.WHITE);
        setArcHeight(10);
        setArcWidth(10);
        this.rowVal = rowVal;
        this.colVal = colVal;

    }
    public Block(){

    }

    public void createShips() {

        Random rand = new Random();
        int x = rand.nextInt(9);
        int y = rand.nextInt(9);


        int x2 = rand.nextInt((21 - 11) + 1) + 11;

        //Runs ten times to give the system a chance to create the ships
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 20; j++){
                if(getRowVal() == x || getColVal() == y){
                    isShip = true;
                }
                if(getRowVal() == x2 || getColVal() == y){
                    isShip = true;
                }
            }
        }

    }

    public int getRowVal() {
        return rowVal;
    }

    public int getColVal() {
        return colVal;
    }

    public boolean isShip() {
        return isShip;
    }

    public String getBlock(){
        return getRowVal() + ", " + getColVal();
    }

    public boolean isSelected(){

        if(getFill() == Color.RED || getFill() == Color.LIGHTGRAY){
            isSelected = true;
            return isSelected;
        }
        else {
            isSelected = false;
            return isSelected;
        }
    }
    public void setShip(boolean ship) {
        isShip = ship;
    }


}
