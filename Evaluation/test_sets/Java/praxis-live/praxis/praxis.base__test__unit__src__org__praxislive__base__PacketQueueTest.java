package org.praxislive.base;

import org.praxislive.core.Call;
import org.praxislive.core.ControlAddress;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public class PacketQueueTest {

    public PacketQueueTest() {
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

    @Test
    public void testPoll() {
        PacketQueue pq = new PacketQueue(0);
        Call c1 = Call.create(ControlAddress.of("/to.c1"), ControlAddress.of("/from.c1"), 1000);
        Call c2 = Call.create(ControlAddress.of("/to.c2"), ControlAddress.of("/from.c2"), 2000);
        pq.add(c1);
        pq.add(c2);
        assertNull(pq.poll());
        pq.setTime(1000);
        assertEquals(c1, pq.poll());
        assertNull(pq.poll());

        pq.add(c1);
        pq.setTime(2500);
        assertEquals(c1, pq.poll());
        assertEquals(c2, pq.poll());

    }
    
    @Test
    public void testWrappingPoll() {
        PacketQueue pq = new PacketQueue(Long.MAX_VALUE - 5000);
        Call c1 = Call.create(ControlAddress.of("/to.c1"), ControlAddress.of("/from.c1"), Long.MAX_VALUE - 1000);
        Call c2 = Call.create(ControlAddress.of("/to.c2"), ControlAddress.of("/from.c2"), Long.MAX_VALUE + 1000);
        pq.add(c1);
        pq.add(c2);
        assertNull(pq.poll());
        pq.setTime(Long.MAX_VALUE - 1000);
        assertEquals(pq.poll(), c1);
        assertNull(pq.poll());

        pq.add(c1);
        pq.setTime(Long.MAX_VALUE + 1500);
        assertEquals(pq.poll(), c1);
        assertEquals(pq.poll(), c2);

    }

}
