package shimizu;

import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

import java.util.*;

public class shimizu_OMS extends OMStrategy{

    private double updateThreshold = 1.1;    // アップデート可能な期間のタイムリミット

    // 初期設定
    public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
        super.init(negotiationSession, model, parameters);
        if (parameters.get("t") != null) {
            updateThreshold = parameters.get("t").doubleValue();
        } else {
            System.out.println("OMStrategy assumed t = 1.1");
        }
    }

    @Override
    // 与えられたビッドの中で,相手にとって最も良いビッドを返す
    public BidDetails getBid(List<BidDetails> allBids) {

        // 1. If there is only a single bid, return this
        // 与えられたビッドが１つならそれを返す
        if (allBids.size() == 1) {
            return allBids.get(0);
        }
        double bestUtil = -1;
        BidDetails bestBid = allBids.get(0);

        // 2. Check that not all bids are assigned at utility of 0
        // to ensure that the opponent model works. If the opponent model
        // does not work, offer a random bid.
        boolean allWereZero = true;
        // 3. Determine the best bid
        // 相手にとって最も良いビッドを見つける
        for (BidDetails bid : allBids) {
            double evaluation = model.getBidEvaluation(bid.getBid());
            if (evaluation > 0.0001) {
                allWereZero = false;
            }
            if (evaluation > bestUtil) {
                bestBid = bid;
                bestUtil = evaluation;
            }
        }
        // 4. The opponent model did not work, therefore, offer a random bid.
        // OpponentModelが機能していなければ与えられたビッドの中からランダムに返す
        if (allWereZero) {
            Random r = new Random();
            return allBids.get(r.nextInt(allBids.size()));
        }
        return bestBid;
    }

    @Override
    // OMをアップデート可能かどうかを返す
    public boolean canUpdateOM() {
        return negotiationSession.getTime() < updateThreshold;
    }

    @Override
    public Set<BOAparameter> getParameterSpec() {
        Set<BOAparameter> set = new HashSet<BOAparameter>();
        set.add(new BOAparameter("t", 1.1, "Time after which the OM should not be updated"));
        return set;
    }

    @Override
    public String getName() {
        return "KANAC agent(shimizu), return BestBid";
    }
}