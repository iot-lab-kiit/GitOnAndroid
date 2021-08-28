package com.manichord.mgit.utils;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CodeGuesserTest {

    @org.junit.Test
    public void testGuessCodeType() {
        assertEquals("expect to recognise java files", "text/x-java", CodeGuesser.guessCodeType("test.java"));
        assertEquals("expect to recognise typescript files", "text/typescript", CodeGuesser.guessCodeType("test.ts"));
        assertEquals("expect to recognise dart files", "text/x-dart", CodeGuesser.guessCodeType("test.dart"));
    }
}
