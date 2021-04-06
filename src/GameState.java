import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;

public class GameState {
	
	public static final long BOTTOM_MASK = 0x40810204081L,
			BOARD_MASK = 0xFDFBF7EFDFBFL, LEFT_MASK = 0x7F;
	private long mask = 0, pos = 0;
	private int moves = 0;
	private static long[] table = new long[99999989];	// Size must be prime
	
	private static TreeSet<Entry> values = new TreeSet<Entry>();
	private static int hits = 0, misses = 0;
	
	public GameState() {
		this("");
	}
	
	public GameState(String moveSequence) {
		for(int i = 0; i < moveSequence.length(); i++) {
			int move = Integer.parseInt(String.valueOf(moveSequence.charAt(i)));
			pos ^= mask;
			mask |= (mask + (1L << ((move - 1) * 7)));
		}
		moves = moveSequence.length();
	}
	
	private GameState(long mask, long pos, int moves) {
		this.mask = mask;
		this.pos = pos;
		this.moves = moves;
	}
	
	public static void generateGameValues(GameState game) throws IOException {
		if(System.in.available() > 0)
			return;
		if(game.getMoves() == 8) {
			Entry e = new Entry(game.getKey(), (byte)0);
			if(values.contains(e)) {
				e.value = values.ceiling(e).value;
				if(misses > 0)
					hits++;
			} else {
				e.value = (byte) game.value();
				values.add(e);
				misses++;
			}
			GameState reflection = game.horizontalReflection();
			values.add(new Entry(reflection.getKey(), e.value));
			return;
		}
		for(int i = 0; i < 7; i++) {
			if(game.validMove(i)) {
				GameState newGame = game.makeMove(i);
				generateGameValues(newGame);
			}
			if(game.getMoves() <= 2)
				System.out.println(game.getMoves() + ": " + i + ", Hits: " + hits + ", Misses: " + misses);
		}
	}
	
	public static void loadSavedValues(InputStream stream) {
		Scanner scanner = new Scanner(stream);
		while(scanner.hasNextLine()) {
			String s = scanner.nextLine();
			String[] x = s.split(" ");
			values.add(new Entry(Long.parseLong(x[0]), Byte.parseByte(x[1])));
		}
		scanner.close();
	}
	
