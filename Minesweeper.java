import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Minesweeper {
	
	private class Cell {
		int val;
		boolean isOpen;
		
		private Cell(int val) {
			this.val = val;
			this.isOpen = false;
		}

		@Override
		public String toString() {
			if (!isOpen) return "+";
			if (val == BOMB) return "*";
			else return val == 0 ? "_" : String.valueOf(val);
		}
	}
	
	private Cell[][] board;
	private Random rand;
	private HashSet<Integer> played;
	private HashSet<Integer> bombs;
	private final int BOMB = -1;
	private boolean WITH_MASK = true;
	private enum Level { 
		EASY, MEDIUM, HARD 
	}
	
	public Minesweeper(int rows, int cols, Level difficulty) {
		if (rows < 3 || cols < 3 || rows > 30 || cols > 30) {
			System.out.println("Minimum size must be 3 * 3 and Maximum size must be 30 * 30!");
			return;
		}
		
		board = new Cell[rows][cols];
		rand = new Random();
		
		int no_bombs = rows * cols;
		int randomness = rand.nextInt(4);
		
		switch (difficulty) {
			case EASY:
				no_bombs = (int) (no_bombs * 0.15 + randomness); // 15% Bombs
				break;
			case MEDIUM:
				no_bombs = (int) (no_bombs * 0.25 + randomness); // 30% Bombs
				break;
			case HARD:
				no_bombs = (int) (no_bombs * 0.50 + randomness); // 50% Bombs
				break;
		}
		
		played = new HashSet<>(rows * cols - no_bombs);
		bombs = new HashSet<>(no_bombs);
		
		//Initialize Board
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				board[i][j] = new Cell(0);
			}
		}
		
		assignBombs(rows, cols, no_bombs);
		
		printSeperator();
		System.out.println("Number of bombs = " + no_bombs);
		print(WITH_MASK);
	}

	private void printSeperator() {
		for (int i = 0; i < Math.pow(board[0].length, 3); i++) System.out.print("#");
		System.out.println();
	}

	private void increaseNeighborsCount(int row, int col) {
		increaseNumber(row, col - 1); // top
		increaseNumber(row + 1, col - 1); // top right
		increaseNumber(row + 1, col); // right
		increaseNumber(row + 1, col + 1); // bottom right
		increaseNumber(row, col + 1); // bottom
		increaseNumber(row - 1, col + 1); // bottom left
		increaseNumber(row - 1, col); // left
		increaseNumber(row - 1, col - 1); // top left
	}

	private void increaseNumber(int row, int col) {
		if (row < 0 || row >= board.length) return;
		if (col < 0 || col >= board[0].length) return;
		if (board[row][col].val == BOMB) return;
		board[row][col].val++;
	}

	private void assignBombs(int rows, int cols, int no_bombs) {
		for (int i = 0; i < no_bombs; i++) {
			int total = rows * cols - 1;
			int random = rand.nextInt(total);
			if (bombs.size() >= no_bombs) break;
			while (bombs.contains(random)) 
				random = (random + 1) % total;
			bombs.add(random);
			int[] rowcol = getRowColFromIndex(random);
			board[rowcol[0]][rowcol[1]].val = BOMB;
			increaseNeighborsCount(rowcol[0], rowcol[1]);
		}
	}
	
	private int[] getRowColFromIndex(int random) {
		int[] rowcol = new int[2];
		int n = board[0].length;
		rowcol[0] = random / n;
		rowcol[1] = random % n;
		return rowcol;
	}
	
	private int getIndexFromRowCol(int row, int col) {
		return row * board[0].length + col;
	}

	// return true if game continues else false
	public boolean play(int row, int col) {
		if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) {
			System.out.println("Invalid input, please enter again : ");
			return true;
		}
		// Check if current cell is BOMB
		if (board[row][col].val == BOMB) {
			print(!WITH_MASK);
			System.out.println("You got destroyed!");
			System.out.println("Game Over!");
			return false;
		}
		// Open cell and all twins if not open
		if (!board[row][col].isOpen) {
			openTwins(row, col, board[row][col].val);
			if (isComplete()) return false;
			print(WITH_MASK);
		} else System.out.println("Cell is already open!");
		return true;
	}

	private boolean isComplete() {
		if (played.size() + bombs.size() == board.length * board[0].length) {
			print(!WITH_MASK);
			System.out.println("Congrats! You WON!!!");
			return true;
		}
		return false;
	}

	private void openTwins(int row, int col, int same_val) {
		if (row < 0 || row >= board.length || col < 0 || col >= board[0].length) return;
		if (board[row][col].isOpen) return;
		if (board[row][col].val == same_val) {
			board[row][col].isOpen = true;
			played.add(getIndexFromRowCol(row, col));
			openTwins(row, col - 1, same_val); // top
			openTwins(row + 1, col, same_val); // right
			openTwins(row, col + 1, same_val); // bottom
			openTwins(row - 1, col, same_val); // left
		}
	}

	private void print(boolean with_mask) {
		printSeperator();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (with_mask) System.out.print(board[i][j] + "\t");
				else System.out.print((board[i][j].val == BOMB ? "*" : board[i][j].val == 0 ? "_" : board[i][j].val) + "\t");
			}
			System.out.println();
		}
		printSeperator();
	}

	public static void main(String[] args) {
		int rows = 10, cols = 5;
		Minesweeper game = new Minesweeper(rows, cols, Level.MEDIUM);
		if (game.board == null) return;
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter row number (1 - " + cols + ") <space> column number (1 - " + rows + ") to play or 'EXIT' to exit : ");
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.equalsIgnoreCase("EXIT")) break;
			try {
				String[] input = line.split(" ");
				int row = Integer.parseInt(input[0]) - 1;
				int col = Integer.parseInt(input[1]) - 1;
				if (!game.play(row, col)) break;
			} catch(Exception e) { 
				System.out.println("Invalid input, please enter again : ");
			}
		}
		in.close();
	}
}
