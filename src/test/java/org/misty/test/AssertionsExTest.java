package org.misty.test;

import org.junit.jupiter.api.Test;

class AssertionsExTest {

    @Test
    void assertThrown() {
        AssertionsEx.asserAwareThrown(this::thrown).isInstanceOf(RuntimeException.class);
    }

    public void thrown() {
        throw new RuntimeException();
    }

}
