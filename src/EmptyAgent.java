import genius.core.Bid;

public class EmptyAgent extends AbstractKANACAgent {
    @Override
    public void init() {
        super.init();
    }

    @Override
    public Bid makeBid() {
//        this.utilitySpace.getReservationValue();  // 留保価格を取得
//        this.utilitySpace.getDiscountFactor();    // 割引係数を取得
//        this.timeline.getTime();                  // 時刻を取得
//        this.pickBidOfUtility(utility);           // 効用値がutilityのBidを取得
//        this.U_min;                               // 最低の効用値(留保価格か最低のBidの高い方)
//        this.U_max;                               // 最大の効用値(1.0)
        return null;
    }

    @Override
    public boolean isAcceptable(Bid nextBid) {
        return false;
    }

    @Override
    public String getName() {
        return "Empty Agent";
    }
}
