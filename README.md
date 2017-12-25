# BattleShip-Game-with-Networking-features
a  simple javafx project that allow users to play the game Battleship against a computer AI or against other players

This program starts off with a window that has two battleship boards on them. One for the user and One for the computer. 
You can play against the computer, or you can click on the "host game" or "Search game" button to initialize
multiplayer mode and play against another user. I used socket programming to set up the networking side of the program using
a TCP connection to communicate with clients. At the moment the program has two servers - the host server and the client 
server - that are run when you press either host game" or "Search game", these two server communicate with each to send the 
moves and the ships to other playing users. This multiplayer side of this program can be run on the same computer and 
you can play multiplayer that way.  I have not configured the IP addresses and port numbers for this program to be run on 
two different computers. But it can be easily done if you know the IP of two different computers.



Playing against the computer
![alt tag](https://github.com/oMARIY/BattleShip-Game-with-Networking-features/blob/master/BattleShip/Battleship%20Images/playing%20against%20ComputerAI.png "Playing against the computer")


Loading Multiplayer Mode
![alt tag](https://github.com/oMARIY/BattleShip-Game-with-Networking-features/blob/master/BattleShip/Battleship%20Images/loading%20multiplayer.png "Loading Multiplayer Mode")


Picking my Battleships
![alt tag](https://github.com/oMARIY/BattleShip-Game-with-Networking-features/blob/master/BattleShip/Battleship%20Images/Picking%20my%20BattleShips.png "Picking my Battleships")


Connected to Another User
![alt tag](https://github.com/oMARIY/BattleShip-Game-with-Networking-features/blob/master/BattleShip/Battleship%20Images/Connected%20to%20Player.png "Connected to Another User")


Waiting for Opponent to pick a block
![alt tag](https://github.com/oMARIY/BattleShip-Game-with-Networking-features/blob/master/BattleShip/Battleship%20Images/PvP%20waiting%20for%20opponet.png "Waiting for Opponent to pick a block")


Hope you enjoy using, editing and playing it as much as I enjoyed creating it.
