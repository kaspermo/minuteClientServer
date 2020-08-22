package com.server.minute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Server works as a simple REST server, that serves the client with the timeHash of Data upon request.
 * Default mapping is "localhost:8080" and postMinutes maps requests to "/minutes".
 * Request should therefore be made to localhost:8080/minutes
 * Each request is logged.
 *
 * @author Kasper Møller Nielsen
 * @version 1.0
 * @since 2020-21-08
 */

@SpringBootApplication
@RestController
public class MinuteApplication {

    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    /**
     * Runs the application and updates the timeHash of Data with setTime() once every minute.
     *
     * @param args command-line arguments.
     */

    public static void main(String[] args) {
        SpringApplication.run(MinuteApplication.class, args);
        ses.scheduleAtFixedRate(Data::setTime, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Upon request (mapped to /minutes of default localhost, port 8080) serves the timeHash of Data to the client
     * that requests it. Each request is logged.
     * @return the timeHash of Data. A String with a hashed time and date, divided by a space.
     */
    @RequestMapping("/minutes")
    public String postMinutes() {
        String timeHash = Data.getTime();
        Logger.getLogger("Server").log(Level.INFO, "requested: " + timeHash);
        return timeHash;
    }

}
