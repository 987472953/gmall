package dsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Arrangements {

	
	static class Node {
		int starTime;
		int endTime;
		public Node(int starTime, int endTime) {
			super();
			this.starTime = starTime;
			this.endTime = endTime;
		}
	}
	
	public static void getSelect(List<Node> list) {
		Collections.sort(list,new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.endTime - o2.endTime;
			}
		});
		
		int j = 0;
		int count = 1;
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i).starTime >= list.get(j).endTime) {
				count++;
				j = i;
			}
		}
		System.out.println(count);
	}
	
	private static List<Node> getNode(String str1,String str2){
		List<Node> result = new ArrayList<Node>();
		String str1String[] = str1.split(" ");
		String str2String[] = str2.split(" ");
		for (int i = 0; i < str1String.length; i++) {
			result.add(new Node(Integer.parseInt(str1String[i]), Integer.parseInt(str2String[i])));
		}
		return result;
	}
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			int n = Integer.parseInt(scanner.nextLine());
			String str1 = scanner.nextLine();
			String str2 = scanner.nextLine();
			List<Node> nodes = getNode(str1, str2);
			getSelect(nodes);
		}
	}
}