	public static void writeSavedValues(File file) {
		try {
			FileWriter fw = new FileWriter(file);
			for(Entry e : values)
				fw.write(e.key + " " + e.value + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// If there are multiple equivalent moves, a random one is chosen
	public int getOptimalMove() {
		int maximum = -1000;
		ArrayList<Integer> optimalMoves = new ArrayList<Integer>();
		for(int col = 0; col < 7; col++) {
			if(validMove(col)) {
				GameState next = makeMove(col);
				if(playerWon(next.pos ^ next.mask)) 
					return col;
				int val = -next.value();
				if(val > maximum) {
					maximum = val;
					optimalMoves = new ArrayList<Integer>();
					optimalMoves.add(col);
				} else if(val == maximum) 
					optimalMoves.add(col);
			}
		}
		return optimalMoves.get((int)(Math.random() * optimalMoves.size()));
	}
	
	public int value() {
		Entry e = new Entry(mask + pos, (byte)0);
		if(moves <= 8 && values.contains(e))
			return values.ceiling(e).value;
		
		int min = -(42 - moves) / 2, max = (43 - moves) / 2;
		while(min < max) {
			int middle = min + (max - min) / 2;
			if(middle <= 0 && min/2 < middle) middle = min/2;
		    else if(middle >= 0 && max/2 > middle) middle = max/2;
			int bound = negamax(middle, middle + 1);
			if(bound <= middle)
				max = bound;
			else
				min = bound;
		}
		return min;
	}
	
	public GameState makeMove(int col) {
		return new GameState(mask | (mask + (1L << (col * 7))), 
				pos ^ mask, moves + 1);
	}
	
	public boolean validMove(int col) {
		return ((mask >> (5 + 7 * col)) & 1) == 0;
	}
	
	// Provides the number of winning moves available to the player after the
	// given move is made
	private int moveScore(int col) {
		long newMask = mask | (mask + (1L << (col * 7)));
		long winningMoves = getWinningMoves(newMask, pos | (mask ^ newMask));
		
		int score = 0;
		for(; winningMoves != 0; score++) 
			winningMoves &= winningMoves - 1;
		return score;
	}
	
	// NOTE: This function assumes the current player cannot win this move
	// This should be checked before calling this function
	private long getNonLosingMoves() {
		long possibleMoves = getPossibleMoves();
		long opponentsWinningMoves = getWinningMoves(mask, pos ^ mask);
		// If the opponent can win, then the current player is forced to play
		// in their winning position to block them
		long forcedMoves = possibleMoves & opponentsWinningMoves;
		if(forcedMoves != 0) {
			// If there are multiple forced moves, return 0 (no non losing moves)
			if((forcedMoves & (forcedMoves - 1)) != 0)
				return 0;
			possibleMoves = forcedMoves;
		}
		return possibleMoves & ~(opponentsWinningMoves >> 1);
	}
	
	// Returns bitmask containing the locations of the moves the current player
	// can make to win
	private static long getWinningMoves(long m, long p) {
		// Vertical
		long r = (p << 1) & (p << 2) & (p << 3);
		
		// Horizontal
		r |= (p << 7) & (p << 14) & (p << 21);
		r |= (p >> 7) & (p >> 14) & (p >> 21);
		r |= (p << 7) & (p >>  7) & (p >> 14);
		r |= (p << 7) & (p >>  7) & (p << 14);
		
		// Diagonal \
		r |= (p << 8) & (p << 16) & (p << 24);
		r |= (p >> 8) & (p >> 16) & (p >> 24);
		r |= (p << 8) & (p >>  8) & (p >> 16);
		r |= (p << 8) & (p >>  8) & (p << 16);
		
		// Diagonal /
		r |= (p << 6) & (p << 12) & (p << 18);
		r |= (p >> 6) & (p >> 12) & (p >> 18);
		r |= (p << 6) & (p >>  6) & (p >> 12);
		r |= (p << 6) & (p >>  6) & (p << 12);
		
		return r & (BOARD_MASK ^ m);
	}
	
	// Returns bitmask containing the locations that the tokens would end up in
	// after each possible move
	private long getPossibleMoves() {
		return (mask + BOTTOM_MASK) & BOARD_MASK;
	}
	
	private int negamax(int alpha, int beta) {
		// A filled element should never be zero. The key is only zero if the
		// board is empty, but the empty board is known to have a non-zero
		// negamax value
		Entry e = new Entry(mask + pos, (byte)0);
		if(moves <= 8 && values.contains(e))
			return values.ceiling(e).value;
		
		if(moves == 42)
			return 0;
		
		for(int i = 0; i < 7; i++)
			if(winningMove(i))
				return (43 - moves) / 2;
		
		long possibleMoves = getNonLosingMoves();
		if(possibleMoves == 0)
			return -(42 - moves) / 2;
		
		int min = -(42 - moves) / 2;
		alpha = Math.max(alpha, min);
		if(alpha >= beta)
			return alpha;
		
		int maximum = (41 - moves) / 2;
		long key = mask + pos;
		int index = (int)(key % table.length);
		if((table[index] & 0x00FFFFFFFFFFFFFFL) == key) 
			maximum = (int)(table[index] >> 56);
		
		beta = Math.min(maximum, beta);
		if(alpha >= beta)
			return beta;
		
		int[] moveScores = new int[7];
		for(int i = 0; i < 7; i++)
			moveScores[i] = moveScore(i);
		Integer[] moveOrder = {3, 2, 4, 1, 5, 0, 6};
		
		Arrays.sort(moveOrder, (a, b) -> -Integer.compare(moveScores[a], moveScores[b]));
		
		for(int move : moveOrder) {
			if((possibleMoves & (LEFT_MASK << (move * 7))) != 0) {
				GameState newState = makeMove(move);
				int score = -newState.negamax(-beta, -alpha);
				if(score >= beta)
					return score;
				alpha = Math.max(alpha, score);
			}
		}
		table[index] = ((long)alpha << 56) + key;
		return alpha;
	}
	
	private boolean winningMove(int col) {
		return ((mask >> (5 + 7 * col)) & 1) == 0 
				&& playerWon(pos | ((mask + (1L << (col * 7))) & LEFT_MASK << (col * 7)));
	}
	
	private static boolean playerWon(long p) {
		long m = (p >> 1) & p;
		long r = (m >> 2) & m;
		
		m =  (p >>  7) & p;
		r |= (m >> 14) & m;
		
		m =  (p >>  6) & p;
		r |= (m >> 12) & m;
		
		m =  (p >>  8) & p;
		r |= (m >> 16) & m;
		return r != 0;
	}
	
	private GameState horizontalReflection() {
		long newMask = 0, newPos = 0;
		for(int i = 0; i < 7; i++) {
			newMask |= ((mask >> (i * 7)) & LEFT_MASK) << ((6-i) * 7);
			newPos |= ((pos >> (i * 7)) & LEFT_MASK) << ((6-i) * 7);
		}
		return new GameState(newMask, newPos, moves);
	}
	
	public boolean playerWon() {
		return playerWon(pos);
	}
	
	public boolean opponentWon() {
		return playerWon(pos ^ mask);
	}
	
	public int getMoves() {
		return moves;
	}
	
	public long getMask() {
		return mask;
	}
	
	public long getPos() {
		return pos;
	}
	
	public int playerToMove() {
		return moves % 2;
	}
	
	public long getKey() {
		return mask + pos;
	}
	
	static class Entry implements Comparable<Entry> {
		long key;
		byte value;
		@Override
		public String toString() {
			return "Entry [key=" + key + ", value=" + value + "]";
		}
		public Entry(long key, byte value) {
			this.key = key;
			this.value = value;
		}
		
		public int compareTo(Entry o) {
			return Long.compare(key, o.key);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			return prime + (int) (key ^ (key >>> 32));
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			return key == other.key;
		}
		
	}
	
}
