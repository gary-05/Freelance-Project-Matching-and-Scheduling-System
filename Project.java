package mySystem;

public class Project {
    String title;
    String description;
    String[] requiredSkills;
    int start;
    int end;
    int pay;

    public Project(String title, String description, String[] requiredSkills, int start, int end, int pay) {
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.start = start;
        this.end = end;
        this.pay = pay;
    }

    @Override
    public String toString() {
        return title + " (Start: " + start + ", End: " + end + ", Pay: Rs." + pay + ")";
    }
}