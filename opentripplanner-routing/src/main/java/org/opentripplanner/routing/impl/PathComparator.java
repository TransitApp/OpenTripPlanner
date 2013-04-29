/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.impl;

import java.util.Comparator;

import org.opentripplanner.routing.spt.GraphPath;

public class PathComparator implements Comparator<GraphPath> {

    boolean compareStartTimes;
    
    public PathComparator(boolean compareStartTimes) {
        this.compareStartTimes = compareStartTimes;
    }
    
    /**
     * For depart-after search results sort by arrival time ascending
     * For arrive-before search results sort by departure time descending
     */
    @Override
    public int compare(GraphPath o1, GraphPath o2) {
    	double o1Weight = o1.getWeight();
    	double o2Weight = o2.getWeight();
    	
    	double time1 = (o1.getEndTime() - o1.states.getFirst().getContext().opt.dateTime);
    	double time2 = (o2.getEndTime() - o2.states.getFirst().getContext().opt.dateTime);
    	double o1TimeDiff = 2.0 * time1;
    	double o2TimeDiff = 2.0 * time2;
    	
        if (compareStartTimes) {
            return (int) (o2Weight * o2.getStartTime() - o1Weight * o1.getStartTime()) / 1000;
        } else {
            return (int) (o1Weight + o1TimeDiff - (o2Weight + o2TimeDiff)) / 1000;
        }
    }

}
