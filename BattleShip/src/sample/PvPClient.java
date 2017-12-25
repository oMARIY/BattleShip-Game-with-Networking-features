package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class PvPClient {

    @FXML
    private GridPane PvPGrid;


    private Block block;

    private AlertBox alert = new AlertBox();

    private Socket clientSocket;
    private ServerSocket serverSocket;

    private String message;

    private List<Block> myShips;
    private List<String> shipPositions;
    private List<String> blockPostions;
    private List<String> enemyShips = new ArrayList<>();

    /*
       This class is used to load the client multiplayer mode. The client is one type of player that can play
       in multiplayer mode. In the vsComputer class I said only one client and one host can be used on one computer.
       That is to say two hosts or two clients can play each other if they are on separate computer. Although, you would
       have to know the IP addresses of those two computers to do that. I have not programmed it that way so
       it won't work if you try that with this version of my project. When you load the client window it will start a
       server and that server has the port number of 4000 as seen in the initialize method below. That is the reason you
       cannot use two client type players on the same computer as the port number is already taken. if you are not
       familiar with network programming, two servers (ServerSockets) on the same computer cannot have the same port
       number. Either you would have to rename all the port numbers in this class to do that and then run it again.
       Or you can configure it so that you can play against someone on a different computer using your computer and
       another computer's IP address.
    */

    public void loadPVP(){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PVP.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Battleship PVP Online Client");
            stage.setScene(new Scene(root, 925, 410));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Runs when the window is loaded and starts a new server on port 4000
    @FXML
    public void initialize(){

        setBoardVsPlayer();

        int port = 4000;
        serverSocket = null;
        try {
              serverSocket = new ServerSocket(port);

            System.out.println("Server Created on Port " + port);
        } catch (IOException e) {
            System.out.println("There was a problem connecting to the port: " + e.getMessage());
            e.printStackTrace();
        }
        //Used to create your own ships
        myShips = createShips();

    }
    //Used to set the BattleShip Board on the grid pane. Very similar to the setBoardVsComputer() method in the
    //vsComputer class
    public void setBoardVsPlayer(){
        PvPGrid.getChildren().clear();

        for(int i = 0; i < 10; i++){
            for(int j = 0; j<10; j++){

                block = new Block(i,j);


                block.setStroke(Color.WHITE);
                block.setFill(Color.BLACK);
                block.setWidth(40);
                block.setHeight(40);
                block.setArcHeight(10);
                block.setArcWidth(10);




                PvPGrid.add(block, i, j);


            }
        }

        for(int i = 11; i < 21; i++){
            for(int j = 0; j < 10; j++){
                block = new Block(i,j);


                block.setStroke(Color.WHITE);
                block.setFill(Color.BLACK);
                block.setWidth(40);
                block.setHeight(40);
                block.setArcHeight(10);
                block.setArcWidth(10);




                PvPGrid.add(block, i, j);

            }
        }
        //UI BUTTONS
        Button playOnline = new Button();
        Button leave = new Button();

        /*
        This setOnAction method is for play online button and it connects to the host server
        if the host is not connected and you try to connect it you will throw an exception:
        java.net.ConnectException: Connection refused (Connection refused)
        This means that the host server is not live so you cannot connect.
        Always make sure both the client and host server are live.
        As soon as you load in the window for the client or host, they wiil start their servers.
        This method will also set up your ships that you create on the enemy's board.
        */
        playOnline.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread t1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendShipsToOpponent();
                            List<String> enemy = getShipsFromOpponent();
                            enemyShips.addAll(enemy);
                            blockPostions = new ArrayList<>();
                            setOnlineEnemyBlockList(enemy);
                            for(Block ships : myShips){
                                String position = ships.getBlock();
                                String newPostion = equateOnlineBlock(position);
                                blockPostions.add(newPostion);
                            }

                            setOnlineMyBlocksList(blockPostions);

                        }
                    });

             t1.start();
            }
        });

        //This button is used to leave the game and go back to playing against the computer
        leave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    serverSocket.close();
                    closeWindow();
                    System.out.println("[BATTLESHIP SERVER] Closing Server! Bye!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                vsComputer controller = new vsComputer();
                controller.loadMain();
                Stage stage = (Stage) leave.getScene().getWindow();
                stage.close();
            }
        });

        playOnline.setText("Play Online");

        leave.setText("vs Computer");

        //Adding the buttons to the window
        PvPGrid.add(playOnline,10, 0);
        PvPGrid.add(leave, 10, 1);


    }

    //This method is used to send your ships to the enemy,
    // they won't see them of course, it will just put them on the enemy's board
    public void sendShipsToOpponent(){
        clientSocket = null;
        String serverHostName = "localhost";

        int port = 3000;

        try{
            clientSocket = new Socket(serverHostName, port);
        } catch (UnknownHostException e){
            System.out.println("[Server Error] could not connect to the server -> " + e.getMessage());
        }catch (IOException e){
            System.out.println("[Commication Error] could not establish stable communications with the server");
        }


        try{

            OutputStream os = clientSocket.getOutputStream();
            PrintStream printStream = new PrintStream(os);


            for(int i = 0; i < myShips.size(); i++){
                Block b = myShips.get(i);
                String block =  b.getBlock();
                printStream.println(block);

            }


        } catch (IOException e){
            System.out.println("[Commication Error] could not establish stable communications with the server, try again: " + e.getMessage());
        }

    }
