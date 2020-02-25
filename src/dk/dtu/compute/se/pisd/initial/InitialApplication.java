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
package dk.dtu.compute.se.pisd.initial;

import dk.dtu.compute.se.pisd.initial.controller.GameController;


import dk.dtu.compute.se.pisd.initial.model.Board;
import dk.dtu.compute.se.pisd.initial.model.Player;

import dk.dtu.compute.se.pisd.initial.view.BoardView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static dk.dtu.compute.se.pisd.initial.model.Heading.WEST;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class InitialApplication extends Application {

    private Board board;
    private BoardView boardView;

    private GameController gameController;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        // creates the model
        board = new Board(8, 8);
        // and game controller
        gameController = new GameController(board);

        Player player = new Player(board, "red", 0);
        board.addPlayer(player);
        board.setCurrentPlayer(player);

        player.setSpace(board.getSpace(0,0));
        player.setHeading(WEST);

        Player player2 = new Player(board, "green", 1);
        player2.setSpace(board.getSpace(1,0));
        board.addPlayer(player2);

        // create the primary scene
        primaryStage.setTitle("Initial Application");

        BorderPane root = new BorderPane();
        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);

        // creates the view (for the model)
        boardView = new BoardView(gameController);

        // add the view and show it
        root.setCenter(boardView);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene(); // this is to fix a likely bug with the nonresizable stage
        primaryStage.show();

        gameController.initializeProgrammingPhase();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}