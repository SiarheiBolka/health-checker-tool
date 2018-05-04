package com.epam.facade.model.accumulator;

import com.epam.facade.model.HealthCheckActionType;
import com.epam.util.common.CheckingParamsUtil;

import java.util.*;
import java.util.function.Consumer;

public class ClusterAccumulatorToken {
    private String clusterName;
    private HealthCheckActionType healthCheckActionType;
    private String token;
    private boolean useSave;

    private ClusterAccumulatorToken( boolean useSave ) {
        this.useSave = useSave;
    }

    public String getClusterName() {
        return clusterName;
    }

    public HealthCheckActionType getHealthCheckActionType() {
        return healthCheckActionType;
    }

    public String getToken() {
        return token;
    }

    public boolean isUseSave() {
        return useSave;
    }

    public static ClusterAccumulatorToken buildAllCheck(String clusterName) {
        return ClusterAccumulatorToken.Builder.get()
                .withClusterName(clusterName).withType(HealthCheckActionType.ALL.name()).buildClusterAccumulatorToken();
    }

    public static ClusterAccumulatorToken buildScheduleAllCheck(String clusterName) {
        return ClusterAccumulatorToken.Builder.get()
                .withClusterName(clusterName).withType(HealthCheckActionType.ALL.name()).withToken("scheduler_"+new Date().getTime() + "_" + clusterName).buildClusterAccumulatorToken();
    }

    public List<HealthCheckActionType> getPassedActionTypes() {
        if (healthCheckActionType == HealthCheckActionType.ALL) {
            return HealthCheckActionType.all();
        }
        if (healthCheckActionType == HealthCheckActionType.NONE) {
            return Collections.emptyList();
        }
        ArrayList<HealthCheckActionType> healthCheckActionTypes = new ArrayList<>();
        healthCheckActionTypes.add(healthCheckActionType);
        return healthCheckActionTypes;
    }

    public static class Builder {
        private ClusterAccumulatorToken clusterAccumulatorToken;

        private Builder() {
            this.clusterAccumulatorToken = new ClusterAccumulatorToken( false );
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withClusterName( String clusterName ) {
            setParamWithCheck( clusterName, s -> {
                clusterAccumulatorToken.clusterName = s;
            });

            return this;
        }

        public Builder withToken( String token ) {
            setParamWithCheck( token, s -> {
                clusterAccumulatorToken.token = s;
            });

            return this;
        }

        public Builder withType( String type ) {
            clusterAccumulatorToken.healthCheckActionType = Arrays.stream( HealthCheckActionType.values() )
                    .filter( actionType -> actionType.name().equalsIgnoreCase( type ) )
                    .findFirst().orElse( HealthCheckActionType.NONE );

            return this;
        }

        public Builder withType( HealthCheckActionType type ) {
            clusterAccumulatorToken.healthCheckActionType = type;

            return this;
        }

        public Builder useSave( boolean useSave ) {
            clusterAccumulatorToken.useSave = useSave;

            return this;
        }

        public ClusterAccumulatorToken buildClusterAccumulatorToken() {
            return clusterAccumulatorToken;
        }

        private void setParamWithCheck(String param, Consumer<String> setParamConsumer) {
            if ( !CheckingParamsUtil.isParamsNullOrEmpty( param ) ) {
                setParamConsumer.accept( param );
            }
            else {
                throw new RuntimeException( "Invalid param!" );
            }
        }
    }
}
