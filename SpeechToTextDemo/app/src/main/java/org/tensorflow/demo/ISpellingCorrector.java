package org.tensorflow.demo;

public interface ISpellingCorrector {
    void putWord(String word);
    String correct(String word);
    boolean containsWord(String word);
}
