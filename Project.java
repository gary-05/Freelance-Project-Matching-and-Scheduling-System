package mySystem;

import java.util.Arrays;
public class Project {
    String id;
    String title;
    String description;
    String[] requiredSkills;
    int start;
    int end;
    int pay;

    public Project(String id, String title, String description, String[] requiredSkills, int start, int end, int pay) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.start = start;
        this.end = end;
        this.pay = pay;
    }

    @Override
    public String toString() {
    	String startInt=String.valueOf(start);
    	String d1=startInt.substring(0,2);
    	String m1=startInt.substring(2,4);
    	String y1=startInt.substring(4);
    	String endInt=String.valueOf(end);
    	String d2=endInt.substring(0,2);
    	String m2=endInt.substring(2,4);
    	String y2=endInt.substring(4);
        return id + ": " +"\n\t"+ title +"\n\t"+ description+"\n\t"+Arrays.toString(requiredSkills)+"\n\t" +"(Start: " + d1+"-"+m1+"-"+y1 + ", End: " + d2+"-"+m2+"-"+y2 + ", Pay: Rs." + pay + ")";
    }
}
