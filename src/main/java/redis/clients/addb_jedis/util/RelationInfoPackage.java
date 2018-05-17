package redis.clients.addb_jedis.util;

import java.io.Serializable;
import java.util.List;

public class RelationInfoPackage 
	implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1771367381540922512L;

	private int columnCount;
	private String partitionInfo;
//	private List<Integer> indexInfo;
	
	public RelationInfoPackage() {
		
	}
	public RelationInfoPackage(RelationInfoPackage relationInfo) {
		this.columnCount = relationInfo.columnCount;
		this.partitionInfo = relationInfo.partitionInfo;
	}
}
