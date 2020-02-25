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
package dk.dtu.compute.se.pisd.initial.controller;

import com.sun.istack.internal.NotNull;

import dk.dtu.compute.se.pisd.initial.model.*;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;

    public GameController(Board board) {
        this.board = board;
    }

    public void moveForward(Player player) {
        if (player != null && player.board == board) {
            Space currentPosition = player.getSpace();

            int x = currentPosition.x;
            int y = currentPosition.y;

            Heading heading = player.getHeading();

            switch (heading) {
                case EAST:
                    x = (x+1)%player.board.width;
                    break;
                case WEST:
                    x = (x+player.board.width-1)%player.board.width;
                    break;
                case NORTH:
                    y = (y+player.board.height-1)%player.board.height;
                    break;
                case SOUTH:
                    y = (y+1)%player.board.height;
                    break;
                default:
            }
            Space newPosition = board.getSpace(x, y);
            if (newPosition != null &&
                    newPosition.getPlayer() == null &&
                    newPosition != currentPosition) {
                newPosition.setPlayer(player);
            }
        }
    }

    public void fastForward(Player player) {
        moveForward(player);
        moveForward(player);
    }

    public void turnRight(Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
            }
    }

    public void turnLeft(Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());

        }
    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null & targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    public void executePlayersOption(Player player, Command option) {
        if (player != null && player.board == board && board.getCurrentPlayer() == player) {
            board.setPhase(Phase.ACTIVATION);
            execute(option);
        }
    }

    public void executePrograms() {
        board.setStepMode(false);
        while (board.getPhase() == Phase.ACTIVATION) {
            executeStep(null);
        }
    }

    private void execute(Command command) {
        executeStep(command);
        if (board.getPhase() == Phase.ACTIVATION && !board.isStepMode()) {
            executePrograms();
        }
    }

    public void initializeProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    player.getProgramField(j).setCard(null);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    player.getCardField(j).setCard(generateRandomCommandCard());
                }
            }
        }
    }

    public void executeStep() {
        board.setStepMode(true);
        executeStep(null);
    }

    private void executeStep(Command option) {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                if (option != null) {
                    executeCommand(currentPlayer, option);
                } else {
                    executeCommandCard(currentPlayer, currentPlayer.getProgramField(step).getCard());
                }
                if (board.getPhase() == Phase.ACTIVATION) {
                    if (currentPlayer.no + 1 < board.getPlayersNumber()) {
                        board.setCurrentPlayer(board.getPlayer(currentPlayer.no + 1));
                    } else {
                        step++;
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    }
                }
            }
            if (board.getPhase() == Phase.ACTIVATION  && (step < 0 || step >= Player.NO_REGISTERS)) {
                initializeProgrammingPhase();
            }
        }

        if (board.getPhase() == Phase.INITIALISATION) {
            initializeProgrammingPhase();
        }
    }

    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    private void executeCommandCard(@NotNull Player player, CommandCard card) {
        if (card != null) {
            executeCommand(player, card.command);
        }
    }

    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {
            // XXX This is an very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a much more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).
            if (command.isInteractive()) {
                board.setPhase(Phase.PLAYER_INTERACTION);
            } else {
                switch (command) {
                    case FORWARD:
                        this.moveForward(player);
                        break;
                    case RIGHT:
                        this.turnRight(player);
                        break;
                    case LEFT:
                        this.turnLeft(player);
                        break;
                    case FAST_FORWARD:
                        this.fastForward(player);
                        break;
                    default:
                        // DO NOTHING (for now)
                }
            }
        }
    }
}
