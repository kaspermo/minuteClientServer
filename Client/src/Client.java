import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client fetches a hashed string from a url at random intervals, de-hashes it and present it in a HTML file.
 * <p>
 * The program requires a server to run in order to fetch anything.
 * The program assumes it will receive a String with two hashes divided by a space.
 * The program assumes that nanoseconds have been omitted before the original LocalDateTime was hashed.
 * Format of result can be changed if desired
 * HtmlFile path can be exchanged if desired
 * Url can be exchanged with another url if desired, this should however match with the url of the server posting
 * X can be exchanged for another int to increase or decrease the possible random delay between fetches (currently set to at least once a minute)
 * Each incident is logged.
 *
 * @author Kasper Møller Nielsen
 * @version 1.0
 * @since 2020-21-08
 */


public class Client {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");        //format yyyy/MM/dd HH:mm:ss can be exchanged if a different format is preferred
    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private static final JEditorPane p = new JEditorPane();
    private static final File htmlFile = new File("Results.html");                                 //can be exchanged for different path
    private static Random r;
    private static final String url = "http://localhost:8080/minutes";                                            //can be exchanged for different url (match with server url)
    private static int counter = 0;
    private static final int x = 60;                                                                              //can be exchanged for different range of random delay between fetches
    private static int ri;
    private static String line;
    private static String s;

    /**
     * The method de-hashes a string of two hashes, one hash for LocalDate and one for LocalTime.
     * <p>
     * This is done by looping over each possible year/month/day and hour/minute/second, until a hash matches.
     * On match, each loop breaks.
     * The string is formatted for convenience and readability before it is returned.
     * If no match is found, the method returns a info string that no match was found.
     *
     * @param hashString String of two hashes (a date and a time) separated by a space.
     * @return a String of the de-hashed date and time, with the format "year/month/day hour:minute:second".
     */

    private static String deHash(String hashString) {

        String[] hashArr = hashString.split(" ");                              //splits the two hashes of the string
        String dateHash = hashArr[0];
        String timeHash = hashArr[1];
        LocalDate dateString = null;
        LocalTime timeString = null;
        for (int y = 0; y < Integer.MAX_VALUE; y++) {                               //date loop, matches date to the first hash of the string, breaks loop on match
            if (dateString != null) break;
            for (int m = 1; m < 13; m++) {
                if (dateString != null) break;
                for (int d = 1; d < YearMonth.of(y, m).lengthOfMonth() + 1; d++) {
                    String str = String.valueOf(LocalDate.of(y, m, d).hashCode());
                    if (str.equals(dateHash)) {
                        dateString = LocalDate.of(y, m, d);
                        break;
                    }
                }
            }
        }

        for (int h = 0; h < 24; h++) {                                              //time loop, matches time to the second hash of the string, breaks loop on match
            if (timeString != null) break;
            for (int m = 0; m < 60; m++) {
                if (timeString != null) break;
                for (int s = 0; s < 60; s++) {
                    String str = String.valueOf(LocalTime.of(h, m, s).hashCode());
                    if (str.equals(timeHash)) {
                        timeString = LocalTime.of(h, m, s);
                        break;
                    }
                }
            }
        }

        if (dateString != null && timeString != null)
            return dtf.format(LocalDateTime.of(dateString, timeString));            //formats the result of the date and time loop
        else
            return "No date/time was the result of de-hashing";                    //if no match is found, return info string

    }

    /**
     * The method updates #htmlFile with the latest result.
     * <p>
     * Increases #counter with each update.
     * Inserts the result above the previous results, so the newest result always is on top.
     * Writes over previous text of #htmlFile with new text (that includes the latest result).
     *
     * @see #line hash string recieved
     * @see #s de-hashed result string of #line
     * @see #htmlFile file path that the method is writing to
     * @see #counter keeps track of the number of results
     */

    private static void updateHtml() {
        try {
            HTMLDocument d = (HTMLDocument) p.getDocument();
            counter++;
            d.insertAfterStart(d.getElement("results"), "<p>" +
                    counter +
                    ".<br>Hash: " +
                    line +
                    "<br>Result: " +
                    s + "</p>");                                                            //html string to add to htmlFile
            p.setDocument(d);

            FileOutputStream fos = new FileOutputStream(htmlFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(p.getText());                                                          //writes all the text to htmlFile
            bw.close();
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up base text and content type for #htmlFile if not already set.
     * <p>
     * Creates a Runnable, that connects to #url and fetches the hashed date and time (#line).
     * #line is de-hashed by deHash() and becomes #s, both #line and #s is logged.
     * The connection is terminated and the htmlFile is updated by updateHtml().
     * The thread is then put to sleep for a random amount of time (between 0 and #x seconds).
     * <p>
     * The runnable is continuously run via #ses until the program is terminated, but effectively at random intervals,
     * due to the sleeping thread.
     *
     * @param args command-line arguments.
     * @see #ses singleThreadScheduledExecutor responsible for continously running the program
     * @see #x the maximum number of seconds the thread can possibly sleep
     * @see #updateHtml() updates #htmlFile with the newest result
     * @see #htmlFile the filepath to the htmlfile that will hold the results
     * @see #deHash(String) de-hashes #line to #s from hashes to date and time string
     * @see #line hash string recieved
     * @see #s de-hashed result string of #line
     * @see #url url to connect to and get hash from
     */

    public static void main(String[] args) {

        if (!p.getContentType().equals("text/html")) {
            p.setContentType("text/html");
            p.setText(" <html>\n" +
                    "   <head>\n" +
                    "     <title>Results of minutesServer\n" +
                    "   </title>\n" +
                    "   </head>\n" +
                    "   <body>\n" +
                    "     <h1>Results\n" +
                    "     </h1>\n" +
                    "     <div id=\"results\">\n" +
                    "     </div>\n" +
                    "   </body>\n" +
                    " </html>");
        }

        Runnable runnable = () -> {
            try {
                URL u = new URL(url);                                                                   //url to connect to
                HttpURLConnection con = (HttpURLConnection) u.openConnection();                         //connect to url
                con.setRequestMethod("GET");                                                            //specify get as HTTP method call
                if(con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    line = br.readLine();                                                                   //get hash from url connection
                    s = deHash(line);                                                                       //de-hash the line
                    con.disconnect();

                    Logger.getLogger("Client").log(Level.INFO, "Received: " +
                            line +
                            " Result: " +
                            s);                                                                             //logs the result

                    updateHtml();                                                                           //updates the html file

                    r = new SecureRandom();
                    ri = r.nextInt(x);                                                                      //random int between 0 and x
                    Thread.sleep(ri * 1000);                                                          //put the thread asleep for ri seconds
                } else {
                    System.out.println("GET request was not successful");
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

        };

        ses.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);                      //runs runnable every second
    }
}
