package scripts;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import org.tribot.api.input.Mouse;

import scripts.Defines.ItemIDs;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private static Preferences preferences = Preferences.userRoot().node("LanChaosKiller_UserSettings");
	private HashMap<JCheckBox, ItemIDs> checkBoxes = new HashMap<JCheckBox, ItemIDs>();
	Point start_drag, start_loc;

	public GUI() {

		// Load GUI settings
		Mouse.setSpeed(preferences.getInt("mouseSpeed", 70));
		LANChaosKiller.foodCount = preferences.getInt("foodCount", 1);
		LANChaosKiller.foodName = preferences.get("foodName", "Lobster");
		LANChaosKiller.eatBelowPercent = preferences.getInt("eatBelowPercent", 50);

		for (ItemIDs i : ItemIDs.values()) {
			if (preferences.getBoolean(i.name(), false))
				LANChaosKiller.lootIDs.add(i.getID());
		}

		checkBoxes.put(lootGuam, ItemIDs.GUAM_LEAF);
		checkBoxes.put(lootMarrentill, ItemIDs.MARRENTILL);
		checkBoxes.put(lootTarromin, ItemIDs.TARROMIN);
		checkBoxes.put(lootHarralander, ItemIDs.HARRALANDER);
		checkBoxes.put(lootRanarr, ItemIDs.RANARR);
		checkBoxes.put(lootIrit, ItemIDs.IRIT);
		checkBoxes.put(lootAventoe, ItemIDs.AVANTOE);
		checkBoxes.put(lootKwuarm, ItemIDs.KWUARM);
		checkBoxes.put(lootCadantine, ItemIDs.CADANTINE);
		checkBoxes.put(lootDwarf, ItemIDs.DWARF_WEED);
		checkBoxes.put(lootLantadyme, ItemIDs.LANTADYME);
		checkBoxes.put(lootLaw, ItemIDs.LAW_RUNE);
		checkBoxes.put(lootNature, ItemIDs.NATURE_RUNE);
		checkBoxes.put(lootBolts, ItemIDs.MITHRIL_BOLTS);
		checkBoxes.put(lootJavelin, ItemIDs.RUNE_JAVELIN);

		setTitle("LAN ChaosKiller - Settings");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setBounds(new Rectangle(0, 0, 344, 510));
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		setUndecorated(true);
		setPreferredSize(new Dimension(345, 510));
		getContentPane().setLayout(null);

		for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
			JCheckBox checkBox = entry.getKey();
			checkBox.setOpaque(false);
			getContentPane().add(checkBox);
		}

		lootKwuarm.setBounds(250, 100, 100, 27);
		lootGuam.setBounds(20, 70, 100, 27);
		lootMarrentill.setBounds(20, 100, 100, 27);
		lootTarromin.setBounds(130, 40, 100, 27);
		lootHarralander.setBounds(130, 70, 110, 27);
		lootRanarr.setBounds(130, 100, 70, 27);
		lootIrit.setBounds(250, 40, 100, 27);
		lootAventoe.setBounds(250, 70, 100, 27);
		lootDwarf.setBounds(20, 170, 100, 27);
		lootCadantine.setBounds(20, 140, 100, 27);
		lootLantadyme.setBounds(130, 140, 110, 27);
		lootLaw.setBounds(20, 230, 70, 27);
		lootNature.setBounds(20, 260, 110, 27);
		lootBolts.setBounds(130, 230, 110, 27);
		lootJavelin.setBounds(130, 260, 110, 27);

		mouseSpeed.setOpaque(false);
		getContentPane().add(mouseSpeed);
		mouseSpeed.setBounds(70, 345, 200, 23);
		mouseSpeed.setMaximum(175);
		mouseSpeed.setMinimum(10);
		mouseSpeed.setValue(Mouse.getSpeed());

		foodCountSpinner.setOpaque(false);
		foodCountSpinner.setModel(new SpinnerNumberModel(LANChaosKiller.foodCount, 0, 28, 1));
		getContentPane().add(foodCountSpinner);
		foodCountSpinner.setBounds(280, 302, 40, 20);

		foodNameTextField.setOpaque(false);
		foodNameTextField.setText(LANChaosKiller.foodName);
		getContentPane().add(foodNameTextField);
		foodNameTextField.setBounds(100, 302, 60, 20);

		eatBelowPctSpinner.setOpaque(false);
		eatBelowPctSpinner.setModel(new SpinnerNumberModel(LANChaosKiller.eatBelowPercent, 10, 90, 5));
		getContentPane().add(eatBelowPctSpinner);
		eatBelowPctSpinner.setBounds(220, 390, 40, 20);

		btnSave.setText("Save Settings");
		btnSave.setOpaque(false);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveSettingsClicked(evt);
			}
		});
		getContentPane().add(btnSave);
		btnSave.setBounds(90, 415, 170, 40);

		try {
			backgroundLabel.setIcon(new ImageIcon(new URL("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/scriptSettings.png")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		backgroundLabel.setText("Failed to load background :(");
		backgroundLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) { 
				backgroundMousePressed(evt);
			}
		});
		backgroundLabel.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt) {
				backgroundMouseDragged(evt);
			}
		});
		getContentPane().add(backgroundLabel);
		backgroundLabel.setBounds(0, 0, 337, 495);

		Object[] lootids = LANChaosKiller.lootIDs.toArray();
		for (int i = 0; i < lootids.length; i++) {
			for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
				if (lootids[i] != null && lootids[i].equals(entry.getValue().getID())) {
					entry.getKey().setSelected(true);
					break;
				}
			}
		}

		//pack();
		this.setLocationRelativeTo(null);
		this.toFront();
	}

	protected void backgroundMousePressed(MouseEvent evt) {
		this.start_drag = this.getScreenLocation(evt);
		this.start_loc = this.getLocation();
	}

	Point getScreenLocation(MouseEvent e) {
		Point cursor = e.getPoint();
		Point target_location = this.getLocationOnScreen();
		return new Point((int) (target_location.getX() + cursor.getX()), (int) (target_location.getY() + cursor.getY()));
	}

	protected void backgroundMouseDragged(MouseEvent evt) {
		Point current = this.getScreenLocation(evt);
		Point offset = new Point((int) current.getX() - (int) start_drag.getX(), (int) current.getY() - (int) start_drag.getY());
		Point new_location = new Point((int) (this.start_loc.getX() + offset.getX()), (int) (this.start_loc.getY() + offset.getY()));
		this.setLocation(new_location);
	}

	protected void btnSaveSettingsClicked(ActionEvent evt) {
		LANChaosKiller.lootIDs.clear();

		for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
			if (entry.getKey().isSelected())
				LANChaosKiller.lootIDs.add(entry.getValue().getID());
		}

		Mouse.setSpeed(mouseSpeed.getValue());
		LANChaosKiller.foodName = (String) foodNameTextField.getText();
		LANChaosKiller.foodCount = (int) foodCountSpinner.getValue();
		LANChaosKiller.eatBelowPercent = (int) eatBelowPctSpinner.getValue();

		LANChaosKiller.waitForGUI = false;
		this.setVisible(false);

		// Save these settings.
		preferences.putInt("mouseSpeed", mouseSpeed.getValue());
		preferences.put("foodName", LANChaosKiller.foodName);
		preferences.putInt("foodCount", LANChaosKiller.foodCount);
		preferences.putInt("eatBelowPercent", LANChaosKiller.eatBelowPercent);

		for (ItemIDs i : ItemIDs.values()) {
			preferences.putBoolean(i.name(), LANChaosKiller.lootIDs.contains(i.getID()));
		}
	}

	private JButton btnSave = new JButton();
	private JLabel backgroundLabel = new JLabel();
	private JCheckBox lootAventoe = new JCheckBox();
	private JCheckBox lootBolts = new JCheckBox();
	private JCheckBox lootCadantine = new JCheckBox();
	private JCheckBox lootDwarf = new JCheckBox();
	private JCheckBox lootGuam = new JCheckBox();
	private JCheckBox lootHarralander = new JCheckBox();
	private JCheckBox lootIrit = new JCheckBox();
	private JCheckBox lootJavelin = new JCheckBox();
	private JCheckBox lootKwuarm = new JCheckBox();
	private JCheckBox lootLantadyme = new JCheckBox();
	private JCheckBox lootLaw = new JCheckBox();
	private JCheckBox lootMarrentill = new JCheckBox();
	private JCheckBox lootNature = new JCheckBox();
	private JCheckBox lootRanarr = new JCheckBox();
	private JCheckBox lootTarromin = new JCheckBox();
	private JSlider mouseSpeed = new JSlider();
	private JSpinner foodCountSpinner = new JSpinner();
	private JTextField foodNameTextField = new JTextField();
	private JSpinner eatBelowPctSpinner = new JSpinner();
}
