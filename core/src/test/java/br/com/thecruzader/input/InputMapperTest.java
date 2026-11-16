package br.com.thecruzader.input;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Input;

import org.junit.jupiter.api.Test;

class InputMapperTest {
    @Test
    void specialPressIsConsumedOnceAndClearedWithFocus() {
        InputMapper input = new InputMapper();
        input.keyDown(Input.Keys.Q);
        input.keyUp(Input.Keys.Q);
        assertEquals(0, input.consume().specialSlot());
        assertEquals(-1, input.consume().specialSlot());
        input.keyDown(Input.Keys.E);
        assertEquals(1, input.consume().specialSlot());
        input.keyDown(Input.Keys.E);
        assertEquals(-1, input.consume().specialSlot());
        input.keyUp(Input.Keys.E);
        input.keyDown(Input.Keys.E);
        input.clear();
        assertEquals(PlayerInput.NONE, input.consume());
    }

    @Test
    void tapBetweenStepsIsNotLost() {
        InputMapper input = new InputMapper();
        input.keyDown(Input.Keys.J);
        input.keyUp(Input.Keys.J);
        assertTrue(input.consume().attack());
        assertFalse(input.consume().attack());
    }

    @Test
    void keyRepeatDoesNotCreateExtraAttacks() {
        InputMapper input = new InputMapper();
        input.keyDown(Input.Keys.J);
        assertTrue(input.consume().attack());
        input.keyDown(Input.Keys.J);
        assertFalse(input.consume().attack());
        input.keyUp(Input.Keys.J);
        input.keyDown(Input.Keys.J);
        assertTrue(input.consume().attack());
    }

    @Test
    void focusClearRemovesMovementAndPendingActions() {
        InputMapper input = new InputMapper();
        input.keyDown(Input.Keys.D);
        input.keyDown(Input.Keys.SPACE);
        input.clear();
        assertEquals(PlayerInput.NONE, input.consume());
    }
    @Test
    void downAndBlockStayHeldUntilReleasedOrCleared() {
        InputMapper input = new InputMapper();
        input.keyDown(Input.Keys.S);
        input.keyDown(Input.Keys.L);
        assertTrue(input.consume().down());
        assertTrue(input.consume().block());
        input.keyUp(Input.Keys.S);
        input.keyUp(Input.Keys.L);
        assertFalse(input.consume().down());
        assertFalse(input.consume().block());
        input.keyDown(Input.Keys.DOWN);
        input.keyDown(Input.Keys.X);
        assertTrue(input.consume().down());
        assertTrue(input.consume().block());
        input.clear();
        assertEquals(PlayerInput.NONE, input.consume());
    }
}
