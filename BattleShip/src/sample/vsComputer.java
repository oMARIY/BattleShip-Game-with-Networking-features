package sample;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;


import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class vsComputer {
    @FXML
    private GridPane grid;

    private Block block;

    private AlertBox alert = new AlertBox();

    private PvPClient pvp = new PvPClient();

    private PvPHost pvpHost = new PvPHost();

    private List<Block> availableBlocks;

/*
 * the vsComputer class is used to set up the Battleship game where the user of the application would be playing against
 * a computer AI. The AI itself is just places random moves on the Battleship board on the window.
 *The ships are also generated automatically, meaning you don't get to choose your ships. The ships' positions are assigned
 * randomly so you won't get  the traditional kind of ships in this mode of the game i.e. one ship of five,
 * one ship of four and so on until you reach one.
 * Also there is no rule as to whether or not you or the computer have the same amount of ships as
 * they are assigned randomly.
*/

    //This method is run when the window is created
    @FXML
    public void initialize(){
        setBoardVsComputer();

    }
    //This method is used to set up the Battleship game board on the window
    public void setBoardVsComputer(){
        try {
            /*
            Whenever you need to restart the game, this piece of code will check if there is anything on the GridPane
            that covers the entire window. The GridPane is the container of all elements on the window. Whenever
            we want to restart we first need to get rid of all the existing elements on the GridPane
            This only runs if there is something on the GridPane, meaning the first time it won't run
            */
            if(grid.getChildren().size() > 0){
                grid.getChildren().clear();
            }

            /*
            These for loops below are used to set up the BattleShip board. We add a block (a class that extends Rectangle)
            to our GridPane. 10 rows and 10 columns for a hundred blocks. This is the user's Battleship Board.
            */

            for(int i = 0; i < 10; i++){
                for(int j = 0; j<10; j++){

                    block = new Block(i,j);


                    block.setStroke(Color.WHITE);
                    block.setFill(Color.BLACK);
                    block.setWidth(40);
                    block.setHeight(40);
                    block.setArcHeight(10);
                    block.setArcWidth(10);




                    grid.add(block, i, j);
                    shoot(block);

                }
            }

            /*
            We set up another board for the computer here. Does the exact same thing except the row index starts from
            11 and finishes at 20
            */

            for(int i = 11; i < 21; i++){
                for(int j = 0; j < 10; j++){
                    block = new Block(i,j);


                    block.setStroke(Color.WHITE);
                    block.setFill(Color.BLACK);
                    block.setWidth(40);
                    block.setHeight(40);
                    block.setArcHeight(10);
                    block.setArcWidth(10);




                    grid.add(block, i, j);
                    //shoot(block);
                }
            }

        } catch (NullPointerException e){
            System.out.println(e.getMessage());
        }

        //UI BUTTONS
        Button restartGameBTN = new Button();
        Button vsPlayerClient = new Button();
        Button vsPlayerHost = new Button();

        //This will reset the board
        restartGameBTN.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                restart();
            }
        });

        restartGameBTN.setText("Restart Game");
        vsPlayerClient.setText("Search Game");
        vsPlayerHost.setText("Host Game");

        //Adding the Buttons to the window
        grid.add(restartGameBTN,10, 0);
        grid.add(vsPlayerClient, 10, 1);
        grid.add(vsPlayerHost, 10, 2);

        //This method is used to create the ships that the computer and the user will have.
        createShips();

        //SetonAction (a method that tells the button what to do when clicked) are set up on these methods below
        twoPlayerClient(vsPlayerClient);
        twoPlayerHost(vsPlayerHost);
    }

    /*
     This method is used to create an eventhandler that will allow the user to click his/her side of the board.
     It is called in the method above when we set up the user's Battleship board
    */
    private void shoot(Block block){
        block.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(block.getFill() == Color.BLACK && block.isShip()){
                    //This will set the color of a block red if it is ship and it's clicked
                    //You will be able to have another turn if this is the case
                    block.setFill(Color.RED);
                }else if(block.getFill() == Color.BLACK){
                    //This will set the color of a block light gray if it is a ship
                    //Your turn will be finished and the computer will hava a go (enemyMove();)
                    block.setFill(Color.LIGHTGRAY);
                    enemyMove();
                }
                else if(block.getFill() == Color.LIGHTGRAY || block.getFill()==Color.RED){
                    alert.display("WARNING", "Block Already Selected");
                    //If you click a block that you have already clicked before this dialog window will pop up
                }
                checkWinner();

            }
        });

    }

