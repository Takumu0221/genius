import genius.core.Bid;

public class TitForTatAgent extends AbstractKANACAgent {
    private int w;
    private double a;
    private double b;

    @Override
    public void init() {
        super.init();

        // ここのパラメーターを変えるといいと思う
        this.w = 1;
        this.a = 1.0D;
        this.b = 0.0D;
    }

    @Override
    public Bid makeBid() {
//        this.utilitySpace.getReservationValue();  // 留保価格を取得
//        this.utilitySpace.getDiscountFactor();    // 割引係数を取得
//        this.timeline.getTime();                  // 時刻を取得
        if (this.opponentHistory.size() < this.w + 1) { return this.pickBidOfUtility(1.0D); }
        Bid oppNthBid = this.opponentHistory.getHistory().get(this.opponentHistory.size() - (this.w + 1)).getBid();

        double myUtil = this.getUndiscountedUtility(this.getMyLastBid());
        double oppUtil = this.getUndiscountedUtility(this.getOpponentLastBid());
        double oppNthUtil = this.getUndiscountedUtility(oppNthBid);

        double targetUtil = Math.min(Math.max(myUtil * oppNthUtil / oppUtil, this.U_min), this.U_max);
        return this.pickBidOfUtility(targetUtil);
    }

    @Override
    public boolean isAcceptable(Bid nextBid) {
        Bid oppBid = this.getOpponentLastBid();
        return this.a * this.getUndiscountedUtility(oppBid) + this.b >= this.getUndiscountedUtility(nextBid);
    }

    @Override
    public String getName() {
        return "Tit for tat Agent";
    }
}
