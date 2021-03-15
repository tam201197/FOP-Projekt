package fop.model.board;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import fop.model.cards.CardAnchor;
import fop.model.cards.GoalCard;
import fop.model.cards.PathCard;
import fop.model.graph.Graph;

/**
 * 
 * Stellt das Wegelabyrinth als Liste von Karten und als Graph dar.
 *
 */
public class Gameboard {
	
	protected final Map<Position, PathCard> board = new HashMap<>();
	protected final Graph<BoardAnchor> graph = new Graph<>();
	
	/**
	 * Erstellt ein leeres Wegelabyrinth und platziert Start- sowie Zielkarten.
	 */
	public Gameboard() {
		clear();
	}
	
	/**
	 * Zum Debuggen kann hiermit der Graph ausgegeben werden.<br>
	 * Auf {@code http://webgraphviz.com/} kann der Code dargestellt werden.
	 */
	public void printGraph() {
		graph.toDotCode().forEach(System.out::println);
	}
	
	/**
	 * Leert das Wegelabyrinth.
	 */
	public void clear() {
		board.clear();
		graph.clear();
	}
	
	// add, remove //
	
	/**
	 * Setzt eine neue Wegekarte in das Wegelabyrinth.<br>
	 * Verbindet dabei alle Kanten des Graphen zu benachbarten Karten,
	 * sofern diese einen Knoten an der benachbarten Stelle besitzen.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu platzierende Wegekarte
	 */
	public void placeCard(int x, int y, PathCard card) {
		// TODO Aufgabe 4.1.4
		Position p = new Position(x,y);
		Set<CardAnchor> cardAnchor = card.getGraph().vertices();
		if(!card.isGoalCard() && !card.isStartCard())
			if(!canCardBePlacedAt(x,y,card)) {
				return;
		}
		// stehen lassen
		board.put(p, card);
		for(CardAnchor c1 : cardAnchor) {
			Position pofneighboor = c1.getAdjacentPosition(p);
			BoardAnchor b1 = BoardAnchor.of(p, c1);
			if(isPositionEmpty(pofneighboor.x(),pofneighboor.y())) {
				graph.addVertex(b1);
				for(CardAnchor c2 : card.getGraph().getAdjacentVertices(c1)) {
					BoardAnchor b2 = BoardAnchor.of(p, c2);
					graph.addEdge(b1, b2);
				}
				continue;
			}
			for(CardAnchor c3 : board.get(pofneighboor).getGraph().vertices()) {
				if (c3.equals(c1.getOppositeAnchor())) {
					BoardAnchor b3 = BoardAnchor.of(pofneighboor, c3);
					graph.addEdge(b1, b3);
					break;
				}
			}
		}
		// check for goal cards
		checkGoalCards();
	}
	
	/**
	 * Prüft, ob eine Zielkarte erreichbar ist und dreht diese gegebenenfalls um.
	 */
	private void checkGoalCards() {
		for (Entry<Position, PathCard> goal : board.entrySet().stream().filter(e -> e.getValue().isGoalCard()).collect(Collectors.toList())) {
			int x = goal.getKey().x();
			int y = goal.getKey().y();
			if (existsPathFromStartCard(x, y)) {
				GoalCard goalCard = (GoalCard) goal.getValue();
				if (goalCard.isCovered()) {
					// turn card
					goalCard.showFront();
					// generate graph to match all neighbor cards
					goalCard.generateGraph(card -> doesCardMatchItsNeighbors(x, y, card));
					// connect graph of card
					placeCard(x, y, goalCard);
				}
			}
		}
		
	}
	
