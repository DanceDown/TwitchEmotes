package me.dancedown.twitchemotes.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkHandlerTest {

    @Test
    void testOauthValidationFails() {
        NetworkHandler networkHandler = new NetworkHandler();
        assertFalse(networkHandler.validateTwitchOauthToken("test", "test"));
    }
}
