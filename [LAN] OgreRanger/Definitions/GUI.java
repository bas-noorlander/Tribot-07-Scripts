package scripts.Definitions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import org.tribot.api.General;

import scripts.LANOgreRanger;


/**
 * @author Laniax
 *
 */
@SuppressWarnings("serial")
public class GUI extends JFrame{
	
	private static Preferences preferences = null;

	public GUI() {
		
		// Load GUI settings
		try {
			preferences = Preferences.userRoot().node("LanOgreRanger_UserSettings");
			LANOgreRanger.foodCount = preferences.getInt("foodCount", 1);
			LANOgreRanger.foodName = preferences.get("foodName", "Lobster");
			LANOgreRanger.scriptLocation = Location.valueOf(preferences.get("scriptLocation", Location.CASTLE_WARS.toString()));
			LANOgreRanger.arrowId = preferences.getInt("arrowId", 0);
			LANOgreRanger.pickUpArrows = preferences.getBoolean("pickUpArrows", false);
			LANOgreRanger.pickUpArrowsOnlyAboveAmount = preferences.getBoolean("pickUpArrowsOnlyAboveAmount", false);
			LANOgreRanger.pickUpArrowCount = preferences.getInt("pickUpArrowCount", 10);
			LANOgreRanger.useSpec = preferences.getBoolean("useSpec", false);
			
		} catch (Exception e) {
			General.println("Error while loading settings from last time. This can be caused by some VPS's.");
		}

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle("[LAN] OgreRanger Settings");
        setResizable(false);
        setAlwaysOnTop(true);
        
        location.setModel(new DefaultComboBoxModel<Location>(Location.values()));
        location.setSelectedIndex(LANOgreRanger.scriptLocation.getIndex());

        arrowId.setText(LANOgreRanger.arrowId+"");

        pickUpArrows.setText("Pick up arrows");
        pickUpArrows.setSelected(LANOgreRanger.pickUpArrows);
        pickUpArrows.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	
            	boolean isSelected = pickUpArrows.isSelected();
                pickUpArrowsAbove.setEnabled(isSelected);
                pickUpArrowAboveCount.setEnabled(isSelected);
            }
        });

        pickUpArrowsAbove.setText("Only pick up if more then");
        pickUpArrowsAbove.setEnabled(LANOgreRanger.pickUpArrows);
        pickUpArrowsAbove.setSelected(LANOgreRanger.pickUpArrowsOnlyAboveAmount);

        pickUpArrowAboveCount.setEnabled(LANOgreRanger.pickUpArrows);
        pickUpArrowAboveCount.setValue(LANOgreRanger.pickUpArrowCount);

        foodName.setText(LANOgreRanger.foodName);
        foodAmount.setValue(LANOgreRanger.foodCount);

        jLabel1.setText("Location:");

        jLabel2.setText("Arrow ID:");

        jLabel3.setText("Food name:");

        useSpec.setText("Use magic shortbow special attack");
        useSpec.setSelected(LANOgreRanger.useSpec);

        jLabel4.setText("*** Picking up every arrow (or a low amount)");

        jLabel5.setText("will result in your char running into the ogres");

        jLabel6.setText("after every kill! *** This will use a lot of food");

        jLabel7.setText("Use this script at own risk! Always wear a ring of life");

        jLabel8.setText("Amount of food:");
        
        save.setText("Save");
        save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				saveSettingsClicked(evt);
			}
		});

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useSpec)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                          //  .addComponent(pickUpArrowsAlways)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pickUpArrowsAbove)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pickUpArrowAboveCount, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(pickUpArrows, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(location, 0, 152, Short.MAX_VALUE)
                                    .addComponent(arrowId)))
                            .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel3))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(foodName, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(foodAmount, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE))))
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(location, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(arrowId, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pickUpArrows)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
             //   .addComponent(pickUpArrowsAlways)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(pickUpArrowsAbove)
                    .addComponent(pickUpArrowAboveCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(foodName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(foodAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useSpec)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setLocationRelativeTo(null);
        pack();
		
	}

	protected void saveSettingsClicked(ActionEvent evt) {
	
		LANOgreRanger.scriptLocation = Location.valueOf(location.getSelectedIndex());
		LANOgreRanger.foodCount = (int) foodAmount.getValue();
		LANOgreRanger.foodName = foodName.getText();
		LANOgreRanger.scriptLocation = Location.valueOf(location.getSelectedIndex());
		LANOgreRanger.arrowId = Integer.parseInt(arrowId.getText());
		LANOgreRanger.pickUpArrows = pickUpArrows.isSelected();
		LANOgreRanger.pickUpArrowsOnlyAboveAmount = pickUpArrowsAbove.isSelected();
		LANOgreRanger.pickUpArrowCount = (int)pickUpArrowAboveCount.getValue();
		LANOgreRanger.useSpec = useSpec.isSelected();
		
		LANOgreRanger.waitForGUI = false;
		setVisible(false);
		
		// Save these settings.
		try {
			preferences = Preferences.userRoot().node("LanOgreRanger_UserSettings");
			preferences.put("foodName", LANOgreRanger.foodName);
			preferences.putInt("foodCount", LANOgreRanger.foodCount);
			preferences.put("scriptLocation", LANOgreRanger.scriptLocation.toString());
			preferences.putInt("arrowId", LANOgreRanger.arrowId);
			preferences.putBoolean("pickUpArrows", LANOgreRanger.pickUpArrows);
			preferences.putBoolean("pickUpArrowsOnlyAboveAmount", LANOgreRanger.pickUpArrowsOnlyAboveAmount);
			preferences.putInt("pickUpArrowCount", LANOgreRanger.pickUpArrowCount);
			preferences.putBoolean("useSpec", LANOgreRanger.useSpec);

		} catch (Exception e) {
			General.println("Error while saving these settings for next time. This can be caused by some VPS's.");
		}
	}

	private JTextField arrowId = new JTextField();
    private JSpinner foodAmount = new JSpinner();
    private JTextField foodName = new JTextField();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();
    private JLabel jLabel4 = new JLabel();
    private JLabel jLabel5 = new JLabel();
    private JLabel jLabel6 = new JLabel();
    private JLabel jLabel7 = new JLabel();
    private JLabel jLabel8 = new JLabel();
    private JButton save = new JButton();
    private JSeparator jSeparator1 = new JSeparator();
    private JComboBox<Location> location = new JComboBox<Location>();
    private JSpinner pickUpArrowAboveCount = new JSpinner();
    private JCheckBox pickUpArrows = new JCheckBox();
    private JCheckBox pickUpArrowsAbove = new JCheckBox();
    private JCheckBox useSpec = new JCheckBox();

}