	/**
	 * 
	 * Entfernt die Wegekarte an der übergebenen Position.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return die Karte, die an der Position lag
	 */
	public PathCard removeCard(int x, int y) {
		// TODO Aufgabe 4.1.5
		Position p = new Position(x,y);
		for(CardAnchor c : CardAnchor.values()) {
			BoardAnchor b = BoardAnchor.of(x, y, c);
			graph.removeVertex(b);
		}
		return board.remove(p);
	}
	
	
	// can //
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Karte an der übergebene Position platziert werden kann.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu testende Karte
	 * @return {@code true}, wenn die Karte dort platziert werden kann; sonst {@code false}
	 */
	public boolean canCardBePlacedAt(int x, int y, PathCard card) {
		return isPositionEmpty(x, y) && existsPathFromStartCard(x, y) && doesCardMatchItsNeighbors(x, y, card);
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn auf der übergebenen Position keine Karte liegt.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return {@code true}, wenn der Platz frei ist; sonst {@code false}
	 */
	private boolean isPositionEmpty(int x, int y) {
		// TODO Aufgabe 4.1.6
		Position p = new Position(x,y);
		if(board.containsKey(p)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Position von einer Startkarte aus erreicht werden kann.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return {@code true}, wenn die Position erreichbar ist; sonst {@code false}
	 */
	private boolean existsPathFromStartCard(int x, int y) {
		// TODO Aufgabe 4.1.7
		for (Entry<Position, PathCard> start : board.entrySet().stream().filter(e -> e.getValue().isStartCard()).collect(Collectors.toList())) {
			for(CardAnchor cend : CardAnchor.values()) {
				Position pofneighboor = cend.getAdjacentPosition(new Position(x,y));
				if(isPositionEmpty(pofneighboor.x(),pofneighboor.y()))
					continue;
				BoardAnchor bend = BoardAnchor.of(pofneighboor, cend.getOppositeAnchor());
				for(CardAnchor cstart : start.getValue().getGraph().vertices()) {
					BoardAnchor bstart = BoardAnchor.of(start.getKey(), cstart);
					if(graph.hasPath(bstart, bend))
						return true;
				}
			}
		}
		return false;
		// die folgende Zeile entfernen und durch den korrekten Wert ersetzen
//		return board.computeIfAbsent(CardAnchor.left.getAdjacentPosition(Position.of(x + 1, y)), p -> null) == null;
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Karte an der übergebene Position zu ihren Nachbarn passt.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu testende Karte
	 * @return {@code true}, wenn die Karte dort zu ihren Nachbarn passt; sonst {@code false}
	 */
	private boolean doesCardMatchItsNeighbors(int x, int y, PathCard card) {
		// TODO Aufgabe 4.1.8
		for(CardAnchor c1 : CardAnchor.values()) {
			Position pneighboor = c1.getAdjacentPosition(new Position(x,y));
			if(isPositionEmpty(pneighboor.x(),pneighboor.y()))
				continue;
			if(!board.get(pneighboor).isGoalCard() && 
					(!card.getGraph().hasVertex(c1) && graph.hasVertex(BoardAnchor.of(pneighboor, c1.getOppositeAnchor())) || 
					(card.getGraph().hasVertex(c1) && !graph.hasVertex(BoardAnchor.of(pneighboor, c1.getOppositeAnchor())))))
				return false;
		}
		return true;
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn eine aufgedeckte Goldkarte im Wegelabyrinth liegt.
	 * @return {@code true} wenn eine Goldkarte aufgedeckt ist; sonst {@code false}
	 */
	public boolean isGoldCardVisible() {
		return board.values().stream().anyMatch(c -> c.isGoalCard() && ((GoalCard) c).getType() == GoalCard.Type.Gold && !((GoalCard) c).isCovered());
	}
	
	
	// get //
	
	public Map<Position, PathCard> getBoard() {
		return board;
	}
	
	public int getNumberOfAdjacentCards(int x, int y) {
		Set<Position> neighborPositions = Set.of(Position.of(x - 1, y), Position.of(x + 1, y), Position.of(x, y - 1), Position.of(x, y + 1));
		return (int) board.keySet().stream().filter(pos -> neighborPositions.contains(pos)).count();
	}
	
}
