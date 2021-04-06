import java.io.IOException;
import java.util.Scanner;

public class Connect4 {

	public static void main(String[] args) throws IOException, InterruptedException {
		//GameState.loadSavedValues(Connect4.class.getResourceAsStream("eight_move_games.txt"));
		GameState.loadSavedValues(
				GameState.class.getResourceAsStream("eight_move_games.txt"));
		
		Terminal.displayCursor(false);
		Terminal.clearScreen();
		
		GameGUI game = new GameGUI(args);
		while(!game.displayEndgame())
			game.playMove();
				
		Terminal.close();
				
//		findGameValues(new GameState());
//		writeSavedValues();
	}
	
	static void testFile() {
		Scanner scanner = new Scanner(Connect4.class.getResourceAsStream("Test_L1_R2"));
		int numGames = 0;
		long startTime = System.nanoTime();
		while(scanner.hasNext()) {
			String[] x = scanner.nextLine().split(" ");
			GameState state = new GameState(x[0]);
			int value = state.value();
			if(value != Integer.parseInt(x[1]))
				System.out.println("INCORRECT: " + x[0] + ": " + x[1] + " != " + value);
			numGames++;
			System.out.println(numGames);
		}
		System.out.println("Average time: " + (System.nanoTime() - startTime) / (1e9 * numGames));
		scanner.close();
	}

}

