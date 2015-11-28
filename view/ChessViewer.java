package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.print.DocFlavor.INPUT_STREAM;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ChessViewer extends JFrame {

	private static final Font FONT_STATUS_LABEL = new Font("Serif", Font.PLAIN, 40);
	private static final int CONSOLE_FONT_SIZE = 20;
	private static final Font FONT_CONSOLE = new Font("Serif", Font.PLAIN, CONSOLE_FONT_SIZE);

	private boolean wb;
	private ChessViewerControl viewControl;
	private JLabel statusLabel;
	private SquareLabel[][] labels;
	private JTextArea myConsole;
	private String existence;
	private ConsoleListener listener;
	private ArrayList<SquareLabel> highlighted;

	private boolean waitForResponse;
	
	/**
	 * construct a chess view given a controller
	 * 
	 * @param controller
	 * @param b
	 */
	public ChessViewer(ChessViewerControl controller, boolean whiteOrBlack) {
		this.viewControl = controller;
		this.wb = whiteOrBlack;
		highlighted = new ArrayList<>();
		
		if (wb) {
			setTitle("The Great Chess Game white view");
		} else {
			setTitle("The Great Chess Game black view");
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// configure chess board
		JPanel chessBoardSpace = new JPanel();
		chessBoardSpace.setLayout(new FlowLayout());
		chessBoardSpace.setVisible(true);
		JPanel chessBoard = new JPanel();
		chessBoard.setSize(SquareLabel.SQUARE_WIDTH * 9, SquareLabel.SQUARE_WIDTH * 9);
		chessBoard.setLayout(new GridLayout(9, 9));
		chessBoard.setVisible(true);
		labels = new SquareLabel[9][9];

		for (int j = 0; j < 9; j++) {
			for (int i = 0; i < 9; i++) {
				if (j == 8) {
					labels[i][j] = new SquareLabel();
					String s = "";
					if (i > 0)
						s += (char) (wb ? (i + 96) : (105 - i));
					labels[i][j].setText(s);
					labels[i][j].setOpaque(false);
				} else if (i == 0) {
					labels[i][j] = new SquareLabel();
					String s = "";
					s += wb ? (8 - j) : j + 1;
					labels[i][j].setText(s);
					labels[i][j].setOpaque(false);
				} else {
					labels[i][j] = new SquareLabel(i, j, controller, wb);
				}
				chessBoard.add(labels[i][j]);
			}
		}
		chessBoardSpace.add(chessBoard);

		// configure status label
		statusLabel = new JLabel("            Welcome to Wonderful Chess Game             ", JLabel.CENTER);
		statusLabel.setFont(FONT_STATUS_LABEL);
		statusLabel.setVisible(true);

		// configure console panel
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new FlowLayout());
		consolePanel.setVisible(true);
		myConsole = new JTextArea("Welcome to little Chess Game. Enter \"help\" for instructions.\n",
				130 / CONSOLE_FONT_SIZE, 1000 / CONSOLE_FONT_SIZE);
		myConsole.setFont(FONT_CONSOLE);
		listener = new ConsoleListener();
		myConsole.addKeyListener(listener);
		existence = myConsole.getText();
		consolePanel.add(new JScrollPane(myConsole));

		add(statusLabel, BorderLayout.NORTH);
		add(chessBoardSpace, BorderLayout.CENTER);
		add(consolePanel, BorderLayout.SOUTH);
		setVisible(true);
		pack();
	}

	public void setSymbolProvider(ChessSymbolProvider symProvider) {
		for (SquareLabel[] sqlist : labels) {
			for (SquareLabel sq : sqlist) {
				sq.setSymbolProvider(symProvider);
			}
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return {link {@link SquareLabel} at (x , y) coordinate
	 */
	public SquareLabel labelAt(int x, int y) {
		return wb ? labels[x][8 - y] : labels[9 - x][y - 1];
	}

	/**
	 * print the message in the console.
	 * 
	 * @param message
	 */
	public void printOut(String message) {
		existence = myConsole.getText();
		if (message != null) {
			existence = existence + message + "\n";
			myConsole.setText(existence);
		}
	}

	/**
	 * print out the temporal string in the console, without updating the
	 * record. The temp string printed can be clear with
	 * {@link ChessViewer#cleanTemp()}
	 * 
	 * @param temp
	 */
	public void printTemp(String temp) {
		myConsole.setText(existence + temp);
	}

	/**
	 * erase the temporal string in the console printed by
	 * {@link ChessViewer#printTemp(String)}
	 */
	public void cleanTemp() {
		myConsole.setText(existence);
	}

	/**
	 * 
	 * @param str
	 */
	public void setStatusLabelText(String str) {
		statusLabel.setText(str);
	}

	public void highLightAll(ArrayList<SquareLabel> hightlight) {
		highlighted = hightlight;
		for (SquareLabel sqrl : highlighted)
			sqrl.highLight();
	}

	/**
	 * dehighlight the whole board
	 */
	public void deHighLightWholeBoard() {
		for (SquareLabel sqrl : highlighted)
			sqrl.deHighLight();
		highlighted = new ArrayList<>();
	}

	public String getResponse() {
		waitForResponse = true;
		while (waitForResponse) {
//			synchronized (listener) {
//				try {
//					repaint();
					System.out.println("start waiting" + waitForResponse);
//					wait();
//					System.out.println("finish waiting" + waitForResponse);
//				} catch (InterruptedException e) {
//				}
//			}
		}
		return listener.input;
	}

	public void notifyResponse() {
		waitForResponse = false;
		this.notifyAll();
	}

	class ConsoleListener extends KeyAdapter {
		String input;

		@Override
		public void keyReleased(KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
				String text = myConsole.getText();
				if (existence.length() < text.length()) {
					input = text.substring(existence.length(), text.length() - 1);
					if (input.length() > 0) {
						viewControl.handleCommand(input, wb);
					}
				}
			}
		}

	}

}
