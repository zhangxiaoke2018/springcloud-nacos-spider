package com.jinguduo.spider.cluster.spider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public class PageRule {
	
	private final Collection<Rule> rules = Collections.synchronizedList(new ArrayList<Rule>());

	public static PageRule build() {
		return new PageRule();
	}
	
	public PageRule add(String regex, PageRuleProcessor processor) {
		return add(Pattern.compile(regex), processor);
	}
	
	public PageRule add(Pattern pattern, PageRuleProcessor func) {
		rules.add(new Rule(pattern, func));
		
		return this;
	}
	
	public Collection<Rule> getAll() {
		return rules;
	}
	
	public class Rule {
		final Pattern pattern;
		final PageRuleProcessor processor;
		
		public Rule(Pattern pattern, PageRuleProcessor processor) {
			this.pattern = pattern;
			this.processor = processor;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public PageRuleProcessor getProcessor() {
			return processor;
		}
	}
}
