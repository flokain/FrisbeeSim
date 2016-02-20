package visualization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.lowagie.text.pdf.hyphenation.TernaryTree.Iterator;

import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.GroupedInspector;
import sim.util.Bag;
import sim.util.Double3D;
import sim.util.Interval;
import sim.util.Properties;
import sim.util.gui.DisclosurePanel;
import sim.util.gui.LabelledList;
import ultimate.Ultimate;
import ultimate.steppableEntity.Frisbee;

public class FrisbeeControlPanel extends JPanel {
	
	JPanel pnlButton = new JPanel();
    JButton button = new JButton("Fly!");
    
	public static GroupedInspector getFrisbeeControlPanel(UltimateWithUI ultimateGui) 
	{
		Interval interal;
		JButton button;
		DisclosurePanel disclosurePanel;
		Frisbee disc = ((Ultimate)ultimateGui.state).frisbee;
	  
	    Bag variables = new Bag();
	    variables.add(disc.getOrientation());
	    variables.add(disc.getVelocity());
	    variables.add(disc.getOmega());
	    variables.add(disc.getAcceleration());
	    variables.add(disc.getAlpha());
	    
	    Properties prop = Properties.getProperties(disc, true, true, true,true);
		return null;
    }
}
