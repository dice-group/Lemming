package org.aksw.simba.lemming.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.simba.lemming.algo.refinement.operator.RefinementOperator;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.mimicgraph.colourselection.IClassSelector;
import org.aksw.simba.lemming.mimicgraph.metricstorage.ConstantValueStorage;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private ApplicationContext applicationContext;
	
	@Value("${metrics.store}") 
	private String cacheName;
	
	@Value("#{PropertySplitter.toList('${metrics}')}")
	private List<String> metrics;
	
	@Value("${refinement.operator}") 
	private String refinementOperator;

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
	
	@Bean(name = "refOperator")
	@Scope(value = "prototype")
	public RefinementOperator createRefinementOperator(List<SingleValueMetric> metrics) {
		return (RefinementOperator) applicationContext.getBean(refinementOperator, metrics);
	}

	public List<SingleValueMetric> getMetrics() {
		List<SingleValueMetric> finalMetrics = new ArrayList<>();
		for (String metric : metrics) {
			SingleValueMetric met = applicationContext.getBean(metric, SingleValueMetric.class);
			finalMetrics.add(met);
		}
		return finalMetrics;
	}

	@Bean
	@Scope(value = "prototype")
	public ConstantValueStorage constantValueStorage(String datasetPath) {
		List<SingleValueMetric> finalMetrics = getMetrics();
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
	/**
	 * 
	 * @param property String to be parsed
	 * @return Parse string as Set<String>
	 */
	public Set<String> toSet(String property) {
		Set<String> set = new HashSet<String>();
		if (!property.trim().isEmpty()) {
			Collections.addAll(set, property.split(","));
		}
		return set;
	}

	/**
	 * 
	 * @param property String to be parsed
	 * @return Parse string as List<String>
	 */
	public List<String> toList(String property) {
		List<String> list = new ArrayList<>();
		if (!property.trim().isEmpty()) {
			list.addAll(Arrays.asList(property.split(",")));
		}
		return list;
	}
}
