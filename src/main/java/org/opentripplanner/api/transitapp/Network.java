package org.opentripplanner.api.transitapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Network {
	public static HashSet<String> parse(String networksString) {
		HashSet<String> networks = new HashSet<String>();
		
        for (String networkString : networksString.split(",")) {
            if (networkString.length() == 0)
                continue;
            
            networks = new HashSet<String>(Arrays.asList(networkString.split("|")));
        }
        
        return networks;
	}
	
	public Network(String newNetworkName, String newLocation, int newGlobalModeId) {
		networkName = newNetworkName;
		location = newLocation;
		globalModeId = newGlobalModeId;
	}
	
	public String networkName;
    public String location;
    public int globalModeId;
}