//This method is used to get the enemy's ship, the enemy's ships will then be set up on your board, but they won't
    //be visible to you
    public List<String> getShipsFromOpponent(){
        Socket playerTwo = null;

        try{
           playerTwo = serverSocket.accept();
        } catch (UnknownHostException e){
            System.out.println("[Server Error] could not connect to the server -> " + e.getMessage());
        }catch (IOException e){
            System.out.println("[Commication Error] could not establish stable communications with the server");
        }


        try{
            InputStream is = playerTwo.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);


            BufferedReader bufferedReader = new BufferedReader(reader);
            String message;



             shipPositions = new ArrayList<>();


            System.out.println("GETTING ENEMY SHIPS");


           while((message = bufferedReader.readLine()) != null){
               System.out.println("Message Received from Opposing: \n\t" + message);
               shipPositions.add(message);
               sleep(bufferedReader);
               if(!bufferedReader.ready()){
                   break;
               }
              }


        } catch (IOException e){
                System.out.println("[Commication Error] could not establish stable communications with the server, try again: " + e.getMessage());
        }

        return shipPositions;
    }

//This method is used to allow the users to click (shoot) the blocks, check for a winner, retrieve the enemy's moves
    //as well as send your move to the opponent
    public void shoot(Block block){
        getOppenentMove();
            block.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (block.getFill() == Color.BLACK && block.isShip()) {
                        block.setFill(Color.RED);
                        sendMoveToOpponent(block);
                        checkWinner(enemyShips);
                        alert.waitingForPlayer("Opposing Player's Turn", "Waiting for Opposing Player");
                    } else if (block.getFill() == Color.BLACK) {
                        block.setFill(Color.LIGHTGRAY);
                        sendMoveToOpponent(block);
                        checkWinner(enemyShips);
                        alert.waitingForPlayer("Opposing Player's Turn", "Waiting for Opposing Player");
                    } else if (block.getFill() == Color.LIGHTGRAY || block.getFill() == Color.RED) {
                        alert.display("WARNING", "Block Already Selected");
                    }

                }
            });

    }
