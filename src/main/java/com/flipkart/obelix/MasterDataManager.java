package com.flipkart.obelix;

import java.util.Map;

public interface MasterDataManager
{
	Map<Integer, String> getData(DimensionName name);
}
