package fop.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fop.model.ScoreEntry;

/**
 *
 * Wird genutzt, um {@link ScoreEntry} Objekte zu schreiben und zu lesen.<br>
 * <br>
 * Es handelt sich um die Datei {@value #PATH}.<br>
 * Mit {@link #loadScoreEntries()} werden die Elemente gelesen.<br>
 * Mit {@link #writeScoreEntries(List)} werden die Elemente geschrieben.
 *
 */
public final class ScoreEntryIO {

	/** Der Pfad zur ScoreEntry Datei */
	private static String PATH = "highscores.txt";

	private ScoreEntryIO() {}

	/**
	 * Liest eine Liste von {@link ScoreEntry} Objekten aus der Datei {@value #PATH}.<br>
	 * Die Liste enthält die Elemente in der Reihenfolge, in der sie in der Datei vorkommen.<br>
	 * Ungültige Einträge werden nicht zurückgegeben.
	 * @return die ScoreEntry Objekte
	 */
	public static List<ScoreEntry> loadScoreEntries() {
		// TODO Aufgabe 4.2.2
		File file = new File (PATH);
		List <ScoreEntry> result = new ArrayList <>();

		if (!file.exists() || !file.canRead()) {
			return result;
		}
		else {
			BufferedReader reader;
			try{
				reader = new BufferedReader(new FileReader (PATH));
				String line = reader.readLine();
				while (line != null) {
					result.add(ScoreEntry.read(line));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			Collections.sort(result, (p1, p2) -> p2.compareTo(p1));

			return result;
		}

	}

	/**
	 * Schreibt eine Liste von {@link ScoreEntry} Objekten in die Datei {@value #PATH}.<br>
	 * Die Elemente werden in der Reihenfolge in die Datei geschrieben, in der sie in der Liste vorkommen.
	 * @param scoreEntries die zu schreibenden ScoreEntry Objekte
	 * @throws FileNotFoundException
	 */
	public static void writeScoreEntries(List<ScoreEntry> scoreEntries) throws FileNotFoundException {
		// TODO Aufgabe 4.2.2
		PrintWriter p = new PrintWriter(new FileOutputStream(new File(PATH),true));
		for(ScoreEntry s:scoreEntries) {
			s.write(p);
		}
	}

	/**
	 * Schreibt das übergebene {@link ScoreEntry} Objekt an der korrekten Stelle in die Datei {@value #PATH}.<br>
	 * Die Elemente sollen absteigend sortiert sein. Wenn das übergebene Element dieselbe Punktzahl wie ein
	 * Element der Datei hat, soll das übergebene Element danach eingefügt werden.
	 * @param scoreEntry das ScoreEntry Objekt, das hinzugefügt werden soll
	 * @throws FileNotFoundException
	 */
	public static void addScoreEntry(ScoreEntry scoreEntry) throws FileNotFoundException {
		// TODO Aufgabe 4.2.3
		List <ScoreEntry> result = loadScoreEntries();
		result.add(scoreEntry);
		Collections.sort(result, (p1, p2) -> p2.compareTo(p1));
		writeScoreEntries(result);

	}

}
