package dsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WholeArrangement {
	private List<List<Integer>> total = new ArrayList<>();

	public static void main(String[] args) {
		int num[] = {1, 1, 2, 2};
		List<List<Integer>> permute = new WholeArrangement().permuteUnique(num);
		System.out.println(permute);
	}
	
	class Node {
		int value;
		int index;

		public Node(int value, int index) {
			super();
			this.value = value;
			this.index = index;
		}

		@Override
		public boolean equals(Object obj) {
			Node node = (Node) obj;
			return value == node.value && index == node.index;
		}
	}

	private boolean isN(List<Integer> next) {
		Iterator<List<Integer>> iterator = total.iterator();
		while (iterator.hasNext()) {
			List<Integer> list = iterator.next();
			if (list == next) continue;
			if (isY(list,next)) return true;
		}
		return false;
	}

	private boolean isY(List<Integer> list, List<Integer> next) {
		int size = list.size();
		if (next.size() != size) return false;
		for(int i = 0 ; i < size ; i++) {
			if (list.get(i) != next.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	private List<Integer> arrayCopy(List<Node> list) {
		List<Integer> temp = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			temp.add(list.get(i).value);
		}
		return temp;
	}

	private void per(Node node, List<Node> list, List<Node> numNodes) {
		if (list.size() == numNodes.size()) {
			total.add(arrayCopy(list));
			return;
		}

		for (int i = 0; i < numNodes.size(); i++) {
			if (!node.equals(numNodes.get(i)) && !list.contains(numNodes.get(i))) {
				list.add(numNodes.get(i));
				per(numNodes.get(i), list, numNodes);
				list.remove(list.indexOf(numNodes.get(i)));
			}
		}
	}
	
	

	
	public List<List<Integer>> permuteUnique(int[] nums) {
		List<Node> numNodes = new ArrayList<>();
		for (int i = 0; i < nums.length; i++) {
			numNodes.add(new Node(nums[i], i));
		}
		for (int i = 0; i < numNodes.size(); i++) {
			List<Node> list = new ArrayList<>();
			list.add(numNodes.get(i));
			per(numNodes.get(i), list, numNodes);
		}
		
		Iterator<List<Integer>> iterator = total.iterator();
		while (iterator.hasNext()) {
			List<Integer> next = iterator.next();
			if (isN(next)) {
				iterator.remove();
			}
		}
		return total;
	}
}
