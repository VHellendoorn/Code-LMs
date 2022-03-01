
package org.praxislive.tracker.impl;

import org.praxislive.tracker.impl.PatternParser;
import org.praxislive.core.types.PNumber;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praxislive.tracker.Pattern;
import org.praxislive.tracker.Patterns;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PatternParserTest {
    
    public PatternParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class PatternParser.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        String data = "#comment\n1 . .\n.\n. 2\n\nPattern Foo Bar\n.\n.\nPraxis . {.}";
        Patterns result = PatternParser.parse(data);
        System.out.println("Pattern count : " + result.getPatternCount());
        assertEquals(result.getPatternCount(), 2);
        
        Pattern p = result.getPattern(0);
        assertEquals(p.getValueAt(0, 0), PNumber.of(1));
        assertEquals(p.getValueAt(1, 2), null);
        assertEquals(p.getValueAt(2, 1).toString(), "2");
        
        p = result.getPattern(1);
        assertEquals(p.getValueAt(0, 1).toString(), "Foo");
        assertEquals(p.getValueAt(3, 0).toString(), "Praxis");
        assertEquals(p.getValueAt(3, 2).toString(), ".");
        
        result = PatternParser.parse("#comment\n    ");
        assertEquals(result.getPatternCount(), 1);
        assertEquals(result.getPattern(0), Pattern.EMPTY);
        
    }
    
}
