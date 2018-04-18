package com.epam.health.tool.facade.common.service.action.fs;

import com.epam.facade.model.ClusterHealthSummary;
import com.epam.facade.model.ClusterSnapshotEntityProjectionImpl;
import com.epam.facade.model.accumulator.HealthCheckResultsAccumulator;
import com.epam.facade.model.fs.MemoryMetricsJson;
import com.epam.facade.model.projection.MemoryUsageEntityProjection;
import com.epam.health.tool.dao.cluster.ClusterDao;
import com.epam.health.tool.facade.common.service.action.CommonRestHealthCheckAction;
import com.epam.health.tool.facade.exception.InvalidResponseException;
import com.epam.health.tool.model.ClusterEntity;
import com.epam.util.common.CommonUtilException;
import com.epam.util.common.json.CommonJsonHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Vasilina_Terehova on 4/9/2018.
 */
public abstract class GetMemoryStatisticsAction extends CommonRestHealthCheckAction {
    @Autowired
    protected ClusterDao clusterDao;

    @Override
    protected ClusterHealthSummary performRestHealthCheck(ClusterEntity clusterEntity) throws InvalidResponseException {
        return new ClusterHealthSummary(
                new ClusterSnapshotEntityProjectionImpl(null, null,
                        getMemoryTotal(clusterEntity.getClusterName()), null, null));
    }

    @Override
    protected void saveClusterHealthSummaryToAccumulator(HealthCheckResultsAccumulator healthCheckResultsAccumulator, ClusterHealthSummary clusterHealthSummary) {
        ClusterHealthSummary tempClusterHealthSummary = healthCheckResultsAccumulator.getClusterHealthSummary();

        if (tempClusterHealthSummary == null) {
            tempClusterHealthSummary = clusterHealthSummary;
        } else {
            tempClusterHealthSummary = new ClusterHealthSummary(
                    new ClusterSnapshotEntityProjectionImpl(recreateClusterEntityProjection(tempClusterHealthSummary.getCluster()),
                            tempClusterHealthSummary.getServiceStatusList(), clusterHealthSummary.getCluster().getMemoryUsage(),
                            tempClusterHealthSummary.getCluster().getHdfsUsage(), tempClusterHealthSummary.getCluster().getNodes()));
        }

        healthCheckResultsAccumulator.setClusterHealthSummary(tempClusterHealthSummary);
    }

    public abstract String getActiveResourceManagerAddress(String clusterName) throws InvalidResponseException;

    private MemoryUsageEntityProjection getMemoryTotal(String clusterName) throws InvalidResponseException {
        ClusterEntity clusterEntity = clusterDao.findByClusterName(clusterName);
        //todo: get active rm
        try {
            String activeResourceManagerAddress = getActiveResourceManagerAddress(clusterEntity.getClusterName());
            String url = "http://" + activeResourceManagerAddress + "/ws/v1/cluster/metrics";

            System.out.println(url);
            String answer = httpAuthenticationClient.makeAuthenticatedRequest(clusterEntity.getClusterName(), url);
            System.out.println(answer);
            MemoryMetricsJson memoryMetricsJson = CommonJsonHandler.get().getTypedValueFromInnerField(answer, MemoryMetricsJson.class, "clusterMetrics");
            System.out.println(memoryMetricsJson);

            return memoryMetricsJson;
        } catch (CommonUtilException ex) {
            throw new InvalidResponseException("Elements not found.", ex);
        }
    }
}