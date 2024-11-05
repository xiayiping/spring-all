package org.xyp.shared.excel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IImport {

	List<Map<String, Object>> importFromSourceToMap(File source, String configName);
	List<Map<String, Object>> importFromSourceToMap(byte[] source, String configName);

}
