package krankenhaus.view;

import java.awt.GridLayout;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import krankenhaus.model.PatientenHaus;
import krankenhaus.model.Zimmer;
import krankenhaus.model.ZimmerAenderung;
import krankenhaus.model.ZimmerObserver;

public class MatrixPanel extends JPanel implements ZimmerObserver {

	private static final long serialVersionUID = 1L;
	private final Map<Integer, ZimmerButton> buttons = new TreeMap<>();

	public MatrixPanel(PatientenHaus haus, ZimmerKlickListener klicked) {
		setLayout(new GridLayout(haus.getAnzahlEtagen(), haus.getZimmerAnzahl()));
		for (Zimmer z : haus.getAlleZimmer()) {
			ZimmerButton b = new ZimmerButton(z);
			b.addActionListener(e -> klicked.zimmerGeklickt(z.getZimmerNr()));
			add(b);
			buttons.put(z.getZimmerNr(), b);
		}
		haus.addObserver(this);
	}

	@Override
	public void aktualisiere(ZimmerAenderung zA) {
		int welchesZimmer = zA.zimmer().getZimmerNr();
		buttons.get(welchesZimmer).aktualisiere(zA);
	}
}
