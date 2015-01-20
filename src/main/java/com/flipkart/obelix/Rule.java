package com.flipkart.obelix;

import java.util.Collection;
import java.util.Map;

import lombok.Getter;

import com.google.common.collect.Maps;

public class Rule
{

	@Getter
	private Map<DimensionName, Dimension> input;
	@Getter
	private Collection<Dimension> outputDimensions;

	public Rule(Collection<Dimension> inputDimensions, Collection<Dimension> outputDimensions)
	{
		this.input = Maps.newHashMap();
		for (Dimension dimension : inputDimensions)
		{
			input.put(dimension.getName(), dimension);
		}
		this.outputDimensions = outputDimensions;
	}

	public void addInputDimension(Dimension dimension)
	{
		input.put(dimension.getName(), dimension);
	}

	public boolean evaluate(Collection<Dimension> inputDimensions)
	{
		boolean result = true;
		for (Dimension dimension : inputDimensions)
		{
			Dimension ruleDimension = input.get(dimension.getName());
			if (false == ruleDimension.match(dimension))
			{
				//return false;
				result = false;
			}
		}
		return result;
	}

	public boolean intersects(Rule anotherRule)
	{
		if (evaluate(anotherRule.input.values()))
		{
			return true;
		} else if (anotherRule.evaluate(this.input.values()))
		{
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "Input Dimensions " + input + " Output Dimensions " + outputDimensions;
	}
}
