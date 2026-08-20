import java.util.Arrays;
import java.util.Scanner;

public class SortArrayCompilationError {
	public static void main(String args[]) {
		Scanner sc = new Scanner(System.in);
		int len = sc.nextInt() // no ;
		
		int[] arr = new int[len];
		
		for (int i = 0; i < len; i++) arr[i] = sc.nextInt();

		Arrays.sort(arr);
		for (int i : arr) System.out.print(i + " ");
	}
}
