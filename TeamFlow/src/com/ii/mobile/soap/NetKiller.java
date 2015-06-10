package com.ii.mobile.soap;

import com.ii.mobile.home.MyToast;
import com.ii.mobile.util.L;

public enum NetKiller {
	Instance;
	static float percentKill = .50f;
	static boolean netKillerBoolean = false;
	static float seconds = .5f;

	public static boolean kill() {
		return kill(true);
	}

	public static boolean kill(boolean printOut) {
		if (!netKillerBoolean)
			return false;
		float randomKill = (float) Math.random();
		boolean killed = (randomKill > percentKill ? false : true);
		if (printOut)
			L.out("killed: " + killed + " percent: " + ((int) (percentKill * 100)) + "%" + " random: "
					+ randomKill);
		if (killed) {
			int wait = (int) (seconds * 1000 * Math.random());
			// MyToast.show("Killed - took: " + wait + " ms");
			L.sleep(wait);
		}
		return killed;
	}

	public static void setKiller() {
		setKiller(!netKillerBoolean);
	}

	static void setKiller(boolean flag) {
		netKillerBoolean = flag;
		percentKill = (float) Math.random();
		L.out("percent killed: " + percentKill);
		if (netKillerBoolean)
			MyToast.show("NetKiller on: " + ((int) (100 - (percentKill * 100))) + "% Killed!");
		else
			MyToast.show("NetKiller turned off!");
	}

}
