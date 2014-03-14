package scripts.Managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Objects;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSObject;

/**
 * Helper class that manages logic related to general actions in the runescape world.
 * @author Laniax
 *
 */
public class GeneralMgr {

	/**
	 * Find the nearest object(s) based on the objects available actions and interacts with it based on the given condition
	 * 
	 * @param conditionToReach - after clicking on the action it will wait for this condition
	 * @param action
	 * @return returns true if condition was reached before the timeout.
	 */
	public static boolean interactWithObject(final Condition conditionToReach, final String action) {

		return interactWithObject(conditionToReach, action, General.random(5000,8000));

	}

	/**
	 * Find the nearest object(s) based on the objects available actions and interacts with it based on the given condition
	 * 
	 * @param conditionToReach - after clicking on the action it will wait for this condition
	 * @param action
	 * @param timeout
	 * @return returns true if condition was reached before the timeout.
	 */
	public static boolean interactWithObject(final Condition conditionToReach, final String action, final int timeout) {
		RSObject[] obj = Objects.findNearest(15, Filters.Objects.actionsContains(action));
		if (obj.length > 0) {
			if (!obj[0].isOnScreen())
				Camera.turnToTile(obj[0]);

			if (Clicking.click(action, obj[0])) {
				return Timing.waitCondition(conditionToReach, timeout);
			}
		}
		return false;
	}

	/**
	 * Waits until the specified condition is true or the default timeout is reached.
	 * 
	 * @param condition
	 * @return true when done.
	 */
	public static boolean waitFor(Condition cond) {

		return Timing.waitCondition(cond, General.random(7500, 9000));
	}

	/**
	 * Waits until the specified condition is true using or the timeout is reached.
	 * 
	 * @param condition
	 * @param timeout
	 * @return true when done.
	 */
	public static boolean waitFor(Condition cond, int timeout) {
		return Timing.waitCondition(cond, timeout);
	}

	/**
	 * Converts an Arraylist full of Integers into a int[]
	 * 
	 * @param integers
	 * @return
	 */
	public static int[] buildIntArray(ArrayList<Integer> integers) {
		int[] ints = new int[integers.size()];
		int i = 0;
		for (Integer n : integers) {
			ints[i++] = n;
		}
		return ints;
	}

	/**
	 * Sends data to my website in order to generate dynamic signatures.
	 * Uses encryption to prevent scriptkiddies from tampering with the data
	 * 
	 * @param runtimeInSeconds -  runtime of the script
	 * @param var1-4 -  stats we want to send/keep track off
	 * @return true if succesfully posted the data
	 */
	public static boolean sendSignatureData(long runtimeInSeconds, int var1, int var2, int var3, int var4) {
		// In order to provide some security, so that people will not tamper the data and post it themselves, we will be encrypting it here and decrypting it in php.

		String privateKey = "<CENSORED>";
		String initVector = "<CENSORED>";

		try {
			// data we will be encrypting. you can remove the var's if you want (username and runtime are required though)
			String data = initVector+","+General.getTRiBotUsername()+","+runtimeInSeconds+","+var1+","+var2+","+var3+","+var4; // comma delimited so we can split in php

			// set up iv and key for encrypting
			IvParameterSpec ivspec = new IvParameterSpec(initVector.getBytes());
			SecretKeySpec keyspec = new SecretKeySpec(privateKey.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

			// encrypt
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

			// convert to hex
			String token = "";
			for (int i = 0; i < encrypted.length; i++) {
				if ((encrypted[i] & 0xFF) < 16) {
					token = token + "0" + java.lang.Integer.toHexString(encrypted[i] & 0xFF);
				} else {
					token = token + java.lang.Integer.toHexString(encrypted[i] & 0xFF);
				}
			}

			// And post it :)
			URL url = new URL("<CENSORED>"+token);
			URLConnection conn = url.openConnection();

			// fake request coming from browser (solves permission issue on shared webhosting)
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			in.readLine();
			in.close();
			return true;
		} catch (Exception e) {
			General.println(e.getMessage());
		}
		return false;
	}
}
