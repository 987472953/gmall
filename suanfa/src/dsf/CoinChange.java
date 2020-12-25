package dsf;

import java.util.Arrays;
import java.util.Scanner;

public class CoinChange {

    public static void getCoinChange(int[] coin, int count) {
        if (coin == null || coin.length == 0) {
            System.out.println("Impossible");
        }

        int[] nums = new int[count + 1];

        for (int i = 1; i < nums.length; i++) {
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < coin.length; j++) {
                if (i < coin[j]) {
                    continue;
                }
                if (nums[i - coin[j]] < 0 || nums[i - coin[j]] >= min) {
                    continue;
                }
                min = nums[i - coin[j]];
            }
            if (min == Integer.MAX_VALUE) {
                nums[i] = -1;
            } else {
                nums[i] = min + 1;
            }
        }
        if (nums[count] == -1) {
            System.out.println("Impossible");
        } else {
            System.out.println(nums[count]);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            int n = Integer.parseInt(scanner.nextLine());
            String line = scanner.nextLine();
            int coin[] = Arrays.stream(line.split(" ")).mapToInt(Integer::parseInt).toArray();
            int count = Integer.parseInt(scanner.nextLine());
            getCoinChange(coin, count);
        }
    }
}
