package space._2ndelement.rmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * 会议
 *
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/11/4 19:04
 */
public class Meeting implements Serializable {
    private int id;
    private String title;
    private User organizer;
    private ArrayList<User> otherUsers;
    private Date start;
    private Date end;

    public Meeting(int id, String title, User organizer, ArrayList<User> otherUsers, Date start, Date end) {
        this.id = id;
        this.title = title;
        this.organizer = organizer;
        this.otherUsers = otherUsers;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public ArrayList<User> getOtherUsers() {
        return otherUsers;
    }

    public void setOtherUsers(ArrayList<User> otherUsers) {
        this.otherUsers = otherUsers;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Meeting meeting = (Meeting) o;
        return id == meeting.id &&
                Objects.equals(title, meeting.title) &&
                Objects.equals(organizer, meeting.organizer) &&
                Objects.equals(otherUsers, meeting.otherUsers) &&
                Objects.equals(start, meeting.start) &&
                Objects.equals(end, meeting.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, organizer, otherUsers, start, end);
    }

    @Override
    public String toString() {
        return "Meeting{" + "meetingId=" + id +
                ", title='" + title + '\'' +
                ", organizer=" + organizer +
                ", otherUsers=" + otherUsers +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
