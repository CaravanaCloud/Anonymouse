package cloud.caravana.anonymouse.tdc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.time.LocalDate.*;
import static java.util.stream.Collectors.toList;

@StolenFrom(value = "ederign",
        link = "https://twitter.com/ederign/status/1375447652859584518")
// https://twitter.com/faermanj/status/1391045470433292290/photo/1
public class PracticandoLasNuevasFeatures {

    public static void main(String[] args) throws Exception {
        helpfulNPEs();
        localVars();
        textBlocks();
        funSwitch();
        bookRecords();
        convenientCollections();
        defaultMethods();
        executorsExecuted();
        http2ClientRocks();
        https://www.oracle.com/java/technologies/javase/jmc8-install.html
        jdkMissionControl();
        https://twitter.com/java/status/1281595701546192901
        thankYou();
    }

    private static void executorsExecuted() {
        //TODO: ExecutorService.newVirtualThreadExecutor();
        // var executor = Executors.newWorkStealingPool();
        var executor = Executors.newSingleThreadScheduledExecutor();
        for (int i = 0_0; i < 333_333; i++) {
            executor.submit(() -> medioBesta());
        }
    }

    private static void medioBesta() {
        final var rand = new Random();
        for (long j = 0; j < Long.MAX_VALUE ; j++) {
            if (j * rand.nextInt() % 333_333 == 0)
                System.out.println(j + " Jackpot!!!");
        }
    }

    private static void convenientCollections() {
        var xs = List.of(
          new Guinness("Fastest time to eat a bowl of pasta","26.69 seconds","Michelle Lesco",now(),true),
          new Guinness("Longest Time Burried","Still Counting","JANAKA BASNAYAKE",now(),false),
          new Guinness("Most consecutive pinky pull-ups","36","Tazio Gavioli",now(),true)
        );
        @ShowingOff("Method References")
        var xxs = xs.stream()
                .filter(Guinness::survived);
        xxs.forEach(System.out::println);
    }

    private static void defaultMethods() {
        var padre = new Darwin("Voar de balao",
                "Ate onde nao se sabe...",
                "Padre", now());
        System.out.println(padre.describeWithDefaultAndInstanceOf());
    }

    private static void http2ClientRocks() throws Exception {
        HttpClient httpClient
                = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .uri(new URI("http://site.caravana.cloud/"))
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(
                "Status of operation performed:"
                        + httpResponse.statusCode());

        System.out.println(httpResponse.body().substring(0, 100));
    }

    private static void jdkMissionControl() {
    }


    @faermanj
    private static void thankYou() {
        https://twitter.com/faermanj
        System.out.println("See you!");
    }


    sealed interface Award
            permits Guinness, Darwin {
        @ThoseKeywordsAreNew
        default String describeWithDefaultAndInstanceOf(){
            if (this instanceof Guinness g) {
                return ("Guinness " + g.wow());
            } else if (this instanceof Darwin d) {
                return ("Darwin " + d.rip());
            } else throw new IllegalArgumentException();
        }
    }

    record Guinness(String feat,
                    String description,
                    String holder,
                    LocalDate date,
                    Boolean survived
    ) implements Award {
        String wow() {
            return "WOW";
        }
    }

    record Darwin(String feat,
                  String description,
                  String holder,
                  LocalDate date
    ) implements Award {
        String rip() {
            return "RIP";
        }
    }

    //record TopOfMind(String anything) implements Award { }

    private static void bookRecords() {
        LocalDate now = now();
        var r1 = new Guinness("Largest tonge", "10.10 cm", "NIck Stoeberl", now, true);
        var r2 = new Guinness("Largest tonge", "10.10 cm", "NIck Stoeberl", now, true);
        if (r1.equals(r2)) {
            System.out.println(r1);
        }
    }

    enum Suits {
        Spades, Clubs, Hearts, Diamonds;
    }

    private static void funSwitch() {
        System.out.println(funSwitch(Suits.Diamonds));
    }

    private static String funSwitch(Suits suit) {
        return switch (suit) {
            case Spades -> "Ace";
            case Clubs -> "Closed";
            case Hearts -> "Wild";
            default -> {
                yield "Forever".toLowerCase();
            }
        };
    }

    private static void localVars() {
        var msg = "hello world %d";
        try (var writer = new PrintWriter("test.txt")) {
            for (var i = 0; i < 10; i++) {
                writer.println(msg.formatted(i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void textBlocks() {
        //Type Inference is Cool!
        var sql = """
                 SELECT hello, \n\t"world" 
                        count(hello)
                 FROM world
                 WHERE 1=1
                    AND %s BETWEEN %d AND %d 
                 GROUP BY \
                    hello
                """;
        var psql = sql.formatted("1", 2, 3);
        System.out.println(psql);
    }

    private static void helpfulNPEs() throws Exception{
        HelpfullNPE: NPEUtil.main(null);
    }
}
