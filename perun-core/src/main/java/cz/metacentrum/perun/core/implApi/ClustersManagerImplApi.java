package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ClusterNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
* Manages clusters.
*
* @author  Michal Prochazka
* @author  Michal Karm Babacek
*/
public interface ClustersManagerImplApi {

	/**
	 * List Cluster's hosts' id.
	 * 
	 * @param perunSession
	 * @param pageSize
	 * @param pageNum
	 * @param cluster
	 * 			
	 * @return hosts Hosts' id from the Cluster
	 * 
	 * @throws InternalErrorException
	 */
	List<Integer> getHostsIds(PerunSession perunSession, int pageSize, int pageNum, Facility cluster) throws InternalErrorException;
	
	/**
	 * Count hosts in the cluster.
	 * 
	 * @param perunSession
	 * @param cluster
	 * 
	 * @return int The number of hosts present in the Cluster.
	 * 
	 * @throws InternalErrorException			
	 */
	int getHostsCount(PerunSession perunSession, Facility cluster) throws InternalErrorException;
	
	/**
	 * Adds hosts to the Cluster. Note that if you wish to add more hosts, it is recommended to
	 * prepare a List<Host> of them so as there can be only one database call.
	 * 
	 * @param perunSession
	 * @param hosts
	 * @param cluster
	 * 
	 * @throws InternalErrorException			
	 */
	void addHosts(PerunSession perunSession, List<Host> hosts, Facility cluster) throws InternalErrorException, HostExistsException; 
	
	/**
	 * Remove hosts from the Cluster.
	 * 
	 * @param perunSession
	 * @param hosts
	 * @param cluster
	 * 
	 * @throws InternalErrorException
	 */
	void removeHosts(PerunSession perunSession, List<Host> hosts, Facility cluster) throws InternalErrorException;
	
	/**
	 * Create a new cluster.
	 * 
	 * @param perunSession
	 * @param cluster
	 * 
	 * @return cluster Cluster from the system
	 * 
	 * @throws InternalErrorException
	 */
	Facility createCluster(PerunSession perunSession, Facility cluster) throws InternalErrorException;
	
	/**
	 * Delete Cluster
	 * Note: 	All hosts are going to be removed from the cluster
	 * 			prior to the deleting process.
	 * Note: 	This operation might fail e.g. when there are
	 * 			some operations or services going on. 
	 * 
	 * @param perunSession
	 * @param cluster
	 * 
	 * @throws InternalErrorException
	 */
	void deleteCluster(PerunSession perunSession, Facility cluster) throws InternalErrorException;
	
	/**
	 * Check if facility exists in underlaying data source and has type=cluster
	 * 
	 * @param perunSession
	 * @param cluster
	 * @return true if cluster exists in underlaying data source, false othewise
	 * 
	 * @throws InternalErrorException
	 */
	boolean clusterExists(PerunSession perunSession, Facility cluster) throws InternalErrorException;


	/**
	 * Check if cluster exists in underlaying data source and has type=cluster
	 * 
	 * @param perunSession
	 * @param cluster
	 * 
	 * @throws InternalErrorException
	 * @throws ClusterNotExistsException
	 */
	void checkClusterExists(PerunSession perunSession, Facility cluster) throws InternalErrorException, ClusterNotExistsException;

        /**
         * Check if host exists in the cluster.
         * 
         * @param sess
         * @param cluster
         * @param host
         * @return true if exists, false otherwise
         * @throws InternalErrorException
         */
        public boolean hostExists(PerunSession sess, Facility cluster, Host host) throws InternalErrorException;

}
