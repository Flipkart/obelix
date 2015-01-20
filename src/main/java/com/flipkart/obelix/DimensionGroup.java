package com.flipkart.obelix;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.Lists;

public class DimensionGroup
{
	Collection<Rule> rules = Lists.newArrayList();
	private Collection<DimensionName> inputDimensions;
	private Collection<DimensionName> outputDimensions;

	public DimensionGroup(Collection<DimensionName> inputDimensions, Collection<DimensionName> outputDimensions)
	{
		this.inputDimensions = inputDimensions;
		this.outputDimensions = outputDimensions;

	}

	public void addRule(Collection<Dimension> inputDimensions, Collection<Dimension> outputDimensions)
	{
		filterDimensions(inputDimensions, this.inputDimensions);
		filterDimensions(outputDimensions, this.outputDimensions);

		Rule rule = new Rule(inputDimensions, outputDimensions);
		
		for (Rule existingrule : rules)
		{
			if(rule.intersects(existingrule))
			{
				throw new RuntimeException("Conflicting with existing rules");
			}
		}
		
		rules.add(rule);
	}

	private void filterDimensions(Collection<Dimension> inputDimensions, Collection<DimensionName> validDimensions)
	{
		Iterator<Dimension> iterator = inputDimensions.iterator();
		while (iterator.hasNext())
		{
			Dimension dimension = iterator.next();
			if (false == validDimensions.contains(dimension.getName()))
			{
				iterator.remove();
			}
		}
	}

	public Collection<Dimension> match(Collection<Dimension> inputDimensions)
	{
		filterDimensions(inputDimensions, this.inputDimensions);
		for (Rule rule : rules)
		{
			if (rule.evaluate(inputDimensions))
			{
				return rule.getOutputDimensions();
			}
		}
		return Collections.EMPTY_LIST;
	}
}
