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
import java.util.List;

import javax.print.DocFlavor.READER;

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
		List <ScoreEntry> result = new ArrayList <>();	
		try{
			BufferedReader reader = new BufferedReader(new FileReader (PATH));
			String line= reader.readLine();
		    while (line!=null){
		        if(ScoreEntry.read(line) == null)
		        	return result;
		        result.add(ScoreEntry.read(line));
		        line= reader.readLine();
		    }
			reader.close();
		}
		catch (IOException e){
			return new ArrayList<ScoreEntry>();
		}
		return result;
	}

	/**
	 * Schreibt eine Liste von {@link ScoreEntry} Objekten in die Datei {@value #PATH}.<br>
	 * Die Elemente werden in der Reihenfolge in die Datei geschrieben, in der sie in der Liste vorkommen.
	 * @param scoreEntries die zu schreibenden ScoreEntry Objekte
	 */
	public static void writeScoreEntries(List<ScoreEntry> scoreEntries) {
		// TODO Aufgabe 4.2.2
		PrintWriter p;
		try {
			p = new PrintWriter(new FileOutputStream(PATH));
			for(ScoreEntry s:scoreEntries) {
				s.write(p);
			}
			p.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Schreibt das übergebene {@link ScoreEntry} Objekt an der korrekten Stelle in die Datei {@value #PATH}.<br>
	 * Die Elemente sollen absteigend sortiert sein. Wenn das übergebene Element dieselbe Punktzahl wie ein
	 * Element der Datei hat, soll das übergebene Element danach eingefügt werden.
	 * @param scoreEntry das ScoreEntry Objekt, das hinzugefügt werden soll
	 */
	public static void addScoreEntry(ScoreEntry scoreEntry){
		// TODO Aufgabe 4.2.3
		List<ScoreEntry> listScore = loadScoreEntries();
		if(listScore.isEmpty()) {
			listScore.add(scoreEntry);
			writeScoreEntries(listScore);
			return;
		}
		List<ScoreEntry> result = new ArrayList<>();
		int index = 0;
		for(ScoreEntry s : listScore) {
			if(s.compareTo(scoreEntry)>=0) {
				result.add(s);
				index++;
			} else break;
		}
		result.add(scoreEntry);
		for(int i = index;i<listScore.size();i++) {
			result.add(listScore.get(i));
		}
		writeScoreEntries(result);

	}

}
