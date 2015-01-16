package cz.sefira.jira.wltransfer.services;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.util.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WLTransferServiceImpl implements WLTransferService {

  private static final Logger log = LoggerFactory.getLogger(WLTransferServiceImpl.class);

  private IssueManager issueManager;
  private WorklogManager worklogManager;

  public WLTransferServiceImpl(IssueManager im, WorklogManager wm) {
    this.issueManager = im;
    this.worklogManager = wm;
  }

  @Override
  public List<Worklog> getWorklogs(@NotNull String issuekey, String username) {
    Worklog[] worklogsArray = new Worklog[0];
    List<Worklog> worklogsAll = worklogManager.getByIssue(issueManager.getIssueObject(issuekey));
    List<Worklog> worklogsOut = new ArrayList<Worklog>();
    if (username != null && !username.equals("")) {
      for (int i = 0; i < worklogsAll.size(); i++) {
        if (worklogsAll.get(i).getAuthor().equals(username)) worklogsOut.add(worklogsAll.get(i));
      }
    } else worklogsOut = worklogsAll;
    worklogsArray = worklogsOut.toArray(worklogsArray);
    Arrays.sort(worklogsArray, new Comparator<Worklog>() {
      @Override
      public int compare(Worklog o1, Worklog o2) {
        return o2.getStartDate().compareTo(o1.getStartDate());
      }
    });
    return Arrays.asList(worklogsArray);
  }
}
