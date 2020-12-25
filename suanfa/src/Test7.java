public class Test7 {

    public static void main(String[] args) {
        int n = 6;
        int c[][] = new int [7][7];

        //初始化矩阵
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                c[i][j] = 999;
            }
        }
        c[1][2] = 6;
        c[1][3] = 1;
        c[1][4] = 5;
        c[2][1] = 6;
        c[2][3] = 5;
        c[2][5] = 6;
        c[3][1] = 1;
        c[3][2] = 5;
        c[3][4] = 5;
        c[3][5] = 6;
        c[3][6] = 4;
        c[4][1] = 5;
        c[4][3] = 5;
        c[4][6] = 2;
        c[5][2] = 3;
        c[5][3] = 6;
        c[5][5] = 6;
        c[6][3] = 4;
        c[6][4] = 2;
        c[6][5] = 6;
        Test7 test7 = new Test7();
        test7.prim(n, c);

    }


    void prim(int n, int c[][]) {
        int lowcost[] = new int[10];
        int closest[] = new int[10];
        boolean s[] = new boolean[10];
        s[1] = true;
        for (int i = 2; i <= n; i++) {
            lowcost[i] = c[1][i];
            closest[i] = 1;
            s[i] = false;
        }
        for (int i = 1; i < n; i++) {
            int min = 999;
            int j = 1;
            for (int k = 2; k <= n; k++) {
                if ((lowcost[k] < min) && (!s[k])) {
                    min = lowcost[k];
                    j = k;
                }
            }
            System.out.println("j: " + j + " closest[j]: " + closest[j]);
            s[j] = true;
            for (int k = 1; k <=n; k++) {
                if ((c[j][k]<lowcost[k]) && (!s[k])){
                    lowcost[k] = c[j][k];
                    closest[k]= j;
                }
            }
        }
    }
}
