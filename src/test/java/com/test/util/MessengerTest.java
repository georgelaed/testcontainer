package com.test.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Locale;

@Slf4j
class MessengerTest {

    @Test
    void rb() {
        log.info("$$$ {} ", Locale.getDefault());

        log.info("$$$ {} ", Messenger.ALREADY_FALLBACK.translate());

        log.info("$$$ {} ", Messenger.ALREADY_FALLBACK.toDutch());
        log.info("$$$ {} ", Messenger.ALREADY_FALLBACK.toHindi());
        log.info("$$$ {} ", Messenger.ALREADY_FALLBACK.toTagalog());
    }

}
