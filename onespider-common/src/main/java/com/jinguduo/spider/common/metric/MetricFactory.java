package com.jinguduo.spider.common.metric;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricFactory {
    
    private final static Holder holder = new Holder();
    
    public static MetricBuilder builder() {
        return new MetricBuilder();
    }

    static synchronized Metrizable get(String namespace, String metricName, int type, Map<String, String> dimensions) {
        return holder.get(namespace, metricName, type, dimensions);
    }

    public static class MetricBuilder {
    	private String namespace;
        private String metricName;
        private int type = 0;  // default:0
        private Map<String, String> dimensions = new HashMap<>();
        
        public MetricBuilder namespace(String namespace) {
        	this.namespace = namespace;
        	return this;
        }
        
        public MetricBuilder metricName(String metricName) {
            this.metricName = metricName;
            return this;
        }
        
        public MetricBuilder type(int type) {
            this.type = type;
            return this;
        }
        
        public MetricBuilder addDimension(String name, String v) {
            this.dimensions.put(name, v);
            return this;
        }
        
        public Metrizable build() {
            return get(namespace, metricName, type, dimensions);
        }
    }

    public static Collection<MetricBean> getAndResetMetrics() {
        return holder.stream()
                .map(e -> e.getMetricBean())
                .collect(Collectors.toList());
    }
    
    static class Holder {
        private final Map<String, Map<String, Metrizable>> table = new HashMap<>();

        public Metrizable get(String namespace, String metricName, int type, Map<String, String> dimensions) {
        	String k = namespace + ":" + metricName;
            Map<String, Metrizable> metries = table.get(k);
            if (metries == null) {
            	metries = new HashMap<>();
                table.put(k, metries);
            }
            String dimension = String.valueOf(dimensions.hashCode());
            Metrizable r = metries.get(dimension);
            if (r == null) {
            	r = new SimpleMetric(namespace, metricName, type, dimensions);
                metries.put(dimension, r);
            }
            return r;
        }

        public Stream<Metrizable> stream() {
            return table.values().stream().flatMap(e -> e.values().stream());
        }
    }
}
