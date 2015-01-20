package com.flipkart.obelix;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

import lombok.Getter;

import com.google.common.collect.Maps;

public class Dimension
{
	private BitSet value;
	@Getter
	private DimensionName name;
	private MasterDataManager dataSource;

	public Dimension(DimensionName name, Collection<Integer> values, MasterDataManager dataSource)
	{
		this.name = name;
		this.dataSource = dataSource;
		Map<Integer, String> data = dataSource.getData(name);
		value = new BitSet(data.size());
		for (Integer id : values)
		{
			value.set(id);
		}
	}

	public Map<Integer, String> value()
	{
		Map<Integer, String> data = dataSource.getData(name);
		Map<Integer, String> map = Maps.newHashMap();
		for (int i = 0; i < value.length(); i++)
		{
			if (value.get(i))
			{
				map.put(i, data.get(i));
			}
		}
		return map;
	}

	public boolean match(Dimension dimension)
	{
		int cardinality = dimension.value.cardinality();
		BitSet valueClone = (BitSet) dimension.value.clone();
		valueClone.and(value);
		MetricsHelper.markBitAnds(value);
		return cardinality ==  valueClone.cardinality();
	}

	public boolean isEmpty()
	{
		return value.isEmpty();
	}
	
	@Override
	public String toString()
	{
		return name + " " + value.toString();
	}
}
