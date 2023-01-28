package edu.neu.info7255.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanserviceCostShares {
    private Integer deductible;
    private String _org;
    private Integer copay;
    private String objectId;
    private String objectType;
}
