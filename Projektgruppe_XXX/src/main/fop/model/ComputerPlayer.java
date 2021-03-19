package fop.model;

import static org.junit.jupiter.api.DynamicTest.stream;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import fop.controller.GameController;
import fop.model.board.Gameboard;
import fop.model.board.Position;
import fop.model.cards.ActionCard;
import fop.model.cards.BrokenToolCard;
import fop.model.cards.Card;
import fop.model.cards.CardAnchor;
import fop.model.cards.FixedToolCard;
import fop.model.cards.GoalCard;
import fop.model.cards.PathCard;
import fop.model.cards.RockfallCard;
import fop.model.cards.ToolType;
import fop.model.cards.GoalCard.Type;
import fop.model.graph.Graph;

import javax.lang.model.util.ElementScanner6;
import javax.swing.SwingWorker;

/***
 * 
 * Stellt einen Computerspieler dar.
 *
 */
public class ComputerPlayer extends Player {

	private ArrayList<Position> goalCardPositions;
	private boolean useMap;

	public ComputerPlayer(String name) {
		super(name);
		GameController.addPropertyChangeListener(GameController.NEXT_PLAYER, evt -> {
			// skip if it is not the players turn
			if (GameController.getActivePlayer() != this)
				return;

			// do action in background worker
			new SwingWorker<Object, Void>() {

				@Override
				protected Object doInBackground() throws Exception {
					sleep(800);
					doAction();
					sleep(800);
					return null;
				}
			}.execute();
		});

		goalCardPositions = new ArrayList<Position>();
		goalCardPositions.add(Position.of(8, 0));
		goalCardPositions.add(Position.of(8, -2));
		goalCardPositions.add(Position.of(8, 2));
		if (role == Role.SABOTEUR)
			useMap = false;
		else
			useMap = true;
	}

	@Override
	public boolean isComputer() {
		return true;
	}

