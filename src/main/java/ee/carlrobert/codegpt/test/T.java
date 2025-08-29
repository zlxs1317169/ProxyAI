package ee.carlrobert.codegpt.test;



public class T {

    /**
     * 剪枝算法示例：使用回溯法解决N皇后问题
     * 通过剪枝减少无效搜索，提高算法效率
     */
    public static void main(String[] args) {
        int n = 8; // 8皇后问题
        int[] board = new int[n]; // board[i]表示第i行皇后放置的列号
        solveNQueens(board, 0);
    }

    // 递归回溯求解N皇后问题
    private static void solveNQueens(int[] board, int row) {
        int n = board.length;
        if (row == n) {
            printBoard(board);
            return;
        }

        for (int col = 0; col < n; col++) {
            if (canPlace(board, row, col)) { // 剪枝判断当前列是否可放置皇后
                board[row] = col;
                solveNQueens(board, row + 1);
                // 回溯：这里不需要显式清理board[row]，下一次赋值会覆盖
            }
        }
    }

    // 判断(row, col)位置是否可以放置皇后（剪枝条件）
    private static boolean canPlace(int[] board, int row, int col) {
        for (int i = 0; i < row; i++) {
            // 判断同列或对角线冲突
            if (board[i] == col || Math.abs(board[i] - col) == Math.abs(i - row)) {
                return false;
            }
        }
        return true;
    }

    // 打印当前皇后摆放方案
    private static void printBoard(int[] board) {
        int n = board.length;
        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(board[i] == j ? "Q " : ". ");
            }
            System.out.println(sb.toString());
        }
        System.out.println();
    }
}
