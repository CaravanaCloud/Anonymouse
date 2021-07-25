package cloud.caravana.anonymouse.classifier;

import cloud.caravana.anonymouse.Classification;
import cloud.caravana.anonymouse.util.Wordlists;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static cloud.caravana.anonymouse.PIIClass.CPF;

@ApplicationScoped
public class CPFClassifier extends Classifier<String> {
    Logger log = Logger.getLogger("CPFClassifier");

    public static boolean isPIICPF(String cpf) {
        return ! Wordlists.isAnonymCPF(cpf);
    }


    @Override
    public Optional<Classification> classify(String value,
                                             String... context) {
        return ifDeclared(value, CPF, context);
    }

    @Override
    public String generateString(String columnValue, long index, String... context) {
        return Wordlists.randomAnonymCPF();
    }

    @Override
    protected boolean isAnonymized(String value) {
        return ! isPIICPF(value);
    }

    public static void main(String[] args) {
        generateCPFs("00000000000", 99999, "cpflist.txt");
    }

    private static void generateCPFs(String cpf,
                                     Integer gen,
                                     String destination) {
        try(PrintStream ps = new PrintStream(destination)){
            for (var i = 0; i < gen; i++) {
                cpf = nextCPF(cpf);
                ps.println(cpf);
            }
            ps.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String nextCPF(String cpf) {
        var base = cpf.substring(0, 9);
        var parsed = Integer.valueOf(base.toString());
        parsed += 1;
        var newBase = String.format("%09d", parsed);
        Integer s1 = 0;
        for (int i = 0; i < newBase.length(); i++) {
            char digChar = newBase.charAt(i);
            int digInt = digChar - '0';
            s1 += digInt * (10 - i);
        }
        var d1 = 11 - (s1 % 11);
        if (d1 == 10 || d1 == 11) {
            d1 = 0;
        }
        newBase += d1;
        Integer s2 = 0;
        for (int i = 0; i < newBase.length(); i++) {
            char digChar = newBase.charAt(i);
            int digInt = digChar - '0';
            s2 += digInt * (11 - i);
        }
        var d2 = 11 - (s2 % 11);
        if (d2 == 10 || d2 == 11) {
            d2 = 0;
        }

        var cpfStr = newBase + d2;
        return cpfStr;
    }
}