	/**
	 * Pausiert das Programm, damit die Änderungen auf der Benutzeroberfläche
	 * sichtbar werden.
	 * 
	 * @param timeMillis zu wartende Zeit in Millisekunden
	 */
	protected void sleep(int timeMillis) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeMillis);
		} catch (InterruptedException ignored) {
		}
	}

	protected void selectCard(Card card) {
		GameController.selectCard(card);
		sleep(800);
	}

	/**
	 * Führt einen Zug des Computerspielers aus.<br>
	 * Benutzt {@link #selectCard(Card)}, um eine Karte auszuwählen.<br>
	 * Benutzt Methoden in {@link GameController}, um Aktionen auszuführen.
	 * 
	 * @throws FileNotFoundException
	 */
	protected void doAction() throws FileNotFoundException {
		// TODO Aufgabe 4.3.3
		// Sie dürfen diese Methode vollständig umschreiben und den vorhandenen Code
		// entfernen.

		if (useMap) {
			if (lookAtGoalCard())
				return;
		}

		// Prioritäten als nicht Saboteur
		if (role != Role.SABOTEUR) {
			if (repairTool())
				;
			else if (breakRandomTool())
				;
			else if (placePathCard())
				;
			else if (useRockFallCard())
				;
			else
				discardUselessCardNotSaboteur();
			// Prioritäten als Saboteur
		} else {
			if (breakRandomTool())
				;
			else if (useRockFallCard())
				;
			else if (repairTool())
				;
			else if (placePathCard())
				;
			else
				discardUselessCardSaboteur();
		}

	}

	// Beim Aufruf dieser Methode wird versucht möglichst gut in Richtung Zielkarte
	// zu bauen
	protected boolean placePathCard() throws FileNotFoundException {

		if (this.hasBrokenTool())
			return false;

		ArrayList<Card> pathCards = handCards.stream()
				.filter(c -> c.isPathCard() && !c.getName().startsWith("dead") && role != Role.SABOTEUR || c.getName().startsWith("dead") && role == Role.SABOTEUR)
				.collect(Collectors.toCollection(ArrayList::new));

		if (pathCards.isEmpty())
			return false;

		var board = GameController.getGameboard().getBoard();

		ArrayList<PathCardInfo> pCIList = new ArrayList<PathCardInfo>();

		for (Card card : pathCards) {
			PathCardInfo pCI = optimalPathCard((PathCard) card, board);
			if (pCI.inPlay)
				pCIList.add(pCI);
		}

		if (pCIList.isEmpty())
			return false;

		Collections.shuffle(pCIList);
		PathCardInfo optimalPathCard = pCIList.get(0);

		for (PathCardInfo pCI : pCIList) {
			if (pCI.distance < optimalPathCard.distance)
				optimalPathCard = pCI;
		}

		selectCard(optimalPathCard.pathCard);
		GameController.placeSelectedCardAt(optimalPathCard.position.x(), optimalPathCard.position.y());
		return true;
	}

	/**
	 * 
	 * @param card
	 * @param board
	 * @return die Position mit der kleinsten Distanz zu einer der Zielkarten falls
	 *         es mehrere gleiche kleinsten Distanzen gibt wird die erste gefundene
	 *         kleinste Position zurückgegeben
	 */
	protected PathCardInfo optimalPathCard(PathCard card, Map<Position, PathCard> board) {
		PathCardInfo pCI = new PathCardInfo(card, 0, null, false);
		double dst = Double.MAX_VALUE;

		for (Position pos : board.keySet()) {
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {

					if (GameController.canCardBePlacedAt(x + pos.x(), y + pos.y(), card)) {

						Graph<CardAnchor> graph = card.getGraph();
						Set<CardAnchor> ancher = graph.vertices();

						
						// Für jeden Anker wird die Distanz für jede adjacent Position mit jeder
						// Zielkarte berechnet und die kleinste genommen
						for (CardAnchor cardAnchor : ancher) {

							for (Position secPos : goalCardPositions) {

								double newDst = distance(
										cardAnchor.getAdjacentPosition(Position.of(pos.x() + x, pos.y() + y)), secPos);

								if (newDst < dst) {
									pCI.distance = newDst;
									pCI.position = Position.of(x + pos.x(), y + pos.y());
									pCI.inPlay = true;
									dst = newDst;
								}
							}
						}
					}
				}
			}
		}
		return pCI;
	}

	protected double distance(Position pos, Position pos2) {
		double x = pos.x() - pos2.x();
		x *= x;
		double y = pos.y() - pos2.y();
		y *= y;

		return Math.sqrt(x + y);
	}

	protected void discardUselessCardNotSaboteur() throws FileNotFoundException{
		ArrayList<Card> uselessCards = handCards.stream().filter(c -> c.isMap() && !useMap || c.getName().startsWith("dead"))
		.collect(Collectors.toCollection(ArrayList::new));

		if (uselessCards.isEmpty()){
			discardRandomCard();
			return;
		}

		Card card = uselessCards.get((int) (Math.random() * uselessCards.size()));
		selectCard(card);
		GameController.discardSelectedCard();
	}

	protected void discardUselessCardSaboteur() throws FileNotFoundException{
		ArrayList<Card> uselessCards = handCards.stream().filter(c -> c.isMap() || !c.getName().startsWith("dead"))
		.collect(Collectors.toCollection(ArrayList::new));

		if (uselessCards.isEmpty()){
			discardRandomCard();
			return;
		}

		Card card = uselessCards.get((int) (Math.random() * uselessCards.size()));
		selectCard(card);
		GameController.discardSelectedCard();
	}

	protected void discardRandomCard() throws FileNotFoundException {

		// erhalte zufällige Handkarte
		Card card = handCards.get((int) (Math.random() * handCards.size()));

		// wähle Karte aus
		selectCard(card);

		// werfe Karte ab

		GameController.discardSelectedCard();
	}

	// macht ein zufälliges Werkzeug von einem anderem zufälligem Spieler kaputt falls die Möglichkeit da ist
	protected boolean breakRandomTool() {
		ArrayList<Card> lockCards = handCards.stream().filter(c -> c.isBrokenTool())
				.collect(Collectors.toCollection(ArrayList::new));

		if (lockCards.isEmpty())
			return false;

		// erhalte zufällige BreakKarte
		Card card = lockCards.get((int) (Math.random() * lockCards.size()));

		// erhalte Spieler, die nicht der Spieler am Zug sind und mit der ausgewählten Karte beschädigt werden können
		ArrayList<Player> players = Arrays.asList(GameController.getPlayers()).stream()
				.filter(p -> p != this && p.canToolBeBroken((BrokenToolCard) card))
				.collect(Collectors.toCollection(ArrayList::new));

		if (players.isEmpty())
			return false;

		// wähle zufälligen Spieler aus
		Player randomPlayer = players.get((int) Math.random() * players.size());

		// Zerbreche sein Tool
		selectCard(card);
		GameController.breakToolWithSelectedCard(randomPlayer);

		return true;
	}

	// repariert ein Tool von sich oder Spielern selber Rolle, falls die Möglichkeit bestehtSS
	protected boolean repairTool() {
		
		if (!this.hasBrokenTool())
			return false;
		
		ArrayList<Card> fixCards = handCards.stream().filter(c -> c.isFixedTool())
				.collect(Collectors.toCollection(ArrayList::new));

		if (fixCards.isEmpty())
			return false;

		// heilige
			for (Card card : fixCards) {
				// double cast weil warum nicht
				FixedToolCard fTCard = (FixedToolCard) (ActionCard) card;
				for (ToolType toolType : fTCard.getToolTypes()) {
					if (this.hasBrokenTool(toolType)) {
						BrokenToolCard brokenToolCard = this.getBrokenTool(toolType);
						selectCard(card);
						GameController.fixBrokenToolCardWithSelectedCard(this, brokenToolCard);
						return true;
					}
				}
			}
		

		return false;
	}

	// schaut sich einer der Zielkarten an
	protected boolean lookAtGoalCard() {
		ArrayList<Card> cards = handCards.stream().filter(c -> c.isMap())
				.collect(Collectors.toCollection(ArrayList::new));

		if (cards.isEmpty())
			return false;

		Collections.shuffle(goalCardPositions);

		selectCard(cards.get(0));
		Position thisGoalCardsPosition = goalCardPositions.get(0);
		GoalCard goalCard = (GoalCard) (GameController.getCardAt(thisGoalCardsPosition));

		switch (role) {
		case GOLD_MINER:

			// Falls die ZielKarte eine Goldkarte ist, entfernt man beide anderen Zielkarten
			// aus dem Array
			if (goalCard.getType() == Type.Gold) {
				goalCardPositions = new ArrayList<Position>();
				goalCardPositions.add(thisGoalCardsPosition);
				useMap = false;
			} else {
				goalCardPositions.remove(0);
			}

			break;
		case STONE_MINER:
			// Falls die ZielKarte eine Goldkarte ist, entfernt man diese aus dem Array
			if (goalCard.getType() == Type.Gold) {
				goalCardPositions.remove(0);
				useMap = false;
			}
			break;
		default:
			return false;
		}

		GameController.lookAtGoalCardWithSelectedCard(goalCard);

		return true;
	}

	// Macht eine PfadKarte kaputt
	// Als Saboteur, zerstört nur nicht Dead End Karten
	// Als nicht Saboteur, zerstört nur Dead End Karten
	protected boolean useRockFallCard() {
		RockfallCard card = (RockfallCard) handCards.stream().filter(c -> c.isRockfall()).findFirst().orElse(null);

		if (card == null)
			return false;


		double dst = Double.MAX_VALUE;
		Position destroyAtPos = null;

		var board = GameController.getGameboard().getBoard();
		for (Position pos : board.keySet()) {

			PathCard thisCard = GameController.getCardAt(pos);
			if (thisCard.getName().startsWith("dead") && role == Role.SABOTEUR || thisCard.isGoalCard()  || thisCard.isStartCard())
				continue;
			
			if (!thisCard.getName().startsWith("dead") && role != Role.SABOTEUR || thisCard.isGoalCard()  || thisCard.isStartCard())
				continue;

				for (Position goalCardPos : goalCardPositions){
					double dstBetween = distance(pos, goalCardPos);
					if (dstBetween < dst){
						dst = dstBetween;
						destroyAtPos = pos;
					}
				}
		}

		if (destroyAtPos == null)
		return false;

		selectCard(card);
		GameController.destroyCardWithSelectedCardAt(destroyAtPos.x(), destroyAtPos.y());
		return true;
	}

	class PathCardInfo {
		public PathCard pathCard;
		public double distance;
		public Position position;

		public boolean inPlay;

		public PathCardInfo(PathCard pathCard, double distance, Position position, boolean inPlay) {
			this.pathCard = pathCard;
			this.distance = distance;
			this.position = position;
			this.inPlay = inPlay;
		}
	}
}
