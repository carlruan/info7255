package edu.neu.info7255.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
