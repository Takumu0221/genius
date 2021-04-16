package KANAC;

import genius.core.analysis.pareto.ParetoFrontierF;
import genius.core.analysis.pareto.PartialBidPoint;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.*;
import genius.core.misc.Range;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class shimizu_OS extends OfferingStrategy {

    /** Outcome space */
    private SortedOutcomeSpace outcomespace;


    public void init(NegotiationSession negoSession, OpponentModel model, OMStrategy oms,
                     Map<String, Double> parameters){
        // セッションの情報を取得
        super.init(negoSession, parameters);
        this.negotiationSession = negoSession;

        // outcomespaceを設定
        outcomespace = new SortedOutcomeSpace(negoSession.getUtilitySpace());
        negotiationSession.setOutcomeSpace(outcomespace);

        // 他のセクションで求めた情報を取得
        this.opponentModel = model;
        this.omStrategy = oms;
    }

    @Override
    // 最初は自分にとって最も良いビッドを出力
    public BidDetails determineOpeningBid() {
        return outcomespace.getMaxBidPossible();
    }

    @Override
    public BidDetails determineNextBid() {
        AdditiveUtilitySpace additiveUtilitySpace, opponentAddictiveUtilitySpace;
        double resVal = negotiationSession.getUtilitySpace().getReservationValue(),  // 留保価格
                disFac = negotiationSession.getDiscountFactor();  // 割引率

        // paretoFrontier を取得
        ParetoFrontierF paretoFrontierF = new ParetoFrontierF((AdditiveUtilitySpace)negotiationSession.getUtilitySpace(),
                (AdditiveUtilitySpace)opponentModel.getOpponentUtilitySpace());

        Collection<PartialBidPoint> pareto;
        pareto = paretoFrontierF.getFrontier();

        // 最終的に収束するUtilityを決定する
        double finalTarget = 0.75 - 0.3 * (1 - disFac);
        pareto = Sort(pareto);       // paretoFrontierを並び替え

        for(PartialBidPoint i : pareto){
            if(i.utilA() < i.utilB()){  // 自分の方が高いパレートフロンティアの内最小値を見つける
                finalTarget = i.utilA();    // finalTargetを更新
                break;
            }
        }
        // System.out.println("finalTarget:" + finalTarget);

        // 作成した関数に従ってtargetをfinalTargetに近づけ，
        // targetの近くでOMSの返すビッドを出力
        double target, max = outcomespace.getMaxBidPossible().getMyUndiscountedUtil();
        double ex = 2 - 4 * (1 - disFac) + resVal * 5 / 3;  // 指数の決定法
        target = max - (max - finalTarget) * Math.pow(negotiationSession.getTime(), ex);

        return getBid(outcomespace, target);
    }

    private final double RANGE_INCREMENT = 0.01;
    private final int EXPECTED_BIDS_IN_WINDOW = 100;
    private final double INITIAL_WINDOW_RANGE = 0.01;

    // 与えられたOutcomeSpaceの中からrangeに当てはまるビッドのリストを返す
    public BidDetails getBid(OutcomeSpace space, Range range) {
        List<BidDetails> bids = space.getBidsinRange(range);
        if (bids.size() == 0) {
            if (range.getUpperbound() < 1.01) {
                range.increaseUpperbound(RANGE_INCREMENT);
                return getBid(space, range);
            } else {
                negotiationSession.setOutcomeSpace(space);
                return negotiationSession.getMaxBidinDomain();
            }
        }
        return omStrategy.getBid(bids);
    }

    // 与えられたOutcomeSpaceの中からtargetUtility以上で相手にとって良いビッドを返す
    public BidDetails getBid(SortedOutcomeSpace space, double targetUtility) {
        Range range = new Range(targetUtility, targetUtility + INITIAL_WINDOW_RANGE);
        List<BidDetails> bids = space.getBidsinRange(range);
        if (bids.size() < EXPECTED_BIDS_IN_WINDOW) {
            if (range.getUpperbound() < 1.01) {
                range.increaseUpperbound(RANGE_INCREMENT);   // rangeを広げる
                return getBid(space, range);                 // rangeに含まれるビッドを取得
            } else {
                // futher increasing the window does not help
                return omStrategy.getBid(bids);              // これ以上rangeを広げても意味が無い場合はOMSに移行
            }
        }
        return omStrategy.getBid(bids);
    }


    @Override
    public String getName() {
        return "KANAC agent(shimizu), TimeDependentOffering considering reservation value and discount factor" ;
    }

    // paretoFrontierの並べ替え（挿入ソート）
    private Collection<PartialBidPoint> Sort(Collection<PartialBidPoint> c){
        ArrayList<PartialBidPoint> list = new ArrayList<>(), collection;
        PartialBidPoint maximum;    // 最大値を一時的に格納

        collection = new ArrayList<>(c);

        for(int i=0; i<c.size(); i++){
            maximum = collection.get(0);    // 初期化
            for(PartialBidPoint j : collection){
                if(maximum.utilA() < j.utilA()) maximum = j;   // 大きければ最大値に設定
            }
            list.add(maximum);    // リストに追加
            collection.remove(maximum);    // コレクションから削除
        }
        // System.out.println("list" + list);  // 確認

        return list;
    }
}