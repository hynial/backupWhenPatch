package me;

public class TmpMain {

    public static void main(String[] args) {
        String[] t = doWhatYouWant();

        System.out.println(t[0] + t[1]);
    }

    public static String[] doWhatYouWant() {
        String a = "1", b = "2";
        String[] t = new String[] {a, b};
        return t;
    }
}
