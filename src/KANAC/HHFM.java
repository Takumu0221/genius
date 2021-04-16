package KANAC;

import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.*;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class HHFM extends OpponentModel {
    /*
     * the learning coefficient is the weight that is added each turn to the
     * issue weights which changed. It's a trade-off between concession speed
     * and accuracy.
     */
    // イシューに関する係数
    private double learnCoef;
    /*
     * value which is added to a value if it is found. Determines how fast the
     * value weights converge.
     */
    // 出現回数の多いバリューの重みを増加するための変数
    private int learnValueAddition;
    private int amountOfIssues;  //イシューの総数
    private double goldenValue;

    @Override
    public void init(NegotiationSession negotiationSession,
                     Map<String, Double> parameters) {
        this.negotiationSession = negotiationSession;
        if (parameters != null && parameters.get("l") != null) {
            learnCoef = parameters.get("l");
        } else {
            learnCoef = 0.2;
        }
        learnValueAddition = 1;
        opponentUtilitySpace = (AdditiveUtilitySpace) negotiationSession     //相手のutilityspaceを作成
                .getUtilitySpace().copy();
        amountOfIssues = opponentUtilitySpace.getDomain().getIssues().size();
        /*
         * This is the value to be added to weights of unchanged issues before
         * normalization. Also the value that is taken as the minimum possible
         * weight, (therefore defining the maximum possible also).
         */
        // 変化しないイシューの重みを増加させる
        goldenValue = learnCoef / amountOfIssues;

        initializeModel();

    }

    //相手のモデルを更新
    @Override
    public void updateModel(Bid opponentBid, double time) {
        if (negotiationSession.getOpponentBidHistory().size() < 2) {
            return;
        }
        int numberOfUnchanged = 0;
        BidDetails oppBid = negotiationSession.getOpponentBidHistory()   //相手のビッドを取得
                .getHistory()
                .get(negotiationSession.getOpponentBidHistory().size() - 1);
        BidDetails prevOppBid = negotiationSession.getOpponentBidHistory()   //相手の1つ前のビッドを取得
                .getHistory()
                .get(negotiationSession.getOpponentBidHistory().size() - 2);
        HashMap<Integer, Integer> lastDiffSet = determineDifference(prevOppBid,  //相手のビッドが変化しているか調べる
                oppBid);  // 変化していたら1，そうでなければ0

        // count the number of changes in value
        //変化していないバリューを数える
        for (Integer i : lastDiffSet.keySet()) {
            if (lastDiffSet.get(i) == 0)
                numberOfUnchanged++;
        }

        // The total sum of weights before normalization.
        //正規化前の重みの合計を取得
        double totalSum = 1d + goldenValue * numberOfUnchanged;
        // The maximum possible weight
        double maximumWeight = 1d - (amountOfIssues) * goldenValue / totalSum;

        // re-weighing issues while making sure that the sum remains 1
        // 合計が1となるようにイシューの重みを変更する：正規化
        for (Integer i : lastDiffSet.keySet()) {
            Objective issue = opponentUtilitySpace.getDomain()  // イシューを取得
                    .getObjectivesRoot().getObjective(i);
            double weight = opponentUtilitySpace.getWeight(i);  // 現在のイシューの重みを取得
            double newWeight;

            if (lastDiffSet.get(i) == 0 && weight < maximumWeight) {
                newWeight = (weight + goldenValue) / totalSum;  // ゴールデンバリューを加算
            } else {
                newWeight = weight / totalSum; // そのまま正規化
            }
            opponentUtilitySpace.setWeight(issue, newWeight);
        }

        // Then for each issue value that has been offered last time, a constant
        // value is added to its corresponding ValueDiscrete.
        // 多く出現するバリューの重みを変更
        try {
            for (Entry<Objective, Evaluator> e : opponentUtilitySpace
                    .getEvaluators()) {
                EvaluatorDiscrete value = (EvaluatorDiscrete) e.getValue();  // 現在のEvaluatorを取得
                IssueDiscrete issue = ((IssueDiscrete) e.getKey());  //　現在のイシューを取得
                /*
                 * add constant learnValueAddition to the current preference of
                 * the value to make it more important
                 */
                // 今回出現したバリューの重みを増す
                ValueDiscrete issuevalue = (ValueDiscrete) oppBid.getBid()
                        .getValue(issue.getNumber());
                Integer eval = value.getEvaluationNotNormalized(issuevalue);
                value.setEvaluation(issuevalue, (learnValueAddition + eval));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //あるビッドの相手の評価値を計算
    @Override
    public double getBidEvaluation(Bid bid) {
        double result = 0;
        try {
            result = opponentUtilitySpace.getUtility(bid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return "HardHeaded Frequency Model 1";
    }

    @Override
    public Set<BOAparameter> getParameterSpec() {
        Set<BOAparameter> set = new HashSet<BOAparameter>();
        set.add(new BOAparameter("l", 0.2,
                "The learning coefficient determines how quickly the issue weights are learned"));
        return set;
    }

    // 相手のモデルを初期化
    private void initializeModel() {
        double commonWeight = 1D / amountOfIssues;  // 1をイシューの数で割る

        for (Entry<Objective, Evaluator> e : opponentUtilitySpace
                .getEvaluators()) {

            opponentUtilitySpace.unlock(e.getKey());
            e.getValue().setWeight(commonWeight);   // イシューの重みの設定
            try {
                // set all value weights to one (they are normalized when
                // calculating the utility)
                // すべてのバリューの重みを1とする
                for (ValueDiscrete vd : ((IssueDiscrete) e.getKey())
                        .getValues())
                    ((EvaluatorDiscrete) e.getValue()).setEvaluation(vd, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // 相手のビッドが前回のそれと異なるかをHashMapとして計算　等しければ0，そうでなければ1
    // 前回のものとしか比べていない
    private HashMap<Integer, Integer> determineDifference(BidDetails first,
                                                          BidDetails second) {

        HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();
        try {
            for (Issue i : opponentUtilitySpace.getDomain().getIssues()) {
                Value value1 = first.getBid().getValue(i.getNumber());
                Value value2 = second.getBid().getValue(i.getNumber());
                diff.put(i.getNumber(), (value1.equals(value2)) ? 0 : 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return diff;
    }

}
