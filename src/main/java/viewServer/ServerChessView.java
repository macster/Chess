package viewServer;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentChange.Type;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import view.IChessViewer;
import view.IChessViewerControl;

public class ServerChessView implements IChessViewer {

  private boolean whiteOrBlack;
  private BoardData board;

  private DocumentReference ref;
  private IChessViewerControl controller;
  private ActionEventListener actionListener;
  private ActionData action;
  private CollectionReference requestRef;

  public ServerChessView() {
  }

  public static ServerChessView newInstance(DocumentReference firebaseReference,
      String player,
      boolean whiteOrBlack) {
    ServerChessView p = new ServerChessView();
    p.board = new BoardData();
    p.ref = firebaseReference;
    p.whiteOrBlack = whiteOrBlack;
    p.board.whiteOrBlack = whiteOrBlack;
    p.board.player = player;
    p.ref.set(p.board);
    return p;
  }

  @Override
  public void printOut(String message) {
    // System.out.println("[printOut]: "+message);
  }

  @Override
  public void printTemp(String temp) {
    // System.out.println("[printTemp]: "+temp);
  }

  @Override
  public void cleanTemp() {
    // TODO Auto-generated method stub
  }

  @Override
  public void setStatusLabelText(String status) {
    this.ref.update("status", status);
  }

  @Override
  public void highLight(int file, int rank) {
    this.board.highLight(getI(file, rank), getJ(file, rank));
  }

  @Override
  public void deHighLightWholeBoard() {
    this.board.deHighLightWholeBoard();
  }

  @Override
  public void repaint() {
    this.ref.set(this.board);
  }

  private CollectionReference actionRef;

  @Override
  public void upDatePiece(int file, int rank, char pieceType, boolean whiteOrBlack) {
    this.board.updatePiece(getI(file, rank), getJ(file, rank), pieceType, whiteOrBlack);
  }

  private int getFile(int i, int j) {
    if (this.whiteOrBlack) {
      return 1 + j;
    }
    return 8 - j;
  }

  private int getJ(int file, int rank) {
    if (this.whiteOrBlack) {
      return file - 1;
    }
    return 8 - file;
  }

  private int getRank(int i, int j) {
    if (this.whiteOrBlack) {
      return 8 - i;
    }
    return 1 + i;
  }

  private int getI(int file, int rank) {
    if (this.whiteOrBlack) {
      return 8 - rank;
    }
    return rank - 1;
  }

  @Override
  public void clearLabel(int file, int rank) {
    this.board.clearPiece(getI(file, rank), getJ(file, rank));
  }

  @Override
  public void initializeViewController(IChessViewerControl controller) {
    this.controller = controller;
    this.actionListener = new ActionEventListener(ref.collection("action"));
    this.requestRef = ref.collection("request");
  }

  private class ActionEventListener implements EventListener<QuerySnapshot> {

    public ActionEventListener(CollectionReference actionRef) {
      ServerChessView.this.actionRef = actionRef;
      actionRef.addSnapshotListener(this);
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirestoreException error) {
      (new Thread(() -> {
        for (DocumentChange documentChange : value.getDocumentChanges()) {
          if (documentChange.getType() == Type.ADDED) {
            action = documentChange.getDocument().toObject(ActionData.class);
            if (action == null) {
              return;
            }
            if (action.click != null) {
              int i = (int) action.click.i;
              int j = (int) action.click.j;
              controller.click(getFile(i, j), getRank(i, j), whiteOrBlack);
            }
            if (action.requestDraw == true) {
              controller.askForDraw(whiteOrBlack);
            }
            if (action.resign == true) {
              controller.resign(whiteOrBlack);
            }
            if (action.agreeDraw == true) {
              synchronized (ServerChessView.this) {
                ServerChessView.this.notifyAll();
              }
            }
            if (action.promotionTo != null) {
              synchronized (ServerChessView.this) {
                ServerChessView.this.notifyAll();
              }
            }
            actionRef.document(documentChange.getDocument().getId()).delete();
          }
        }
      })).start();
    }
  }

  @Override
  public synchronized boolean askForDraw() {
    this.requestRef.add(ImmutableMap.of("askForDraw", true));
    try {
      wait();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return action.agreeDraw;
  }

  @Override
  public synchronized String getPromoteTo() {
    this.requestRef.add(ImmutableMap.of("promotionTo", true));
    try {
      wait();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return this.action.promotionTo;
  }

  @Override
  public void close() {
    // this.ref.rem(this.actionListener);
  }
}
