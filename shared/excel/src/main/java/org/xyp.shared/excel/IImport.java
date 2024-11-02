package org.xyp.shared.excel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IImport {

	public List<Map<String, Object>> importFromSourceToMap(File source, String configName);
	public List<Map<String, Object>> importFromSourceToMap(byte[] source, String configName);

}
