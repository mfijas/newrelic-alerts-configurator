package com.ocado.pandateam.newrelic.sync;

import com.ocado.pandateam.newrelic.api.NewRelicApi;
import com.ocado.pandateam.newrelic.api.model.applications.Application;
import com.ocado.pandateam.newrelic.api.model.conditions.AlertsCondition;
import com.ocado.pandateam.newrelic.api.model.policies.AlertsPolicy;
import com.ocado.pandateam.newrelic.api.model.transactions.KeyTransaction;
import com.ocado.pandateam.newrelic.sync.configuration.PolicyConfiguration;
import com.ocado.pandateam.newrelic.sync.configuration.condition.Condition;
import com.ocado.pandateam.newrelic.sync.configuration.condition.terms.TermsUtils;
import com.ocado.pandateam.newrelic.sync.exception.NewRelicSyncException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
class ConditionSynchronizer {
    private final NewRelicApi api;

    ConditionSynchronizer(@NonNull NewRelicApi api) {
        this.api = api;
    }

    void sync(@NonNull PolicyConfiguration config) {
        LOG.info("Synchronizing alerts conditions for policy {}...", config.getPolicyName());

        Optional<AlertsPolicy> policyOptional = api.getAlertsPoliciesApi().getByName(config.getPolicyName());
        AlertsPolicy policy = policyOptional.orElseThrow(
            () -> new NewRelicSyncException(format("Policy %s does not exist", config.getPolicyName())));


        List<AlertsCondition> allAlertsConditions = api.getAlertsConditionsApi().list(policy.getId());
        List<AlertsCondition> alertsConditionsFromConfig = config.getConditions().stream()
            .map(this::createAlertsCondition)
            .collect(Collectors.toList());
        List<Integer> updatedAlertsConditionsIds = createOrUpdateAlertsConditions(
            policy, alertsConditionsFromConfig, allAlertsConditions);

        cleanupOldAlertsConditions(policy, allAlertsConditions, updatedAlertsConditionsIds);
        LOG.info("Alerts conditions for policy {} synchronized", config.getPolicyName());
    }

    private List<Integer> createOrUpdateAlertsConditions(AlertsPolicy policy,
                                                         List<AlertsCondition> alertsConditionsFromConfig,
                                                         List<AlertsCondition> allAlertsConditions) {
        List<AlertsCondition> updatedAlertConditions = new LinkedList<>();
        for (AlertsCondition alertConditionFromConfig : alertsConditionsFromConfig) {
            Optional<AlertsCondition> alertsConditionToUpdate = allAlertsConditions.stream()
                .filter(alertCondition -> sameInstance(alertCondition, alertConditionFromConfig))
                .findFirst();
            if (alertsConditionToUpdate.isPresent()) {
                AlertsCondition updatedCondition = api.getAlertsConditionsApi().update(
                    alertsConditionToUpdate.get().getId(), alertConditionFromConfig);
                LOG.info("Alerts condition {} (id: {}) updated for policy {} (id: {})",
                    updatedCondition.getName(), updatedCondition.getId(), policy.getName(), policy.getId());
                updatedAlertConditions.add(updatedCondition);
            } else {
                AlertsCondition newCondition = api.getAlertsConditionsApi().create(
                    policy.getId(), alertConditionFromConfig);
                LOG.info("Alerts condition {} (id: {}) created for policy {} (id: {})",
                    newCondition.getName(), newCondition.getId(), policy.getName(), policy.getId());
            }
        }

        return updatedAlertConditions.stream()
            .map(AlertsCondition::getId)
            .collect(Collectors.toList());
    }

    private void cleanupOldAlertsConditions(AlertsPolicy policy, List<AlertsCondition> allAlertsConditions,
                                            List<Integer> updatedAlertsConditionsIds) {
        allAlertsConditions.stream()
            .filter(alertsCondition -> !updatedAlertsConditionsIds.contains(alertsCondition.getId()))
            .forEach(
                alertsCondition -> {
                    api.getAlertsConditionsApi().delete(alertsCondition.getId());
                    LOG.info("Alerts condition {} (id: {}) removed from policy {} (id: {})",
                        alertsCondition.getName(), alertsCondition.getId(), policy.getName(), policy.getId());
                }
            );
    }

    private AlertsCondition createAlertsCondition(Condition condition) {
        return AlertsCondition.builder()
            .type(condition.getTypeString())
            .name(condition.getConditionName())
            .enabled(condition.isEnabled())
            .entities(getEntities(condition))
            .metric(condition.getMetric())
            .conditionScope(condition.getConditionScope())
            .runbookUrl(condition.getRunBookUrl())
            .terms(TermsUtils.createTerms(condition.getTerms()))
            .build();
    }

    private Collection<Integer> getEntities(Condition condition) {
        switch (condition.getType()) {
            case APM_APP:
                return condition.getEntities().stream()
                    .map(
                        entity -> {
                            Optional<Application> applicationOptional = api.getApplicationsApi().getByName(entity);
                            Application application = applicationOptional.orElseThrow(
                                () -> new NewRelicSyncException(format("Application %s does not exist", entity)));
                            return application.getId();
                        }
                    )
                    .collect(Collectors.toList());
            case APM_KT:
                return condition.getEntities().stream()
                    .map(
                        entity -> {
                            Optional<KeyTransaction> ktOptional = api.getKeyTransactionsApi().getByName(entity);
                            KeyTransaction kt = ktOptional.orElseThrow(
                                () -> new NewRelicSyncException(format("Key transaction %s does not exist", entity)));
                            return kt.getId();
                        }
                    )
                    .collect(Collectors.toList());
            default:
                throw new NewRelicSyncException(format("Could not get entities for condition %s", condition.getConditionName()));
        }
    }

    private static boolean sameInstance(AlertsCondition alertsCondition1, AlertsCondition alertsCondition2) {
        return StringUtils.equals(alertsCondition1.getName(), alertsCondition2.getName())
            && StringUtils.equals(alertsCondition1.getType(), alertsCondition2.getType());
    }
}
