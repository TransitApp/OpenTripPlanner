package org.opentripplanner.api.transitapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.opentripplanner.util.HttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum NetworkUtility {
    INSTANCE;
	
	private Map<String, ArrayList<Object>> networkMap = new HashMap<String, ArrayList<Object>>();
	private Map<Integer, Set<String>> routeMap = new HashMap<Integer, Set<String>>();
	private final Lock lock = new ReentrantLock();

    NetworkUtility() {
    	ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    	
    	Runnable periodicTask = new Runnable() {
    	    public void run() {
    	    	updateNetworkDefinition();
    	    }
    	};
    	
    	//Run each 15 min
    	executor.scheduleAtFixedRate(periodicTask, 0, 5, TimeUnit.MINUTES);
    }

    public static NetworkUtility getInstance() {
        return INSTANCE;
    }   
    
    public Set<String> networksForRouteId(String routeId) {
    	lock.lock();
    	Set<String> networks = new HashSet<String>();
    	try {
        	networks = routeMap.get(Integer.parseInt(routeId));
		} catch (NumberFormatException format) {
			//We don't have a number route_id, this is not from Transit
		}
    	lock.unlock();
    	return networks;
    }
    
    public boolean networkEnabledByDefault(String network) {
    	lock.lock();
    	boolean enabled = (boolean) networkMap.get(network).get(0);
    	lock.unlock();
    	return enabled;
    }
    
    @SuppressWarnings("unchecked")
	private void updateNetworkDefinition() {
    	try {
			InputStream stream = HttpUtils.getData("http://api.transitapp.com/v3/admin/networks?format=json");
			String jsonString = convertStreamToString(stream);
			
		    ObjectMapper mapper = new ObjectMapper();
		    JsonNode rootNode = mapper.readTree(jsonString);
		    
		    if (rootNode.isObject()) {
		    	Map<String, ArrayList<Object>> newNetworkMap = mapper.convertValue(rootNode, Map.class);
		    	
		    	Map<Integer, Set<String>> newRouteMap = new HashMap<Integer, Set<String>>();
		    	for (String networkKey : newNetworkMap.keySet()) {
		    		ArrayList<Integer> routeIds = (ArrayList<Integer>)newNetworkMap.get(networkKey).get(1);
		    		
					for (Integer routeId : routeIds) {
		    			Set<String> networks = newRouteMap.get(routeId);
		    			if (networks == null) {
		    				networks = new HashSet<String>();
		    				newRouteMap.put(routeId, networks);
		    			}
		    			networks.add(networkKey);
		    		}
		    	}
		    	
		    	System.out.println("Updated network definition");
		    	
		    	lock.lock();
		    	networkMap = newNetworkMap;
		    	routeMap = newRouteMap;
		    	lock.unlock();
		    }
		        
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner scanner = null;
        String result="";
        try {
           
            scanner = new java.util.Scanner(is).useDelimiter("\\A");
            result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        }
        finally
        {
           if(scanner!=null)
               scanner.close();
        }
        return result;
        
    }
}
