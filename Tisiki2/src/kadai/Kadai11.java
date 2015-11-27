package kadai;

import java.io.*;

/**
 * Created by Khromium on 2015/11/04.
 */
public class Kadai11 {

    public static final int DATA_SIZE = 196;
    public static final int DATA_ARRAY = 200;
    public static final int FILE_NUM = 46;
    //ファイルとデータの配列
    private double datas[][];

    public static void main(String[] args) {
        Kadai11 a = new Kadai11();
        try {
            a.getAvarageData();
        } catch (Exception e) {
            System.out.println("ERROR" + e.toString());
        }
    }


    public void getAvarageData() throws IOException {
        double[] average = new double[DATA_SIZE];
        try {
            getDatas();
        } catch (IOException e) {
            System.out.println("READ ERROR" + e.toString());
            System.exit(1);
        }

        for (int k = 0; k < FILE_NUM; k++) {
            average = getAvarage(k);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(getFileName(k+1, false))));
            for (int i = 0; i < DATA_SIZE; i++) {
                pw.println(average[i]);
            }
            pw.close();
        }
    }

    public void getDatas() throws IOException {
        BufferedReader buf;
        datas = new double[FILE_NUM][DATA_SIZE * DATA_ARRAY];
        for (int i = 0; i < FILE_NUM; i++) {
            buf = new BufferedReader(new FileReader(new File(getFileName(i+1, true))));
            for (int j = 0; j < DATA_SIZE * DATA_ARRAY; j++) {
                datas[i][j] = Integer.parseInt(buf.readLine());
            }
            buf.close();
        }

    }

    public double[] getAvarage(int input) {
        double[] avarage = new double[DATA_SIZE];
        for (int j = 0; j < DATA_SIZE; j++) {
            for (int i = j; i < DATA_SIZE * DATA_ARRAY; i += DATA_SIZE) {
                avarage[j] += datas[input][i];
            }
        }
        for (int i = 0; i < DATA_SIZE; i++) {
            avarage[i] = avarage[i] / DATA_ARRAY;
        }
        return avarage;
    }

    /**
     * ファイル名の生成
     *
     * @param value ファイルの数字
     * @param mode  true:input  false:output
     * @return
     */
    public String getFileName(int value, boolean mode) {
        if (mode) {
            return "c" + String.format("%1$02d", value) + ".txt";
        } else {
            return "means" + String.format("%1$02d", value) + ".txt";
        }
    }

}
