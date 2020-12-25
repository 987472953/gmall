package dsf;

import java.util.Scanner;

public class MaxCommonDivisor {
	public static int getCommonDivisor(int m, int n) {
		if (n == 0) {
			return m;
		}else {
			return getCommonDivisor(n,m % n);
		}
	}
	
	public static void main(String[] args) {
		int m;
		int n;
		Scanner scanner = new Scanner(System.in);
		System.out.println("请输入m的值:");
		m = scanner.nextInt();
		System.out.println("请输入n的值:");
		n = scanner.nextInt();
		System.out.println("最大公约数为:" + getCommonDivisor(m, n));
	}

}
