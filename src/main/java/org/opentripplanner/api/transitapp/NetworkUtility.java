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

import org.opentripplanner.util.HttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum NetworkUtility {
    INSTANCE;
	
	private Map<String, ArrayList<Object>> networkMap;
	private Map<Integer, Set<String>> routeMap;
	private Lock lock;

    NetworkUtility() {
    	ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    	
    	Runnable periodicTask = new Runnable() {
    	    public void run() {
    	    	updateNetworkDefinition();
    	    }
    	};
    	
    	//Run each 15 min
    	executor.scheduleAtFixedRate(periodicTask, 0, 15, TimeUnit.MINUTES);
    }

    public static NetworkUtility getInstance() {
        return INSTANCE;
    }   
    
    public Set<String> networksForRouteId(String routeId) {
    	lock.lock();
    	Set<String> networks = routeMap.get(routeId);
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
			InputStream stream = HttpUtils.getData("http://staging.transitapp.com/v3/admin/networks?format=json");
			String jsonString = convertStreamToString(stream);
			
		    ObjectMapper mapper = new ObjectMapper();
		    JsonNode rootNode = mapper.readTree(jsonString);
		    
		    if (rootNode.isObject()) {
		    	Map<String, ArrayList<Object>> newNetworkMap = mapper.convertValue(rootNode, Map.class);
		    	
		    	Map<Integer, Set<String>> newRouteMap = new HashMap<Integer, Set<String>>();
		    	for (String networkKey : networkMap.keySet()) {
		    		ArrayList<Integer> routeIds = (ArrayList<Integer>)networkMap.get(networkKey).get(1);
					for (Integer routeId : routeIds) {
		    			newRouteMap.get(routeId).add(networkKey);
		    		}
		    	}
		    	
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
