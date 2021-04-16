package KANAC;

import genius.core.boaframework.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class NEXT extends AcceptanceStrategy{
// 次に自分が用意しているビッドの評価値より大きければ受け入れる
private double a;
    private double b;

    /**
     * Empty constructor for the BOA framework.
     */
    public NEXT() {
    }

    public NEXT(NegotiationSession negoSession, OfferingStrategy strat,
                double alpha, double beta) {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;
        this.a = alpha;
        this.b = beta;
    }

    @Override
    public void init(NegotiationSession negoSession, OfferingStrategy strat,
                     OpponentModel opponentModel, Map<String, Double> parameters)
            throws Exception {
        this.negotiationSession = negoSession;
        this.offeringStrategy = strat;

        if (parameters.get("a") != null || parameters.get("b") != null) {
            a = parameters.get("a");
            b = parameters.get("b");
        } else {
            a = 1;
            b = 0;
        }
    }

    @Override
    public String printParameters() {
        String str = "[a: " + a + " b: " + b + "]";
        return str;
    }

    @Override
    public Actions determineAcceptability() {
        double nextMyBidUtil = offeringStrategy.getNextBid()  // 次の自分のビッドの評価値を取得
                .getMyUndiscountedUtil();
        double lastOpponentBidUtil = negotiationSession.getOpponentBidHistory()
                .getLastBidDetails().getMyUndiscountedUtil();  // 相手が提案した最後のビッドの評価値
        System.out.println(lastOpponentBidUtil);

        if (a * lastOpponentBidUtil + b >= nextMyBidUtil) {
            return Actions.Accept;
        }
        if (negotiationSession.getTime() > 0.999 && Math.random() < Math.pow(lastOpponentBidUtil, 3) ){
            return Actions.Accept;
        }
        return Actions.Reject;
    }

    @Override
    public Set<BOAparameter> getParameterSpec() {

        Set<BOAparameter> set = new HashSet<BOAparameter>();
        set.add(new BOAparameter("a", 1.0,
                "Accept when the opponent's utility * a + b is greater than the utility of our current bid"));
        set.add(new BOAparameter("b", 0.0,
                "Accept when the opponent's utility * a + b is greater than the utility of our current bid"));

        return set;
    }

    @Override
    public String getName() {
        return "Acceptance1 SAIL";
    }
}
