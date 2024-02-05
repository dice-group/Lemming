package org.aksw.simba.lemming.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import grph.Grph.DIRECTION;

/**
 * Bean configuration file
 * 
 * @author Ana Silva
 *
 */
@Configuration
@PropertySource(value = "classpath:application.properties")
public class BeanFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanFactory.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	@Value("${metrics.store}") 
	private String cacheName;
	
	@Value("#{PropertySplitter.toList('${metrics}')}")
	private List<String> metrics;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean(name = "maxindegree")
	@Scope(value = "prototype")
	public MaxVertexDegreeMetric createMaxVertexDegreeMetricIn() {
		return new MaxVertexDegreeMetric(DIRECTION.in);
	}

	@Bean(name = "maxoutdegree")
	@Scope(value = "prototype")
	public MaxVertexDegreeMetric createMaxVertexDegreeMetricOut() {
		return new MaxVertexDegreeMetric(DIRECTION.out);
	}

	@Bean(name = "stdindegree")
	@Scope(value = "prototype")
	public StdDevVertexDegree createStdDevVertexDegreeIn() {
		return new StdDevVertexDegree(DIRECTION.in);
	}

	@Bean(name = "stdoutdegree")
	@Scope(value = "prototype")
	public StdDevVertexDegree createStdDevVertexDegreeOut() {
		return new StdDevVertexDegree(DIRECTION.out);
	}

	@Bean(name = "metrics")
	public List<SingleValueMetric> getMetrics() {
		List<SingleValueMetric> finalMetrics = new ArrayList<>();
		for (String metric : metrics) {
			SingleValueMetric met = applicationContext.getBean(metric, SingleValueMetric.class);
			finalMetrics.add(met);
		}
		return finalMetrics;
	}
	
	@Autowired
	List<SingleValueMetric> finalMetrics;

	@Bean
	@Scope(value = "prototype")
	public ConstantValueStorage constantValueStorage(String datasetPath) {
		return new ConstantValueStorage(cacheName, datasetPath, finalMetrics);
	}

}

/**
 * This class can be used to parse string arrays as Set<String> from the
 * properties file.
 * 
 * @author Ana Silva
 *
 */
@Component("PropertySplitter")
class PropertySplitter {
	public Set<String> toSet(String property) {
		Set<String> set = new HashSet<String>();
		if (!property.trim().isEmpty()) {
			Collections.addAll(set, property.split(","));
		}
		return set;
	}

	public List<String> toList(String property) {
		List<String> list = new ArrayList<>();
		if (!property.trim().isEmpty()) {
			list.addAll(Arrays.asList(property.split(",")));
		}
		return list;
	}
}
