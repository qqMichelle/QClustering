import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class RandPick {

	public static int[] pickNRandom(int[] array, int n) {

		List<Integer> list = new ArrayList<Integer>(array.length);
		for (int i : array) {
			list.add(i);
		}
		Collections.shuffle(list);

		int[] answer = new int[n];
		for (int i = 0; i < n; i++)
			answer[i] = list.get(i);
		Arrays.sort(answer);

		return answer;

	}

	public static void main(String[] args) {
		int input_size = Integer.valueOf(args[0]);
		int output_size = Integer.valueOf(args[1]);
		int[] input_array = new int[input_size];
		for (int i = 0; i < input_array.length; i++) {
			input_array[i] = i + 1;
		}
		if (output_size < input_size) {
			int[] output_array = pickNRandom(input_array, output_size);
			for (int o : output_array) {
				System.out.print(o + " ");
			}
		}
		return;
	}
}
