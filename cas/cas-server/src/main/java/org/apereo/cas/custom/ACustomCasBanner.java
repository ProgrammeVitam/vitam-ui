package org.apereo.cas.custom;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

/**
 * The Vitam UI custom abnner.
 */
public class ACustomCasBanner extends AbstractCasBanner {

    @Override
    protected String getTitle() {
        return "   _______           _____  __      ___ _                  _    _ _______  \n" +
            "  / / ____|   /\\    / ____| \\ \\    / (_) |                | |  | |_   _\\ \\ \n" +
            " | | |       /  \\  | (___    \\ \\  / / _| |_ __ _ _ __ ___ | |  | | | |  | |\n" +
            " | | |      / /\\ \\  \\___ \\    \\ \\/ / | | __/ _` | '_ ` _ \\| |  | | | |  | |\n" +
            " | | |____ / ____ \\ ____) |    \\  /  | | || (_| | | | | | | |__| |_| |_ | |\n" +
            " | |\\_____/_/    \\_\\_____/      \\/   |_|\\__\\__,_|_| |_| |_|\\____/|_____|| |\n" +
            "  \\_\\                                                                  /_/ \n" +
            "                                                                           \n";
    }
}
