package br.com.thecruzader.presentation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RoomTransitionTest {
    @Test void crossingIntoRoomCutsUnderBlackAndPausingHoldsFade() {
        var room = new RoomTransition();
        room.reset(false);
        room.update(true, .016f, false);
        assertTrue(room.interior());
        assertEquals(1, room.opacity());
        room.update(true, .1f, true);
        assertEquals(1, room.opacity());
        for (int frame = 0; frame < 4; frame++) room.update(true, .1f, false);
        assertEquals(0, room.opacity());
    }

    @Test void leavingAndReenteringEachRevealTheCorrectView() {
        var room = new RoomTransition();
        room.reset(true);
        room.update(false, .016f, false);
        assertFalse(room.interior());
        assertEquals(1, room.opacity());
        room.update(true, .016f, false);
        assertTrue(room.interior());
        assertEquals(1, room.opacity());
    }

    @Test void respawnClearsThePreviousRoomAndInterruptedFade() {
        var room = new RoomTransition();
        room.update(true, .016f, false);
        room.reset(false);
        assertFalse(room.interior());
        assertEquals(0, room.opacity());
        room.update(false, .016f, false);
        assertEquals(0, room.opacity());
    }
}
