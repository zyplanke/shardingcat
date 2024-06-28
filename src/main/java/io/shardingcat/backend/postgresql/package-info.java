/**
 * @author Coollf
 *
 */
package io.shardingcat.backend.postgresql;
/*

postgresql shardingcat 相关支持

config demo
============================================================================================================


    <schema-config>
        <schema name="shardingcat" checkSQLschema="true" sqlMaxLimit="100" dataNode="dn1" />        	
		<dataNode name="dn1" dataHost="pghost" database="shardingcat" />      
	    <dataHost name="pghost" maxCon="100" minCon="5" balance="1" 
	       writeType="0" dbType="PostgreSQL" dbDriver="native" switchType="1">
			<heartbeat>select 1</heartbeat>
			<writeHost host="host_a" url="localhost:5432" user="postgres"
				password="coollf"/>
		</dataHost>		
    </schema-config>

=============================================================================================================


*/