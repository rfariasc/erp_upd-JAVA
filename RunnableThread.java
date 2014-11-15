import java.io.*;

public class RunnableThread {
    static PrintWriter out = new PrintWriter(System.out, true);

    public static void main (String args[]) {
	// first task: some pseudo-I/O operation
	RunnablePseudoIO pseudo = new RunnablePseudoIO();
	Thread thread = new Thread (pseudo);
	thread.start();
	// second task: some random task
	showElapsedTime("Another task starts");
    }

    static long baseTime = System.currentTimeMillis();

    // show the time elapsed since the program started
    static void showElapsedTime(String message) {
	long elapsedTime = System.currentTimeMillis() -baseTime;
	out.println(message + " at " + (elapsedTime/1000.0) + " seconds");
    }
}

// pseudo I/O operation run in a separate thread
class RunnablePseudoIO implements Runnable {
    int data = -1;

    RunnablePseudoIO() {  // constructor
	RunnableThread.showElapsedTime("RunnablePseudoIO created");
    }

    public void run() {
	RunnableThread.showElapsedTime("RunnablePseudoIO starts");
	try {
	    Thread.sleep(10000);  // 10 seconds
	    data = 999;		// data ready
	    RunnableThread.showElapsedTime("RunnablePseudoIO finishes");
	} catch (InterruptedException e) {}
    }
}
