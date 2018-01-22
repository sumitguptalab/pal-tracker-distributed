package io.pivotal.pal.tracker.timesheets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String endpoint;

    private final Map<Long, ProjectInfo> projectsCache = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
    }

    @HystrixCommand(fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        logger.info("Calling real service for projectId : " + projectId);
        ProjectInfo projectInfo = restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class);
        logger.info("Updating cache for projectId : " + projectId);
        projectsCache.put(projectId, projectInfo);
        return projectsCache.get(projectId);
    }

    public ProjectInfo getProjectFromCache(long projectId) {
        logger.info("reading from Cache for projectId : " + projectId);
        return projectsCache.get(projectId);
    }
}
