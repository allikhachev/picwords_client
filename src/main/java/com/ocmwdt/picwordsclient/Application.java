package com.ocmwdt.picwordsclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.openqa.selenium.remote.UnreachableBrowserException;

import com.ocmwdt.picwordsclient.exceptions.ClientException;

/**
 * Game client controller.
 *
 * @author alexey.likhachev
 */
public class Application {

    private static final String DEFAULT_PASSWORD = "663399";
    private static final String DEFAULT_EMAIL = "some85one@mail.ru";

    private static Logger LOG = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) throws IOException, ClientException, InterruptedException {

        initLogger();

        String email = DEFAULT_EMAIL;
        String password = DEFAULT_PASSWORD;
        if (args.length == 2) {
            email = args[0];
            password = args[1];
        }

        for (;;) {
            try {
                GameController controller = new MainController();
                controller.run(email, password, 0, 0, 0);
            } catch (UnreachableBrowserException ube) {
                LOG.log(Level.SEVERE, "Browser is closed", ube);
                break;
            } catch (RuntimeException re) {
                LOG.log(Level.SEVERE, re.toString(), re);
            }
        }

    }

    private static void initLogger() {
        try {
            LogManager.getLogManager().readConfiguration(
                Application.class.getResourceAsStream("/META-INF/logging.properties"));
        } catch (IOException ioe) {
            System.err.println("Could not setup logger configuration: " + ioe.toString());
        }
    }
}
