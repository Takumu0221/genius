import genius.core.Bid;

public class TimeDependentAgent extends AbstractKANACAgent {
    private double k;
    private double a;
    private double b;
    private double e;

    @Override
    public void init() {
        super.init();
        this.k = 0.0D;

        // ここのパラメーターを変えるといいと思う
        this.e = 1.0D;
        this.a = 1.0D;
        this.b = 0.0D;
    }

    public double f(double t) {
        if (this.e == 0.0D) {
            return 0.0D;
        } else {
            return this.k + (1.0D - this.k) * Math.pow(t, 1.0D / this.e);
        }
    }

    public double p(double t) {
        return this.U_min + (this.U_max - this.U_min) * (1.0D - this.f(t));
    }

    @Override
    public Bid makeBid() {
//        this.utilitySpace.getReservationValue();  // 留保価格を取得
//        this.utilitySpace.getDiscountFactor();    // 割引係数を取得
//        this.timeline.getTime();                  // 時刻を取得
        double t = this.timeline.getTime();
        double targetUtil = this.p(t);
        return this.pickBidOfUtility(targetUtil);
    }

    @Override
    public boolean isAcceptable(Bid nextBid) {
        Bid oppBid = this.getOpponentLastBid();
        return this.a * this.getUndiscountedUtility(oppBid) + this.b >= this.getUndiscountedUtility(nextBid);
    }

    @Override
    public String getName() {
        return "Time-Dependent Agent";
    }
}
