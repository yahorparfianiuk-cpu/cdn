package com.bitmovin.platform.challenge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Distribution {

    @Id
    private String id; // same as customerId
    private boolean active = true; // false if capped

}

