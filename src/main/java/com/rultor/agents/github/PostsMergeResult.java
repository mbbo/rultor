/**
 * Copyright (c) 2009-2014, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Posts merge results to Github pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class PostsMergeResult implements TalkAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Ctor.
     * @param ghub Github client
     */
    public PostsMergeResult(final Github ghub) {
        this.github = ghub;
    }

    @Override
    public void execute(final Talk talk) throws IOException {
        final XML xml = talk.read();
        if (xml.nodes("//merge-request-git").isEmpty()) {
            Logger.info(this, "no merge requests here");
        } else if (xml.nodes("//merge-request-git/finished").isEmpty()) {
            Logger.info(this, "merge request is not finished yet");
        } else {
            final XML req = xml.nodes("/talk/merge-request-git").get(0);
            final Issue.Smart issue = new TalkIssues(this.github).get(talk);
            final boolean success = Boolean.parseBoolean(
                req.xpath("success/text()").get(0)
            );
            final String msg;
            if (success) {
                msg = "done!";
            } else {
                msg = "oops";
            }
            issue.comments().post(msg);
            talk.modify(
                new Directives().xpath("/talk/merge-request-git")
                    .strict(1).remove()
            );
        }
    }

}