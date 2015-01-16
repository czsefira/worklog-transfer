package cz.sefira.jira.wltransfer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.HashMap;
import java.util.Map;

public class ContextProvider extends AbstractJiraContextProvider {

  private final WebResourceManager webResourceManager;

  public ContextProvider(WebResourceManager wrm) {
    this.webResourceManager = wrm;
  }

  @Override
  public Map getContextMap(User user, JiraHelper jiraHelper) {
    webResourceManager.requireResource("cz.sefira.jira.worklog-transfer:wlt-web-resources");
    return new HashMap();
  }
}
