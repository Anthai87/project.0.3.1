/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.initial.view;

import com.sun.istack.internal.NotNull;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.initial.controller.GameController;
import dk.dtu.compute.se.pisd.initial.model.Board;
import dk.dtu.compute.se.pisd.initial.model.Phase;
import dk.dtu.compute.se.pisd.initial.model.Player;
import dk.dtu.compute.se.pisd.initial.model.Space;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class BoardView extends VBox implements ViewObserver {

    private Board board;

    private GridPane mainBoardPane;
    private SpaceView[][] spaces;

    private PlayersView playersView;

    private Label statusLabel;

    private GameController gameController;
    private SpaceEventHandler spaceEventHandler;

    public BoardView(@NotNull GameController gameController) {
        this.gameController = gameController;
        board = gameController.board;

        mainBoardPane = new GridPane();
        playersView = new PlayersView(gameController);
        statusLabel = new Label("<no status>");

        this.getChildren().add(mainBoardPane);
        this.getChildren().add(playersView);
        this.getChildren().add(statusLabel);

        spaces = new SpaceView[board.width][board.height];

        spaceEventHandler = new SpaceEventHandler(gameController);

        for (int x = 0; x < board.width; x++) {
            for (int y = 0; y < board.height; y++) {
                Space space = board.getSpace(x, y);
                SpaceView spaceView = new SpaceView(space);
                spaces[x][y] = spaceView;
                mainBoardPane.add(spaceView, x, y);
                spaceView.setOnMouseClicked(spaceEventHandler);
            }
        }

        update(board);
        board.attach(this);
    }

    public SpaceView getSpaceView(Space space) {
        if (space != null && space.board == this.board &&
                space.x < board.width && space.y < board.height ) {
            SpaceView spaceView = spaces[space.x][space.y];
            if (spaceView.space == space) {
                return spaceView;
            }
        }
        return null;
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == board) {
            Phase phase = board.getPhase();
            String text = "";
            text = "Phase: " + phase.name();
            if (phase == Phase.ACTIVATION) {
                text = text + ", register = " + board.getStep() + ", player = " + board.getCurrentPlayer().getName();
            } else if (phase == Phase.PLAYER_INTERACTION) {
                text = text + ", player = " + board.getCurrentPlayer().getName();
            }
            statusLabel.setText(text);
        }
    }

    // XXX this handler and its uses should eventually be deleted! This is just to help test the
    //      behaviour of the game by being able to explicitly move the players on the board!
    private class SpaceEventHandler implements EventHandler<MouseEvent> {

        final public GameController gameController;

        public SpaceEventHandler(@NotNull GameController gameController) {
            this.gameController = gameController;
        }

        @Override
        public void handle(MouseEvent event) {
            Object source = event.getSource();
            if (source instanceof SpaceView) {
                SpaceView spaceView = (SpaceView) source;
                Space space = spaceView.space;
                Board board = space.board;
                Player player = space.getPlayer();
                if (player != null && board == gameController.board) {
                    if (event.isShiftDown()) {
                        gameController.turnRight(player);
                    } else if (event.isControlDown()) {
                        gameController.turnLeft(player);
                    } else {
                        gameController.moveForward(player);
                    }
                    event.consume();
                }
            }
        }

    }

}
