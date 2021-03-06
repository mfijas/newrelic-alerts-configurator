package com.ocadotechnology.newrelic.apiclient.model.policies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;

@Value
@Builder
@AllArgsConstructor
public class AlertsPolicyChannels {
    @JsonProperty("id")
    Integer policyId;
    @JsonProperty("channel_ids")
    Collection<Integer> channelIds;
}
