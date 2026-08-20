import java.util.Scanner;

public class ReverseArrayWrongAnswer {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for(int i = 0; i < n; i++) arr[i] = sc.nextInt();

        // print without rev
        for(int i = 0; i < n; i++) System.out.print(arr[i] + " ");
    }
}
