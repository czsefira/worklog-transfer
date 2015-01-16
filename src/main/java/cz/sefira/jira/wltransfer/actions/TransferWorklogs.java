package cz.sefira.jira.wltransfer.actions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import cz.sefira.jira.wltransfer.services.WLTransferService;

import java.util.*;

public class TransferWorklogs extends AbstractIssueSelectAction {

  private final BeanFactory beanFactory;
  private final IssueManager issueManager;
  private final WorklogManager worklogManager;
  private final PermissionManager permissionManager;
  private final WLTransferService wlTransferService;
  private final GroupManager groupManager;
  private final UserManager userManager;
  private Long[] selected = new Long[0];
  private Long destinationIssueId = -1L;
  private String selectProject = "";
  private String worker = getLoggedInUser().getName();
  private String issueGroup = getLoggedInUser().getName();
  private String issueStatus = "";
  private boolean recountEstimate = false;

  public TransferWorklogs(BeanFactory beanFactory, WorklogManager worklogManager, PermissionManager permissionManager,
                          IssueManager issueManager, WLTransferService wlTransferService, UserManager userManager,
                          GroupManager groupManager) {
    this.beanFactory = beanFactory;
    this.worklogManager = worklogManager;
    this.permissionManager = permissionManager;
    this.issueManager = issueManager;
    this.wlTransferService = wlTransferService;
    this.userManager = userManager;
    this.groupManager = groupManager;
  }

  @Override
  public String doDefault() throws Exception {
    return INPUT;
  }

  @Override
  public Collection<String> getErrorMessages() {
    // noinspection unchecked
    return super.getErrorMessages();
  }

  @Override
  public Map<String, String> getErrors() {
    // noinspection unchecked
    return super.getErrors();
  }



  @Override
  protected String doExecute() throws Exception {
    MutableIssue dstIssue = issueManager.getIssueObject(destinationIssueId);
    if (dstIssue.getId().equals(getIssueObject().getId()))
      return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    Worklog newWL;
    Long loggedSum = 0L,
        origWorkedSrc = getIssueObject().getTimeSpent() != null ? getIssueObject().getTimeSpent() : 0,
        origWorkedDest = dstIssue.getTimeSpent() != null ? dstIssue.getTimeSpent() : 0;

    for (int i = 0; i < selected.length; i++) {
      Worklog oldWL = worklogManager.getById(selected[i]);
      dstIssue = issueManager.getIssueObject(destinationIssueId);
      newWL = new WorklogImpl(worklogManager, dstIssue, null, oldWL.getAuthor(),
          oldWL.getComment(), oldWL.getStartDate(), oldWL.getGroupLevel(),
          oldWL.getRoleLevelId(), oldWL.getTimeSpent());
      worklogManager.create(userManager.getUser(newWL.getAuthor()), newWL, null, true);
      worklogManager.delete(userManager.getUser(oldWL.getAuthor()), oldWL, null, true);
      loggedSum = loggedSum + newWL.getTimeSpent();
    }

    MutableIssue thisIssue = getIssueObject();
    dstIssue = issueManager.getIssueObject(destinationIssueId);
    boolean negative  = origWorkedSrc - loggedSum < 0;
    // recount the logged work
    thisIssue.setTimeSpent(negative ? 0 : origWorkedSrc - loggedSum);
    dstIssue.setTimeSpent(origWorkedDest + loggedSum);
    if (recountEstimate) {
      // recount the original estimate
      if (thisIssue.getOriginalEstimate() != null) {
        negative = thisIssue.getOriginalEstimate() - loggedSum < 0;
        thisIssue.setOriginalEstimate(negative ? 0 : thisIssue.getOriginalEstimate() - loggedSum);
      }

      if (dstIssue.getOriginalEstimate() != null) {
        dstIssue.setOriginalEstimate(dstIssue.getOriginalEstimate() + loggedSum);
      }
    } else {
      // recount the remaining estimate
      if (dstIssue.getEstimate() != null) {
        negative = dstIssue.getEstimate() - loggedSum < 0;
        dstIssue.setEstimate(negative ? 0 : dstIssue.getEstimate() - loggedSum);
      }
      if (thisIssue.getEstimate() != null)
        thisIssue.setEstimate(thisIssue.getEstimate() + loggedSum);
    }
    if (dstIssue.getEstimate() == null) dstIssue.setEstimate(0L);
    issueManager.updateIssue(getLoggedInUser(), dstIssue, EventDispatchOption.ISSUE_UPDATED, false);
    issueManager.updateIssue(getLoggedInUser(), thisIssue, EventDispatchOption.ISSUE_UPDATED, false);
    return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
  }

