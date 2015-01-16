package cz.sefira.jira.wltransfer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

public class UserCanLogWorkCondition extends AbstractIssueCondition {

  private final PermissionManager permissionManager;

  public UserCanLogWorkCondition(PermissionManager pm) {
    this.permissionManager = pm;
  }
  @Override
  public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper) {
    return permissionManager.hasPermission(Permissions.WORK_ISSUE, issue, user);
  }
}
