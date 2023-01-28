package edu.neu.info7255.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalPlan {
    private PlanCostShares planCostShares;
    private List<LinkedPlanServices> linkedPlanServices;
    private String _org;
    private String objectId;
    private String objectType;
    private String planType;
    private String creationDate;
    private String md5Hash;
}