/*
    This method will be used to create your ships, unlike the vsComputer board, you get to pick your ships.
    How this works is that you get to pick up to 15 ships each ship that you select will become red and then you
    press the play online button which will send your ships to your opponent as well as you getting your opponent's
    ship
*/
    public List<Block> createShips(){
        ObservableList<Node> board = PvPGrid.getChildren();
        List<Block> myOwnShips = new ArrayList<>();

        System.out.println("YOUR SHIPS");
        for(Node block : board){
            if(block instanceof Block){
                if(((Block) block).getRowVal() < 10){
                    Block currentBlock = (Block) block;
                    currentBlock.setOnMouseClicked(new EventHandler<MouseEvent>() {
                         @Override
                         public void handle(MouseEvent event) {

                             if(myOwnShips.size() >= 15){
                                 currentBlock.setOnMouseClicked(null);
                                 System.out.println("You have enough Ships now! Play online!");
                             }else{
                                 if(myOwnShips.contains(currentBlock)){
                                     alert.display("Warning", "This block has already been selected as a ship");
                                 }else {
                                     myOwnShips.add(currentBlock);
                                     ((Block) block).setFill(Color.RED);
                                 }
                             }


                         }
                     });

                }

            }
        }
       return myOwnShips;
    }


    public void checkWinner(List<String> enemy){

        ObservableList<Node> board = PvPGrid.getChildren();
        List<Block> allEnemyBlocks = new ArrayList<>();
        List<Block> hitEnemyBlocks = new ArrayList<>();

        List<Block> allMyShips = new ArrayList<>();
        List<Block> hitShips = new ArrayList<>();

        for(Node block : board){
            if(block instanceof Block){
                if(((Block) block).getRowVal() < 10){

                    for(int i = 0; i < enemy.size(); i++){

                        if(((Block) block).getBlock().equals(enemy.get(i))){
                            allEnemyBlocks.add(((Block) block));
                            if(((Block) block).getFill() == Color.RED){
                                hitEnemyBlocks.add(((Block) block));
                            }
                        }

                    }

                }

                if(((Block) block).getRowVal() >= 11){

                    for(int i = 0; i< blockPostions.size(); i++){

                        if(((Block) block).getBlock().equals(blockPostions.get(i))){
                            allMyShips.add((Block) block);
                            if(((Block) block).getFill() == Color.RED){
                                hitShips.add(((Block) block));
                            }
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
            alert.window.close();
        }

        if(myShipsAmount == myHitShipsAmount){
            alert.display("Sorry!", "YOU'VE LOST, ALL YOUR SHIPS HAVE BEEN DESTROY");
            alert.window.close();
        }

    }


    private void sleep(BufferedReader in) throws IOException {
        /*
        This method is used for Socket programming, it does not have a role in affecting the application UI.
        It is just used to break out of while loops that are used to read from a socket.
        */
        long time = System.currentTimeMillis();
        while(System.currentTimeMillis()-time < 1000){
            if(in.ready()){
                break;
            }
        }
    }
    /*
    This method (equateOnlineBlock) converts the position of a block on your board, to the equivalent position
    on the enemy's board. This is so that when you click a block on your board,
    it will be selected on the your opponents window on their opponent's board, which is your board.
    */
    private String equateOnlineBlock (String block){
        String blockPostion;

        switch(block){
            case "0, 0":
                blockPostion = "11, 0";
                break;
            case "1, 0":
                blockPostion = "12, 0";
                break;
            case "2, 0":
                blockPostion = "13, 0";
                break;
            case "3, 0":
                blockPostion = "14, 0";
                break;
            case "4, 0":
                blockPostion = "15, 0";
                break;
            case "5, 0":
                blockPostion = "16, 0";
                break;
            case "6, 0":
                blockPostion = "17, 0";
                break;
            case "7, 0":
                blockPostion = "18, 0";
                break;
            case "8, 0":
                blockPostion = "19, 0";
                break;
            case "9, 0":
                blockPostion = "20, 0";
                break;


            case "0, 1":
                blockPostion = "11, 1";
                break;
            case "1, 1":
                blockPostion = "12, 1";
                break;
            case "2, 1":
                blockPostion = "13, 1";
                break;
            case "3, 1":
                blockPostion = "14, 1";
                break;
            case "4, 1":
                blockPostion = "15, 1";
                break;
            case "5, 1":
                blockPostion = "16, 1";
                break;
            case "6, 1":
                blockPostion = "17, 1";
                break;
            case "7, 1":
                blockPostion = "18, 1";
                break;
            case "8, 1":
                blockPostion = "19, 1";
                break;
            case "9, 1":
                blockPostion = "20, 1";
                break;



            case "0, 2":
                blockPostion = "11, 2";
                break;
            case "1, 2":
                blockPostion = "12, 2";
                break;
            case "2, 2":
                blockPostion = "13, 2";
                break;
            case "3, 2":
                blockPostion = "14, 2";
                break;
            case "4, 2":
                blockPostion = "15, 2";
                break;
            case "5, 2":
                blockPostion = "16, 2";
                break;
            case "6, 2":
                blockPostion = "17, 2";
                break;
            case "7, 2":
                blockPostion = "18, 2";
                break;
            case "8, 2":
                blockPostion = "19, 2";
                break;
            case "9, 2":
                blockPostion = "20, 2";
                break;


            case "0, 3":
                blockPostion = "11, 3";
                break;
            case "1, 3":
                blockPostion = "12, 3";
                break;
            case "2, 3":
                blockPostion = "13, 3";
                break;
            case "3, 3":
                blockPostion = "14, 3";
                break;
            case "4, 3":
                blockPostion = "15, 3";
                break;
            case "5, 3":
                blockPostion = "16, 3";
                break;
            case "6, 3":
                blockPostion = "17, 3";
                break;
            case "7, 3":
                blockPostion = "18, 3";
                break;
            case "8, 3":
                blockPostion = "19, 3";
                break;
            case "9, 3":
                blockPostion = "20, 3";
                break;



            case "0, 4":
                blockPostion = "11, 4";
                break;
            case "1, 4":
                blockPostion = "12, 4";
                break;
            case "2, 4":
                blockPostion = "13, 4";
                break;
            case "3, 4":
                blockPostion = "14, 4";
                break;
            case "4, 4":
                blockPostion = "15, 4";
                break;
            case "5, 4":
                blockPostion = "16, 4";
                break;
            case "6, 4":
                blockPostion = "17, 4";
                break;
            case "7, 4":
                blockPostion = "18, 4";
                break;
            case "8, 4":
                blockPostion = "19, 4";
                break;
            case "9, 4":
                blockPostion = "20, 4";
                break;


            case "0, 5":
                blockPostion = "11, 5";
                break;
            case "1, 5":
                blockPostion = "12, 5";
                break;
            case "2, 5":
                blockPostion = "13, 5";
                break;
            case "3, 5":
                blockPostion = "14, 5";
                break;
            case "4, 5":
                blockPostion = "15, 5";
                break;
            case "5, 5":
                blockPostion = "16, 5";
                break;
            case "6, 5":
                blockPostion = "17, 5";
                break;
            case "7, 5":
                blockPostion = "18, 5";
                break;
            case "8, 5":
                blockPostion = "19, 5";
                break;
            case "9, 5":
                blockPostion = "20, 5";
                break;



            case "0, 6":
                blockPostion = "11, 6";
                break;
            case "1, 6":
                blockPostion = "12, 6";
                break;
            case "2, 6":
                blockPostion = "13, 6";
                break;
            case "3, 6":
                blockPostion = "14, 6";
                break;
            case "4, 6":
                blockPostion = "15, 6";
                break;
            case "5, 6":
                blockPostion = "16, 6";
                break;
            case "6, 6":
                blockPostion = "17, 6";
                break;
            case "7, 6":
                blockPostion = "18, 6";
                break;
            case "8, 6":
                blockPostion = "19, 6";
                break;
            case "9, 6":
                blockPostion = "20, 6";
                break;




            case "0, 7":
                blockPostion = "11, 7";
                break;
            case "1, 7":
                blockPostion = "12, 7";
                break;
            case "2, 7":
                blockPostion = "13, 7";
                break;
            case "3, 7":
                blockPostion = "14, 7";
                break;
            case "4, 7":
                blockPostion = "15, 7";
                break;
            case "5, 7":
                blockPostion = "16, 7";
                break;
            case "6, 7":
                blockPostion = "17, 7";
                break;
            case "7, 7":
                blockPostion = "18, 7";
                break;
            case "8, 7":
                blockPostion = "19, 7";
                break;
            case "9, 7":
                blockPostion = "20, 7";
                break;



            case "0, 8":
                blockPostion = "11, 8";
                break;
            case "1, 8":
                blockPostion = "12, 8";
                break;
            case "2, 8":
                blockPostion = "13, 8";
                break;
            case "3, 8":
                blockPostion = "14, 8";
                break;
            case "4, 8":
                blockPostion = "15, 8";
                break;
            case "5, 8":
                blockPostion = "16, 8";
                break;
            case "6, 8":
                blockPostion = "17, 8";
                break;
            case "7, 8":
                blockPostion = "18, 8";
                break;
            case "8, 8":
                blockPostion = "19, 8";
                break;
            case "9, 8":
                blockPostion = "20, 8";
                break;




            case "0, 9":
                blockPostion = "11, 9";
                break;
            case "1, 9":
                blockPostion = "12, 9";
                break;
            case "2, 9":
                blockPostion = "13, 9";
                break;
            case "3, 9":
                blockPostion = "14, 9";
                break;
            case "4, 9":
                blockPostion = "15, 9";
                break;
            case "5, 9":
                blockPostion = "16, 9";
                break;
            case "6, 9":
                blockPostion = "17, 9";
                break;
            case "7, 9":
                blockPostion = "18, 9";
                break;
            case "8, 9":
                blockPostion = "19, 9";
                break;
            case "9, 9":
                blockPostion = "20, 9";
                break;


            default:
                System.out.println("No block with that position is on the board");
                blockPostion = null;
        }

        return blockPostion;
    }
    /*
       This method (setOnlineEnemyBlockList) will get the retrieved enemy ships from the initial connection i.e. when
       you selected your ships and pressed play online, and sets them up on your board.
    */
    private void setOnlineEnemyBlockList(List<String> enemyShips){
        ObservableList<Node> board = PvPGrid.getChildren();

        for(Node node : board){
            if(node instanceof Block){
                if(((Block) node).getRowVal() < 10){
                    ((Block) node).setFill(Color.BLACK);
                    shoot((Block) node);
                    for(String position : enemyShips){
                        if(position.equals(((Block) node).getBlock())){
                            ((Block) node).setShip(true);
                        }
                    }
                }

            }
        }
    }
    /*
        This method (hitEnemyBlock) is used to present your opponent's move on their board on your window.
    */
    private void hitEnemyBlock (String enemyShip){
        ObservableList<Node> board = PvPGrid.getChildren();

        for(Node node : board){
            if(node instanceof Block){
                if(((Block) node).getRowVal() >= 11){
                       if(enemyShip.equals(((Block) node).getBlock())){
                           if(((Block) node).getFill() == Color.CYAN){
                               ((Block) node).setFill(Color.RED);
                           }else if(((Block) node).getFill() == Color.BLACK){
                               ((Block) node).setFill(Color.LIGHTGRAY);
                           }
                       }
                }
                }

            }
        }

    /*
       This method (setOnlineMyBlocksList) is used to set up your ships on the enemy's board
    */
    private void setOnlineMyBlocksList(List<String> myships){
        ObservableList<Node> board = PvPGrid.getChildren();

        for(Node node : board){
            if(node instanceof Block){
                    for(String position : myships){
                        if(((Block) node).getRowVal() >= 11) {
                            if (position.equals(((Block) node).getBlock())) {
                                ((Block) node).setFill(Color.CYAN);
                            }
                        }
                    }


            }
        }

    }
    /*
        This method is used to send your move or the block you just clicked to the opponent
    */
    private void sendMoveToOpponent(Block block){
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientSocket = null;
                String serverHostName = "localhost";
                int port = 3000;

                try{
                    clientSocket = new Socket(serverHostName, port);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try{

                    OutputStream os = clientSocket.getOutputStream();
                    PrintStream printStream = new PrintStream(os);


                    String blockPostion = block.getBlock();
                    String newBlockPosition = equateOnlineBlock(blockPostion);

                    printStream.println(newBlockPosition);

                    clientSocket.close();

                } catch (IOException e) {
                    System.out.println("[Commication Error] could not establish stable communications with the server, try again: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        }).start();
}

//This method (getOppenentMove) is used to get the block that your opponent just shot
    private void getOppenentMove(){
    new Thread(new Runnable() {
        @Override
        public void run() {
           Socket readSocket = null;

            try{
                readSocket = serverSocket.accept();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }catch (SocketException e){
                System.out.println("Server has been closed");
                try {
                    readSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try{
                InputStream is = readSocket.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);

                BufferedReader bufferedReader = new BufferedReader(reader);

                while((message = bufferedReader.readLine()) != null){
                    hitEnemyBlock(message);
                    sleep(bufferedReader);
                    if(!bufferedReader.ready()){
                        break;
                    }
                }

                closeWindow();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }).start();
}

    private void closeWindow(){
        Runnable closeWindow = new Runnable() {
            @Override
            public void run() {
                alert.window.close();
            }
        };

    Platform.runLater(closeWindow);
}


}
