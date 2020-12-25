import java.util.ArrayList;
import java.util.List;

public class te {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("sdffsd");
        list.add("sdf");
        getewr(list);

        for (String s : list) {
            System.out.println(s);
        }
        System.out.println("dsf".equals(null));
    }

    private static void getewr(ArrayList<String> list) {
        list.remove(1);
    }
}
