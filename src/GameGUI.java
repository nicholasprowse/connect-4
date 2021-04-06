
public class GameGUI {
	
	private static final char EMPTY_CIRCLE = '\u25EF',
							  SOLID_CIRCLE = '\u25CF',
							  DOWN_ARROW   = '\u25BC';
	private GameState game;
	private int index = 0;
	private String[] players;
	public GameGUI(String[] players) {
		this.players = players;
		game = new GameState();
	}
	
	public void drawGameBoard() {
		for(int i = 0; i < 49; i++) {
			if(i % 7 == 6)
				continue;
			char symbol = EMPTY_CIRCLE;
			int colour = Terminal.WHITE;
			if(((game.getMask() >> i) & 1) == 1) {
				symbol = SOLID_CIRCLE;
				colour = ((game.getPos() >> i) & 1) == (game.playerToMove()) ? 
						Terminal.YELLOW : Terminal.RED;
			}
			Terminal.addString(7 - i % 7, 3 + 2 *(i/7), symbol, 
					colour + Terminal.BRIGHT);
		}
		Terminal.addString(8, 2, "Press q to quit");
	}
	
	public boolean displayEndgame() {
		if(game.opponentWon() || game.getMoves() == 42) {
			drawGameBoard();
			String winner = game.playerToMove() != 0 ? "    Red wins!    ":
													   "  Yellow  wins!  ";
			if(game.getMoves() == 42)
				winner = "      Draw!      ";
			Terminal.addString(8, 1, winner);
			Terminal.addString(9, 1, "  Press any key");
			Terminal.getKeyPressed(true, true);
			Terminal.clearScreen();
			return true;
		}
		
		return false;
	}
	
	public void dropPiece(int col) {
		try {
			long end = ((game.getMask() + GameState.BOTTOM_MASK) >> (col * 7)) & GameState.LEFT_MASK;
			int colour = game.playerToMove() == 0 ? Terminal.RED : Terminal.YELLOW;
			Terminal.addString(2, 3 + 2 * col, SOLID_CIRCLE, Terminal.BRIGHT + colour);
			for(int i = 4; (1L << i) > end; i--) {
				Thread.sleep(50);
				Terminal.addString(6 - i, 3 + 2 * col, EMPTY_CIRCLE);
				Terminal.addString(7 - i, 3 + 2 * col, SOLID_CIRCLE, Terminal.BRIGHT + colour);
			}
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void playMove() {
		if(players[game.getMoves() % 2].equals("c"))
			playComputerMove();
		else
			playHumanMove();
	}
	
	private void playComputerMove() {
		drawGameBoard();
		long start = System.currentTimeMillis();
		int move = game.getOptimalMove();
		try {
			int time = (int)(System.currentTimeMillis() - start);
			if(time < 750)
				Thread.sleep(750 - time);
			int colour = game.playerToMove() == 0 ? Terminal.RED : Terminal.YELLOW;
			Terminal.addString(1, 3 + 2 * move, DOWN_ARROW, Terminal.BRIGHT + colour);
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Terminal.clearLine(1);
		dropPiece(move);
		game = game.makeMove(move);
		String key = Terminal.getKeyPressed(false, false);
		if(key.equals("q")) {
			Terminal.close();
			System.exit(0);
		}
	}
	
	private void playHumanMove() {
		drawGameBoard();
		while(!game.validMove(index))
			index = (index + 1) % 7;
		while(true) {
			int colour = game.playerToMove() == 0 ? Terminal.RED : Terminal.YELLOW;
			Terminal.clearLine(1);
			Terminal.addString(1, 3 + 2 * index, DOWN_ARROW, Terminal.BRIGHT + colour);
			String key = Terminal.getKeyPressed(true, true);
			
			if(key.equals(Terminal.KEY_RIGHT)) {
				index = (index + 1) % 7;
				while(!game.validMove(index))
					index = (index + 1) % 7;
			}
			if(key.equals(Terminal.KEY_LEFT)) {
				index = (index + 6) % 7;
				while(!game.validMove(index))
					index = (index + 6) % 7;
			}
			
			int keyVal = (int)key.charAt(0);
			if(keyVal == 13 || keyVal == 10) {
				Terminal.clearLine(1);
				dropPiece(index);
				game = game.makeMove(index);
				return;
			}
			if(key.equals("q")) {
				Terminal.close();
				System.exit(0);
			}
		}
	}
	
}
