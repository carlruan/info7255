package edu.neu.info7255.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkedService {
    private String _org;
    private String objectId;
    private String objectType;
    private String name;
}
