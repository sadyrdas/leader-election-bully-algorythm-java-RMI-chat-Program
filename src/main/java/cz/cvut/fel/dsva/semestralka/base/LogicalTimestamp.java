package cz.cvut.fel.dsva.semestralka.base;

public class LogicalTimestamp {
    private static long counter = 0;

    public static synchronized String nextStamp() {
        return String.valueOf(++counter);
    }
}
