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


public class PvPHost {
    @FXML
    private GridPane PvPGridHost;



    private Block block;

    private AlertBox alert = new AlertBox();

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private String message;

    private List<Block> myShips;
    private List<String> shipPositions;
    private List<String> blockPostions;
    private List<String> enemyShips = new ArrayList<>();

/*
   This class is used to load the host multiplayer mode. The host along with the client
    is one type of player that can play in multiplayer mode. The host starts a server on port 3000
    and only one host can be on one computer unless you change the port number. This class has a lot of the same
    methods and fields that the PvPclient class has, the only difference is that the PvPclient class's methods
    are sending and retrieving data from the PvPHost's server and the PvPhost's methods are sending and getting
    data from the PvPclient's servers.
*/
    @FXML
    public void initialize() {
        setBoardVsPlayer();

        int port = 3000;
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);

            System.out.println("MultiThreaded Server Created on Port " + port);
        } catch (IOException e) {
            System.out.println("There was a problem connecting to the port: " + e.getMessage());
            e.printStackTrace();
        }

        myShips = createShips();
    }

    public void loadPVP() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PvPHost.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Battleship PVP Online Host");
            stage.setScene(new Scene(root, 925, 410));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBoardVsPlayer() {
        PvPGridHost.getChildren().clear();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {

                block = new Block(i, j);


                block.setStroke(Color.WHITE);
                block.setFill(Color.BLACK);
                block.setWidth(40);
                block.setHeight(40);
                block.setArcHeight(10);
                block.setArcWidth(10);


                PvPGridHost.add(block, i, j);


            }
        }

        for (int i = 11; i < 21; i++) {
            for (int j = 0; j < 10; j++) {
                block = new Block(i, j);


                block.setStroke(Color.WHITE);
                block.setFill(Color.BLACK);
                block.setWidth(40);
                block.setHeight(40);
                block.setArcHeight(10);
                block.setArcWidth(10);


                PvPGridHost.add(block, i, j);

            }
        }

        Button playOnline = new Button();
        Button leave = new Button();


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
                        for (Block ships : myShips) {
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


        PvPGridHost.add(playOnline, 10, 0);
        PvPGridHost.add(leave, 10, 1);
    }


    private List<Block> createShips() {
        ObservableList<Node> board = PvPGridHost.getChildren();
        List<Block> myOwnShips = new ArrayList<>();

        System.out.println("YOUR SHIPS");
        for (Node block : board) {
            if (block instanceof Block) {
                if (((Block) block).getRowVal() < 10) {
                    Block currentBlock = (Block) block;
                    currentBlock.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {

                            if (myOwnShips.size() >= 15) {
                                currentBlock.setOnMouseClicked(null);
                                System.out.println("You have enough Ships now! Play online!");
                            } else {
                                if (myOwnShips.contains(currentBlock)) {
                                    alert.display("Warning", "This block has already been selected as a ship");
                                } else {
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

    private void sendShipsToOpponent(){
        clientSocket = null;
        String serverHostName = "localhost";
        int port = 4000;
        try{
            clientSocket = new Socket(serverHostName, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }


        try{
            OutputStream os = clientSocket.getOutputStream();
            PrintStream printStream = new PrintStream(os);

            for (int i = 0; i < myShips.size(); i++) {
                Block b = myShips.get(i);
                String block = b.getBlock();

                printStream.println(block);

            }

            System.out.println("[BATTLESHIP SERVER 1] sending message to Opposing player");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<String> getShipsFromOpponent() {
        Socket playerOne = null;
        try {

            playerOne = serverSocket.accept();


            InputStream inputStream = playerOne.getInputStream();
            InputStreamReader playerOneInputStream = new InputStreamReader(inputStream);

            BufferedReader playerOnereader = new BufferedReader(playerOneInputStream);
            String messageFromPlayerOne;


            shipPositions = new ArrayList<>();

            System.out.println("GETTING ENEMY SHIPS");

            while ((messageFromPlayerOne = playerOnereader.readLine()) != null) {
                System.out.println("Message Received from Opposing: \n\t" + messageFromPlayerOne);
                shipPositions.add(messageFromPlayerOne);
                sleep(playerOnereader);
                if (!playerOnereader.ready()) {
                    break;
                }

            }


            System.out.println("[BATTLESHIP SERVER 1] recieved message to Opposing player");
        } catch (IOException e) {
            System.out.println("Error - cannot connect the players -> " + e.getMessage());
            e.printStackTrace();

        }

        return shipPositions;
    }


    private void shoot(Block block) {
        getOpponentMove();
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


    private void checkWinner(List<String> enemy){

        ObservableList<Node> board = PvPGridHost.getChildren();
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
            closeWindow();

        }

        if(myShipsAmount == myHitShipsAmount){
            alert.display("Sorry!", "YOU'VE LOST, ALL YOUR SHIPS HAVE BEEN DESTROY");
            closeWindow();
        }

    }

    private void sleep(BufferedReader in) throws IOException {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < 1000) {
            if (in.ready()) {
                break;
            }
        }
    }

    private String equateOnlineBlock(String block) {
        String blockPostion;

        switch (block) {
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

    private void setOnlineEnemyBlockList(List<String> enemyShips) {
        ObservableList<Node> board = PvPGridHost.getChildren();

        for (Node node : board) {
            if (node instanceof Block) {
                if (((Block) node).getRowVal() < 10) {
                    ((Block) node).setFill(Color.BLACK);
                    shoot((Block) node);
                    for (String position : enemyShips) {
                        if (position.equals(((Block) node).getBlock())) {
                            ((Block) node).setShip(true);
                        }
                    }
                }

            }
        }
    }

    private void setOnlineEnemyString(String enemyShip) {
        ObservableList<Node> board = PvPGridHost.getChildren();

        for (Node node : board) {
            if (node instanceof Block) {
                if (((Block) node).getRowVal() >= 11) {
                    if (enemyShip.equals(((Block) node).getBlock())) {
                        if (((Block) node).getFill() == Color.CYAN) {
                            ((Block) node).setFill(Color.RED);
                        } else if (((Block) node).getFill() == Color.BLACK) {
                            ((Block) node).setFill(Color.LIGHTGRAY);
                        }
                    }
                }
            }

        }
    }

    private void setOnlineMyBlocksList(List<String> myships) {
        ObservableList<Node> board = PvPGridHost.getChildren();

        for (Node node : board) {
            if (node instanceof Block) {
                for (String position : myships) {
                    if (((Block) node).getRowVal() >= 11) {
                        if (position.equals(((Block) node).getBlock())) {
                            ((Block) node).setFill(Color.CYAN);
                        }
                    }
                }


            }
        }

    }

    private void getOpponentMove() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket clientSocket = null;

                try {
                    clientSocket = serverSocket.accept();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e){
                    System.out.println("Server has been closed");
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    InputStream is = clientSocket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);

                    BufferedReader bufferedReader = new BufferedReader(reader);

                    while ((message = bufferedReader.readLine()) != null) {
                        setOnlineEnemyString(message);
                        sleep(bufferedReader);
                        if (!bufferedReader.ready()) {
                            break;
                        }
                    }

                    closeWindow();


                } catch (IOException e) {
                    System.out.println("[Commication Error] could not establish stable communications with the server, try again: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMoveToOpponent(Block block) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clientSocket = null;
                String serverHostName = "localhost";
                int port = 4000;

                try {
                    clientSocket = new Socket(serverHostName, port);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try{
                  OutputStream outputStream = clientSocket.getOutputStream();
                  PrintStream printStream = new PrintStream(outputStream);

                    String blockPostion = block.getBlock();
                    String newBlockPosition = equateOnlineBlock(blockPostion);;

                    printStream.println(newBlockPosition);

                    clientSocket.close();
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