package project;

import lotus.notes.NotesThread;
import models.Person;
import org.riverframework.River;
import org.riverframework.core.IndexedDatabase;
import org.riverframework.core.Session;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by mario.sotil on 10/20/2015.
 */
@Singleton
public class DatabaseConnection {

    private Session session;
    private IndexedDatabase database;

    @Inject
    public DatabaseConnection(ApplicationLifecycle lifecycle) {
        String server = (String) null;
        String username = (String) null;
        String password = "passw0rd";
        String filepath = "d:\\test-play-database.nsf";

        // Creating a new Notes thread
        NotesThread.sinitThread();

        // This will close the session and the Notes thread
        lifecycle.addStopHook(() -> {
            Logger.info("Stopping River session");

            if (session != null) session.close();
            NotesThread.stermThread();

            Logger.info("River session stopped");

            return F.Promise.pure(null);
        });

        // Opening a new River Framework session
        session = River.getSession(River.LOTUS_DOMINO, server, username, password);
        Logger.info("River session opened: " + session.getUserName());

        // Opening the database
        database = session.getDatabase(AddressBook.class, "", filepath);

        // If not, then we create a new one
        if (!database.isOpen()) {
            database = session.createDatabase(AddressBook.class, "", filepath);

            if (!database.isOpen()) {
                Logger.error("I could not create the test database at the local client, with the name '" + filepath + "'");
            } else {
                Logger.info("Database opened: " + database.getName());

                // Creating five hundred person documents
                System.out.println("Creating documents with random data...");
                for (int i = 0; i < 500; i++) {

                    //Getting some random values
                    String name = RandomValues.getAFirstName();
                    String lastName = RandomValues.getALastName();
                    String domain = RandomValues.getADomain();

                    //Saving as a new document
                    database.createDocument(Person.class)
                            .setField("FirstName", name)
                            .setField("LastName", lastName)
                            .setField("FullName", name + " " + lastName)
                            .setField("InternetAddress", (name + "." + lastName + "@" + domain).toLowerCase())
                            .generateId()
                            .save();
                }

                Logger.info("Random values created");
            }

        }
    }

    public Session getSession() {
        return session;
    }

    public IndexedDatabase getDatabase() {
        return database;
    }
}
