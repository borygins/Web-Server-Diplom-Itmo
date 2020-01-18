package ru.ifmo.server;

import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class SessionTest {
    @Test
    public void testParams() throws IOException {
        Session session = new Session();
        session.setParam("name", "Vasya");
        assertEquals("Vasya", session.getParam("name"));
    }

    @Test(expected = SessionException.class)
    public void testExpired() throws IOException {
        Session session = new Session();
        session.setExpired(true);
        session.setParam("name", "Vasya");
    }

    @Test
    public void testLiveTime() throws IOException {

        int min = 1;

        Session session = new Session();
        session.setExpire(min);
        assertEquals(session.getExpire(), LocalDateTime.now().plusMinutes(min));
    }
}
