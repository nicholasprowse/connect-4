import java.io.IOException;

public class Terminal {
	private static final char ESCAPE = '\u001B';
	public static final String KEY_UP    = ESCAPE + "[A",
							   KEY_DOWN  = ESCAPE + "[B",
							   KEY_RIGHT  = ESCAPE + "[C",
							   KEY_LEFT = ESCAPE + "[D";
	public static final int BLACK = 30, RED = 31, GREEN = 32, YELLOW = 33,
			BLUE = 34, MAGENTA = 35, CYAN = 36, WHITE = 37, BRIGHT = 60;
	public static void setCursorPosition(int line, int col) {
		System.out.print(ESCAPE + "[" + line + ";" + col + "H");
	}
	
	public static void addString(int line, int col, Object str, int textColor, int background) {
		setColour(textColor, background);
		setCursorPosition(line, col);
		System.out.print(str);
	}
	
	public static void addString(int line, int col, Object str, int textColor) {
		addString(line, col, str, textColor, BLACK);
	}
	
	public static void addString(int line, int col, Object str) {
		addString(line, col, str, BRIGHT + WHITE);
	}
	
	public static void setColour(int textColor, int background) {
		setTextColour(textColor);
		setBackgroundColour(background);
	}
	
	public static void setTextColour(int color) {
		System.out.print(ESCAPE + "[" + color + "m");
	}
	
	public static void setBackgroundColour(int color) {
		System.out.print(ESCAPE + "[" + (color + 10) + "m");
	}
	
	public static void displayCursor(boolean show) {
		System.out.print(ESCAPE + "[" + (show ? "?25h" : "?25l"));
	}
	
	public static void clearScreen() {
		setBackgroundColour(BLACK);
		System.out.print(ESCAPE + "[2J");
	}
	
	public static void clearScrollBackBuffer() {
		System.out.print(ESCAPE + "[3J");
	}
	
	public static void clearLine(int line) {
		setCursorPosition(line, 1);
		System.out.print(ESCAPE + "[2K");
	}
	
	public static String getKeyPressed(boolean blocking, boolean clearBuffer) {
		try {
			if(clearBuffer)
				System.in.readNBytes(System.in.available());
			if(blocking) {
				int b = System.in.read();
				return (char)b + new String(System.in.readNBytes(System.in.available()));
			}
			else if(System.in.available() > 0)
				return new String(System.in.readNBytes(System.in.available()));
			return "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void close() {
		System.out.print("\u001B[0m");	// Reset colours and other graphics
		Terminal.displayCursor(true);
		Terminal.clearScrollBackBuffer();
		Terminal.setCursorPosition(1, 1);
		// Small delay so that commands are executed before the program terminates
		for(int i = 0; i < 1000; i++);
	}
	
}
