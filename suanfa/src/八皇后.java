

public class 八皇后 {

    private Integer count = 0;
    private int N;

    public static void main(String[] args) {
        int b[][] = new int[6][6];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                b[i][j] = 0;
            }
        }

        八皇后 queue = new 八皇后();
        queue.print(b);
        queue.N = 6;
        queue.fun(b, 0);
        System.out.println(queue.count);
    }


    boolean fun(int b[][], int c)// 从第 c 列开始放置
    {
        boolean flag = true;
        if (c >= b.length)
            return true;//已经没有地方可以再放置了

        for (int i = 0; i < N; i++) {
            flag = true;
            for (int j = 0; j <= c; j++) {
                if (b[i][j] == 1 || (i - j >= 0) && (c - j >= 0) && (b[i - j][c - j] == 1)) {
                    flag = false;
                    break;
                }
                if ((i + j < N) && (c - j >= 0) && (b[i + j][c - j] == 1)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                b[i][c] = 1; //放置一个皇后
                if (fun(b, c + 1))//递归的考虑下一列
                {
                    count++;
                    print(b);
                }
                b[i][c] = 0; //回溯
            }
        }
        return false;
    }

    public void print(int b[][]) {

        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b.length; j++) {
                System.out.print(b[i][j] + "  ");
            }
            System.out.println();
        }
        System.out.println();
    }

}

