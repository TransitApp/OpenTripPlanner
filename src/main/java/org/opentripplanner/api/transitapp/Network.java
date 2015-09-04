package org.opentripplanner.api.transitapp;

import java.util.Arrays;
import java.util.HashSet;

public class Network {
	public static HashSet<String> parse(String networksString) {        
        return new HashSet<String>(Arrays.asList(networksString.split(",")));
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
