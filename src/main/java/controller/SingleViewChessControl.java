package controller;

import model.Move;
import model.Piece.Player;
import view.ChessViewer;
import view.IChessViewerControl;

/**
 * The chess controller opens a single chess view
 * 
 * @author zhang
 *
 */
public class SingleViewChessControl extends ViewController {
	private ChessViewer view;

	/**
	 * start my little chess game!!!!
	 * 
	 * @param args
	 *            ignored
	 */
	public SingleViewChessControl() {
		super();
		view = new ChessViewer(this, "The Great Chess Game", true);
		updateChessBoard();
	}

	@Override
	public ChessViewer chooesView(boolean whiteOrBlack) {
		return view;
	}

	protected void updateGuiToMove(Move previousMove) {
		updateChessBoard();
		
		view.setStatusLabelText(chess.lastMoveDiscript());
		view.cleanTemp();
		view.printOut(chess.lastMoveOutPrint());
		view.printOut("Next move -- " + side(previousMove.getWhoseTurn() == Player.BLACK));
	}

	@Override
	public void handleCommand(String input, boolean isWhiteView) {
		handleSingleCommand(view, input, isWhiteView);
	}

	public static void main(String[] args) {
		new SingleViewChessControl();
	}
}
