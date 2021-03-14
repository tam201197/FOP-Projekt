package fop.view.menu;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import fop.io.ScoreEntryIO;
import fop.model.ScoreEntry;
import fop.view.MainFrame;

public class HighscoreView extends MenuView{
	
	


	public HighscoreView(MainFrame window) {
		super(window, "Highscores");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addContent(JPanel contentPanel) {
		// TODO Auto-generated method stub
		/*GridBagConstraints aboutScoresTable = new GridBagConstraints();
		aboutScoresTable.weightx = 1.0;
		aboutScoresTable.weighty = 1.0;
		aboutScoresTable.fill = GridBagConstraints.BOTH;
		aboutScoresTable.insets = new Insets(0, 2, 2, 2);
		aboutScoresTable.gridx = 0;
		aboutScoresTable.gridy = 0;*/
		
		String[] headers = new String[] {
	            "Datum und Uhrzeit", "Name", "Punkte"
	     };
		ScoreEntry[] a = ScoreEntryIO.loadScoreEntries().toArray(new ScoreEntry[0]); 

		String [][] data = new String [a.length][3];
		
		for(int x = 0;x<a.length;x++) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			data[x][0] = a[x].getDateTime().format(dtf).toString();
			data[x][1] = a[x].getName();
			data[x][2] = String.valueOf(a[x].getScore());
		}
		JTable scoreTable = new JTable(data, headers);
		JTableHeader header = scoreTable.getTableHeader();
		header.setBackground(contentPanel.getBackground());
		((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
				
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(700,275));
		scrollPane.getViewport().add(scoreTable);
		
		scoreTable.setAutoCreateRowSorter(true);
		
		contentPanel.add(scrollPane);
		
		
		
		
		
		
		//Back Botton
		GridBagConstraints rightImageConstraints = new GridBagConstraints();
		rightImageConstraints.insets = new Insets(2, 2, 0, 2);
		rightImageConstraints.gridx = 0;
		rightImageConstraints.gridy = 1;
		JButton backButton = createButton("Zurück");
		backButton.addActionListener(evt -> getWindow().setView(new MainMenu(getWindow())));
		contentPanel.add(backButton, rightImageConstraints);
	}

}
