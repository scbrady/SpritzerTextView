package com.andrewgiang.textspritzer.lib;

/**
 * Created by andrewgiang on 3/19/14.
 */
public class DefaultDelayStrategy implements DelayStrategy {
    @Override
    public float delayMultiplier(String word) {
        if (word.endsWith(".") || word.endsWith("?") || word.endsWith("!"))
            return 2.5f;
        if (word.contains(",") || word.contains(":") || word.contains(";") || word.contains("\""))
            return 1.3f;
        if (word.length() > 6)
            return 1.1f;
        return 1;
    }
}