/*
   This method is used to create the ships that the user and computer will have on their board
*/
    private void createShips(){
        //This gets all the elements on the GridPane container and puts them in an arraylist
        //This includes all the buttons and blocks we added
        ObservableList<Node> board = grid.getChildren();
        System.out.println("YOUR SHIPS");
        for(Node block : board){
            /*
            * We only are interested in the blocks, not buttons
            * so the below if statement makes sure we only work with
            * blocks
            */
            if(block instanceof Block){
                //This is a call to the createShips method in the Block class
                //That is where we actually assign blocks as ships randomly
                ((Block) block).createShips();
                if(((Block) block).isShip()){
                    /*
                    Sets all the ships on the computer's board as CYAN(blue) rather than black so you can see
                    your ships. Obviously, you won't be able to see the computer's ships on your side of the board.
                    */
                    if(((Block) block).getRowVal() >= 11){

                        ((Block) block).setFill(Color.CYAN);
                    }
                }
            }
        }

    }

    @FXML
    private void restart(){
        setBoardVsComputer();
    }


    private void checkWinner(){
    /*
    This method checks if the computer or user have won. Every time the user clicks a block and the computer makes a
    move this method is called. it gets all the ships that you have, puts that into a list, then it gets all your
    ships that have been hit, puts that into another list. If the sizes of these two lists are equal, then it means
    all your ships have been hit and you have lost. It also does this for the computer's ships.
    */
        ObservableList<Node> board = grid.getChildren();
        List<Block> allEnemyBlocks = new ArrayList<>();
        List<Block> hitEnemyBlocks = new ArrayList<>();

        List<Block> allMyShips = new ArrayList<>();
        List<Block> hitShips = new ArrayList<>();

        for(Node block : board){
            if(block instanceof Block){
                    if(((Block) block).getRowVal() < 10){
                        if(((Block) block).isShip()){
                            allEnemyBlocks.add(((Block) block));
                            if(((Block) block).getFill() == Color.RED){
                                hitEnemyBlocks.add(((Block) block));
                            }
                        }
                    }

                    if(((Block) block).getRowVal() >= 11){
                        if(((Block) block).isShip()){
                            allMyShips.add((Block) block);
                            if(((Block) block).getFill() == Color.RED){
                                hitShips.add(((Block) block));
                            }
                        }
                    }
            }
        }

        int allBlocksAmount = allEnemyBlocks.size();
        int hitBlocksAmount = hitEnemyBlocks.size();

        int myShipsAmount = allMyShips.size();
        int myHitShipsAmount = hitShips.size();


            if(allBlocksAmount == hitBlocksAmount){
                alert.display("Congratulations", "YOU'VE WON, ALL ENEMY SHIPS ARE DESTROYED");
                restart();
        }

        if(myShipsAmount == myHitShipsAmount){
                alert.display("Sorry!", "YOU'VE LOST, ALL YOUR SHIPS HAVE BEEN DESTROY");
                restart();
        }

    }

    private void enemyMove() {
        /*
        This method is used as the AI for the computer. It makes a move on the computer's board as soon as
        the user clicks a block on their side. The moves are random and not designed to be actively looking
        for a win. If you need this kind of AI you can make the changes accordingly
        */
        Random rand = new Random();

        List<Block> blocks = new ArrayList<>();
        availableBlocks = new ArrayList<>();
        ObservableList<Node> board = grid.getChildren();

        for(Node block : board) {
            if (block instanceof Block) {
                blocks.add((Block) block);
            }

        }

        for(Block b : blocks){
            if(!b.isSelected() && b.getRowVal() >= 11){
                availableBlocks.add(b);
            }

       }

        int randomBlock = rand.nextInt(availableBlocks.size() - 1);


        Block block = availableBlocks.get(randomBlock);

        if(block.getFill() == Color.CYAN){
            block.setFill(Color.RED);

            enemyMove();
        }else if(block.getFill() == Color.BLACK){
            block.setFill(Color.LIGHTGRAY);

        }

        checkWinner();

       }


    public void twoPlayerClient (Button vsPlayer) {
        //This will load a new window for Multiplayer Mode as a Client
        vsPlayer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pvp.loadPVP();
                Stage stage = (Stage) vsPlayer.getScene().getWindow();
                stage.close();
            }
        });

    }

    public void twoPlayerHost (Button vsPlayer){
        //This will load a new window for Multiplayer Mode as a host
        /*
        NOTE: A host cannot play a host and a client can not play a client
        Furthermore if you do not know much about Socket programming and can't change the program accordingly
        Then know that there can only be one client and one host per computer.
        Meaning you cannot run this application twice and click 'host game' twice
         The same goes for 'Search game'.
         */
        vsPlayer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pvpHost.loadPVP();
                Stage stage = (Stage) vsPlayer.getScene().getWindow();
                stage.close();
            }
        });

    }

    public void loadMain(){
        //This method is used in other classes e.g. PvPClient & PvPHost
        //It is used to reload the window for this game mode in other classes
        //i.e. if you go into multiplayer mode and then you want to come back to play the computer
        //There will be a button when click that uses this mehtod
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Battleship");
            stage.setScene(new Scene(root, 925, 410));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
