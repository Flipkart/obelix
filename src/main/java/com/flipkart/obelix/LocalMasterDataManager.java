package com.flipkart.obelix;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LocalMasterDataManager implements MasterDataManager
{
	Map<DimensionName, Map<Integer, String>> masterData = Maps.newHashMap();

	public LocalMasterDataManager() throws IOException
	{
		DimensionName[] dimensionNames = DimensionName.values();
		for (DimensionName dimensionName : dimensionNames)
		{
			String fileName = dimensionName.toString().toLowerCase() + ".txt";
			System.out.println("Going to read from " + fileName);
			InputStream resource = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(fileName);
			try
			{
				List<String> lines = IOUtils.readLines(resource);
				Set<String> uniqueValues = Sets.newHashSet(lines);
				Map<Integer, String> data = Maps.newHashMap();
				int count = 0;
				for (String value : uniqueValues)
				{
					data.put(count, value);
					count++;
				}
				masterData.put(dimensionName, data);
			} finally
			{
				IOUtils.closeQuietly(resource);
			}
		}
	}

	@Override
	public Map<Integer, String> getData(DimensionName name)
	{
		return masterData.get(name);
	}

}
