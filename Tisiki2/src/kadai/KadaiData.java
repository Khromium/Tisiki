package kadai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Khromium on 2015/11/29.
 */
public class KadaiData {


    public static final int FILE_NUM = 46;          //ファイル数
    public static final int MODE_FILE_INPUT = 0;   //ファイル入力
    public static final int MODE_MEAN_OUTPUT = 1;  //平均特徴量
    public static final int MODE_SIGMA_OUTPUT = 2; //共分散行列
    public static final int MODE_VECTOR_OUTPUT = 3;//固有ベクトル
    public static final int MODE_LAMBDA_OUTPUT = 4;//固有値
    public static final int DATA_SIZE = 196;        //データのサイズ
    public static final int DATA_ARRAY = 200;       //1ファイルのデータの個数
    //ファイルとデータの配列
    //[文字の種類][文字の数][特徴量]
    private double datas[][][];

    public KadaiData() {
        try {
            getDatas();
        } catch (Exception e) {
            System.out.println("file load error\n" + e.toString());
            System.exit(1);
        }
    }

    /**
     * データをすべて取得し、配列に収納します。
     *
     * @throws IOException
     */
    public void getDatas() throws IOException {
        BufferedReader buf;
        datas = new double[FILE_NUM][DATA_ARRAY][DATA_SIZE];
        for (int i = 0; i < FILE_NUM; i++) {
            buf = new BufferedReader(new FileReader(new File(getFileName(i + 1, MODE_FILE_INPUT))));
            for (int j = 0; j < DATA_ARRAY; j++) {
                for (int k = 0; k < DATA_SIZE; k++) {
                    datas[i][j][k] = Double.parseDouble(buf.readLine());
                }
            }
            buf.close();
        }

    }
//    /**
//     * 配列に取り込んだデータを取得します。
//     *
//     * @param file ファイル番号
//     * @param num  文字番号
//     * @param fea  特徴番号
//     * @return データ
//     */
//    public double getData(int file, int num, int fea) {
//        return datas[file][num][fea];
//    }

    /**
     * データを返します
     * @param moji 何文字目か
     * @param dataNum 何データ目か
     * @param place 何番目のデータか
     * @return データ
     */
    public double getConponentOf(int moji, int dataNum, int place) {
        return datas[moji][dataNum][place];
    }

    /**
     * ファイル名の生成
     *
     * @param value ファイルの数字
     * @param mode
     * @return
     */
    public static String getFileName(int value, int mode) {
        switch (mode) {
            case MODE_FILE_INPUT:
                return "c" + String.format("%1$02d", value) + ".txt";
            case MODE_MEAN_OUTPUT:
                return "means" + String.format("%1$02d", value) + ".txt";
            case MODE_SIGMA_OUTPUT:
                return "sigma" + String.format("%1$02d", value) + ".txt";
            case MODE_VECTOR_OUTPUT:
                return "vector" + String.format("%1$02d", value) + ".txt";
            case MODE_LAMBDA_OUTPUT:
                return "lambda" + String.format("%1$02d", value) + ".txt";
            default:
                return null;
        }
    }
}
