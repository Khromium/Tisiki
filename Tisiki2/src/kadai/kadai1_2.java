package kadai;


import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class kadai1_2 {

    public static final int DATA_SIZE = 196;        //データのサイズ
    public static final int DATA_ARRAY = 200;       //1ファイルのデータの個数
    public static final int FILE_NUM = 46;          //ファイル数
    public static final int LOOP_MAX = 500000000;   //最大繰り返し回数
    public static final double EPS = 1.0e-5;        //収束条件
    private static final int MODE_FILE_INPUT = 0;   //ファイル入力
    private static final int MODE_MEAN_OUTPUT = 1;  //平均特徴量
    private static final int MODE_SIGMA_OUTPUT = 2; //共分散行列
    private static final int MODE_VECTOR_OUTPUT = 3;//固有ベクトル
    private static final int MODE_LAMBDA_OUTPUT = 4;//固有値

    //ファイルとデータの配列
    //[文字の種類][文字の数][特徴量]
    private double datas[][][];

    public kadai1_2() {
        try {
            getDatas();
        } catch (IOException e) {
            System.out.println("file load error\n" + e.toString());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        kadai1_2 a = new kadai1_2();
        try {
//            System.out.println("平均特徴量を計算し書き込みます。");
//            a.writeAvarageFetureArray();
//            System.out.println("分散・共分散行列を計算して書き込みます。");
//            a.writeCovariance();
            System.out.println("ヤコビ法を使い、固有値・固有ベクトルを算出し、書き込みます。");
            a.writeJakobi();
        } catch (Exception e) {
            System.out.println("ERROR\n" + e.toString());
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
     * ヤコビ行列を計算し、書き込みます
     *
     * @throws IOException          書き込みができなかった時
     * @throws EndressLoopException 値が収束しなかった時
     */
    public void writeJakobi() throws IOException, EndressLoopException {
        JacobiKey[] yakobi;
        for (int k = 0; k < FILE_NUM; k++) {
            yakobi = getJakobi(k);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(_getFileName(k + 1, MODE_VECTOR_OUTPUT))));
            PrintWriter lm = new PrintWriter(new BufferedWriter(new FileWriter(_getFileName(k + 1, MODE_LAMBDA_OUTPUT))));
            //ソートしました
            Collections.sort(Arrays.asList(yakobi), new Comparator<JacobiKey>() {
                @Override
                public int compare(JacobiKey o1, JacobiKey o2) {
                    if (o1.getLambda() < o2.getLambda()) {
                        return 1;
                    } else if (o1.getLambda() > o2.getLambda()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (int j = 0; j < DATA_SIZE; j++) {
                for (int i = 0; i < DATA_SIZE; i++) {
                    pw.print(String.format("%1$11.6f ", yakobi[j].getADatas()[i]));
                }
                lm.println(String.format("%1$11.6f ", yakobi[j].getLambda()));
                pw.println();
            }
            System.out.println("最初" + yakobi[0].getLambda());
            pw.close();
            lm.close();
            System.out.println("固有ベクトルファイル" + _getFileName(k + 1, MODE_VECTOR_OUTPUT) + "作成完了");


        }

    }

    /**
     * 対角行列を返します。
     *
     * @param data
     * @return
     */
    public double[] getDiagonalComponent(double[][] data) {
        double[] result = new double[DATA_SIZE];
        for (int i = 0; i < DATA_SIZE; i++) {
            result[i] = data[i][i];
        }
        return result;
    }

    /**
     * ヤコビ法を行い、固有値固有ベクトルを計算します
     *
     * @param moji 何文字目か
     * @return 固有値固有ベクトルのセット
     * @throws EndressLoopException 値が収束しなかった時。
     */
    public JacobiKey[] getJakobi(int moji) throws EndressLoopException {
        double[][] cova = getCovariance(moji); //covarince。共分散行列
        double[][] result = new double[DATA_SIZE][DATA_SIZE];
        JacobiKey[] yakobi = new JacobiKey[DATA_SIZE];
        boolean status = false;
        int count = 0, i = 0, j = 0;
        double amax, amaxtmp, theta, co, si, co2, si2, cosi, aii, aij, ajj, aik, ajk;

        //対角成分が1の行列を作成します。
        for (int k = 0; k < DATA_SIZE; k++) {
            result[k][k] = 1.0;
        }

        while (count <= LOOP_MAX) {
            amax = 0.0;
            //非対角成分の最大値を検索
            for (int k = 0; k < DATA_SIZE; k++) {
                for (int m = k + 1; m < DATA_SIZE; m++) {
                    if (m == k) continue;
                    amaxtmp = Math.abs(cova[k][m]);
                    if (amaxtmp > amax) {
                        amax = amaxtmp;
                        i = k;
                        j = m;
                    }
                }
            }

            //収束判定するよ
            if (amax <= EPS) {
                status = true;
                break;
            } else {
                aii = cova[i][i];
                aij = cova[i][j];
                ajj = cova[j][j];

                if (Math.abs(aii - ajj) < EPS) {
                    theta = 0.25 * Math.PI * aij / Math.abs(aij);
                } else {
                    theta = 0.5 * Math.atan(2.0 * aij / (aii - ajj));
                }

                co = Math.cos(theta);
                si = Math.sin(theta);
                co2 = Math.pow(co, 2);
                si2 = Math.pow(si, 2);
                cosi = co * si;

                cova[i][i] = co2 * aii + 2.0 * cosi * aij + si2 * ajj;
                cova[j][j] = si2 * aii - 2.0 * cosi * aij + co2 * ajj;
                cova[i][j] = 0.0;
                cova[j][i] = 0.0;
                for (int k = 0; k < DATA_SIZE; k++) {
                    if (k == i || k == j) continue;
                    aik = cova[k][i];
                    ajk = cova[k][j];
                    cova[k][i] = co * aik + si * ajk;
                    cova[i][k] = cova[k][i];
                    cova[k][j] = -1 * si * aik + co * ajk;
                    cova[j][k] = cova[k][j];
                }
                //固有ベクトル
                for (int k = 0; k < DATA_SIZE; k++) {
                    aik = result[k][i];
                    ajk = result[k][j];
                    result[k][i] = co * aik + si * ajk;
                    result[k][j] = -1 * si * aik + co * ajk;
                }
                count++;
            }
        }
        double[] lamda = getDiagonalComponent(cova);
        double[] column = new double[DATA_SIZE];
        for (int k = 0; k < DATA_SIZE; k++) {
            for (int m = 0; m < DATA_SIZE; m++) {
                column[m] = result[m][k];
            }
            yakobi[k] = new JacobiKey(column, lamda[k]);
        }

        if (status == false) throw new EndressLoopException("値が収束しませんでした");
        return yakobi;

    }

    /**
     * 平均特徴量を計算して書き込みます。
     *
     * @throws IOException
     */
    public void writeAvarageFetureArray() throws IOException {
        double[][] average = getAvarageFetureArray();
        for (int k = 0; k < FILE_NUM; k++) {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(_getFileName(k + 1, MODE_MEAN_OUTPUT))));
            for (int i = 0; i < DATA_SIZE; i++) {
                pw.println(average[k][i]);
            }
            pw.close();
        }
    }

    /**
     * 平均特徴量を返します
     * 使うところがあるかは微妙
     *
     * @return [文字][平均特徴量]
     * @throws IOException
     */
    public double[][] getAvarageFetureArray() throws IOException {
        double[][] average = new double[FILE_NUM][DATA_SIZE];
        for (int k = 0; k < FILE_NUM; k++) {
            average[k] = getAvarage(k);
        }
        return average;
    }

    /**
     * 共分散行列を計算し書き出します。
     *
     * @throws IOException
     */
    public void writeCovariance() throws IOException {
        double[][] datas;
        for (int k = 0; k < FILE_NUM; k++) {
            datas = getCovariance(k);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(_getFileName(k + 1, MODE_SIGMA_OUTPUT))));
            for (int j = 0; j < DATA_SIZE; j++) {
                for (int i = 0; i < DATA_SIZE; i++) {
                    pw.print(String.format("%1$11.6f ", datas[j][i]));

                }
                pw.println();
            }
            pw.close();
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
            buf = new BufferedReader(new FileReader(new File(_getFileName(i + 1, MODE_FILE_INPUT))));
            for (int j = 0; j < DATA_ARRAY; j++) {
                for (int k = 0; k < DATA_SIZE; k++) {
                    datas[i][j][k] = Double.parseDouble(buf.readLine());
                }
            }
            buf.close();
        }

    }

    /**
     * 指定されている文字の各データ毎の特徴量の平均を返します
     *
     * @param input 指定された文字
     * @return
     */
    public double[] getAvarage(int input) {
        double[] avarage = new double[DATA_SIZE];
        for (int j = 0; j < DATA_SIZE; j++) {
            for (int i = 0; i < DATA_ARRAY; i++) {
                avarage[j] += datas[input][i][j];
            }
        }
        for (int i = 0; i < DATA_SIZE; i++) {
            avarage[i] = avarage[i] / DATA_ARRAY;
        }
        return avarage;
    }

    /**
     * 共分散行列を取得します
     *
     * @param moji 何文字目？
     * @return 共分散行列
     */
    public double[][] getCovariance(final int moji) {
        double result[][] = new double[DATA_SIZE][DATA_SIZE], mi = 0, mj = 0;
        double[] ave = getAvarage(moji);
        for (int i = 0; i < DATA_SIZE; i++) {
            for (int j = 0; j < DATA_SIZE; j++) {
                for (int k = 0; k < DATA_ARRAY; k++) {
                    result[i][j] += datas[moji][k][i] * datas[moji][k][j];
                }
                result[i][j] /= DATA_ARRAY;
                result[i][j] -= ave[i] * ave[j];
            }
        }
        return result;
    }


    /**
     * ファイル名の生成
     *
     * @param value ファイルの数字
     * @param mode
     * @return
     */
    private String _getFileName(int value, int mode) {
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

    /**
     * 行列を一つ保持し、その中にラムダの値を入れられるクラス
     */
    public class JacobiKey {
        private double[] _datas;
        private double _lambda;

        public JacobiKey(double[] _datas, double _lambda) {
            this._datas = _datas;
            this._lambda = _lambda;
        }

//        public void setLambda(double lambda) {
//            _lambda = lambda;
//        }

        public double getLambda() {
            return _lambda;
        }

        public double[] getADatas() {
            return _datas;
        }

//        public void setADatas(double[] datas) {
//            this._datas = datas;
//        }
    }

    /**
     * 値が収束しなかったとき用
     */
    class EndressLoopException extends Exception {
        public EndressLoopException(String str) {
            super(str);
        }
    }
}
