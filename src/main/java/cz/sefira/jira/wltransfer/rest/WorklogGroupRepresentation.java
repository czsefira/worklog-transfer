package cz.sefira.jira.wltransfer.rest;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.worklog.Worklog;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "worklogs")
public class WorklogGroupRepresentation {

  public WorklogGroupRepresentation() {
    worklogs = new ArrayList<WorklogRepresentation>();
  }

  public WorklogGroupRepresentation(List<Worklog> list, DateTimeFormatter dateTimeFormatter) {
    this();
    for (Worklog w : list)
      worklogs.add(new WorklogRepresentation(w, dateTimeFormatter));
  }

  @XmlElement
  private List<WorklogRepresentation> worklogs;
}