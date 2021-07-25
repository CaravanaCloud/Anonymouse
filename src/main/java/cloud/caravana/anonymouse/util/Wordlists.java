package cloud.caravana.anonymouse.util;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Wordlists {
    static final Map<String, List<String>> wordlists = new HashMap<>();
    public static final String FULL_NAMES = "/wordlists/anonymouse/full_name.txt";
    private static final String CPF = "/wordlists/anonymouse/cpf.txt" ;


    public static String randomFrom(String wordlistName) {
        var word = (String) null;
        List<String> worlist = getWordlist(wordlistName);
        var size = worlist.size();
        if (size > 0){
            var rand = new Random().nextInt(size);
            word = worlist.get(rand);
        }
        return word;
    }

    private static List<String> getWordlist(String wordlistName) {
        var wordlist = wordlists.get(wordlistName);
        if (wordlist == null) {
            try {
                wordlist = loadWordList(wordlistName);
            } catch (IOException e) {
                return null;
            }
            wordlists.put(wordlistName, wordlist);
        }
        return wordlist;
    }

    public static String nameFrom(String wordlist) throws IOException {
        var input = randomFrom(wordlist);
        return  input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static List<String> loadWordList(String wordlistName) throws IOException {
        var res = Utils.class.getResourceAsStream(wordlistName);
        var reader = new BufferedReader(new InputStreamReader(res));
        var line = (String) null;
        var wordlist = new ArrayList<String>();
        while ((line = reader.readLine()) != null){
            wordlist.add(line);
        }
        return wordlist;
    }

    private static String fullBRName() throws IOException {
        var result = new StringBuffer();
        result.append(nameFrom("/wordlists/pt/first_name.txt"));
        var surnames = 1 + new Random().nextInt(3);
        for (int i = 0; i < surnames; i++) {
            result.append(" ");
            result.append(nameFrom("/wordlists/pt/last_name.txt"));
        }
        return result.toString();
    }

    public static String randomAnonymCPF() {
        return randomFrom(CPF);
    }

    public static final String randomAnonymFullName() throws IOException {
        return nameFrom(FULL_NAMES);
    }

    public static final boolean isAnonymFullName(String fullName) throws IOException {
        return getWordlist(FULL_NAMES).contains(fullName);
    }


    public static final void genNames(int cnt) throws IOException {
        var writer = new BufferedWriter(new FileWriter("full_names.txt"));
        for (int i = 0; i < cnt ; i++) {
            writer.write(fullBRName()+"\n");
        }
        writer.close();
    }

    public static boolean isAnonymCPF(String cpf) {
        return getWordlist(CPF).contains(cpf);

    }

    public static void main(String[] args) throws IOException {
        String anonymFullName = randomAnonymFullName();
        System.out.println(anonymFullName);
        System.out.println(isAnonymFullName(anonymFullName+" da Silva"));
    }

}
