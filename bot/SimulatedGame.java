package bot;

import game.RealGame;
import game.Tetrimino;
import game.TetrisGame;
import game.Tile;

public class SimulatedGame extends TetrisGame{
	/*
	 * Allows creation of a simulation by using the current state of the real game
	 * This allows us to test all possible moves and retrieve game states from them in order
	 * to determine which is the best move
	 * 
	 * This class will be created by using TetrisGame as a parameter.
	 * It will recreate the board state
	 * 
	 * It will have a simulate move method which takes in a tetrimino and an input of moves.
	 * 
	 * There will also be a score method which calculates the score of the current state
	 */
	
	private Tetrimino currentTetrimino;
	
	public SimulatedGame(Tetrimino currentTetrimino) {
		this.currentTetrimino = currentTetrimino;
		
		RealGame game = RealGame.getInstance();
		
		tiles = new Tile[game.height()][game.width()];
		
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[0].length; j++) {
				
				Tile tile = new Tile(i,j);
				tile.setState(game.get(i, j).state());
				tiles[i][j] = tile;
				
			}
		}
		
	}
	
	public void simulateMove(String move) {
		/*
		 * Figure out position to drop in
		 *  -> can deduce from tetrimino's orientation and start position
		 * Figure out how far down it will go
		 *  -> need to get height of each block
		 * Simulate move
		 *  -> build the tetrimino from the deepest point
		 * Check if lines made
		 * 
		 * 
		 * alternative:
		 * Find start position
		 * Check the lowest tetrimino can go before colliding
		 * Build tetrimino from there
		 */
		
		int orientation = 0;
		int leftMovement = 0;
		int rightMovement = 0;
		
		
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
		
		Tile[][] tileRepresentation = currentTetrimino.tileRepresentation(orientation);
		
		int startingPosition = currentTetrimino.startingPosition(orientation) - leftMovement + rightMovement;
		
		boolean hasCollided = false;
		
		int startingHeight = 0;
		
		for (startingHeight = 0; startingHeight < height() - currentTetrimino.height(orientation); startingHeight++) {
			int tetriminoI = 0;
			int tetriminoJ = 0;
			
			for (int i = startingHeight; i < startingHeight + currentTetrimino.height(orientation); i++) {
				tetriminoJ = 0;
				
				for (int j = startingPosition; j < startingPosition + currentTetrimino.width(orientation); j++) {
					
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
		
		startingHeight--;

		for (int i = startingHeight; i < startingHeight + currentTetrimino.height(orientation); i++) {
			tetriminoJ = 0;

			for (int j = startingPosition; j < startingPosition + currentTetrimino.width(orientation); j++) {

				if (tileRepresentation[tetriminoI][tetriminoJ].filled()) {
					tiles[i][j].setState(tileRepresentation[tetriminoI][tetriminoJ].state());

				}
						
				tetriminoJ++;
			}
			
			tetriminoI++;
		}		
		
		// Check collision for each line
		
	}
	
	public int calculateScore() {
		return 0;
	}
	
}
