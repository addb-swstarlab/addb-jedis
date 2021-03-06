package redis.clients.addb_jedis.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
//import java.util.Collections;

public class CommandArgsObject 
	implements Serializable{
	
	/*
	 *
	 */
	private static final long serialVersionUID = 4025867204577084613L;

//	datakey, tableInfo(+partitionInfo,indexInfo), data
	private String datakey;
//	private RelationInfoPackage relationInfo;
	private String partitionInfo;
	private String columnCount;
	private List<String> data;
	
	private String requiredColumnIndice;

	/*
	 * Default constructor
	 */
	public CommandArgsObject() {
		
	}

	// fpscan
	public CommandArgsObject(String datakey, String requiredColumnIndice) {
		this.datakey = datakey;
		this.requiredColumnIndice = requiredColumnIndice;
	}
	// fpwrite
	public CommandArgsObject(String datakey, String columnCount, String partitionInfo, List<String> data) {
		this.datakey = datakey;
		this.partitionInfo = partitionInfo;
		this.columnCount = columnCount;
//		this.relationInfo = relationInfo;
		this.data = new ArrayList<String>(data);
//		Collections.copy(this.data, data);
	}
	
	// Getter
	public String getDataKey() {
		return this.datakey;
	}
	public String getPartitionInfo() {
		return this.partitionInfo;
	}
	public String getColumnCount() {
		return this.columnCount;
	}
	public List<String> getData() {
		return this.data;
	}
	public String getRequiredColumnIndice() {
		return this.requiredColumnIndice;
	}
	
}
