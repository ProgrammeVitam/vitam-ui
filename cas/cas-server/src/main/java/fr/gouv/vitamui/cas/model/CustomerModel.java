package fr.gouv.vitamui.cas.model;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class CustomerModel implements Serializable {
    String customerId;
    String code;
    String name;
}
