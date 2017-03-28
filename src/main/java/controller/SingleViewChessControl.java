package controller;

import java.util.ArrayList;

import model.ChessGameException;
import model.InvalidMoveException;
import model.Move;
import model.Pawn;
import model.Piece.Player;
import model.Square;
import view.ChessViewer;
import view.IChessViewerControl;
import view.SquareLabel;

/**
 * The chess controller opens a single chess view
 * 
 * @author zhang
 *
 */
public class SingleViewChessControl extends ViewController implements IChessViewerControl {
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
		repaintAll(view);
	}

	@Override
	public ChessViewer chooesView(boolean whiteOrBlack) {
		return view;
	}

	public void restart() {
		restartView(view);
	}

	/**
	 * This method will be called, if the user types a command to make a move.
	 * 
	 * Interpret the command, and find out if it is legal to do make this move.
	 * If it is, make this move.
	 * 
	 * @param s
	 *            the input command
	 * @return
	 */
	public boolean makeMove(String s) {
		Move move = null;
		try {
			move = chess.getMove(s);
		} catch (InvalidMoveException e) {
			switch (e.type) {
			case invalidFormat:
				view.printOut("The command is not in a valid format.");
				break;
			case ambiguousMove:
				view.printOut("Fail to guess move: There is ambiguity, multiple possible moves.");
				break;
			case castleNotAllowed:
				view.printOut("You cannot do castling, please check the rules for castling.");
				break;
			case impossibleMove:
				view.printOut("This is not a possible move.");
				break;
			case incorrectPiece:
				view.printOut("The chessman in the start Position is not correct! "
						+ "\n R(Root), N(Knight), B(Bishop), Q(Queen), K(King), omission for pawn");
				break;
			case pieceNotPresent:
				view.printOut("There is no piece at the start position.");
				break;
			}
		}

		if (move != null) {
			chess.makeMove(move);
			return true;
		}

		return false;
	}

	@Override
	public void click(SquareLabel label, boolean whiteOrBlack) {
		if (chess.hasEnd()) {
			view.printOut("Game is already over! Type restart to start a new game");
		} else {
			Square spot = labelToSquare(label, chess);
			if (chosen != null) {
				if (label.isHighLight() && !spot.equals(chosen.getSpot())) {
					Move move;
					if ((move = chess.performMove(chosen, spot)) == null) {
						throw new ChessGameException(
								"Illegal move of " + chosen.getName() + " did not correctly caught from UI!");
					} else {
						updateGuiToMove(move);
					}
				} else
					view.cleanTemp();
				chosen = null;
				view.deHighLightWholeBoard();
			} else {
				if (spot.occupiedBy(chess.getWhoseTurn())) {
					chosen = spot.getPiece();
					ArrayList<Square> reachable = chosen.getReachableSquares();
					reachable.add(spot);
					ArrayList<SquareLabel> hightlight = getAllViewLabels(reachable, view);
					view.highLightAll(hightlight);

					if (spot.getPiece().isType(Pawn.class))
						view.printTemp(spot.toString());
					else
						view.printTemp(spot.getPiece().getType() + spot.toString());

				}
			}
		}

		repaintAll(view);
	}

	private void updateGuiToMove(Move previousMove) {
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
