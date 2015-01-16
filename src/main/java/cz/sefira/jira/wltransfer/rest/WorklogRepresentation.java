package cz.sefira.jira.wltransfer.rest;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.worklog.Worklog;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "worklog")
public class WorklogRepresentation {

  public WorklogRepresentation() {
    startDate = timeSpent = "";
    id = null;
    authorFullName = comment = "";
  }

  public WorklogRepresentation(Worklog worklog, DateTimeFormatter dateTimeFormatter) {
    this.startDate = dateTimeFormatter.format(worklog.getStartDate());
    this.authorFullName = worklog.getAuthorFullName();
    this.comment = worklog.getComment();
    this.timeSpent = getFormattedTime(worklog.getTimeSpent());
    this.id = worklog.getId();
  }

  @XmlElement
  private String authorFullName;

  @XmlElement
  private Long id;

  @XmlElement
  private String timeSpent;

  @XmlElement
  private String comment;

  @XmlElement
  private String startDate;

  private static String getFormattedTime(long timeSpent) {
    timeSpent /= 60;
    long m = timeSpent % 60;
    timeSpent /= 60;
    long h = timeSpent % 8;
    timeSpent /= 8;
    long d = timeSpent % 5;
    timeSpent /= 5;
    long w = timeSpent;
    String ret = "";
    if (w != 0) ret = ret + w + "w ";
    if (d != 0) ret = ret + d + "d ";
    if (h != 0) ret = ret + h + "h ";
    if (m != 0) ret = ret + m + "m";
    return ret;
  }
}
