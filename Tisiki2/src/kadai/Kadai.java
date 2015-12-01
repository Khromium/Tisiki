package kadai;


import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Kadai {

    public static final int LOOP_MAX = 500000000;   //最大繰り返し回数
    public static final double EPS = 1.0e-5;        //収束条件
    private KadaiData kadaiData;
    public static final int THREAD = 4;

    public Kadai() {
        kadaiData = new KadaiData();
    }

    public static void main(String[] args) {
        Kadai a = new Kadai();
        try {
//            System.out.println("平均特徴量を計算し書き込みます。");
//            a.writeAvarageFetureArray();
//            System.out.println("分散・共分散行列を計算して書き込みます。");
//            a.writeCovariance();
            System.out.println("ヤコビ法を使い、固有値・固有ベクトルを算出し、書き込みます。");
//            a.writeJakobi();
            a.threadAdapter();
        } catch (Exception e) {
            System.out.println("ERROR\n" + e.toString());
        }
    }

    public void threadAdapter() throws InterruptedException {
        long start = System.currentTimeMillis();
        int cnt = 0;
        SubThread[] sub = new SubThread[THREAD];
        int range = (int) Math.floor(KadaiData.FILE_NUM / THREAD);
        for (int i = 0; i < THREAD; i++) {
            if ((THREAD - i - 1) != 0) {
                sub[i] = new SubThread(cnt, cnt + range);
            } else {
                sub[i] = new SubThread(cnt, KadaiData.FILE_NUM);
            }
            sub[i].start();
            cnt += range;
        }
        for (int i = 0; i < THREAD; i++) {
            sub[i].join();
        }
        long end = System.currentTimeMillis();
        System.out.println("処理時間" + (end - start) + "ms");
        System.out.println("1ファイルあたりの処理時間" + (int)(end - start) / KadaiData.FILE_NUM + "ms");
    }
//
//    /**
//     * ヤコビ行列を計算し、書き込みます
//     *
//     * @throws IOException          書き込みができなかった時
//     * @throws EndressLoopException 値が収束しなかった時
//     */
//    public void writeJakobi() throws IOException, EndressLoopException {
//        JacobiKey[] yakobi;
//        for (int k = 0; k < KadaiData.FILE_NUM; k++) {
//            long start = System.currentTimeMillis();
//            yakobi = getJakobi(k);
//            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_VECTOR_OUTPUT))));
//            PrintWriter lm = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_LAMBDA_OUTPUT))));
//            //ソートしました
//            Collections.sort(Arrays.asList(yakobi), new Comparator<JacobiKey>() {
//                @Override
//                public int compare(JacobiKey o1, JacobiKey o2) {
//                    if (o1.getLambda() < o2.getLambda()) {
//                        return 1;
//                    } else if (o1.getLambda() > o2.getLambda()) {
//                        return -1;
//                    } else {
//                        return 0;
//                    }
//                }
//            });
//            for (int j = 0; j < KadaiData.DATA_SIZE; j++) {
//                for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
//                    pw.print(String.format("%1$11.6f ", yakobi[j].getADatas()[i]));
//                }
//                lm.println(String.format("%1$11.6f ", yakobi[j].getLambda()));
//                pw.println();
//            }
//            pw.close();
//            lm.close();
//            long end = System.currentTimeMillis();
//            System.out.println("処理時間" + (end - start) + "ms");
//            System.out.println(KadaiData.getFileName(k + 1, KadaiData.MODE_VECTOR_OUTPUT) + "　" + KadaiData.getFileName(k + 1, KadaiData.MODE_LAMBDA_OUTPUT) + "作成完了");
//        }
//    }

    /**
     * 対角行列を返します。
     *
     * @param data
     * @return
     */
    public double[] getDiagonalComponent(double[][] data) {
        double[] result = new double[KadaiData.DATA_SIZE];
        for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
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
        double[][] result = new double[KadaiData.DATA_SIZE][KadaiData.DATA_SIZE];
        JacobiKey[] yakobi = new JacobiKey[KadaiData.DATA_SIZE];
        boolean status = false;
        int count = 0, i = 0, j = 0;
        double amax, amaxtmp, theta, co, si, co2, si2, cosi, aii, aij, ajj, aik, ajk;

        //対角成分が1の行列を作成します。
        for (int k = 0; k < KadaiData.DATA_SIZE; k++) {
            result[k][k] = 1.0;
        }

        while (count <= LOOP_MAX) {
            amax = 0.0;
            //非対角成分の最大値を検索
            for (int k = 0; k < KadaiData.DATA_SIZE; k++) {
                for (int m = k + 1; m < KadaiData.DATA_SIZE; m++) {
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
                for (int k = 0; k < KadaiData.DATA_SIZE; k++) {
                    if (k == i || k == j) continue;
                    aik = cova[k][i];
                    ajk = cova[k][j];
                    cova[k][i] = co * aik + si * ajk;
                    cova[i][k] = cova[k][i];
                    cova[k][j] = -1 * si * aik + co * ajk;
                    cova[j][k] = cova[k][j];
                }
                //固有ベクトル
                for (int k = 0; k < KadaiData.DATA_SIZE; k++) {
                    aik = result[k][i];
                    ajk = result[k][j];
                    result[k][i] = co * aik + si * ajk;
                    result[k][j] = -1 * si * aik + co * ajk;
                }
                count++;
            }
        }
        double[] lamda = getDiagonalComponent(cova);
        double[] column;
        for (int k = 0; k < KadaiData.DATA_SIZE; k++) {
            column = new double[KadaiData.DATA_SIZE];
            for (int m = 0; m < KadaiData.DATA_SIZE; m++) {
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
        for (int k = 0; k < KadaiData.FILE_NUM; k++) {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_MEAN_OUTPUT))));
            for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
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
        double[][] average = new double[KadaiData.FILE_NUM][KadaiData.DATA_SIZE];
        for (int k = 0; k < KadaiData.FILE_NUM; k++) {
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
        for (int k = 0; k < KadaiData.FILE_NUM; k++) {
            datas = getCovariance(k);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_SIGMA_OUTPUT))));
            for (int j = 0; j < KadaiData.DATA_SIZE; j++) {
                for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
                    pw.print(String.format("%1$11.6f ", datas[j][i]));

                }
                pw.println();
            }
            pw.close();
        }
    }


    /**
     * 指定されている文字の各データ毎の特徴量の平均を返します
     *
     * @param input 指定された文字
     * @return
     */
    public double[] getAvarage(int input) {
        double[] avarage = new double[KadaiData.DATA_SIZE];
        for (int j = 0; j < KadaiData.DATA_SIZE; j++) {
            for (int i = 0; i < KadaiData.DATA_ARRAY; i++) {
                avarage[j] += kadaiData.getConponentOf(input, i, j);
            }
        }
        for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
            avarage[i] = avarage[i] / KadaiData.DATA_ARRAY;
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
        double result[][] = new double[KadaiData.DATA_SIZE][KadaiData.DATA_SIZE], mi = 0, mj = 0;
        double[] ave = getAvarage(moji);
        for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
            for (int j = 0; j < KadaiData.DATA_SIZE; j++) {
                for (int k = 0; k < KadaiData.DATA_ARRAY; k++) {
                    result[i][j] += kadaiData.getConponentOf(moji, k, i) * kadaiData.getConponentOf(moji, k, j);
                }
                result[i][j] /= KadaiData.DATA_ARRAY;
                result[i][j] -= ave[i] * ave[j];
            }
        }
        return result;
    }


    /**
     * 値が収束しなかったとき用
     */
    class EndressLoopException extends Exception {
        public EndressLoopException(String str) {
            super(str);
        }
    }

    /**
     * ヤコビ法がトロすぎるのでマルチスレッド化
     */
    class SubThread extends Thread {
        private int _start;
        private int _end;

        public SubThread(int start, int end) {
            _start = start;
            _end = end;
        }

        public void run() {
            try {
                writeJakobiMod();
            } catch (Exception e) {
                System.out.println(e.toString());
                System.exit(1);
            }
        }

        /**
         * ヤコビ行列を計算し、書き込みます
         *
         * @throws IOException          書き込みができなかった時
         * @throws EndressLoopException 値が収束しなかった時
         */
        public void writeJakobiMod() throws IOException, EndressLoopException {
            JacobiKey[] yakobi;
            for (int k = _start; k < _end; k++) {

                yakobi = getJakobi(k);
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_VECTOR_OUTPUT))));
                PrintWriter lm = new PrintWriter(new BufferedWriter(new FileWriter(KadaiData.getFileName(k + 1, KadaiData.MODE_LAMBDA_OUTPUT))));
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

                for (int j = 0; j < KadaiData.DATA_SIZE; j++) {
                    for (int i = 0; i < KadaiData.DATA_SIZE; i++) {
                        pw.print(String.format("%1$11.6f ", yakobi[j].getADatas()[i]));
                    }
                    lm.println(String.format("%1$11.6f ", yakobi[j].getLambda()));
                    pw.println();
                }
                pw.close();
                lm.close();
                System.out.println(KadaiData.getFileName(k + 1, KadaiData.MODE_VECTOR_OUTPUT) + "　" + KadaiData.getFileName(k + 1, KadaiData.MODE_LAMBDA_OUTPUT) + "作成完了");
            }

        }
    }
}
