import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.misc.Range;

import java.util.List;
import java.util.Random;

public class RandomAgent extends AbstractKANACAgent {
    private Random rand = new Random();
    private double threshold;
    private double a;
    private double b;

    public void init() {
        super.init();

        // ここのパラメーターを変えるといいと思う
        this.threshold = 0.8D;
        this.a = 1.0D;
        this.b = 0.0D;
    }

    @Override
    public Bid makeBid() {
//        this.utilitySpace.getReservationValue();  // 留保価格を取得
//        this.utilitySpace.getDiscountFactor();    // 割引係数を取得
//        this.timeline.getTime();                  // 時刻を取得
        Range range = new Range(Math.max(this.threshold, this.U_min), this.U_max);
        List<BidDetails> bids = this.outcomeSpace.getBidsinRange(range);
        return bids.get(this.rand.nextInt(bids.size())).getBid();
    }

    @Override
    public boolean isAcceptable(Bid nextBid) {
        Bid oppBid = this.getOpponentLastBid();
        return this.a * this.getUndiscountedUtility(oppBid) + this.b >= this.getUndiscountedUtility(nextBid);
    }

    @Override
    public String getName() {
        return "Random Agent";
    }
}
