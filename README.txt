DESCRIPTION:

minuteClientServer is a client-server project build in IntelliJ IDEA.

minuteServer is a Java, Spring based server with two Java classes: MinuteApplication and Data


 - MinuteApplication.java is the main application, which every minute on request serves a string that holds the hashed values of the local current date and time from Data.java via REST.
   Each request handled is logged and the string can be requested by navigating to localhost:8080/minutes in a browser.

 - Data.java is a simple data structure that holds and updates a string of two hashes (time and date), divided by a space.

Client is a Java based client with one Java class: Client.java

 - Client.java fetches a string at random intervals (at least once pr. minute is default) from an url (localhost:8080/minutes is default), de-hashes it, logs the hash and the result string 
   and presents it in a HTML file (Results.html is default).


HOW TO RUN:


To run the program, run the main method of MinuteApplication.java first and after it is up and running, run the main method of Client.java.


NOTES/DESIGN CHOICES:


Since hashing is inherently supposed to be a one-way operation, de-hashing can be a long and taxing endeavour. If a single hash for date and time had been used and my personal computer was used. 
Checking hashes of a year worth of seconds, without nanoseconds being accounted for, would take approximately 3.2 seconds, which would mean that each de-hashing of a date up until 2020 would take
approximately 1.8 hours, which means that even with parallel and/or concurrent programming, this would still be way too long to be constructive, when a new date/time would be created every minute.
For this reason, date and time was divided, hashed separately and served as a single string. Nanoseconds was omitted from the time for the same reason as well.

.hashCode() was chosen for simplicity, while a more advanced hashing could be implemented for increased security and variation of hashes.

