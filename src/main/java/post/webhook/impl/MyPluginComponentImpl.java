package post.webhook.impl;

import com.atlassian.bitbucket.event.repository.RepositoryCreatedEvent;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryCloneLinksRequest;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.repository.RepositoryCloneLinksRequest.Builder;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.component.ComponentLocator;

import post.webhook.api.MyPluginComponent;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({MyPluginComponent.class})
@Named ("myPluginComponent")
public class MyPluginComponentImpl implements MyPluginComponent
{
	
	final String NOTIFY_COMMIT = "git/notifyCommit?url=";
	final String[] JENKINS_MASTERS = new String[] {"http://jenkins-test1:8080/","http://jenkins-test2:8080/","http://jenkins-test3:8080/"};
	final String POST_WEBHOOK_KEY="com.atlassian.stash.plugin.stash-web-post-receive-hooks-plugin:postReceiveHook";
	
	RepositoryService repositoryService = ComponentLocator.getComponent(RepositoryService.class);
	RepositoryHookService repositoryHookService = ComponentLocator.getComponent(RepositoryHookService.class);
	Builder cloneLinkBuilder;
	
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public MyPluginComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.cloneLinkBuilder = new RepositoryCloneLinksRequest.Builder();
    }
    
    @EventListener
    public void onRepoCreated(final RepositoryCreatedEvent repoCreatedEvent) {
    	
    	Repository repo = repoCreatedEvent.getRepository();
    	RepositoryHook postHook = repositoryHookService.getByKey(repo, POST_WEBHOOK_KEY);
    	Settings hookSettings = repositoryHookService.getSettings(repo, POST_WEBHOOK_KEY);
    	
    	if (!postHook.isEnabled() && !postHook.isConfigured()) {
    		System.out.println("configuring post-hook for: " + repo);
    		String repoCloneURL = getCloneLink(repo);
    		SettingsBuilder settingsBuilder = repositoryHookService.createSettingsBuilder();
    		for (int i = 0; i < JENKINS_MASTERS.length; i++) {
    			settingsBuilder.add("hook-url-"+i, JENKINS_MASTERS[i] + NOTIFY_COMMIT + repoCloneURL);
    		}
    		repositoryHookService.setSettings(repo, POST_WEBHOOK_KEY, settingsBuilder.build());
    		repositoryHookService.enable(repo, POST_WEBHOOK_KEY);
    	}
    }
    
    public String getCloneLink(Repository repo) {
    	return repositoryService.getCloneLinks(cloneLinkBuilder.repository(repo).protocol("http").user(null).build()).iterator().next().getHref();
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
}