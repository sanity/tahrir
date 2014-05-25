package tahrir.tools;

public class Flag implements Runnable {

	private boolean flag = false;

	public void run() {
		flag = true;
	}

	public boolean isSet() {
		return flag;
	}

	public void set() {
		flag = true;
	}
}