  @Override
  public String getReturnUrlForCancelLink() {
      return request.getContextPath() + "/browse/" + getIssueObject().getKey();
  }

  public Collection<User> getEmployees() {
    //return groupManager.getDirectUsersInGroup(groupManager.getGroup("SEFIRA")); //TODO
    return userManager.getAllUsers();
  }

  public Collection<User> getWorkers() {
    List<Worklog> wls = worklogManager.getByIssue(getIssueObject());
    Set<String> workers = new HashSet<String>();
    for (Worklog w : wls) {
       if (!workers.contains(w.getAuthor()))
         workers.add(w.getAuthor());
    }
    List<User> returnWorkers = new ArrayList<User>(workers.size());
    for (String username : workers)
      returnWorkers.add(UserUtils.getUser(username));
    return returnWorkers;
  }

  public void setEstimate(String str) {
    this.recountEstimate = str.equals("on");
  }

  public String getIssueKey() {
    return getIssueObject().getKey();
  }

  public boolean getCheckboxEstimate() {
   return this.recountEstimate;
  }

  public void setSelectIssueGroup(String str) {
    this.issueGroup = str;
  }

  public String getSelectIssueGroup() {
    return this.issueGroup;
  }

  public void setSelectIssueStatus(String str) {
    this.issueStatus = str;
  }

  public String getSelectIssueStatus() {
    return this.issueStatus;
  }

  public void setSelectWorker(String str) {
    this.worker = str;
  }

  public String getSelectWorker() {
    return this.worker;
  }

  public String getSelectIssue() {
    return "" + this.destinationIssueId;
  }

  public void setSelectIssue(String str) {
    this.destinationIssueId = Long.parseLong(str);
  }

  public String getSelectProject() {
    return this.selectProject;
  }

  public void setSelectProject(String str) {
    this.selectProject = str;
  }

  public void setSelectWorklogs(String str) {
    if (str.equals("")) return;
    String[] array = str.split(",");
    selected = new Long[array.length];
    for (int i = 0; i < array.length; i++)
      selected[i] = Long.parseLong(array[i]);
  }

  public String getSelectWorklogs() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < selected.length; i++)
      sb.append(selected[i]).append(',');
    if (sb.length() > 1)
      sb.replace(sb.length() - 1, sb.length(), "]");
    else sb.append(']');
    return sb.toString();
  }

  public Collection<Project> getProjects() {
    Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, getLoggedInUser());
    List<Project> tmp = new ArrayList<Project>(projects.size() - 1);
    Long thisId = getIssueObject().getProjectObject().getId();
    for (Project p : projects)
      if (!p.getId().equals(thisId)) tmp.add(p);
    Project[] ret = new Project[projects.size()];
    for (int i = 0; i < tmp.size(); i++)
      ret[i + 1] = tmp.get(i);
    ret[0] = getIssueObject().getProjectObject();
    return Arrays.asList(ret);
  }

  public List<Worklog> getWorklogs() {
    return wlTransferService.getWorklogs(getIssueObject().getKey(), getLoggedInUser().getName());
  }

  @Override
  protected void doValidation() {
    super.doValidation();
    boolean checkown = true, checkothers = true, checktarget = true;
    I18nHelper i18n = beanFactory.getInstance(getLoggedInUser());
    if (selected.length == 0) {
      addError("selectWorklogs", i18n.getText("errors.validation.no.worklog"));
    }
    if (destinationIssueId == -1 || issueManager.getIssueObject(destinationIssueId) == null) {
      addError("selectIssue", i18n.getText("errors.validation.no.issue"));
    } else for (int i = 0; i < selected.length && checktarget; i++)
      if (!permissionManager.hasPermission(Permissions.WORK_ISSUE, issueManager.getIssueObject(destinationIssueId), userManager.getUser(worklogManager.getById(selected[i]).getAuthor()))) {
        {
          addErrorMessage(i18n.getText("errors.validation.log.work", worklogManager.getById(selected[i]).getAuthor()));
          checktarget = false;
        }
    }
    for (int i = 0; i < selected.length; i++) {
      if (worklogManager.getById(selected[i]).getAuthor().equals(getLoggedInUser().getName())) {
        if (checkown && !permissionManager.hasPermission(Permissions.WORKLOG_DELETE_OWN, getIssueObject(), getLoggedInUser())) {
          addErrorMessage(i18n.getText("errors.validation.delete.own"));
        }
        checkown = false;
      } else {
        if (checkothers && !permissionManager.hasPermission(Permissions.WORKLOG_DELETE_ALL, getIssueObject(), getLoggedInUser())){
          addErrorMessage(i18n.getText("errors.validation.delete.others"));
          checkown = false;
        }
        checkothers = false;
      }
    }
  }
}