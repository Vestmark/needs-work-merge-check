package com.vestmark.bitbucket.merge.checks;

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Iterator;

import javax.annotation.Nonnull;

@Component("needsWorkMergeCheck")
public class NeedsWorkMergeCheck implements RepositoryMergeCheck {

    private final I18nService i18nService;
    private final PermissionService permissionService;

    @Autowired
    public NeedsWorkMergeCheck(@ComponentImport I18nService i18nService, 
                             @ComponentImport PermissionService permissionService) {
        this.i18nService = i18nService;
        this.permissionService = permissionService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull PullRequestMergeHookRequest request) {
        Set<PullRequestParticipant> participants = request.getPullRequest().getReviewers();
        Boolean okToMerge = true;
        System.out.println("#### In RepositoryHookResult!!!  #####");
        System.out.println("#### Reviewers.size = " + participants.size());
        Iterator<PullRequestParticipant> it = participants.iterator();
        while (it.hasNext()){
            PullRequestParticipant p = it.next();
            System.out.println("participant status = " + p.getStatus());
            if (p.getStatus() == PullRequestParticipantStatus.NEEDS_WORK ) {
                okToMerge = false;
                break;
            }
        }
        Repository repository = request.getPullRequest().getToRef().getRepository(); 
        if (!okToMerge && !permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
            String summaryMsg = i18nService.getText("vestmark.plugin.merge.check.needswork.summary",
                    "Merges are blocked if at least one reviewer has marked the Pull Request with a \"NEEDS WORK\" recommendation.");
            String detailedMsg = i18nService.getText("vestmark.plugin.merge.check.needswork.detailed",
                    "Merges are blocked if at least one reviewer has marked the Pull Request with a \"NEEDS WORK\" recommendation.");
            return RepositoryHookResult.rejected(summaryMsg, detailedMsg);
        }
        return RepositoryHookResult.accepted();
    }
}