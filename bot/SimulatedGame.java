package bot;

import java.util.LinkedList;

import game.RealGame;
import game.Tetrimino;
import game.TetrisGame;
import game.Tile;
import game.TileState;

/**
 * This class allows the creation of a simulated game by using the current state of the actual game.
 * 
 * This allows us to test all possible moves for the tetriminos and retrieve their states in order to 
 * determine what the best move is by making use of the simulateMove and calculateScore methods.
 * @author Andy Tang
 *
 */
public class SimulatedGame extends TetrisGame{
	
	private LinkedList<Tetrimino> tetriminoList;
	private boolean hasLost;
	private int clearedLines;
	
	public SimulatedGame() {
		
		tetriminoList = new LinkedList<Tetrimino>();
		
		hasLost = false;
		
		RealGame game = RealGame.getInstance();
		
		tiles = new Tile[game.height()][game.width()];
		
		// Copy the state of the actual game
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				
				Tile tile = new Tile(i,j);
				tile.setState(game.get(i, j).state());
				tiles[i][j] = tile;
				
			}
		}
		
		
	}
	
	/**
	 * Adds the tetrimino to end of the LinkedList
	 */
	public void add(Tetrimino tetrimino) {
		tetriminoList.add(tetrimino);
	}
	
	/**
	 * This method simulates the inputed move for the tetrimino in the head of the Linked List.
	 * 
	 */
	public void simulateMove(String move) {
		
		// Ensures it does not try to poll an empty list
		if (tetriminoList.isEmpty()) {
			return;
		}
		
		// Get head of list
		Tetrimino currentTetrimino = tetriminoList.pollFirst();
		
		int orientation = 0;
		int leftMovement = 0;
		int rightMovement = 0;
		
		// Iterate through the moves String to determine orientation
		// and starting position
		for (char c : move.toCharArray()) {
			
			if (c == 'c') {
				orientation++;
			}
			
			else if (c == 'l') {
				leftMovement++;
			}
			
			else {
				rightMovement++;
			}
			
		}
		
		// Get tile representation of the tetrimino
		Tile[][] tileRepresentation = currentTetrimino.tileRepresentation(orientation);
		
		// Caclulate startingPosition
		int startingPosition = currentTetrimino.startingPosition(orientation) - leftMovement + rightMovement;
		
		// Initialising variables
		boolean hasCollided = false;
		int startingHeight = 0;
		
		// This nested for loop determines the collision point of the tetrimino at its position and orientation
		
		// Iterate through entire height of board (minus the height of the tetrimino to prevent array out of bounds)
		for (startingHeight = 0; startingHeight <= height() - currentTetrimino.height(orientation); startingHeight++) {
			
			// Resetting variables (these are used to retrieve tiles from the tetrimino)
			int tetriminoI = 0;
			int tetriminoJ = 0;
			
			// This nested loop iterates through the size of the tetrimino (its width and height)
			for (int i = startingHeight; i < startingHeight + currentTetrimino.height(orientation); i++) {
				tetriminoJ = 0;
				
				// Checks whether or not each tile of the tetrimino collides with the tile on the board
				for (int j = startingPosition; j < startingPosition + currentTetrimino.width(orientation); j++) {
					
					// Breaks if collision found (since now we have our starting height)
					if (tiles[i][j].filled() && tileRepresentation[tetriminoI][tetriminoJ].filled()) {
						hasCollided = true;
						break;
					}
					
					tetriminoJ++;
				}
				
				if (hasCollided) {
					break;
					
				}
				tetriminoI++;
			}
			
			if (hasCollided) {
				break;
			}
		}
		
		
		int tetriminoI = 0;
		int tetriminoJ = 0;
		
		// Decrement startingHeight to get the height before collision
		startingHeight--;
		
		// This for loop 'draws' the tetrimino onto the board
		if (startingHeight >= 0) {
			
			// Iterate through the size of the tetrimino, starting at the startingHeight that was found
			for (int i = startingHeight; i < startingHeight + currentTetrimino.height(orientation); i++) {
				tetriminoJ = 0;

				for (int j = startingPosition; j < startingPosition + currentTetrimino.width(orientation); j++) {
					
					// Replaces the tile in the board with the tile in the tetrimino ONLY if it is filled
					// This is essentially 'drawing' in the tetrimino
					if (tileRepresentation[tetriminoI][tetriminoJ].filled()) {
						tiles[i][j].setState(tileRepresentation[tetriminoI][tetriminoJ].state());

					}
							
					tetriminoJ++;
				}
				
				tetriminoI++;
			}		
		}
		
		else {
			// If startingHeight is < 0, then the move will result in a loss.
			hasLost = true;
		}
		
		removeFilledLines();
		
	}
	
	/**
	 * This method is used to check the board for any filled lines and removes them
	 */
	private void removeFilledLines() {
		
		clearedLines = 0;
		
		// Iterate through all rows of the game
		for (int i = 0; i < height(); i++) {
			boolean isFilled = true; // reset boolean
			
			// Checks if the entire row is filled
			for (int j = 0; j < width(); j++) {
				if (tiles[i][j].empty()) {
					isFilled = false;
					break;
				}
			}
			
			// If entire row is filled
			if (isFilled) {
				clearedLines++;
				
				// Iterate from that row and continue upwards
				for (int m = i; m > 0; m--) {
					
					// Replace tiles in that row with the tiles in the row above
					for (int n = 0; n < width(); n++) {
						tiles[m][n].setState(tiles[m-1][n].state());
					}
				}
				
				// Set row 0 to all empty tiles
				for (int n = 0; n < width(); n++) {
					tiles[0][n].setState(TileState.EMPTY);
				}
			}
		}
	}
	
	/**
	 * This method calculates a score based on the current board state.
	 * 
	 * It currently uses 3 factors to determine the score:
	 * 	1. Max Height: The number representing the highest column
	 * 	2. Total Height Variance: The sum of the differences between adjacent columns
	 * 	3. Gaps: The total number of gaps
	 * 	4. Lines Sent: The amount of lines sent to the other player
	 * 
	 * The weights for each respective factor determines the importance of that factor in calculating the score
	 */
	public int calculateScore() {
		
		// Score is 0 if the simulated move results in a loss
		if (hasLost) {
			return -10000;
		}
				
		// Weights for scoring
		int heightWeight = 0;
		int varianceWeight= -10;
		int gapWeight = -50;
		int sentLinesWeight = 10;
		
		int maxHeight = columnHeight(0);
		int previousHeight = columnHeight(0);
		int totalHeightVariance = 0;
		
		// Iterate through all columns
		for (int j = 1; j < width(); j++) {
			int currentHeight = columnHeight(j); // Get height of column
			
			// Check if it is higher than the current max height
			if (currentHeight > maxHeight) {
				maxHeight = currentHeight;
			}
			
			// Calculate the height variance with the column to the right of it (difference between heights) and add it to the total
			totalHeightVariance += Math.abs(previousHeight - currentHeight);
			
			previousHeight = currentHeight; // Set the previous height for the next iteration
		}
		
		int gaps = 0;
		
		// Iterate through each column
		for (int j = 0; j < width(); j++) {
			boolean filledFound = false; // resetting boolean
			
			// Iterate down the column
			for (int i = 0; i < height(); i++) {
				
				// Set flag if filled tile found
				if (tiles[i][j].filled()) {
					filledFound = true;
				}
				
				// Increase gap count whenever an empty tile is found after the flag has been set
				else if (filledFound) {
					gaps++;
				}
			}
		}
		
		int sentLines = 0;
		
		if (clearedLines == 1) {
			sentLines = 0;
		}
		
		else {
			sentLines = clearedLines * clearedLines;
		}
		
		// Calculate and return the score
		return 1000 + (heightWeight * maxHeight) + (varianceWeight * totalHeightVariance) + (gapWeight * gaps) + (sentLinesWeight * sentLines);
	}
	
}




















