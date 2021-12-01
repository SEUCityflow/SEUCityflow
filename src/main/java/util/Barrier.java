package util;

public class Barrier {
    private final int m_threads;
    private final int[] counter;
    private int currCounter;

    public Barrier(int nb_threads) {
        m_threads = nb_threads;
        counter = new int[2];
        currCounter = 0;
        assert (m_threads != 0);
        counter[0] = m_threads;
    }

    public synchronized void Wait() {
        assert (0 != counter[currCounter]);
        try {
            if ((--counter[currCounter]) == 0) {
                currCounter = (currCounter + 1) % 2;
                counter[currCounter] = m_threads;
                this.notifyAll();
            } else {
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
