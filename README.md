# connect-4
Console base connect 4 game with a perfect AI. The AI will win every game if it plays first. If it plays second, it is possible to beat it, but only if the first move is played in the center.

The AI is created using the negamax algorithm with alpha beta pruning. Transposition tables, iterative deepening and optimised move order for improved search tree pruning are all used to improve performance. Even with these improvements early moves still take up to an hour to compute. So, the negamax game scores for all games of length 8 have been precomputed and saved in a text file. These are used within the first 8 moves to quickly determine optimal moves. After the eighth move the algorithm is fast enough to be useable, but eighth move can still take around 10 seconds to compute.

This has been tested using the Terminal on MacOS. It should work on Linux, and I expect it to work on windows but it hasn't been tested.

The perfect AI is based on this blog http://blog.gamesolver.org/solving-connect-four/01-introduction/

## CONTROLS
Use the arrow keys to select the column to play. Hit enter to play your move. 

## USAGE
In the Terminal navigate to the directory containing this file, then begin a game using the following command

`../connect4 player1 player2`

Where player1 and player2 are one of 'h' or 'c'. 'h' means that the given player is a human player, while 'c' indicates that the given player is a computer player, and the perfect AI will play. Both arguments are optional, where player1 defaults to 'c', player2 defaults to 'h'.

Below is an example of the AI (red) beating a human (yellow) in the Terminal on MacOS

![alt text](https://github.com/nicholasprowse/connect-4/blob/main/connect-4.gif)
