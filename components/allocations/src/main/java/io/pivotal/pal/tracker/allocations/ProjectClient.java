package io.pivotal.pal.tracker.allocations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestOperations;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;

    private final Map<Long, ProjectInfo> projectsCache = new ConcurrentHashMap<>();


    @Autowired
    private StringRedisTemplate template;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

//    @HystrixCommand(fallbackMethod = "getProjectFromInMemoryCache")
//    public ProjectInfo getProject(long projectId) {
//        logger.info("Calling real service for projectId : " + projectId);
//        ProjectInfo projectInfo = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
//        logger.info("Updating cache for projectId : " + projectId);
//        projectsCache.put(projectId, projectInfo);
//        return projectsCache.get(projectId);
//    }

//    public ProjectInfo getProjectFromInMemoryCache(long projectId) {
//        logger.info("reading from Cache for projectId : " + projectId);
//        return projectsCache.get(projectId);
//    }

    @HystrixCommand(fallbackMethod = "getProjectFromRedisCache")
    public ProjectInfo getProject(long projectId) {

        try {

            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("Calling real service for projectId : " + projectId);
            ProjectInfo projectInfo = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
            logger.info("Updating cache for projectId : " + projectId);

            ValueOperations<String, String> ops = this.template.opsForValue();
            if (!this.template.hasKey(String.valueOf(projectId))) {

                ops.set(String.valueOf(projectId), objectMapper.writeValueAsString(projectInfo));
            }

            return objectMapper.readValue(ops.get(String.valueOf(projectId)), ProjectInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectInfo getProjectFromRedisCache(long projectId) throws Exception {
        try {
            logger.info("reading from Redis Cache for projectId : " + projectId);
            ObjectMapper objectMapper = new ObjectMapper();
            ValueOperations<String, String> ops = this.template.opsForValue();
            return objectMapper.readValue(ops.get(String.valueOf(projectId)), ProjectInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
