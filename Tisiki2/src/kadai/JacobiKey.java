package kadai;

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

        public void setLambda(double lambda) {
            _lambda = lambda;
        }

    public double getLambda() {
        return _lambda;
    }

    public double[] getADatas() {
        return _datas;
    }

        public void setADatas(double[] datas) {
            this._datas = datas;
        }
}