import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.misc.Range;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.Math.*;


public class WaveAgent extends AbstractKANACAgent {
    private Random rand = new Random();
    private double k;
    private double a;
    private double b;
    private double e;
    private double targetUtil;

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
        double rng = this.U_max - this.U_min;
        return this.U_max - 0.1 - rng * (0.2 * Math.pow(t, 3) - 0.1 * sin(t * 3.5 * PI));
    }

    @Override
    public Bid makeBid() {
//        this.utilitySpace.getReservationValue();  // 留保価格を取得
//        this.utilitySpace.getDiscountFactor();    // 割引係数を取得
//        this.timeline.getTime();                  // 時刻を取得
        double t = this.timeline.getTime();
        this.targetUtil = this.p(t);
        Range range = new Range(Math.min(Math.max(this.targetUtil, this.U_min), this.U_max), this.U_max);
        List<BidDetails> bids = this.outcomeSpace.getBidsinRange(range);
        return bids.get(this.rand.nextInt(bids.size())).getBid();
    }

    @Override
    public boolean isAcceptable(Bid nextBid) {
        Bid oppBid = this.getOpponentLastBid();
        return this.getUndiscountedUtility(oppBid) >= this.targetUtil;
    }

    @Override
    public String getName() {
        return "Wave Agent(random agent)";
    }
}
