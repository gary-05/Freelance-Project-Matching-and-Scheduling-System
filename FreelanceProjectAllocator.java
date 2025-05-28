package mySystem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FreelanceProjectAllocator {
	public static class KMPMatcher 
	{
	    private static int[] buildLPS(String pattern) {
	        int[] lps = new int[pattern.length()];
	        int len = 0, i = 1;
	        while (i < pattern.length()) {
	            if (pattern.charAt(i) == pattern.charAt(len)) {
	                lps[i++] = ++len;
	            } else if (len > 0) {
	                len = lps[len - 1];
	            } else {
	                lps[i++] = 0;
	            }
	        }
	        return lps;
	    }

	    public static boolean kmpMatch(String text, String pattern) {
	        if (pattern.length() == 0) return false;
	        int[] lps = buildLPS(pattern.toLowerCase());
	        int i = 0, j = 0;
	        text = text.toLowerCase();
	        while (i < text.length()) {
	            if (text.charAt(i) == pattern.charAt(j)) {
	                i++; j++;
	                if (j == pattern.length()) return true;
	            } else if (j > 0) {
	                j = lps[j - 1];
	            } else {
	                i++;
	            }
	        }
	        return false;
	    }
	}



    // Binary search to find the last non-conflicting job
    static int findLastNonConflicting(List<Project> projects, int index) {
        int low = 0, high = index - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (projects.get(mid).end <= projects.get(index).start) {
                if (mid + 1 < index && projects.get(mid + 1).end <= projects.get(index).start) {
                    low = mid + 1;
                } else {
                    return mid;
                }
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    public static int maximizeEarnings(List<Project> projects, List<Project> selectedProjects) {
        // Sort by end time
    	
        projects.sort(Comparator.comparingInt(p -> p.end));

        int n = projects.size();
        int[] dp = new int[n];					// dp[i] stores max earnings up to project i
        int[] prev = new int[n];				// prev[i] stores index of last non-conflicting job before i

        dp[0] = projects.get(0).pay;
        prev[0] = -1;

        for (int i = 1; i < n; i++) {
            int inclPay = projects.get(i).pay;
            int last = findLastNonConflicting(projects, i);
            prev[i] = last;
            if (last != -1) {
                inclPay += dp[last];
            }
            if (inclPay > dp[i - 1]) {
                dp[i] = inclPay;
            } else {
                dp[i] = dp[i - 1];
                prev[i] = prev[i - 1];
            }
        }

        // Reconstruct selected projects
        int i = n - 1;
        while (i >= 0) {
            int last = findLastNonConflicting(projects, i);
            if (last == -1 || dp[i] != dp[i - 1]) {
                selectedProjects.add(projects.get(i));
                i = last;
            } else {
                i--;
            }
        }  
        
        Collections.reverse(selectedProjects);

        return dp[n - 1];
    }

}
