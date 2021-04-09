import agents.anac.y2011.Nice_Tit_for_Tat.BilateralAgent;
import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.SortedOutcomeSpace;

import java.util.List;

public abstract class AbstractKANACAgent extends BilateralAgent {
    protected SortedOutcomeSpace outcomeSpace;
    protected double U_max;
    protected double U_min;

    public abstract Bid makeBid();

    @Override
    public void init() {
        super.init();
        this.initFields();
    }

    protected void initFields() {
        this.outcomeSpace = new SortedOutcomeSpace(this.utilitySpace);
        List allOutcomes = this.outcomeSpace.getAllOutcomes();
        double U_min = ((BidDetails)allOutcomes.get(allOutcomes.size() - 1)).getMyUndiscountedUtil();
        double reservationValue = this.utilitySpace.getReservationValueUndiscounted();
        this.U_max = ((BidDetails)allOutcomes.get(0)).getMyUndiscountedUtil();;
        this.U_min = Math.max(U_min, reservationValue);
    }

    public Bid pickBidOfUtility(double utility) {
        return this.outcomeSpace.getBidNearUtility(utility).getBid();
    }

    @Override
    public Bid chooseCounterBid() {
        return this.makeBid();
    }

    @Override
    public Bid chooseOpeningBid() {
        return this.makeBid();
    }

    @Override
    public Bid chooseFirstCounterBid() {
        return this.makeBid();
    }
}
