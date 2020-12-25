

import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

public class One {
    public static void main(String[] args) throws IOException {
        One one = new One();
        int n = 5;
        int v = 1;
        int dist[] = new int[10];
        int prev[] = new int[10];
        int c[][] = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                c[i][j] = Integer.MAX_VALUE;
            }
        }
        c[1][2] = 10;
        c[1][5] = 100;
        c[1][4] = 30;
        c[2][3] = 50;
        c[3][5] = 10;
        c[4][3] = 20;
        c[4][5] = 60;

        one.dijkstra(n, v, dist, prev, c);
        for (int i = 1; i <= n; i++) {
            System.out.print(prev[i] + " ");
        }
        System.out.println();
        for (int i = 1; i <= n; i++) {
            System.out.print(dist[i] + " ");
        }
		System.out.println();
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <=n; j++) {
				System.out.printf("%11d" , c[i][j]);
			}
			System.out.println();
		}
    }


    //2147483647
    public void dijkstra(int n, int v, int dist[], int prev[], int c[][]) {
        boolean s[] = new boolean[100];
        for (int i = 1; i <= n; i++) {
            dist[i] = c[v][i];
            s[i] = false;
            if (dist[i] == Integer.MAX_VALUE) {
                prev[i] = 0;
            } else {
                prev[i] = v;
            }
        }
        dist[v] = 0;
        s[v] = true;
        for (int i = 1; i < n; i++) {
            int temp = Integer.MAX_VALUE;
            int u = v;
            for (int j = 1; j <= n; j++) {
                if ((!s[j]) && (dist[j] < temp)) {
                    u = j;
                    temp = dist[j];
                }
            }
            s[u] = true;
            for (int k = 1; k <= n; k++) {
                if ((!s[k]) && (c[u][k] < Integer.MAX_VALUE)) {
                    int newdist = dist[u] + c[u][k];
                    if (newdist < dist[k]) {
                        dist[k] = newdist;
                        prev[k] = u;
                    }
                }
            }
        }
    }
}
