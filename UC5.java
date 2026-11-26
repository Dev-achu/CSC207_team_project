package use_case.rebalance;

import java.util.HashMap;
import java.util.Map;


public class rebalancingPlanner {

    private final double driftThreshold;

    public rebalancingPlanner(double driftThreshold) {
        this.driftThreshold = driftThreshold;
    }


    public Map<String, Double> plan(Map<String, Double> targetWeights, Map<String, Double> currentValues) {

        return new HashMap<>();
    }
}
