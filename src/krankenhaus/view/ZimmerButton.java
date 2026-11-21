package krankenhaus.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import krankenhaus.model.Zimmer;
import krankenhaus.model.ZimmerAenderung;
import krankenhaus.model.ZimmerObserver;

public class ZimmerButton extends JButton implements ZimmerObserver {

	private static final long serialVersionUID = 1L;
	private final Zimmer zimmer;

	public ZimmerButton(Zimmer zimmer) {
		this.zimmer = zimmer;
		neuZeichnen();
	}

	private void neuZeichnen() {
		String n;
		if (zimmer.getPatient() == null) {
			n = "Zimmer frei";
			setBackground(new Color(177, 248, 165));
		} else {
			n = zimmer.getPatient().getName();
			setBackground(new Color(253, 153, 179));
		}
		setBorder(BorderFactory.createRaisedBevelBorder());
		setText("<html><center>" + zimmer.getBezeichnung()
				+ "<br>" + zimmer.getAbteilung()
				+ "<br><br>" + n + "</center></html>");
	}

	@Override
	public void aktualisiere(ZimmerAenderung zA) {
		neuZeichnen();
	}
}
